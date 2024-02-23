package me.jm3l.sectors.command.wand.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ClaimToolUtilities {
    public static Entity spawnShulkerAtLocationForPlayerOnly(Location location, Player player, ProtocolManager protocolManager) {
        World world = location.getWorld();
        if (world == null) return null;

        // Spawn the entity server-side in a standard way (this entity won't be used directly)
        Entity tempEntity = world.spawnEntity(location, EntityType.SHULKER);
        tempEntity.remove();

        // create packet and send to player
        PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawnPacket.getIntegers()
                .write(0,tempEntity.getEntityId())
                .write(1, 1);
        spawnPacket.getUUIDs()
                .write(0, tempEntity.getUniqueId());
        spawnPacket.getDoubles()
                .write(0, tempEntity.getLocation().getX())
                .write(1, tempEntity.getLocation().getY())
                .write(2, tempEntity.getLocation().getZ());
        spawnPacket.getEntityTypeModifier()
                .write(0, EntityType.SHULKER);

        try {
            protocolManager.sendServerPacket(player, spawnPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PacketContainer metadata = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        List<WrappedDataValue> dataValues = List.of(
                new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) (0x20 | 0x40)) // GLOWING & INVISIBILITY
        );

        metadata.getIntegers().write(0, tempEntity.getEntityId());
        metadata.getDataValueCollectionModifier().write(0, dataValues);

        try {
            protocolManager.sendServerPacket(player, metadata);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the server-side entity in case it's needed for reference
        // Note: This entity is not the one players will see, but it's linked to the packets sent.
        return tempEntity;
    }

    public static void removeShulkerForPlayer(Player player, Entity shulker, ProtocolManager protocolManager) {
        PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        List<Integer> ids = Collections.singletonList(shulker.getEntityId());
        destroyPacket.getIntLists().write(0, ids);

        try {
            protocolManager.sendServerPacket(player, destroyPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void teleportEntityPacket(Entity shulker, Location newLocation, Player player, ProtocolManager protocolManager) {
        PacketContainer teleportPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        Location playerLocation = player.getLocation();
        if (playerLocation.getBlockY() > newLocation.getBlockY()) {
            newLocation.add(0, -1, 0);
            if(player.getFallDistance() > 1.5){
                newLocation.add(0, -3, 0);
            }
        }

        teleportPacket.getIntegers()
                .write(0, shulker.getEntityId());
        teleportPacket.getDoubles()
                .write(0, newLocation.getX())
                .write(1, newLocation.getY())
                .write(2, newLocation.getZ());

        try {
            protocolManager.sendServerPacket(player, teleportPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}