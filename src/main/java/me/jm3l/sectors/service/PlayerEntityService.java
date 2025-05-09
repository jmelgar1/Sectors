package me.jm3l.sectors.service;

import org.bukkit.entity.Player;

import java.util.*;

public class PlayerEntityService {
    private final Map<UUID, List<Integer>> playerEntityIDs = new HashMap<>();

    public void addEntityIDForPlayer(Player p, int entityID) {
        playerEntityIDs.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>()).add(entityID);
    }

    public List<Integer> getEntityIDsForPlayer(Player p) {
        return playerEntityIDs.getOrDefault(p.getUniqueId(), new ArrayList<>());
    }

    public void clearEntitiesForPlayer(Player p) {
        playerEntityIDs.remove(p.getUniqueId());
    }
}
