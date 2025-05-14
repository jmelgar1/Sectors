package me.jm3l.sectors.command.wand.events;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.wand.util.ClaimToolPacketUtilities;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.objects.claim.ClaimSelection;
import me.jm3l.sectors.objects.claim.util.ClaimUtilities;
import me.jm3l.sectors.utilities.PacketPair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimToolEvents implements Listener {
    private final Sectors plugin;
    public ClaimToolEvents(Sectors plugin) {
        this.plugin = plugin;
    }
    private final Map<UUID, Boolean> playersInClaimMode = new HashMap<>();
    public Map<UUID, Boolean> getPlayersInClaimMode(){
        return this.playersInClaimMode;
    }

    private final Map<UUID, PacketPair> playerClaimPositions = new HashMap<>();
    public Map<UUID, PacketPair> getPlayerClaimPositions() {return this.playerClaimPositions;}

    @EventHandler
    private void onItemHold(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItem(e.getNewSlot());
        if (plugin.getClaimWand().isWand(item)) {
            playersInClaimMode.put(p.getUniqueId(), true);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        ClaimToolPacketUtilities.clearAllPositionsAndMarkers(e.getPlayer(), true, plugin);
    }

    @EventHandler
    private void onSwapOffHand(PlayerSwapHandItemsEvent e){
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (plugin.getClaimWand().isWand(item)) {
            e.setCancelled(true);
            ClaimToolPacketUtilities.clearAllPositionsAndMarkers(e.getPlayer(), false, plugin);
        }
    }

    @EventHandler
    private void onMouseClickSelection(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        Action action = e.getAction();

        if (!plugin.getClaimWand().isWand(item)) {
            return;
        }

        e.setCancelled(true);
        UUID pUUID = p.getUniqueId();
        PacketPair packetPair = playerClaimPositions.get(pUUID);

        if (plugin.getData().getSelection(p) == null) {
            plugin.getData().getSelections().put(p, new ClaimSelection());
        }

        ClaimSelection selection = plugin.getData().getSelection(p);
        if(packetPair != null) {
            if (packetPair.getPacketOne() == null || packetPair.getPacketTwo() == null) {
                WrapperPlayServerSpawnEntity storedPacket = ServiceManager.getMarkerService().getPlayerMarker(p);
                if (storedPacket != null) {
                    Location realLocation = ClaimToolPacketUtilities.getTargetLocation(p, plugin);
                    if (packetPair.getPacketOne() == null && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                        selection.setPos1(realLocation);
                        plugin.getData().getSelections().put(p, selection);

                        WrapperPlayServerSpawnEntity packet = ClaimToolPacketUtilities.setMarkerPacket(realLocation, p);
                        packetPair.setPacketOne(packet);
                        p.sendMessage("Position 1 - " + realLocation.toString());
                    } else if (packetPair.getPacketTwo() == null && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                        selection.setPos2(realLocation);
                        plugin.getData().getSelections().put(p, selection);

                        WrapperPlayServerSpawnEntity packet = ClaimToolPacketUtilities.setMarkerPacket(realLocation, p);
                        packetPair.setPacketTwo(packet);
                        p.sendMessage("Position 2 - " + realLocation.toString());
                    }

                    if (!selection.pos1().isZero() && !selection.pos2().isZero()) {
                        ClaimUtilities.showGlowingBounds(selection.getEdgeLocations(), p, ServiceManager.getPlayerEntityService(), Material.LIME_STAINED_GLASS);
                    }
                }
            } else {
                p.sendMessage("You can only have 2 points, right click a corner to move it.");
            }
        }
    }
}
