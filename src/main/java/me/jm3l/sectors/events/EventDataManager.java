package me.jm3l.sectors.events;

import me.jm3l.sectors.sector.Sector;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized data manager for event-related state across all event listeners
 */
public class EventDataManager {
    // Sector boundary tracking
    private final Map<UUID, Sector> playerCurrentSector = new HashMap<>();
    public Map<UUID, Sector> getPlayerCurrentSector() {
        return playerCurrentSector;
    }

    // Claim tool related
    private final Map<UUID, ItemStack[]> savedHotbars = new HashMap<>();
    public Map<UUID, ItemStack[]> getSavedHotbars() {
        return savedHotbars;
    }

    private final Map<UUID, Integer> scrollCounts = new HashMap<>();
    public Map<UUID, Integer> getScrollCounts() {
        return scrollCounts;
    }

    private final Map<UUID, List<Location>> temporaryPlatforms = new HashMap<>();
    public Map<UUID, List<Location>> getTemporaryPlatforms() {
        return temporaryPlatforms;
    }

    // Boundary removal tasks
    private final Map<UUID, BukkitTask> boundaryRemovalTasks = new HashMap<>();
    public Map<UUID, BukkitTask> getBoundaryRemovalTasks() {
        return boundaryRemovalTasks;
    }
}
