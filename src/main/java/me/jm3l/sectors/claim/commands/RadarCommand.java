package me.jm3l.sectors.claim.commands;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.claim.Claim;
import me.jm3l.sectors.claim.ClaimUtilities;
import me.jm3l.sectors.core.command.SubCommand;
import me.jm3l.sectors.sector.Sector;
import me.jm3l.sectors.sector.exceptions.NotInSector;
import me.jm3l.sectors.shared.service.ServiceManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RadarCommand implements SubCommand {
    private static final Map<UUID, BukkitTask> activeRadarTasks = new HashMap<>();
    private static final Map<UUID, Set<Sector>> shownClaims = new HashMap<>();

    public static boolean isRadarActive(UUID playerId) {
        return activeRadarTasks.containsKey(playerId);
    }

    @Override
    public String getDescription() {
        return "Toggle radar to show all claim boundaries within 100 blocks";
    }

    @Override
    public String getPermission() {
        return "sec.player.radar";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        UUID playerId = p.getUniqueId();

        // Check if radar is already active - if so, turn it off
        BukkitTask existingTask = activeRadarTasks.get(playerId);
        if (existingTask != null) {
            existingTask.cancel();
            activeRadarTasks.remove(playerId);
            shownClaims.remove(playerId);
            ClaimUtilities.removeGlowingBounds(p, plugin);
            p.sendMessage(Component.text("Radar deactivated.").color(TextColor.color(0x9E9E9E)));
            return;
        }

        // Turn radar on
        Set<Sector> currentShownClaims = new HashSet<>();
        shownClaims.put(playerId, currentShownClaims);

        // Initial scan and display
        int initialClaimsFound = showNearbyClaimsForPlayer(p, plugin, currentShownClaims);

        if (initialClaimsFound == 0) {
            p.sendMessage(Component.text("No claims found within 100 blocks.").color(TextColor.color(0xE57373)));
        }

        p.sendMessage(Component.text("Radar activated. Use /s radar again to deactivate.").color(TextColor.color(0x64B5F6)));

        // Create repeating task to detect new claims (runs every 20 ticks = 1 second)
        BukkitTask radarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!p.isOnline()) {
                BukkitTask task = activeRadarTasks.remove(playerId);
                if (task != null) task.cancel();
                shownClaims.remove(playerId);
                return;
            }
            showNearbyClaimsForPlayer(p, plugin, currentShownClaims);
        }, 20L, 20L);

        activeRadarTasks.put(playerId, radarTask);
    }

    private int showNearbyClaimsForPlayer(Player p, Sectors plugin, Set<Sector> alreadyShown) {
        Location playerLoc = p.getLocation();
        Sector playerSector = plugin.getData().getSector(p);
        int newClaimsFound = 0;

        for (Sector s : plugin.getData().getSectors()) {
            if (!s.hasClaim()) continue;
            if (alreadyShown.contains(s)) continue; // Skip already shown claims

            Claim claim = s.getClaim();

            // Check if claim is within 100 blocks
            if (!claim.getWorld().equals(playerLoc.getWorld())) continue;

            // Calculate closest point on claim to player
            int closestX = Math.max(claim.getMinX(), Math.min(playerLoc.getBlockX(), claim.getMaxX()));
            int closestY = Math.max(claim.getMinY(), Math.min(playerLoc.getBlockY(), claim.getMaxY()));
            int closestZ = Math.max(claim.getMinZ(), Math.min(playerLoc.getBlockZ(), claim.getMaxZ()));

            Location closestPoint = new Location(playerLoc.getWorld(), closestX, closestY, closestZ);
            double distance = playerLoc.distance(closestPoint);

            if (distance <= 100) {
                // Determine boundary color based on sector ownership
                org.bukkit.Material boundaryMaterial = (playerSector != null && playerSector.equals(s))
                    ? org.bukkit.Material.GREEN_STAINED_GLASS
                    : org.bukkit.Material.RED_STAINED_GLASS;

                // Show boundaries (hideRadius = 0 to show all blocks, even near player)
                ClaimUtilities.showGlowingBounds(
                    claim.getEdgeLocations(),
                    p,
                    plugin,
                    ServiceManager.getPlayerEntityService(),
                    boundaryMaterial,
                    0.0
                );

                alreadyShown.add(s);
                newClaimsFound++;
            }
        }

        return newClaimsFound;
    }
}
