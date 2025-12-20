package me.jm3l.sectors.claim;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.shared.service.ServiceManager;
import me.jm3l.sectors.shared.service.PlayerEntityService;
import me.jm3l.sectors.shared.util.VectorPair;
import me.jm3l.sectors.shared.util.nms.NmsRegistry;
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

    public static void showGlowingBounds(List<Location> edgeLocations, Player p, Sectors plugin, PlayerEntityService playerEntityService) {
        showGlowingBounds(edgeLocations, p, plugin, playerEntityService, Material.WHITE_STAINED_GLASS);
    }

    public static void showGlowingBounds(List<Location> edgeLocations, Player p, Sectors plugin, PlayerEntityService playerEntityService, Material material) {
        for (Location loc : edgeLocations) {

            // Center the falling block entity in the block space for proper rendering
            double x = loc.getX() + 0.5;
            double y = loc.getY();
            double z = loc.getZ() + 0.5;

            int entityId = nextEntityId.getAndIncrement();

            WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(UUID.randomUUID()),
                EntityTypes.FALLING_BLOCK,
                new Vector3d(x, y, z),
                0.0f,
                0.0f,
                0.0f,
                NmsRegistry.getBlockId(material.createBlockData()),
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

        // BoundingBox uses exclusive maximums, so subtract 1 to get the actual last block
        int maxX = x2 - 1;
        int maxY = y2 - 1;
        int maxZ = z2 - 1;

        // DEBUG
        System.out.println("=== BOUNDARY CALCULATION DEBUG ===");
        System.out.println("Input start vector: " + x + ", " + y + ", " + z);
        System.out.println("Input end vector: " + x2 + ", " + y2 + ", " + z2);
        System.out.println("Calculated corners (after -1): maxX=" + maxX + ", maxY=" + maxY + ", maxZ=" + maxZ);
        System.out.println("Corner 1 (min): " + x + ", " + y + ", " + z);
        System.out.println("Corner 2 (max): " + maxX + ", " + maxY + ", " + maxZ);

        // Top and bottom edges (horizontal lines)
        for (int currentX = x; currentX <= maxX; currentX++) {
            locations.add(new Location(world, currentX, y, z));        // bottom north
            locations.add(new Location(world, currentX, y, maxZ));     // bottom south
            locations.add(new Location(world, currentX, maxY, z));     // top north
            locations.add(new Location(world, currentX, maxY, maxZ));  // top south
        }
        for (int currentZ = z; currentZ <= maxZ; currentZ++) {
            locations.add(new Location(world, x, y, currentZ));        // bottom west
            locations.add(new Location(world, maxX, y, currentZ));     // bottom east
            locations.add(new Location(world, x, maxY, currentZ));     // top west
            locations.add(new Location(world, maxX, maxY, currentZ));  // top east
        }
        // Vertical edges (corner pillars)
        for (int currentY = y; currentY <= maxY; currentY++) {
            locations.add(new Location(world, x, currentY, z));        // northwest corner
            locations.add(new Location(world, maxX, currentY, z));     // northeast corner
            locations.add(new Location(world, x, currentY, maxZ));     // southwest corner
            locations.add(new Location(world, maxX, currentY, maxZ));  // southeast corner
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

        // DEBUG
        System.out.println("=== VECTOR TRANSFORMATION DEBUG ===");
        System.out.println("Vector1 input: " + vector1.getBlockX() + ", " + vector1.getBlockY() + ", " + vector1.getBlockZ());
        System.out.println("Vector2 input: " + vector2.getBlockX() + ", " + vector2.getBlockY() + ", " + vector2.getBlockZ());
        System.out.println("Min (output start): " + x + ", " + y + ", " + z);
        System.out.println("Max (output end): " + x2 + ", " + y2 + ", " + z2);

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

    /**
     * Initializes a claim by generating a platform if needed and finding a safe home location
     * @param claim The claim to initialize
     * @return Safe home location, or null if none could be found
     */
    public static Location initializeClaimHome(Claim claim) {
        World world = claim.getWorld();
        int minX = claim.getMinX();
        int minY = claim.getMinY();
        int minZ = claim.getMinZ();
        int maxX = claim.getMaxX();
        int maxY = claim.getMaxY();
        int maxZ = claim.getMaxZ();

        // Calculate center coordinates
        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;

        // Check if there are any solid blocks in the entire claim
        Location firstSolidLocation = findFirstSolidBlock(world, minX, minY, minZ, maxX, maxY, maxZ);

        // If no solid blocks found, generate 3x3 glass platform at bottom center
        if (firstSolidLocation == null) {
            generatePlatform(world, centerX, centerZ, minY);
            // Return spawn location on center of platform (slightly above glass)
            return new Location(world, centerX + 0.5, minY + 1, centerZ + 0.5);
        }

        // If solid blocks exist, return the first safe location found
        return firstSolidLocation;
    }

    /**
     * Generates a 3x3 glass platform centered at the given coordinates
     * @param world The world to generate in
     * @param centerX Center X coordinate
     * @param centerZ Center Z coordinate
     * @param y Y level to generate at
     */
    private static void generatePlatform(World world, int centerX, int centerZ, int y) {
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                Location platformLoc = new Location(world, x, y, z);
                world.getBlockAt(platformLoc).setType(Material.GLASS);
            }
        }
    }

    /**
     * Creates a temporary 3x3 glass platform at the player's location
     * @param player The player to create the platform for
     * @return List of platform block locations for later removal
     */
    public static java.util.List<Location> createTemporaryPlatform(org.bukkit.entity.Player player) {
        java.util.List<Location> platformLocations = new java.util.ArrayList<>();
        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();
        int centerX = playerLoc.getBlockX();
        int centerZ = playerLoc.getBlockZ();
        int y = playerLoc.getBlockY() - 1; // One block below player

        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                Location platformLoc = new Location(world, x, y, z);
                world.getBlockAt(platformLoc).setType(Material.GLASS);
                platformLocations.add(platformLoc);
            }
        }

        // Teleport player to center of platform
        Location teleportLoc = new Location(world, centerX + 0.5, y + 1, centerZ + 0.5, playerLoc.getYaw(), playerLoc.getPitch());
        player.teleport(teleportLoc);

        return platformLocations;
    }

    /**
     * Removes a temporary platform
     * @param platformLocations List of locations to remove
     */
    public static void removeTemporaryPlatform(java.util.List<Location> platformLocations) {
        if (platformLocations == null) return;
        for (Location loc : platformLocations) {
            loc.getWorld().getBlockAt(loc).setType(Material.AIR);
        }
    }

    /**
     * Checks if the claim has any solid blocks
     * @return true if solid blocks exist, false otherwise
     */
    public static boolean hasSolidBlocks(Claim claim) {
        return findFirstSolidBlock(
            claim.getWorld(),
            claim.getMinX(),
            claim.getMinY(),
            claim.getMinZ(),
            claim.getMaxX(),
            claim.getMaxY(),
            claim.getMaxZ()
        ) != null;
    }

    /**
     * Scans the claim area for the solid block closest to the center with no blocks above it
     * @return Safe spawn location, or null if no solid blocks found
     */
    private static Location findFirstSolidBlock(World world, int minX, int minY, int minZ,
                                                 int maxX, int maxY, int maxZ) {
        // Calculate center coordinates
        double centerX = (minX + maxX) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;

        Location bestLocation = null;
        double closestDistance = Double.MAX_VALUE;

        // Scan from top to bottom to find topmost solid blocks
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // Find the highest solid block at this X,Z coordinate
                for (int y = maxY; y >= minY; y--) {
                    Location loc = new Location(world, x, y, z);
                    Material blockType = world.getBlockAt(loc).getType();

                    if (blockType.isSolid()) {
                        // Check if there are any solid blocks above this one
                        boolean hasBlocksAbove = false;
                        for (int checkY = y + 1; checkY <= maxY; checkY++) {
                            Location aboveLoc = new Location(world, x, checkY, z);
                            if (world.getBlockAt(aboveLoc).getType().isSolid()) {
                                hasBlocksAbove = true;
                                break;
                            }
                        }

                        // Only consider blocks with no solid blocks above
                        if (!hasBlocksAbove) {
                            // Calculate distance to center (2D distance, ignoring Y)
                            double distance = Math.sqrt(
                                Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2)
                            );

                            if (distance < closestDistance) {
                                closestDistance = distance;
                                bestLocation = findSafeSpawnLocation(world, x, y, z, maxY);
                            }
                        }
                        break; // Found the top solid block at this X,Z, move to next column
                    }
                }
            }
        }

        return bestLocation;
    }

    /**
     * Find a safe spawn location on or above a solid block
     * Looks for a 2-block-high air space above a solid block
     * @return Safe spawn location centered on the block
     */
    private static Location findSafeSpawnLocation(World world, int x, int y, int z, int maxY) {
        // Search upward for a safe 2-block-high air space above a solid block
        for (int checkY = y + 1; checkY <= maxY; checkY++) {
            Location feet = new Location(world, x, checkY, z);
            Location head = new Location(world, x, checkY + 1, z);

            Material feetBlock = world.getBlockAt(feet).getType();
            Material headBlock = world.getBlockAt(head).getType();
            Material below = world.getBlockAt(new Location(world, x, checkY - 1, z)).getType();

            // Check if there's a solid block below and 2 air blocks above
            if (below.isSolid() && feetBlock == Material.AIR && headBlock == Material.AIR) {
                // Return centered location
                return new Location(world, x + 0.5, checkY, z + 0.5);
            }
        }

        // Fallback: return location on top of the block
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    /**
     * Checks if a location is inside or within a certain distance of any existing claim
     * @param location Location to check
     * @param plugin Plugin instance to access sector data
     * @param minDistance Minimum distance required from existing claims
     * @return true if location is valid (not in or too close to existing claims), false otherwise
     */
    public static boolean isValidClaimPosition(Location location, Sectors plugin, int minDistance) {
        for (me.jm3l.sectors.sector.Sector s : plugin.getData().getSectors()) {
            if (!s.hasClaim()) continue;

            Claim existingClaim = s.getClaim();

            // Check if location is inside the claim
            if (existingClaim.containsLocation(location)) {
                return false;
            }

            // Check distance to claim boundaries
            int claimMinX = existingClaim.getMinX();
            int claimMinY = existingClaim.getMinY();
            int claimMinZ = existingClaim.getMinZ();
            int claimMaxX = existingClaim.getMaxX();
            int claimMaxY = existingClaim.getMaxY();
            int claimMaxZ = existingClaim.getMaxZ();

            // Calculate closest point on the claim to the location
            int closestX = Math.max(claimMinX, Math.min(location.getBlockX(), claimMaxX));
            int closestY = Math.max(claimMinY, Math.min(location.getBlockY(), claimMaxY));
            int closestZ = Math.max(claimMinZ, Math.min(location.getBlockZ(), claimMaxZ));

            // Calculate distance
            double distance = location.distance(new Location(
                location.getWorld(),
                closestX,
                closestY,
                closestZ
            ));

            if (distance < minDistance) {
                return false;
            }
        }

        return true;
    }

    //refresh
}
