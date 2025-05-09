package me.jm3l.sectors.runnables;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
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

    private final Map<UUID, WrapperPlayServerSpawnEntity> playerMarkers = new HashMap<>();
    public Map<UUID, WrapperPlayServerSpawnEntity> getPlayerMarkers() {return playerMarkers;}

    private final Map<UUID, Integer> playerMarkerDistances = new HashMap<>();
    public Map<UUID, Integer> getPlayerMarkerDistances() {return playerMarkerDistances;}

    private final Sectors plugin;
    public ClaimParticleTask(Sectors plugin) {this.plugin = plugin;}

    @Override
    public void run() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getClaimToolEvents().getClaimModePlayers().containsKey(p.getUniqueId()) &&
                    plugin.getClaimWand().isWand(p.getInventory().getItemInMainHand())) {

                Location targetLocation = ClaimToolPacketUtilities.getTargetLocation(p, plugin);
                WrapperPlayServerSpawnEntity marker = playerMarkers.get(p.getUniqueId());
                PacketPair packetPair = plugin.getClaimToolEvents().getPlayerClaimPositions().get(p.getUniqueId());

                if (marker == null) {
                    WrapperPlayServerSpawnEntity newPacket = ClaimToolPacketUtilities.setMarkerPacket(targetLocation, p, plugin);
                    if (newPacket != null) {
                        playerMarkers.put(p.getUniqueId(), newPacket);
                        plugin.getClaimToolEvents().getPlayerClaimPositions().computeIfAbsent(p.getUniqueId(), k -> new PacketPair(null, null));
                    }
                } else if (packetPair != null && (packetPair.getPacketOne() == null || packetPair.getPacketTwo() == null)) {
                    ClaimToolPacketUtilities.teleportMarkerPacket(marker, targetLocation, p, plugin);
                } else if (packetPair == null) {
                    // Initialize a new packetPair for this player
                    plugin.getClaimToolEvents().getPlayerClaimPositions().put(p.getUniqueId(), new PacketPair(null, null));
                    ClaimToolPacketUtilities.teleportMarkerPacket(marker, targetLocation, p, plugin);
                }
            } else if (playerMarkers.containsKey(p.getUniqueId())) {
                ClaimToolPacketUtilities.clearAllPositionsAndMarkers(p, true, plugin);
            }
        }
    }
}