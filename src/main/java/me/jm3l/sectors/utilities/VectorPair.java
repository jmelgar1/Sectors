package me.jm3l.sectors.utilities;

import org.bukkit.util.Vector;

public class VectorPair {
    private Vector vector1;
    private Vector vector2;

    public VectorPair(Vector vector1, Vector vector2) {
        this.vector1 = vector1;
        this.vector2 = vector2;
    }

    public Vector getFirst() {
        return vector1;
    }

    public Vector getSecond() {
        return vector2;
    }
}
