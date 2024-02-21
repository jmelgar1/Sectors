package me.jm3l.sectors.events;

import com.sk89q.worldedit.internal.annotation.Selection;
import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.objects.ClaimSelection;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Events implements Listener {
    private Sectors plugin;
    public Events(Sectors plugin) {
        this.plugin = plugin;
    }

//    @EventHandler
//    public void onInteract(PlayerInteractEvent event) {
//        Player p = event.getPlayer();
//        if (p.getInventory().getItemInMainHand().getType() == Material.CARROT_ON_A_STICK) {
//            if (playerSelections.containsKey(p.getUniqueId())) {
//                ClaimSelection selection = playerSelections.get(p.getUniqueId());
//                if (selection.isSelectionMode()) {
//                    // Check if action is right or left click to set point 1 or point 2
//                    if (selection.getPoint1() == null) { // Set first point
//                        selection.setPoint1(p.getLocation());
//                        p.sendMessage("First point set." + selection.getPoint1());
//                    } else if (selection.getPoint2() == null) { // Set second point
//                        selection.setPoint2(p.getLocation());
//                        p.sendMessage("Second point set."  + selection.getPoint2());
////                        // Optional: Automatically disable selection mode after second point is set
////                        selection.setSelectionMode(false);
//                        p.sendMessage("Selection complete. You can now use /sectors claim.");
//                    }
//                }
//            }
//        }
//    }

    boolean isActionLegal(Player player, Location event){
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
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Block block = e.getClickedBlock();
        Location location = block.getLocation();
        Player p = e.getPlayer();
        if(plugin.getClaimWand().isWand(e.getItem())) {
            if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                ClaimSelection s = plugin.getData().getSelection(p);
                if (s == null) {
                    ClaimSelection selection = new ClaimSelection();
                    selection.setPos1(location);
                    plugin.getData().getSelections().put(p, selection);
                    p.sendMessage(ChatColor.YELLOW + "Position 1 set.");
                    return;
                }
                s.setPos1(location);
                p.sendMessage(ChatColor.YELLOW + "Position 1 set.");
                return;
            }
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                ClaimSelection s = plugin.getData().getSelection(p);
                if (s == null) {
                    ClaimSelection selection = new ClaimSelection();
                    selection.setPos2(location);
                    plugin.getData().getSelections().put(p, selection);
                    p.sendMessage(ChatColor.YELLOW + "Position 2 set.");
                    e.setCancelled(true);
                    return;
                }
                s.setPos2(location);
                p.sendMessage(ChatColor.YELLOW + "Position 2 set.");
                e.setCancelled(true);
                return;
            }
        }
        if(e.getPlayer().hasPermission("sec.admin")) return;
        if(!isActionLegal(p, location)) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DEFAULT);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(e.getPlayer().hasPermission("sec.admin")) return;
        if(e.isCancelled()) return;
        if(!isActionLegal(e.getPlayer(), e.getBlock().getLocation())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
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
    public void onLeave(PlayerQuitEvent e) {
        plugin.getData().removeSPlayer(e.getPlayer());
    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent e){
        if(plugin.getClaimWand().isWand(e.getItemDrop().getItemStack())){
            e.getItemDrop().remove();
        }
    }
//
//    @EventHandler
//    public void onBlockBreak(BlockBreakEvent event) {
//        Player player = event.getPlayer();
//        if (playerSelections.containsKey(player.getUniqueId())) {
//            ClaimSelection selection = playerSelections.get(player.getUniqueId());
//            if (selection.isSelectionMode()) {
//                // Prevent block breaking in selection mode
//                event.setCancelled(true);
//                player.sendMessage("You cannot break blocks while in selection mode.");
//            }
//        }
//    }

//    @EventHandler
//    public void onBlockPlace(BlockPlaceEvent event) {
//        Player player = event.getPlayer();
//        if (playerSelections.containsKey(player.getUniqueId())) {
//            ClaimSelection selection = playerSelections.get(player.getUniqueId());
//            if (selection.isSelectionMode()) {
//                // Prevent block placing in selection mode
//                event.setCancelled(true);
//                player.sendMessage("You cannot place blocks while in selection mode.");
//            }
//        }
//    }
}
