package me.jm3l.sectors.command.wand.events;

import com.comphenix.protocol.events.PacketContainer;
import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.wand.util.ClaimToolUtilities;
import me.jm3l.sectors.utilities.PacketPair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

    private Map<UUID, PacketPair> playerClaimPositions = new HashMap<>();
    public Map<UUID, PacketPair> getPlayerClaimPositions() {return this.playerClaimPositions;}

    @EventHandler
    public void onItemHold(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItem(e.getNewSlot());
        if (plugin.getClaimWand().isWand(item)) {
            claimModePlayers.put(p.getUniqueId(), true);
        } else {
            ClaimToolUtilities.clearAllPositionsAndMarkers(e.getPlayer(), plugin);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        ClaimToolUtilities.clearAllPositionsAndMarkers(e.getPlayer(), plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        UUID playerUUID = p.getUniqueId();
        if (claimModePlayers.containsKey(playerUUID) && plugin.getClaimWand().isWand(p.getInventory().getItemInMainHand())) {
            Action action = e.getAction();
            int currentDistance = plugin.getClaimParticleTask().getPlayerMarkerDistances().getOrDefault(playerUUID, 5);

            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                // Increase distance
                plugin.getClaimParticleTask().getPlayerMarkerDistances().put(playerUUID, Math.min(currentDistance + 1, ConfigManager.MAX_CLAIM_REACH));
                e.setCancelled(true);
            } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                // Decrease distance
                plugin.getClaimParticleTask().getPlayerMarkerDistances().put(playerUUID, Math.max(currentDistance - 1, ConfigManager.MIN_CLAIM_REACH));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSwapOffhand(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!plugin.getClaimWand().isWand(item)) {
            return;
        }

        e.setCancelled(true);
        UUID pUUID = p.getUniqueId();
        PacketPair packetPair = playerClaimPositions.get(pUUID);

        if (packetPair.getPacketOne() == null || packetPair.getPacketTwo() == null) {
            //get packet location (was creating a new packet at the "same" location but loc changed too fast and created
            //duplicates sometimes
            PacketContainer storedPacket = plugin.getClaimParticleTask().getPlayerMarkers().get(p.getUniqueId());
            Double x = storedPacket.getDoubles().read(0);
            Double y = storedPacket.getDoubles().read(1);
            Double z = storedPacket.getDoubles().read(2);
            Location realLocation = new Location(p.getLocation().getWorld(), x, y, z);
            PacketContainer packet = ClaimToolUtilities.setMarkerPacket(realLocation, p, plugin);

            if (packetPair.getPacketOne() == null) {
                packetPair.setPacketOne(packet);
                p.sendMessage("Set position 1");
            } else if (packetPair.getPacketTwo() == null) {
                packetPair.setPacketTwo(packet);
                p.sendMessage("Set position 2");
            }
        } else {
            p.sendMessage("You can only have 2 points");
        }
    }
}
