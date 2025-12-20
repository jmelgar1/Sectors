package me.jm3l.sectors.claim.events;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.claim.ClaimUtilities;
import me.jm3l.sectors.claim.wand.ClaimToolInventoryUtilities;
import me.jm3l.sectors.claim.wand.ClaimToolPacketUtilities;
import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.events.EventDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.UUID;

/**
 * Handles claim tool interaction events (scrolling, dropping wand)
 */
public class ClaimToolInteractionEvents implements Listener {
    private final Sectors plugin;
    private final EventDataManager dataManager;

    public ClaimToolInteractionEvents(Sectors plugin, EventDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        UUID pUUID = p.getUniqueId();
        int currentDistance = plugin.getClaimParticleTask().getPlayerMarkerDistances().getOrDefault(pUUID, 5);

        boolean isScrollDown = (e.getNewSlot() == 0 && e.getPreviousSlot() == 8) ||
                (e.getNewSlot() > e.getPreviousSlot() && !(e.getPreviousSlot() == 0 && e.getNewSlot() == 8));

        int scrollCount = dataManager.getScrollCounts().getOrDefault(pUUID, 0);
        if (isScrollDown) {
            scrollCount--;
        } else {
            scrollCount++;
        }
        dataManager.getScrollCounts().put(pUUID, scrollCount);
        currentDistance += scrollCount;
        dataManager.getScrollCounts().put(pUUID, 0);
        currentDistance = Math.max(ConfigManager.MIN_CLAIM_REACH, Math.min(currentDistance, ConfigManager.MAX_CLAIM_REACH));
        plugin.getClaimParticleTask().getPlayerMarkerDistances().put(pUUID, currentDistance);
    }

    @EventHandler
    private void onDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (plugin.getClaimWand().isWand(e.getItemDrop().getItemStack())) {
            e.getItemDrop().remove();
            ClaimToolPacketUtilities.clearAllPositionsAndMarkers(p, true, plugin);
            ClaimToolInventoryUtilities.restoreHotbar(p, dataManager.getSavedHotbars(), plugin);

            // Remove temporary platform when leaving claim mode
            java.util.List<Location> tempPlatform = dataManager.getTemporaryPlatforms().remove(p.getUniqueId());
            ClaimUtilities.removeTemporaryPlatform(tempPlatform);
        }
    }
}
