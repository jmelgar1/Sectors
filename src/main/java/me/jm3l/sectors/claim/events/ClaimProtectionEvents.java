package me.jm3l.sectors.claim.events;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.sector.Sector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.Material;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles claim protection events (breaking, placing, interacting with blocks)
 */
public class ClaimProtectionEvents implements Listener {
    private final Sectors plugin;

    // Track who placed TNT blocks (Location -> Player UUID)
    private final Map<Location, UUID> tntBlockPlacers = new HashMap<>();
    // Track TNT entities to their placers (TNT UUID -> Player UUID)
    private final Map<UUID, UUID> tntEntityPlacers = new HashMap<>();
    // Track who placed liquid source blocks (Location -> Player UUID)
    private final Map<Location, UUID> liquidPlacers = new HashMap<>();

    public ClaimProtectionEvents(Sectors plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a player's action is legal in a claim
     * @param player The player performing the action
     * @param location The location of the action
     * @return true if the action is allowed, false otherwise
     */
    private boolean isActionLegal(Player player, Location location) {
        Sector playerSec = plugin.getData().getSector(player);
        for (Sector s : plugin.getData().getSectors()) {
            if (!s.hasClaim()) continue;
            if (!s.getClaim().containsLocation(location)) continue;
            if (s.getDtr() <= 0 && ConfigManager.ENABLE_RAIDING) return true;
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

        if (plugin.getClaimWand().isWand(e.getItem())) {
            e.setCancelled(true);
        }

        if (e.getPlayer().hasPermission("sec.admin")) return;
        if (!isActionLegal(p, location)) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DEFAULT);
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().hasPermission("sec.admin")) return;
        if (e.isCancelled()) return;
        if (!isActionLegal(e.getPlayer(), e.getBlock().getLocation())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setCancelled(true);
        }

        // Clean up liquid tracking for broken blocks
        Location blockLoc = e.getBlock().getLocation();
        liquidPlacers.remove(blockLoc);
        tntBlockPlacers.remove(blockLoc);
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent e) {
        if (e.getItem() != null && plugin.getClaimWand().isWand(e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission("sec.admin")) return;
        if (e.isCancelled()) return;
        if (!isActionLegal(e.getPlayer(), e.getBlock().getLocation())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setCancelled(true);
        }

        // Track TNT placement for explosion attribution
        if (e.getBlock().getType() == Material.TNT) {
            tntBlockPlacers.put(e.getBlock().getLocation(), e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    private void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (e.getPlayer().hasPermission("sec.admin")) return;
        if (e.isCancelled()) return;
        Location liquidLocation = e.getBlock().getLocation();
        if (!isActionLegal(e.getPlayer(), liquidLocation)) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setCancelled(true);
        } else {
            // Track who placed this liquid for flow protection
            liquidPlacers.put(liquidLocation, e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    private void onBucketFill(PlayerBucketFillEvent e) {
        if (e.getPlayer().hasPermission("sec.admin")) return;
        if (e.isCancelled()) return;
        Location liquidLocation = e.getBlock().getLocation();
        if (!isActionLegal(e.getPlayer(), liquidLocation)) {
            e.getPlayer().sendMessage(ChatColor.RED + "Area is claimed");
            e.setCancelled(true);
        } else {
            // Clean up liquid tracking when picking up liquid
            liquidPlacers.remove(liquidLocation);
        }
    }

    @EventHandler
    private void onEntitySpawn(EntitySpawnEvent e) {
        // Track TNT entities to their placers
        if (e.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) e.getEntity();
            Location spawnLoc = tnt.getLocation().getBlock().getLocation();

            // Check if we tracked a player placing TNT at this location
            UUID placerId = tntBlockPlacers.remove(spawnLoc);
            if (placerId != null) {
                tntEntityPlacers.put(tnt.getUniqueId(), placerId);
            } else {
                // Check if TNT was ignited by a player (via getSource())
                Entity source = tnt.getSource();
                if (source instanceof Player) {
                    tntEntityPlacers.put(tnt.getUniqueId(), source.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    private void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled()) return;

        // Get the player who caused the explosion (if any)
        Player causingPlayer = getPlayerFromEntity(e.getEntity());

        // If explosion was caused by an admin, allow it
        if (causingPlayer != null && causingPlayer.hasPermission("sec.admin")) {
            return;
        }

        List<org.bukkit.block.Block> blocksToRemove = new ArrayList<>();

        // Check each block affected by the explosion
        for (org.bukkit.block.Block block : e.blockList()) {
            Location blockLoc = block.getLocation();

            // Find the sector this block belongs to
            Sector blockSector = null;
            for (Sector s : plugin.getData().getSectors()) {
                if (!s.hasClaim()) continue;
                if (s.getClaim().containsLocation(blockLoc)) {
                    blockSector = s;
                    break;
                }
            }

            // If block is in a claim
            if (blockSector != null) {
                // Check if claim is raidable
                boolean isRaidable = blockSector.getDtr() <= 0 && ConfigManager.ENABLE_RAIDING;

                if (!isRaidable) {
                    // If not raidable, check if player is a member
                    if (causingPlayer == null || !blockSector.hasMember(causingPlayer)) {
                        // Player is not a member and claim is not raidable - protect the block
                        blocksToRemove.add(block);
                    }
                }
            }
        }

        // Remove protected blocks from explosion
        e.blockList().removeAll(blocksToRemove);
    }

    /**
     * Gets the player who caused an entity action (handles projectiles, TNT, etc.)
     * @param entity The entity that caused the action
     * @return The player responsible, or null if not caused by a player
     */
    private Player getPlayerFromEntity(Entity entity) {
        if (entity instanceof Player) {
            return (Player) entity;
        }

        // Check if entity is a projectile shot by a player
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }

        // Check if entity is TNT and we tracked who placed it
        if (entity instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) entity;
            UUID placerId = tntEntityPlacers.remove(tnt.getUniqueId());
            if (placerId != null) {
                return org.bukkit.Bukkit.getPlayer(placerId);
            }

            // Check if TNT was ignited by a player
            Entity source = tnt.getSource();
            if (source instanceof Player) {
                return (Player) source;
            }
        }

        // For other entities (creepers, etc.), treat as environmental
        return null;
    }

    @EventHandler
    private void onHangingBreak(HangingBreakByEntityEvent e) {
        if (e.isCancelled()) return;

        // Get the remover entity
        Entity remover = e.getRemover();
        Player causingPlayer = null;

        if (remover instanceof Player) {
            causingPlayer = (Player) remover;
        } else if (remover instanceof Projectile) {
            Projectile projectile = (Projectile) remover;
            if (projectile.getShooter() instanceof Player) {
                causingPlayer = (Player) projectile.getShooter();
            }
        }

        // If removed by admin, allow it
        if (causingPlayer != null && causingPlayer.hasPermission("sec.admin")) {
            return;
        }

        Location hangingLoc = e.getEntity().getLocation();

        // Check if hanging entity is in a claim
        for (Sector s : plugin.getData().getSectors()) {
            if (!s.hasClaim()) continue;
            if (!s.getClaim().containsLocation(hangingLoc)) continue;

            // Check if claim is raidable
            boolean isRaidable = s.getDtr() <= 0 && ConfigManager.ENABLE_RAIDING;

            if (!isRaidable) {
                // If not raidable, check if player is a member
                if (causingPlayer == null || !s.hasMember(causingPlayer)) {
                    // Player is not a member and claim is not raidable - protect the entity
                    e.setCancelled(true);
                    if (causingPlayer != null) {
                        causingPlayer.sendMessage(ChatColor.RED + "Area is claimed");
                    }
                }
            }
            break;
        }
    }

    @EventHandler
    private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;

        // Only protect non-player entities (armor stands, item frames, animals, etc.)
        if (e.getEntity() instanceof Player) {
            return;
        }

        // Get the damaging player
        Entity damager = e.getDamager();
        Player causingPlayer = null;

        if (damager instanceof Player) {
            causingPlayer = (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                causingPlayer = (Player) projectile.getShooter();
            }
        }

        // If damaged by admin, allow it
        if (causingPlayer != null && causingPlayer.hasPermission("sec.admin")) {
            return;
        }

        Location entityLoc = e.getEntity().getLocation();

        // Check if entity is in a claim
        for (Sector s : plugin.getData().getSectors()) {
            if (!s.hasClaim()) continue;
            if (!s.getClaim().containsLocation(entityLoc)) continue;

            // Check if claim is raidable
            boolean isRaidable = s.getDtr() <= 0 && ConfigManager.ENABLE_RAIDING;

            if (!isRaidable) {
                // If not raidable, check if player is a member
                if (causingPlayer == null || !s.hasMember(causingPlayer)) {
                    // Player is not a member and claim is not raidable - protect the entity
                    e.setCancelled(true);
                    if (causingPlayer != null) {
                        causingPlayer.sendMessage(ChatColor.RED + "Area is claimed");
                    }
                }
            }
            break;
        }
    }

    @EventHandler
    private void onBlockFromTo(BlockFromToEvent e) {
        if (e.isCancelled()) return;

        Location fromLoc = e.getBlock().getLocation();
        Location toLoc = e.getToBlock().getLocation();

        // Check if the liquid is flowing (water or lava)
        Material blockType = e.getBlock().getType();
        if (blockType != Material.WATER && blockType != Material.LAVA) {
            return;
        }

        // Find who placed the source liquid by tracing back
        UUID placerId = findLiquidSource(fromLoc, blockType);

        // If we can't determine who placed it, allow environmental flow
        if (placerId == null) {
            return;
        }

        Player placer = org.bukkit.Bukkit.getPlayer(placerId);

        // If placer is admin, allow flow
        if (placer != null && placer.hasPermission("sec.admin")) {
            return;
        }

        // Check if destination is in a claim
        Sector destinationSector = null;
        for (Sector s : plugin.getData().getSectors()) {
            if (!s.hasClaim()) continue;
            if (s.getClaim().containsLocation(toLoc)) {
                destinationSector = s;
                break;
            }
        }

        // If flowing into a claim
        if (destinationSector != null) {
            // Check if claim is raidable
            boolean isRaidable = destinationSector.getDtr() <= 0 && ConfigManager.ENABLE_RAIDING;

            if (!isRaidable) {
                // Check if placer is a member of the destination sector
                if (placer == null || !destinationSector.hasMember(placer)) {
                    // Not a member - block the flow
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Finds the player who placed the liquid source by tracing back flow
     * @param location The location to check
     * @param liquidType The type of liquid (WATER or LAVA)
     * @return The UUID of the player who placed the source, or null if unknown
     */
    private UUID findLiquidSource(Location location, Material liquidType) {
        // Check if this exact location has a tracked placer
        UUID placer = liquidPlacers.get(location);
        if (placer != null) {
            return placer;
        }

        // Trace back to find the source
        // Check adjacent blocks (liquids flow from source)
        Location[] adjacentLocations = {
            location.clone().add(1, 0, 0),
            location.clone().add(-1, 0, 0),
            location.clone().add(0, 1, 0), // Check above for water falls
            location.clone().add(0, 0, 1),
            location.clone().add(0, 0, -1)
        };

        for (Location adjacentLoc : adjacentLocations) {
            UUID adjacentPlacer = liquidPlacers.get(adjacentLoc);
            if (adjacentPlacer != null) {
                // Track this location too for faster lookups
                liquidPlacers.put(location, adjacentPlacer);
                return adjacentPlacer;
            }
        }

        return null;
    }
}
