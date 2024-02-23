package me.jm3l.sectors.runnables;

import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.wand.util.ClaimToolUtilities;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ClaimParticleTask extends BukkitRunnable {

    private Map<UUID, Entity> playerMarkers = new HashMap<>();
    public Map<UUID, Entity> getPlayerMarkers() {return playerMarkers;}

    private Map<UUID, Integer> playerMarkerDistances = new HashMap<>();
    public Map<UUID, Integer> getPlayerMarkerDistances() {return playerMarkerDistances;}

    private Sectors plugin;
    public ClaimParticleTask(Sectors plugin) {this.plugin = plugin;}

    @Override
    public void run() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getClaimToolEvents().getClaimModePlayers().containsKey(p.getUniqueId()) &&
                    plugin.getClaimWand().isWand(p.getInventory().getItemInMainHand())) {

                int distance = playerMarkerDistances.getOrDefault(p.getUniqueId(), ConfigManager.DEFAULT_REACH);
                Vector direction = p.getLocation().getDirection();
                Location targetLocation = p.getEyeLocation().add(direction.multiply(distance));

                Entity marker = playerMarkers.get(p.getUniqueId());

                if (marker == null) {
                    Entity newMarker = ClaimToolUtilities.spawnShulkerAtLocationForPlayerOnly(targetLocation, p, plugin.getProtocolManager());
                    playerMarkers.put(p.getUniqueId(), newMarker);
                } else {
                    ClaimToolUtilities.teleportEntityPacket(marker, targetLocation, p, plugin.getProtocolManager());
                }
            } else if (playerMarkers.containsKey(p.getUniqueId())) {
                ClaimToolUtilities.removeShulkerForPlayer(p, playerMarkers.get(p.getUniqueId()), plugin.getProtocolManager());
                playerMarkers.get(p.getUniqueId()).remove();
                playerMarkers.remove(p.getUniqueId());
            }
        }
    }
}