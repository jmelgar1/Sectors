package me.jm3l.sectors.objects;

import com.sk89q.worldedit.internal.annotation.Selection;
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
                z == claim.z &&
                x2 == claim.x2 &&
                z2 == claim.z2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, x2, z2);
    }

    public Claim(MemorySection map, Sectors plugin){
        this.x = (int) map.get("x");
        this.z = (int) map.get("z");
        this.x2 = (int) map.get("x2");
        this.z2 = (int) map.get("z2");
        this.world = Bukkit.getWorld((String) map.get("world"));
        this.bounds = new BoundingBox(this.x,world.getMinHeight(),this.z,this.x2,world.getMaxHeight(),this.z2);
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

    public void showBounds(Player p){
        double height = p.getWorld().getHighestBlockYAt(this.getBounds().getCenter().toLocation(p.getWorld())) + 25.0;
        double minX = this.getBounds().getMinX();
        double maxX = this.getBounds().getMaxX();
        double minZ = this.getBounds().getMinZ();
        double maxZ = this.getBounds().getMaxZ();
        for (double i = minX; i < maxX; i += 1d / 3d) {
            for (double j = 62; j < height; j += 4.0) {
                p.spawnParticle(Particle.DRIP_LAVA, i, j, minZ, 1);
                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, i, j + 2.0, minZ, 1);
            }
        }
        for (double i = minX; i < maxX; i += 1d / 3d) {
            for (double j = 62; j < height; j += 4.0) {
                p.spawnParticle(Particle.DRIP_LAVA, i, j, maxZ, 1);
                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, i, j + 2.0, maxZ, 1);
            }
        }
        for (double i = minZ; i < maxZ; i += 1d / 3d) {
            for (double j = 62; j < height; j += 4.0) {
                p.spawnParticle(Particle.DRIP_LAVA, minX, j, i, 1);
                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, minX, j + 2.0, i, 1);
            }
        }
        for (double i = minZ; i < maxZ; i += 1d / 3d) {
            for (double j = 62; j < height; j += 4.0) {
                p.spawnParticle(Particle.DRIP_LAVA, maxX, j, i, 1);
                p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, maxX, j + 2.0, i, 1);
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
        map.put("y", this.y); // Serialize the new Y coordinates
        map.put("z", this.z);
        map.put("x2", this.x2);
        map.put("y2", this.y2); // Serialize the new Y2 coordinate
        map.put("z2", this.z2);
        return map;
    }
}
