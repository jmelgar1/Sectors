package me.jm3l.sectors.objects.claim;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.objects.Sector;
import me.jm3l.sectors.objects.claim.util.ClaimUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
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

    public List<Location> getEdgeLocations() {
        return ClaimUtilities.calculateEdgeLocations(
                new Vector(x, y, z), new Vector(x2, y2, z2), world);
    }

//    private List<Location> getEdgeLocations() {
//        List<Location> locations = new ArrayList<>();
//
//        // Top and bottom edges
//        for (int currentX = x; currentX <= x2; currentX++) {
//            locations.add(new Location(world, currentX, y, z));
//            locations.add(new Location(world, currentX, y, z2));
//            locations.add(new Location(world, currentX, y2, z));
//            locations.add(new Location(world, currentX, y2, z2));
//        }
//        for (int currentZ = z; currentZ <= z2; currentZ++) {
//            locations.add(new Location(world, x, y, currentZ));
//            locations.add(new Location(world, x2, y, currentZ));
//            locations.add(new Location(world, x, y2, currentZ));
//            locations.add(new Location(world, x2, y2, currentZ));
//        }
//        // Vertical edges
//        for (int currentY = y; currentY <= y2; currentY++) {
//            locations.add(new Location(world, x, currentY, z));
//            locations.add(new Location(world, x2, currentY, z));
//            locations.add(new Location(world, x, currentY, z2));
//            locations.add(new Location(world, x2, currentY, z2));
//        }
//
//        return locations;
//    }

    public static Claim deserialize(MemorySection map, Sectors plugin){
        return new Claim(map, plugin);
    }

    public String start(){
        return this.x + ", " + this.y + ", " + this.z;
    }
    public String end(){
        return this.x2 + ", " + this.y2 + ", " + this.z2;
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
