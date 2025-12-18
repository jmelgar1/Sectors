package me.jm3l.sectors.claim.wand;

import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.shared.service.ServiceManager;
import me.jm3l.sectors.claim.ClaimUtilities;
import me.jm3l.sectors.shared.util.PacketPair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClaimToolPacketUtilities {
    private static final AtomicInteger nextEntityId = new AtomicInteger(1000000);
    private static final String VALID_TEAM_NAME = "claim_valid";
    private static final String INVALID_TEAM_NAME = "claim_invalid";

    /**
     * Updates the marker color based on position validity
     */
    public static void updateMarkerColor(Player p, UUID entityUUID, boolean isValid) {
        String teamName = isValid ? VALID_TEAM_NAME : INVALID_TEAM_NAME;
        NamedTextColor color = isValid ? NamedTextColor.GREEN : NamedTextColor.RED;

        // Create team with color
        WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
            Component.text(teamName),
            Component.empty(),
            Component.empty(),
            WrapperPlayServerTeams.NameTagVisibility.NEVER,
            WrapperPlayServerTeams.CollisionRule.NEVER,
            color,
            WrapperPlayServerTeams.OptionData.NONE
        );

        // Create or update team
        WrapperPlayServerTeams createTeam = new WrapperPlayServerTeams(
            teamName,
            WrapperPlayServerTeams.TeamMode.CREATE,
            Optional.of(teamInfo)
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, createTeam);

        // Add entity to team by UUID
        WrapperPlayServerTeams addToTeam = new WrapperPlayServerTeams(
            teamName,
            WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
            Optional.empty(),
            entityUUID.toString()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, addToTeam);
    }

    public static WrapperPlayServerSpawnEntity setMarkerPacket(Location location, Player p, Sectors plugin) {
        World world = location.getWorld();
        if (world == null) return null;

        int entityId = nextEntityId.getAndIncrement();
        UUID entityUUID = UUID.randomUUID();
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
            entityId,                              // int
            Optional.of(entityUUID),              // Wrap UUID in Optional
            EntityTypes.SHULKER,                  // Use PacketEvents' EntityTypes
            new Vector3d(location.getX(), location.getY(), location.getZ()), // Vector3d position
            0.0f,                                 // float pitch
            0.0f,                                 // float yaw
            0.0f,                                 // float headYaw
            0,                                    // int data
            Optional.of(new Vector3d(0.0, 0.0, 0.0))
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, spawnPacket);

        List<EntityData<?>> metadataList = new ArrayList<>();
        byte flags = (byte) (0x20 | 0x40); // invisible and glowing
        EntityData flagsData = new EntityData(0, EntityDataTypes.BYTE, flags);
        metadataList.add(flagsData);
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, metadataList);
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, metadataPacket);

        // Set initial color based on position validity
        boolean isValid = ClaimUtilities.isValidClaimPosition(location, plugin, 30);
        updateMarkerColor(p, entityUUID, isValid);

        return spawnPacket;
    }

    public static void removeMarketPacket(Player p, WrapperPlayServerSpawnEntity packet, Sectors plugin) {
        if (packet == null) return;
        int entityId = packet.getEntityId();
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, destroyPacket);
        plugin.getClaimParticleTask().getPlayerMarkers().remove(p.getUniqueId());
    }

    public static void teleportMarkerPacket(WrapperPlayServerSpawnEntity packet, Location newLocation, Player p, Sectors plugin) {
        if (packet == null) {
            WrapperPlayServerSpawnEntity newPacket = setMarkerPacket(newLocation, p, plugin);
            if (newPacket != null) {
                plugin.getClaimParticleTask().getPlayerMarkers().put(p.getUniqueId(), newPacket);
            }
            return;
        }

        try {
            int entityId = packet.getEntityId();
            Location playerLocation = p.getLocation();

            if (playerLocation.getBlockY() > newLocation.getBlockY()) {
                newLocation.add(0, -1, 0);
                if (p.getFallDistance() > 1.5) {
                    newLocation.add(0, -4, 0);
                }
            }

            WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                entityId,
                new Vector3d(newLocation.getX(), newLocation.getY(), newLocation.getZ()),
                new Vector3d(0.0, 0.0, 0.0),
                0, // yaw
                0, // pitch
                RelativeFlag.NONE,
                false // on ground
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, teleportPacket);

            // Update color based on new position validity
            boolean isValid = ClaimUtilities.isValidClaimPosition(newLocation, plugin, 30);
            if (packet.getUUID().isPresent()) {
                updateMarkerColor(p, packet.getUUID().get(), isValid);
            }

            // Create updated spawn packet with new position to keep stored position in sync
            WrapperPlayServerSpawnEntity updatedPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                packet.getUUID(),
                packet.getEntityType(),
                new Vector3d(newLocation.getX(), newLocation.getY(), newLocation.getZ()),
                packet.getPitch(),
                packet.getYaw(),
                packet.getHeadYaw(),
                packet.getData(),
                packet.getVelocity()
            );
            plugin.getClaimParticleTask().getPlayerMarkers().put(p.getUniqueId(), updatedPacket);
        } catch (Exception e) {
            WrapperPlayServerSpawnEntity newPacket = setMarkerPacket(newLocation, p, plugin);
            if (newPacket != null) {
                plugin.getClaimParticleTask().getPlayerMarkers().put(p.getUniqueId(), newPacket);
            }
            e.printStackTrace();
        }
    }

    public static Location getTargetLocation(Player p, Sectors plugin) {
        int distance = plugin.getClaimParticleTask().getPlayerMarkerDistances().getOrDefault(p.getUniqueId(), ConfigManager.DEFAULT_REACH);
        Vector direction = p.getLocation().getDirection();
        return p.getEyeLocation().add(direction.multiply(distance));
    }

    public static void clearAllPositionsAndMarkers(Player p, Boolean removeFromClaimMode, Sectors plugin) {
        UUID pUUID = p.getUniqueId();
        plugin.getClaimParticleTask().getPlayerMarkerDistances().remove(pUUID);

        WrapperPlayServerSpawnEntity marker = plugin.getClaimParticleTask().getPlayerMarkers().get(pUUID);
        if (marker != null) {
            removeMarketPacket(p, marker, plugin);
        }

        if (plugin.getData().getSelection(p) != null) {
            plugin.getData().getSelections().remove(p);
        }

        PacketPair packetPair = plugin.getClaimToolEvents().getPlayerClaimPositions().get(pUUID);
        if (packetPair != null) {
            if (packetPair.getPacketOne() != null) {
                removeMarketPacket(p, (WrapperPlayServerSpawnEntity) packetPair.getPacketOne(), plugin);
            }
            if (packetPair.getPacketTwo() != null) {
                removeMarketPacket(p, (WrapperPlayServerSpawnEntity) packetPair.getPacketTwo(), plugin);
            }
            plugin.getClaimToolEvents().getPlayerClaimPositions().remove(pUUID);
        }

        if (!ServiceManager.getPlayerEntityService().getEntityIDsForPlayer(p).isEmpty()) {
            ClaimUtilities.removeGlowingBounds(p, plugin);
        }

        if (removeFromClaimMode) {
            plugin.getClaimToolEvents().getClaimModePlayers().remove(pUUID);
            p.sendMessage("You have been removed from claim mode!");
        } else {
            WrapperPlayServerSpawnEntity markerPacket = plugin.getClaimParticleTask().getPlayerMarkers().get(pUUID);
            if (markerPacket != null) {
                teleportMarkerPacket(markerPacket, getTargetLocation(p, plugin), p, plugin);
            }
        }
    }
}
