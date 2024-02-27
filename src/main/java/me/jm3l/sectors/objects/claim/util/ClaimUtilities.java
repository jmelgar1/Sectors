package me.jm3l.sectors.objects.claim.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.service.PlayerEntityService;
import me.jm3l.sectors.utilities.VectorPair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimUtilities {
    public static void showGlowingBounds(List<Location> edgeLocations, Player p, Sectors plugin, PlayerEntityService playerEntityService) {
        ProtocolManager protocolManager = plugin.getProtocolManager();

        for (Location loc : edgeLocations) {
            loc.add(0, 1, 0);

            PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            int uniqueId = (int) (Math.random() * Integer.MAX_VALUE);

            spawnPacket.getIntegers()
                    .write(0, uniqueId);
            spawnPacket.getUUIDs()
                    .write(0, UUID.randomUUID());
            spawnPacket.getEntityTypeModifier()
                    .write(0, EntityType.FALLING_BLOCK);
            spawnPacket.getDoubles()
                    .write(0, loc.getX())
                    .write(1, loc.getY())
                    .write(2, loc.getZ());

//            Block block = // whatever
//            int stateId = net.minecraft.world.level.block.Block.getId(((CraftBlockData) block.getData).getState())
//            spawnPacket.getIntegers().write(4, 8);

            protocolManager.sendServerPacket(p, spawnPacket);

            PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            metadataPacket.getModifier().writeDefaults();
            metadataPacket.getIntegers().write(0, uniqueId);
//            metadataPacket.getBlockData().write(0,
//                    WrappedBlockData.createData(Material.GLASS));

            WrappedDataWatcher.Serializer booleanType = WrappedDataWatcher.Registry.get(Boolean.class);
            List<WrappedDataValue> values = Lists.newArrayList(
                    new WrappedDataValue(5, booleanType, true)
            );

            metadataPacket.getDataValueCollectionModifier().write(0, values);
            protocolManager.sendServerPacket(p, metadataPacket);

            playerEntityService.addEntityIDForPlayer(p, uniqueId);
        }
    }

    public static List<Location> calculateEdgeLocations(Vector start, Vector end, World world) {
        List<Location> locations = new ArrayList<>();

        int x = start.getBlockX();
        int y = start.getBlockY();
        int z = start.getBlockZ();
        int x2 = end.getBlockX();
        int y2 = end.getBlockY();
        int z2 = end.getBlockZ();

        // Top and bottom edges
        for (int currentX = x; currentX <= x2; currentX++) {
            locations.add(new Location(world, currentX, y, z));
            locations.add(new Location(world, currentX, y, z2));
            locations.add(new Location(world, currentX, y2, z));
            locations.add(new Location(world, currentX, y2, z2));
        }
        for (int currentZ = z; currentZ <= z2; currentZ++) {
            locations.add(new Location(world, x, y, currentZ));
            locations.add(new Location(world, x2, y, currentZ));
            locations.add(new Location(world, x, y2, currentZ));
            locations.add(new Location(world, x2, y2, currentZ));
        }
        // Vertical edges
        for (int currentY = y; currentY <= y2; currentY++) {
            locations.add(new Location(world, x, currentY, z));
            locations.add(new Location(world, x2, currentY, z));
            locations.add(new Location(world, x, currentY, z2));
            locations.add(new Location(world, x2, currentY, z2));
        }

        return locations;
    }

    public static VectorPair vectorTransformation(Vector vector1, Vector vector2){
        int x = Math.min(vector1.getBlockX(), vector2.getBlockX());
        int y = Math.min(vector1.getBlockY(), vector2.getBlockY());
        int z = Math.min(vector1.getBlockZ(), vector2.getBlockZ());
        int x2 = Math.max(vector1.getBlockX(), vector2.getBlockX());
        int y2 = Math.max(vector1.getBlockY(), vector2.getBlockY());
        int z2 = Math.max(vector1.getBlockZ(), vector2.getBlockZ());

        return new VectorPair(new Vector(x,y,z), new Vector(x2,y2,z2));
    }

    public static void removeGlowingBounds(Player p, Sectors plugin) {
        ProtocolManager protocolManager = plugin.getProtocolManager();
        List<Integer> entityIDsForPlayer = ServiceManager.getPlayerEntityService().getEntityIDsForPlayer(p);
        if (entityIDsForPlayer.isEmpty()) return;

        PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, entityIDsForPlayer);

        protocolManager.sendServerPacket(p, destroyPacket);

        // Clear the list after removal
        entityIDsForPlayer.clear();
    }
}
