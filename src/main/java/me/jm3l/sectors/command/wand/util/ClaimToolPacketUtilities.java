package me.jm3l.sectors.command.wand.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.objects.claim.util.ClaimUtilities;
import me.jm3l.sectors.utilities.PacketPair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ClaimToolPacketUtilities {
    public static PacketContainer setMarkerPacket(Location location, Player p, Sectors plugin) {
        World world = location.getWorld();
        if (world == null) return null;
        Entity tempEntity = world.spawnEntity(location, EntityType.SHULKER);
        tempEntity.remove();

        PacketContainer spawnPacket = plugin.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawnPacket.getIntegers().write(0, tempEntity.getEntityId());
        spawnPacket.getUUIDs().write(0, tempEntity.getUniqueId());
        spawnPacket.getDoubles()
                .write(0, tempEntity.getLocation().getX())
                .write(1, tempEntity.getLocation().getY())
                .write(2, tempEntity.getLocation().getZ());
        spawnPacket.getEntityTypeModifier().write(0, EntityType.SHULKER);
        try {plugin.getProtocolManager().sendServerPacket(p, spawnPacket);} catch (Exception e) {e.printStackTrace();}

        PacketContainer metadata = plugin.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadata.getIntegers().write(0, tempEntity.getEntityId());
        List<WrappedDataValue> dataValues = List.of(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) (0x20 | 0x40)));
        metadata.getDataValueCollectionModifier().write(0, dataValues);

        try {plugin.getProtocolManager().sendServerPacket(p, metadata);} catch (Exception e) {e.printStackTrace();}
        return spawnPacket;
    }

    public static void removeMarketPacket(Player p, PacketContainer packet, Sectors plugin) {
        PacketContainer destroyPacket = plugin.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        List<Integer> ids = Collections.singletonList(packet.getIntegers().read(0));
        destroyPacket.getIntLists().write(0, ids);
        try {
            plugin.getProtocolManager().sendServerPacket(p, destroyPacket);
            plugin.getClaimParticleTask().getPlayerMarkers().remove(p.getUniqueId());
        } catch (Exception e) {e.printStackTrace();}
    }

    public static void teleportMarkerPacket(PacketContainer packet, Location newLocation, Player p, Sectors plugin) {
        PacketContainer teleportPacket = plugin.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        Location playerLocation = p.getLocation();
        if (playerLocation.getBlockY() > newLocation.getBlockY()) {
            newLocation.add(0, -1, 0);
            if(p.getFallDistance() > 1.5){
                newLocation.add(0, -4, 0);
            }
        }

        teleportPacket.getIntegers().write(0, packet.getIntegers().read(0));
        teleportPacket.getDoubles()
                .write(0, (double) newLocation.getBlockX())
                .write(1, (double) newLocation.getBlockY())
                .write(2, (double) newLocation.getBlockZ());

        try {
            plugin.getProtocolManager().sendServerPacket(p, teleportPacket);
            plugin.getClaimParticleTask().getPlayerMarkers().replace(p.getUniqueId(), teleportPacket);
        } catch (Exception e) {e.printStackTrace();}
    }

    public static Location getTargetLocation(Player p, Sectors plugin){
        int distance = plugin.getClaimParticleTask().getPlayerMarkerDistances().getOrDefault(p.getUniqueId(), ConfigManager.DEFAULT_REACH);
        Vector direction = p.getLocation().getDirection();
        return p.getEyeLocation().add(direction.multiply(distance));
    }


    public static void clearAllPositionsAndMarkers(Player p, Boolean removeFromClaimMode, Sectors plugin) {
        UUID pUUID = p.getUniqueId();
        plugin.getClaimParticleTask().getPlayerMarkerDistances().remove(pUUID);

        //remove any active markers
        if(plugin.getClaimParticleTask().getPlayerMarkers().get(pUUID) != null){
            ClaimToolPacketUtilities.removeMarketPacket(p, plugin.getClaimParticleTask().getPlayerMarkers().get(pUUID), plugin);
        }

        //remove any selection the player has
        if(plugin.getData().getSelection(p) != null){plugin.getData().getSelections().remove(p);}

        PacketPair packetPair = plugin.getClaimToolEvents().getPlayerClaimPositions().get(pUUID);
        if (packetPair != null) {
            if (packetPair.getPacketOne() != null) {ClaimToolPacketUtilities.removeMarketPacket(p, packetPair.getPacketOne(), plugin);}
            if (packetPair.getPacketTwo() != null) {ClaimToolPacketUtilities.removeMarketPacket(p, packetPair.getPacketTwo(), plugin);}
            plugin.getClaimToolEvents().getPlayerClaimPositions().remove(pUUID);
        }

        if(!ServiceManager.getPlayerEntityService().getEntityIDsForPlayer(p).isEmpty()) {ClaimUtilities.removeGlowingBounds(p, plugin);}

        if(removeFromClaimMode){
            plugin.getClaimToolEvents().getClaimModePlayers().remove(pUUID);
            p.sendMessage("You have been remove from claim mode!");
        } else {
            PacketContainer marker = plugin.getClaimParticleTask().getPlayerMarkers().get(p.getUniqueId());
            if(marker != null){ClaimToolPacketUtilities.teleportMarkerPacket(marker, ClaimToolPacketUtilities.getTargetLocation(p, plugin), p, plugin);}
        }
    }
}
