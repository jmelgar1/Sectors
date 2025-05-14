package me.jm3l.sectors.runnables;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.wand.util.ClaimToolPacketUtilities;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.service.MarkerService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * This task is responsible for visualizing claim markers in real time.
 * It only focuses on keeping markers updated, not on clearing positions or bounds.
 */
public class ClaimParticleTask extends BukkitRunnable {
    private final Sectors plugin;
    
    public ClaimParticleTask(Sectors plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        MarkerService markerService = ServiceManager.getMarkerService();
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerUuid = player.getUniqueId();
            
            // Skip players who are viewing final claim bounds
            if (markerService.isPlayerViewingFinalBounds(player)) {
                continue;
            }
            
            // Only update markers for players in claim mode with wand in hand
            if (plugin.getClaimToolEvents().getPlayersInClaimMode().containsKey(playerUuid) &&
                    plugin.getClaimWand().isWand(player.getInventory().getItemInMainHand())) {
                
                // Get target location and update the marker
                Location targetLocation = ClaimToolPacketUtilities.getTargetLocation(player, plugin);
                markerService.updatePlayerMarker(player, targetLocation);
            } 
            // // If player has a marker but isn't in claim mode or isn't holding wand, remove markers
            // else if (markerService.hasPlayerMarker(player)) {
            //     ClaimToolPacketUtilities.clearAllPositionsAndMarkers(player, true, plugin);
            // }
        }
    }
}