package me.jm3l.sectors.events;

import me.jm3l.sectors.Sectors;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimToolEvents implements Listener {
    private Sectors plugin;
    public ClaimToolEvents(Sectors plugin) {
        this.plugin = plugin;
    }
    private Map<UUID, Boolean> claimModePlayers = new HashMap<>();
    public Map<UUID, Boolean> getClaimModePlayers(){
        return this.claimModePlayers;
    }

    @EventHandler
    public void onItemHold(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItem(e.getNewSlot());
        if (plugin.getClaimWand().isWand(item)) {
            claimModePlayers.put(player.getUniqueId(), true);
        } else {
            claimModePlayers.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove the player's claim mode status if it's being tracked
        claimModePlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (claimModePlayers.containsKey(playerUUID) && plugin.getClaimWand().isWand(player.getInventory().getItemInMainHand())) {
            Action action = event.getAction();
            int currentDistance = plugin.getClaimParticleTask().getPlayerMarkerDistances().getOrDefault(playerUUID, 5);

            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                // Increase distance
                plugin.getClaimParticleTask().getPlayerMarkerDistances().put(playerUUID, Math.min(currentDistance + 1, 15));
                event.setCancelled(true);
            } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                // Decrease distance
                plugin.getClaimParticleTask().getPlayerMarkerDistances().put(playerUUID, Math.max(currentDistance - 1, 1));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) {
            UUID playerUUID = p.getUniqueId();

            // Check if the player is in claim mode and has a marker
            if (plugin.getClaimToolEvents().getClaimModePlayers().containsKey(playerUUID)) {
                Entity marker = plugin.getClaimParticleTask().getPlayerMarkers().get(playerUUID);

                // Check if the entity being hit is the player's marker
                if (marker != null && event.getEntity().equals(marker)) {
                    int currentDistance = plugin.getClaimParticleTask().getPlayerMarkerDistances().getOrDefault(playerUUID, 5);
                    // Increase distance, considering the marker might be too close
                    plugin.getClaimParticleTask().getPlayerMarkerDistances().put(playerUUID, Math.min(currentDistance + 1, 15));

                    // Cancel the event to prevent damage
                    event.setCancelled(true);
                }
            }
        }
    }
}