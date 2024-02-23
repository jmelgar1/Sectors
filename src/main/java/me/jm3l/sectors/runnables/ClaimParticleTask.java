package me.jm3l.sectors.runnables;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.Sectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ClaimParticleTask extends BukkitRunnable {

    private Map<UUID, Entity> playerMarkers = new HashMap<>();
    public Map<UUID, Entity> getPlayerMarkers() {return playerMarkers;}

    private Map<UUID, Integer> playerMarkerDistances = new HashMap<>();
    public Map<UUID, Integer> getPlayerMarkerDistances() {return playerMarkerDistances;}


    private Sectors plugin;
    public ClaimParticleTask(Sectors plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getClaimToolEvents().getClaimModePlayers().containsKey(p.getUniqueId()) &&
                    plugin.getClaimWand().isWand(p.getInventory().getItemInMainHand())) {

                int distance = playerMarkerDistances.getOrDefault(p.getUniqueId(), ConfigManager.DEFAULT_REACH);
                Vector direction = p.getLocation().getDirection();
                Location targetLocation = p.getEyeLocation().add(direction.multiply(distance));

                Entity marker = playerMarkers.get(p.getUniqueId());

                if (marker == null) {
                    Entity newMarker = spawnShulkerAtLocationForPlayerOnly(targetLocation, p);
                    playerMarkers.put(p.getUniqueId(), newMarker);
                } else {
                    teleportEntityPacket(marker, targetLocation, p);
                }
            } else if (playerMarkers.containsKey(p.getUniqueId())) {
                removeShulkerForPlayer(p, playerMarkers.get(p.getUniqueId()));
                playerMarkers.get(p.getUniqueId()).remove();
                playerMarkers.remove(p.getUniqueId());
            }
        }
    }

    private Entity spawnShulkerAtLocationForPlayerOnly(Location location, Player player) {
        World world = location.getWorld();
        if (world == null) return null; // Safety check

        // Spawn the entity server-side in a standard way (this entity won't be used directly)
        Entity tempEntity = world.spawnEntity(location, EntityType.SHULKER);
        tempEntity.remove();

        // create packet and send to player
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
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

    private void removeShulkerForPlayer(Player player, Entity shulker) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        List<Integer> ids = Collections.singletonList(shulker.getEntityId());
        destroyPacket.getIntLists().write(0, ids);

        try {
            protocolManager.sendServerPacket(player, destroyPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void teleportEntityPacket(Entity shulker, Location newLocation, Player player) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
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