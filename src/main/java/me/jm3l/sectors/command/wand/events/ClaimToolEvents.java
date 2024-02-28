package me.jm3l.sectors.command.wand.events;

import com.comphenix.protocol.events.PacketContainer;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.wand.util.ClaimToolPacketUtilities;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.objects.claim.ClaimSelection;
import me.jm3l.sectors.objects.claim.util.ClaimUtilities;
import me.jm3l.sectors.utilities.PacketPair;
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

    private Map<UUID, PacketPair> playerClaimPositions = new HashMap<>();
    public Map<UUID, PacketPair> getPlayerClaimPositions() {return this.playerClaimPositions;}

    @EventHandler
    private void onItemHold(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItem(e.getNewSlot());
        if (plugin.getClaimWand().isWand(item)) {
            claimModePlayers.put(p.getUniqueId(), true);
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
                PacketContainer storedPacket = plugin.getClaimParticleTask().getPlayerMarkers().get(p.getUniqueId());
                Double x = storedPacket.getDoubles().read(0);
                Double y = storedPacket.getDoubles().read(1);
                Double z = storedPacket.getDoubles().read(2);
                Location realLocation = new Location(p.getLocation().getWorld(), x, y, z);

                if (packetPair.getPacketOne() == null && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                    selection.setPos1(realLocation);
                    plugin.getData().getSelections().put(p, selection);

                    PacketContainer packet = ClaimToolPacketUtilities.setMarkerPacket(realLocation, p, plugin);
                    packetPair.setPacketOne(packet);
                    p.sendMessage("Set position 1");
                } else if (packetPair.getPacketTwo() == null && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                    selection.setPos2(realLocation);
                    plugin.getData().getSelections().put(p, selection);

                    PacketContainer packet = ClaimToolPacketUtilities.setMarkerPacket(realLocation, p, plugin);
                    packetPair.setPacketTwo(packet);
                    p.sendMessage("Set position 2");
                }

                if (!selection.pos1().isZero() && !selection.pos2().isZero()) {

                    p.sendMessage("Pos1: " + selection.pos1().toString());
                    p.sendMessage("Pos2: " + selection.pos2().toString());

                    ClaimUtilities.showGlowingBounds(selection.getEdgeLocations(), p, plugin, ServiceManager.getPlayerEntityService());
                }
            } else {
                p.sendMessage("You can only have 2 points, right click a corner to move it.");
            }
        }
    }
}
