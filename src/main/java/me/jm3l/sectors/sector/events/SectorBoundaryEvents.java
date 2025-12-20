package me.jm3l.sectors.sector.events;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.claim.ClaimUtilities;
import me.jm3l.sectors.claim.commands.RadarCommand;
import me.jm3l.sectors.events.EventDataManager;
import me.jm3l.sectors.sector.Sector;
import me.jm3l.sectors.shared.service.ServiceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Handles sector boundary events (entering/leaving claims)
 */
public class SectorBoundaryEvents implements Listener {
    private final Sectors plugin;
    private final EventDataManager dataManager;

    public SectorBoundaryEvents(Sectors plugin, EventDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlock().equals(e.getTo().getBlock()))
            return;

        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();
        Location toLocation = e.getTo();

        Sector newSector = null;
        for (Sector s : plugin.getData().getSectors()) {
            if (s.hasClaim() && s.getClaim().containsLocation(toLocation)) {
                newSector = s;
                break;
            }
        }

        Sector currentSector = dataManager.getPlayerCurrentSector().get(playerId);
        if (newSector != null && !newSector.equals(currentSector)) {
            p.sendPlainMessage(ChatColor.YELLOW + "You have entered the claim of " + newSector.getName() + ".");
            dataManager.getPlayerCurrentSector().put(playerId, newSector);

            // Only show boundaries if radar is NOT active
            if (!RadarCommand.isRadarActive(playerId)) {
                // Cancel any pending removal task
                BukkitTask removalTask = dataManager.getBoundaryRemovalTasks().remove(playerId);
                if (removalTask != null) {
                    removalTask.cancel();
                }

                // Remove any existing boundaries before showing new ones
                ClaimUtilities.removeGlowingBounds(p, plugin);

                // Determine boundary color based on sector ownership
                Sector playerSector = plugin.getData().getSector(p);
                Material boundaryMaterial = (playerSector != null && playerSector.equals(newSector))
                        ? Material.GREEN_STAINED_GLASS
                        : Material.RED_STAINED_GLASS;

                // Highlight claim boundaries
                ClaimUtilities.showGlowingBounds(
                        newSector.getClaim().getEdgeLocations(),
                        p,
                        plugin,
                        ServiceManager.getPlayerEntityService(),
                        boundaryMaterial
                );
            }
        } else if (currentSector != null && (newSector == null || !newSector.equals(currentSector))) {
            p.sendMessage(ChatColor.YELLOW + "You have left the claim of " + currentSector.getName() + ".");
            dataManager.getPlayerCurrentSector().remove(playerId);

            // Only schedule removal if radar is NOT active
            if (!RadarCommand.isRadarActive(playerId)) {
                // Schedule boundary removal after 3 seconds
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    ClaimUtilities.removeGlowingBounds(p, plugin);
                    dataManager.getBoundaryRemovalTasks().remove(playerId);
                }, 60L);
                dataManager.getBoundaryRemovalTasks().put(playerId, task);
            }
        }
    }
}
