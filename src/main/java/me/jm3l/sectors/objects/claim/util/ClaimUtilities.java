package me.jm3l.sectors.objects.claim.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.service.PlayerEntityService;
import me.jm3l.sectors.utilities.VectorPair;
import me.jm3l.sectors.utilities.nms.NmsRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ClaimUtilities {
    private static final AtomicInteger nextEntityId = new AtomicInteger(1000000);

    /**
     * Shows glowing block outlines for claim boundaries
     * @param edgeLocations List of locations to show
     * @param p Player to show the outline to
     * @param plugin Main plugin instance
     * @param playerEntityService Entity service
     * @param material Material to use for the outline (default is WHITE_STAINED_GLASS if null)
     */
    public static void showGlowingBounds(List<Location> edgeLocations, Player p, PlayerEntityService playerEntityService, Material material) {
        Material blockMaterial = material != null ? material : Material.WHITE_STAINED_GLASS;
        
        for (Location loc : edgeLocations) {
            loc.add(0.5, 0, 0.5);

            int entityId = nextEntityId.getAndIncrement();

            WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(UUID.randomUUID()),
                EntityTypes.FALLING_BLOCK,
                new Vector3d(loc.getX(), loc.getY(), loc.getZ()),
                0.0f,
                0.0f,
                0.0f,
                NmsRegistry.getBlockId(blockMaterial.createBlockData()),
                Optional.of(new Vector3d(0, 0, 0))
            );

            List<EntityData<?>> metadata = new ArrayList<>();
            metadata.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x40)); // Glowing flag
            metadata.add(new EntityData(5, EntityDataTypes.BOOLEAN, true));    // No gravity
            WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, metadata);

            PacketEvents.getAPI().getPlayerManager().sendPacket(p, spawnPacket);
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, metadataPacket);

            playerEntityService.addEntityIDForPlayer(p, entityId);
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

    public static VectorPair vectorTransformation(Vector vector1, Vector vector2) {
        int x = Math.min(vector1.getBlockX(), vector2.getBlockX());
        int y = Math.min(vector1.getBlockY(), vector2.getBlockY());
        int z = Math.min(vector1.getBlockZ(), vector2.getBlockZ());
        int x2 = Math.max(vector1.getBlockX(), vector2.getBlockX());
        int y2 = Math.max(vector1.getBlockY(), vector2.getBlockY());
        int z2 = Math.max(vector1.getBlockZ(), vector2.getBlockZ());

        return new VectorPair(new Vector(x, y, z), new Vector(x2, y2, z2));
    }

    public static void removeGlowingBounds(Player p, Sectors plugin) {
        List<Integer> entityIDsForPlayer = ServiceManager.getPlayerEntityService().getEntityIDsForPlayer(p);
        if (entityIDsForPlayer.isEmpty()) return;

        int[] entityIDs = entityIDsForPlayer.stream().mapToInt(Integer::intValue).toArray();
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityIDs);
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, destroyPacket);

        entityIDsForPlayer.clear();
    }
    //refresh
}
