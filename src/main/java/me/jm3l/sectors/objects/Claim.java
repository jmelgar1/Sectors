package me.jm3l.sectors.objects;

import me.jm3l.sectors.Sectors;
import org.bukkit.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Claim implements ConfigurationSerializable {
    private int x, y, z, x2, y2, z2;
    private BoundingBox bounds;
    private World world;

    private Sectors plugin;

    public Claim(ClaimSelection s, Sectors plugin) {
        Vector pos1 = s.pos1();
        Vector pos2 = s.pos2();

        this.x = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.y = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.z = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        this.world = s.getWorld();
        this.bounds = new BoundingBox(this.x, this.y, this.z, this.x2, this.y2, this.z2);
        this.plugin = plugin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return x == claim.x &&
                y == claim.y &&
                z == claim.z &&
                x2 == claim.x2 &&
                y2 == claim.y2 &&
                z2 == claim.z2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, x2, y2, z2);
    }

    public Claim(MemorySection map, Sectors plugin){
        this.x = (int) map.get("x");
        this.y = (int) map.get("y");
        this.z = (int) map.get("z");
        this.x2 = (int) map.get("x2");
        this.y2 = (int) map.get("y2");
        this.z2 = (int) map.get("z2");
        this.world = Bukkit.getWorld((String) map.get("world"));
        this.bounds = new BoundingBox(this.x,this.y,this.z,this.x2,this.y2,this.z2);
        this.plugin = plugin;
    }


    public BoundingBox getBounds(){
        return this.bounds;
    }
    public boolean containsLocation(Location l){
        return this.bounds.contains(l.toVector()) && this.world.equals(l.getWorld());
    }
    @Override
    public String toString(){
        return ChatColor.YELLOW + "(" + this.x + ", " + this.z2 + "), (" + this.x2 + ", " + this.z2 + ")";
    }

    public boolean overlapsExisting(){
        for(Sector s : plugin.getData().getSectors()){
            if(s.hasClaim()) {
                if (this.getBounds().overlaps(s.getClaim().getBounds())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void showBounds(Player p) {
        double minX = this.getBounds().getMinX();
        double maxX = this.getBounds().getMaxX();
        double minY = this.getBounds().getMinY();
        double maxY = this.getBounds().getMaxY();
        double minZ = this.getBounds().getMinZ();
        double maxZ = this.getBounds().getMaxZ();

        // Adjust the loop to handle 2x2 blocks for the checkered pattern
        for (double x = minX; x <= maxX; x += 2) {
            for (double z = minZ; z <= maxZ; z += 2) {
                // Check for alternating pattern
                boolean isFilledSection = (((int)x / 2) + ((int)z / 2)) % 2 == 0;
                if (isFilledSection) {
                    for (double offsetX = 0; offsetX < 2; ++offsetX) {
                        for (double offsetZ = 0; offsetZ < 2; ++offsetZ) {
                            double finalX = x + offsetX;
                            double finalZ = z + offsetZ;
                            // Ensure we don't go beyond the bounds
                            if (finalX <= maxX && finalZ <= maxZ) {
                                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, finalX, minY, finalZ, 2); // Floor
                                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, finalX, maxY, finalZ, 2); // Ceiling
                            }
                        }
                    }
                }
            }
        }

        // Apply a similar pattern for the walls, ensuring we alternate sections vertically as well
        for (double y = minY; y <= maxY; y += 2) {
            for (double i = minX; i <= maxX; i += 2) { // Front and back walls
                boolean isFilledSection = (((int)y / 2) + ((int)i / 2)) % 2 == 0;
                if (isFilledSection) {
                    for (double offsetY = 0; offsetY < 2; ++offsetY) {
                        for (double offsetI = 0; offsetI < 2; ++offsetI) {
                            double finalY = y + offsetY;
                            double finalI = i + offsetI;
                            if (finalY <= maxY && finalI <= maxX) {
                                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, finalI, finalY, minZ, 2);
                                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, finalI, finalY, maxZ, 2);
                            }
                        }
                    }
                }
            }
            for (double j = minZ; j <= maxZ; j += 2) { // Side walls
                boolean isFilledSection = (((int)y / 2) + ((int)j / 2)) % 2 == 0;
                if (isFilledSection) {
                    for (double offsetY = 0; offsetY < 2; ++offsetY) {
                        for (double offsetJ = 0; offsetJ < 2; ++offsetJ) {
                            double finalY = y + offsetY;
                            double finalJ = j + offsetJ;
                            if (finalY <= maxY && finalJ <= maxZ) {
                                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, minX, finalY, finalJ, 2);
                                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, maxX, finalY, finalJ, 2);
                            }
                        }
                    }
                }
            }
        }
    }

    public static Claim deserialize(MemorySection map, Sectors plugin){
        return new Claim(map, plugin);
    }

    public String start(){
        return this.x + ", " + this.z;
    }
    public String end(){
        return this.x2 + ", " + this.z2;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("world", this.world.getName());
        map.put("x", this.x);
        map.put("y", this.y);
        map.put("z", this.z);
        map.put("x2", this.x2);
        map.put("y2", this.y2);
        map.put("z2", this.z2);
        return map;
    }
}
