package me.jm3l.sectors.runnables;

import com.comphenix.protocol.events.PacketContainer;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.wand.util.ClaimToolPacketUtilities;
import me.jm3l.sectors.utilities.PacketPair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimParticleTask extends BukkitRunnable {

    private Map<UUID, PacketContainer> playerMarkers = new HashMap<>();
    public Map<UUID, PacketContainer> getPlayerMarkers() {return playerMarkers;}

    private Map<UUID, PacketPair> playerClaimPositions = new HashMap<>();
    public Map<UUID, PacketPair> getPlayerClaimPositions() {return playerClaimPositions;}

    private Map<UUID, Integer> playerMarkerDistances = new HashMap<>();
    public Map<UUID, Integer> getPlayerMarkerDistances() {return playerMarkerDistances;}

    private Sectors plugin;
    public ClaimParticleTask(Sectors plugin) {this.plugin = plugin;}

    @Override
    public void run() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getClaimToolEvents().getClaimModePlayers().containsKey(p.getUniqueId()) &&
                    plugin.getClaimWand().isWand(p.getInventory().getItemInMainHand())) {

                Location targetLocation = ClaimToolPacketUtilities.getTargetLocation(p, plugin);
                PacketContainer marker = playerMarkers.get(p.getUniqueId());
                PacketPair packetPair = plugin.getClaimToolEvents().getPlayerClaimPositions().get(p.getUniqueId());

                if (marker == null) {
                    PacketContainer newPacket = ClaimToolPacketUtilities.setMarkerPacket(targetLocation, p, plugin);
                    plugin.getClaimParticleTask().getPlayerMarkers().put(p.getUniqueId(), newPacket);
                    plugin.getClaimToolEvents().getPlayerClaimPositions().computeIfAbsent(p.getUniqueId(), k -> new PacketPair(null, null));
                } else if(packetPair.getPacketOne() == null || packetPair.getPacketTwo() == null){
                    ClaimToolPacketUtilities.teleportMarkerPacket(marker, targetLocation, p, plugin);
                }
            } else if (playerMarkers.containsKey(p.getUniqueId())) {
                ClaimToolPacketUtilities.clearAllPositionsAndMarkers(p, true, plugin);
            }
        }
    }
}
