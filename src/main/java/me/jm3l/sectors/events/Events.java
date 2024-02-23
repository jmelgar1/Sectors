package me.jm3l.sectors.events;

import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.objects.ClaimSelection;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Events implements Listener {
    private Sectors plugin;
    public Events(Sectors plugin) {this.plugin = plugin;}
    private Map<UUID, Sector> playerCurrentSector = new HashMap<>();

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
    public void onLeave(PlayerQuitEvent e) {plugin.getData().removeSPlayer(e.getPlayer());}

    @EventHandler
    public void dropItem(PlayerDropItemEvent e){
        if(plugin.getClaimWand().isWand(e.getItemDrop().getItemStack())){
            e.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlock().equals(e.getTo().getBlock()))
            return;

        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();
        Location toLocation = e.getTo();
        Sector newSector = null;

        // Find if the player has moved into a new sector
        for (Sector s : plugin.getData().getSectors()) {
            if (s.hasClaim() && s.getClaim().containsLocation(toLocation)) {
                newSector = s;
                break;
            }
        }

        Sector currentSector = playerCurrentSector.get(playerId);

        // Notify the player if they have entered a new sector
        if (newSector != null && !newSector.equals(currentSector)) {
            player.sendMessage(ChatColor.YELLOW + "You have entered the claim of " + newSector.getName() + ".");
            playerCurrentSector.put(playerId, newSector);
        }
        // Notify the player if they have left a sector
        else if (currentSector != null && (newSector == null || !newSector.equals(currentSector))) {
            player.sendMessage(ChatColor.YELLOW + "You have left the claim of " + currentSector.getName() + ".");
            playerCurrentSector.remove(playerId);
        }
    }
}