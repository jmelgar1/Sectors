package me.jm3l.sectors.objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class ClaimSelection {
    private Vector start, end;
    private World world;

    public ClaimSelection() {
        this.start = new Vector();
        this.end = new Vector();
    }

    public Vector pos1(){
        return this.start;
    }

    public Vector pos2(){
        return this.end;
    }

    public World getWorld(){
        return this.world;
    }

    public void setPos1(Location l){
        this.start = l.toVector();
        this.world = l.getWorld();
    }

    public void setPos2(Location l){
        this.end = l.toVector();
        this.world = l.getWorld();
    }
}
