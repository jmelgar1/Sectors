package me.jm3l.sectors.events;

import me.jm3l.sectors.command.wand.util.ClaimToolInventoryUtilities;
import me.jm3l.sectors.command.wand.util.ClaimToolPacketUtilities;
import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Events implements Listener {
    private Sectors plugin;
    public Events(Sectors plugin) {this.plugin = plugin;}
    private Map<UUID, Sector> playerCurrentSector = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedHotbars = new HashMap<>();
    public Map<UUID, ItemStack[]> getSavedHotbars() {return this.savedHotbars;}
    private final Map<UUID, Integer> scrollCounts = new HashMap<>();

    private boolean isActionLegal(Player player, Location event){
        Sector playerSec = plugin.getData().getSector(player);
        for(Sector s : plugin.getData().getSectors()){
            if(!s.hasClaim()) continue;
            if(!s.getClaim().containsLocation(event)) continue;
            if(s.getDtr() <= 0 && ConfigManager.ENABLE_RAIDING) return true;
            return Objects.equals(s, playerSec);
        }
        return true;
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Block block = e.getClickedBlock();
        Location location = block.getLocation();
        Player p = e.getPlayer();
        if(plugin.getClaimWand().isWand(e.getItem())) {
            e.setCancelled(true);
        }
        if(e.getPlayer().hasPermission("sec.admin")) return;
        if(!isActionLegal(p, location)) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DEFAULT);
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent e) {
        if(e.getPlayer().hasPermission("sec.admin")) return;
        if(e.isCancelled()) return;
        if(!isActionLegal(e.getPlayer(), e.getBlock().getLocation())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent e) {
        ItemStack item = e.getItem();

        if (item != null && plugin.getClaimWand().isWand(item)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent e) {
        if(e.getPlayer().hasPermission("sec.admin")) return;
        if(e.isCancelled()) return;
        if(!isActionLegal(e.getPlayer(), e.getBlock().getLocation())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (Sector s : plugin.getData().getSectors()) {
            if (s.hasMember(e.getPlayer())) {
                plugin.getData().addSPlayer(e.getPlayer(), s);
                break;
            }
        }
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        UUID pUUID = p.getUniqueId();
        int currentDistance = plugin.getClaimParticleTask().getPlayerMarkerDistances().getOrDefault(pUUID, 5);

        boolean isScrollDown = (e.getNewSlot() == 0 && e.getPreviousSlot() == 8) || (e.getNewSlot() > e.getPreviousSlot() && !(e.getPreviousSlot() == 0 && e.getNewSlot() == 8));

        int scrollCount = scrollCounts.getOrDefault(pUUID, 0);
        if (isScrollDown) {scrollCount--;} else {scrollCount++;}
        scrollCounts.put(pUUID, scrollCount);
        currentDistance += scrollCount;
        scrollCounts.put(pUUID, 0);
        currentDistance = Math.max(ConfigManager.MIN_CLAIM_REACH, Math.min(currentDistance, ConfigManager.MAX_CLAIM_REACH));
        plugin.getClaimParticleTask().getPlayerMarkerDistances().put(pUUID, currentDistance);
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent e) {
        plugin.getData().removeSPlayer(e.getPlayer());
    }

    @EventHandler
    private void onDropItem(PlayerDropItemEvent e){
        Player p = e.getPlayer();
        if(plugin.getClaimWand().isWand(e.getItemDrop().getItemStack())){
            e.getItemDrop().remove();
            ClaimToolPacketUtilities.clearAllPositionsAndMarkers(p, true, plugin);
            ClaimToolInventoryUtilities.restoreHotbar(p, savedHotbars, plugin);
        }
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

        Sector currentSector = playerCurrentSector.get(playerId);
        if (newSector != null && !newSector.equals(currentSector)) {
            p.sendPlainMessage(ChatColor.YELLOW + "You have entered the claim of " + newSector.getName() + ".");
            playerCurrentSector.put(playerId, newSector);
        }
        else if (currentSector != null && (newSector == null || !newSector.equals(currentSector))) {
            p.sendMessage(ChatColor.YELLOW + "You have left the claim of " + currentSector.getName() + ".");
            playerCurrentSector.remove(playerId);
        }
    }
}
