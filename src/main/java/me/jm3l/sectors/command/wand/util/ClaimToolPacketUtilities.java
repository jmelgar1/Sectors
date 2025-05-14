package me.jm3l.sectors.command.wand.util;

import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.objects.claim.util.ClaimUtilities;
import me.jm3l.sectors.service.MarkerService;
import me.jm3l.sectors.utilities.PacketPair;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClaimToolPacketUtilities {
    private static final AtomicInteger nextEntityId = new AtomicInteger(1000000);

public static WrapperPlayServerSpawnEntity setMarkerPacket(Location location, Player p) {
        World world = location.getWorld();
        if (world == null) return null;

        int entityId = nextEntityId.getAndIncrement();
        UUID entityUUID = UUID.randomUUID();
        
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
            entityId,
            Optional.of(entityUUID),
            EntityTypes.SHULKER,
            new Vector3d(location.getX(), location.getY(), location.getZ()),
            0.0f,
            0.0f,
            0.0f,
            0,
            Optional.of(new Vector3d(0.0, 0.0, 0.0))
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, spawnPacket);

        // List<EntityData<?>> metadataList = new ArrayList<>();
        // byte flags = (byte) (0x20 | 0x40);
        // EntityData<Byte> flagsData = new EntityData<>(0, EntityDataTypes.BYTE, flags);
        // metadataList.add(flagsData);       
        // WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, metadataList);
        // PacketEvents.getAPI().getPlayerManager().sendPacket(p, metadataPacket);

        return spawnPacket;
    }

    public static void removeMarketPacket(Player p, WrapperPlayServerSpawnEntity packet, Sectors plugin) {
        if (packet == null) return;
        int entityId = packet.getEntityId();
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, destroyPacket);
        
        // Use MarkerService to remove the player's marker
        MarkerService markerService = ServiceManager.getMarkerService();
        markerService.removePlayerMarker(p);
    }

    public static void teleportMarkerPacket(WrapperPlayServerSpawnEntity packet, Location newLocation, Player p, Sectors plugin) {
        MarkerService markerService = ServiceManager.getMarkerService();
        
        if (packet == null) {
            WrapperPlayServerSpawnEntity newPacket = setMarkerPacket(newLocation, p);
            if (newPacket != null) {
                markerService.setPlayerMarker(p, newPacket);
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
            // Note: Not replacing the packet in the map, as the original spawn packet is still needed
        } catch (Exception e) {
            WrapperPlayServerSpawnEntity newPacket = setMarkerPacket(newLocation, p);
            if (newPacket != null) {
                markerService.setPlayerMarker(p, newPacket);
            }
            e.printStackTrace();
        }
    }

    public static Location getTargetLocation(Player p, Sectors plugin) {
        MarkerService markerService = ServiceManager.getMarkerService();
        int distance = markerService.getPlayerMarkerDistance(p, ConfigManager.DEFAULT_REACH);
        Vector direction = p.getLocation().getDirection();
        return p.getEyeLocation().add(direction.multiply(distance));
    }

    public static void removePlayerFromClaimMode(Player p, Sectors plugin) {
        UUID pUUID = p.getUniqueId();
        plugin.getClaimToolEvents().getPlayersInClaimMode().remove(pUUID);
        Bukkit.broadcastMessage("Removed player from claim mode");
    }

    public static void clearAllPositionsAndMarkers(Player p, Boolean removeFromClaimMode, Sectors plugin) {
        UUID pUUID = p.getUniqueId();
        MarkerService markerService = ServiceManager.getMarkerService();
        
        // Remove marker distances
        markerService.removePlayerMarkerDistance(p);

        // Remove marker entities
        WrapperPlayServerSpawnEntity marker = markerService.getPlayerMarker(p);
        if (marker != null) {
            removeMarketPacket(p, marker, plugin);
        }

        // Remove selection if present
        if (plugin.getData().getSelection(p) != null) {
            plugin.getData().getSelections().remove(p);
        }

        // Handle position markers
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

        // Only remove bounds if we're removing from claim mode completely
        // and player is not viewing final claim bounds
        if (removeFromClaimMode && !markerService.isPlayerViewingFinalBounds(p) && 
            !ServiceManager.getPlayerEntityService().getEntityIDsForPlayer(p).isEmpty()) {
            ClaimUtilities.removeGlowingBounds(p, plugin);
        }

        // Remove from claim mode if requested
        if (removeFromClaimMode) {
            removePlayerFromClaimMode(p, plugin);
        }
    }
}
