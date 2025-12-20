package me.jm3l.sectors.events;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.claim.ClaimUtilities;
import me.jm3l.sectors.sector.Sector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Handles player session events (join, quit)
 */
public class PlayerSessionEvents implements Listener {
    private final Sectors plugin;
    private final EventDataManager dataManager;

    public PlayerSessionEvents(Sectors plugin, EventDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        for (Sector s : plugin.getData().getSectors()) {
            if (s.hasMember(player)) {
                plugin.getData().addSPlayer(player, s);
                break;
            }
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();

        plugin.getData().removeSPlayer(player);
        dataManager.getPlayerCurrentSector().remove(playerId);

        // Cancel any pending removal tasks
        BukkitTask removalTask = dataManager.getBoundaryRemovalTasks().remove(playerId);
        if (removalTask != null) {
            removalTask.cancel();
        }

        // Remove temporary platform if player logs out in claim mode
        java.util.List<Location> tempPlatform = dataManager.getTemporaryPlatforms().remove(playerId);
        ClaimUtilities.removeTemporaryPlatform(tempPlatform);
    }
}
