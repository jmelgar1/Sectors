package me.jm3l.sectors.service;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.wand.util.ClaimToolPacketUtilities;
import me.jm3l.sectors.utilities.PacketPair;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service responsible for managing claim markers and player claim states
 */
public class MarkerService {
    private final Sectors plugin;
    
    // Player marker tracking
    private final Map<UUID, WrapperPlayServerSpawnEntity> playerMarkers = new HashMap<>();
    private final Map<UUID, Integer> playerMarkerDistances = new HashMap<>();
    
    // Player state tracking
    private final Set<UUID> playersViewingFinalBounds = new HashSet<>();
    
    public MarkerService(Sectors plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Get the marker for a player
     */
    public WrapperPlayServerSpawnEntity getPlayerMarker(Player player) {
        return playerMarkers.get(player.getUniqueId());
    }
    
    /**
     * Set a marker for a player
     */
    public void setPlayerMarker(Player player, WrapperPlayServerSpawnEntity marker) {
        playerMarkers.put(player.getUniqueId(), marker);
    }
    
    /**
     * Remove a player's marker
     */
    public void removePlayerMarker(Player player) {
        playerMarkers.remove(player.getUniqueId());
    }
    
    /**
     * Check if a player has a marker
     */
    public boolean hasPlayerMarker(Player player) {
        return playerMarkers.containsKey(player.getUniqueId());
    }
    
    /**
     * Get the marker distance for a player
     */
    public int getPlayerMarkerDistance(Player player, int defaultDistance) {
        return playerMarkerDistances.getOrDefault(player.getUniqueId(), defaultDistance);
    }
    
    /**
     * Set the marker distance for a player
     */
    public void setPlayerMarkerDistance(Player player, int distance) {
        playerMarkerDistances.put(player.getUniqueId(), distance);
    }
    
    /**
     * Remove a player's marker distance
     */
    public void removePlayerMarkerDistance(Player player) {
        playerMarkerDistances.remove(player.getUniqueId());
    }
    
    /**
     * Mark a player as viewing final bounds
     */
    public void addPlayerViewingFinalBounds(Player player) {
        playersViewingFinalBounds.add(player.getUniqueId());
    }
    
    /**
     * Remove a player from viewing final bounds
     */
    public void removePlayerViewingFinalBounds(Player player) {
        playersViewingFinalBounds.remove(player.getUniqueId());
    }
    
    /**
     * Check if a player is viewing final bounds
     */
    public boolean isPlayerViewingFinalBounds(Player player) {
        return playersViewingFinalBounds.contains(player.getUniqueId());
    }
    
    /**
     * Update a player's marker to a new location
     */
    public void updatePlayerMarker(Player player, Location targetLocation) {
        UUID playerUuid = player.getUniqueId();
        WrapperPlayServerSpawnEntity marker = playerMarkers.get(playerUuid);
        PacketPair packetPair = plugin.getClaimToolEvents().getPlayerClaimPositions().get(playerUuid);
        
        if (marker == null) {
            WrapperPlayServerSpawnEntity newPacket = ClaimToolPacketUtilities.setMarkerPacket(targetLocation, player);
            if (newPacket != null) {
                playerMarkers.put(playerUuid, newPacket);
                plugin.getClaimToolEvents().getPlayerClaimPositions().computeIfAbsent(playerUuid, k -> new PacketPair(null, null));
            }
        } else if (packetPair != null && (packetPair.getPacketOne() == null || packetPair.getPacketTwo() == null)) {
            ClaimToolPacketUtilities.teleportMarkerPacket(marker, targetLocation, player, plugin);
        } else if (packetPair == null) {
            // Initialize a new packetPair for this player
            plugin.getClaimToolEvents().getPlayerClaimPositions().put(playerUuid, new PacketPair(null, null));
            ClaimToolPacketUtilities.teleportMarkerPacket(marker, targetLocation, player, plugin);
        }
    }
} 