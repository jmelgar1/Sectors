package me.jm3l.sectors.objects;

import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.objects.claim.Claim;
import me.jm3l.sectors.objects.claim.util.ClaimUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

public class Sector implements ConfigurationSerializable {
    private Sectors plugin;

    //Attributes
    private final String name;

    public String getName() {
        return this.name;
    }

    private Location home;

    public void setHome(Player p) {
        if (this.claim == null) {
            p.sendMessage(ConfigManager.MUST_HAVE_CLAIM);
            return;
        }
        if (this.claim.containsLocation(p.getLocation())) {
            this.home = p.getLocation();
            p.sendMessage(ConfigManager.SUCCESS);
        } else {
            p.sendMessage(ConfigManager.HOME_MUST_BE_IN_CLAIM);
        }
    }
    public void tpHome(Player p) {
        if (this.home != null) {
            p.teleport(this.home, PlayerTeleportEvent.TeleportCause.COMMAND);
        } else {
            p.sendMessage(ChatColor.RED + "Your sector does not have a home.");
        }
    }

    private String description;

    public void setDescription(String s) {
        this.description = s;
    }

    private int kills;

    public void addKill() {
        this.kills++;
    }

    public int getKills() {
        return this.kills;
    }

    private int dtr;

    public void setDtr(int dtr) {
        this.dtr = dtr;
    }

    public int maxDtr() {
        return Math.min((this.members.size() + 2), ConfigManager.MAXIMUM_DTR);
    }

    public void regenDtr() {
        if (this.dtr < maxDtr()) this.dtr += 1; // Add 1 for the leader of sector
    }

    public int getDtr() {
        return this.dtr;
    }

    public void loseDtr() {
        if (this.dtr > ConfigManager.MINIMUM_DTR) this.dtr--;
    }

    private UUID leader;

    public UUID getLeader() {
        return this.leader;
    }

    private ArrayList<UUID> members = new ArrayList<>();

    public ArrayList<UUID> getMembers() {
        return this.members;
    }

    public Player[] getOnlineMembers() {
        ArrayList<Player> members = new ArrayList<>();
        if (Bukkit.getPlayer(this.leader) != null) members.add(Bukkit.getPlayer(this.leader));
        for (UUID member : this.members) {
            if (Bukkit.getPlayer(member) != null) members.add(Bukkit.getPlayer(member));
        }
        Player[] online = new Player[members.size()];
        members.toArray(online);
        return online;
    }

    public boolean hasMember(Player p) {
        if (this.members.contains(p.getUniqueId())) return true;
        return this.leader.equals(p.getUniqueId());
    }

    private ChatColor color = ChatColor.WHITE;

    public ChatColor getColor() {
        return this.color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    private Claim claim;

    public Claim getClaim() {
        return this.claim;
    }

    public void setClaim(Claim c) {
        this.claim = c;
    }

    public boolean hasClaim() {
        if (this.claim == null) return false;
        return true;
    }


    //Hashcode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sector sector = (Sector) o;
        return name.equals(sector.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    //Constructor for new sector
    public Sector(final String name, final Player p, final Sectors data) {
        this.name = name;
        this.leader = p.getUniqueId();
        this.dtr = 2;
        this.plugin = data;
        this.plugin.getData().addSPlayer(p, this);
        this.kills = 0;
    }

    //Constructor for loaded sector
    public Sector(Map<String, Object> map, final Sectors data) {
        this.plugin = data;
        this.name = (String) map.get("name");
        if (map.get("leader") != null) this.leader = UUID.fromString((String) map.get("leader"));
        for (String m : (ArrayList<String>) map.get("members")) {
            this.members.add(UUID.fromString(m));
            if (Bukkit.getOfflinePlayer(UUID.fromString(m)).isOnline())
                data.getData().addSPlayer(Bukkit.getPlayer(UUID.fromString(m)), this);
        }
        this.description = (String) map.get("description");
        this.dtr = (int) map.get("dtr");
        if (map.get("kills") == null) this.kills = 0; else this.kills = (int) map.get("kills");
        this.color = ChatColor.getByChar((String) map.get("color"));
        this.home = (Location) map.get("home");
        if (map.get("claim") != null) this.claim = Claim.deserialize((MemorySection) map.get("claim"), data);
        else this.claim = null;

    }


    //Broadcast to all members
    public void broadcast(final String s) {
        for (UUID id : this.members) {
            if (Bukkit.getOfflinePlayer(id).isOnline()) {
                Player player = Bukkit.getPlayer(id);
                player.sendMessage(s);
            }
        }
        if (Bukkit.getOfflinePlayer(this.leader).isOnline()) {
            Bukkit.getPlayer(this.leader).sendMessage(s);
        }
    }

    //Set new sector leader & add old one to members.
    public void setLeader(final UUID id) {
        this.members.add(this.leader);
        this.leader = id;
        for (UUID uuid : this.members) {
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                Player p = Bukkit.getPlayer(uuid);
                p.sendMessage(ChatColor.YELLOW + Bukkit.getOfflinePlayer(this.leader).getName() + ChatColor.YELLOW + " is now the leader of the sector!");
            }
        }
        if (Bukkit.getOfflinePlayer(this.leader).isOnline())
            Bukkit.getPlayer(this.leader).sendMessage(ChatColor.YELLOW + "You are now the leader of your sector!");
    }

    // Sector info display.
    public void showInfo(final Player p) {
        p.sendMessage(ChatColor.YELLOW + "----==== Sector Info: " + this.color + this.name + ChatColor.YELLOW + " ====----");
        p.sendMessage(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + this.kills);
        p.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + this.description);
        if (ConfigManager.ENABLE_RAIDING)
            p.sendMessage(ChatColor.YELLOW + "DTR: " + ChatColor.WHITE + this.dtr + (this.dtr <= 0 ? ChatColor.RED + "| RAIDABLE" : ""));
        p.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + (Bukkit.getOfflinePlayer(this.leader).isOnline() ? Bukkit.getPlayer(this.leader).getName() : Bukkit.getOfflinePlayer(this.leader).getName()));
        p.sendMessage(ChatColor.YELLOW + "Members:" + ChatColor.WHITE);
        for (UUID id : this.members) {
            String name;
            if (Bukkit.getOfflinePlayer(id).isOnline()) {
                name = ChatColor.GREEN + Bukkit.getPlayer(id).getName();
            } else {
                name = ChatColor.RED + Bukkit.getOfflinePlayer(id).getName();
            }
            p.sendMessage(name);
        }
        if (!ConfigManager.SHOW_COORDS_IN_INFO) return;
        if (this.hasClaim()) {
            p.sendMessage(ChatColor.GREEN + "Claim start: " + this.claim.start() +
                    "\n" + "Claim end: " + this.claim.end());
            if (this.claim.getBounds().contains(p.getLocation().toVector())) {
                ClaimUtilities.showGlowingBounds(this.claim.getEdgeLocations(), p, plugin, ServiceManager.getPlayerEntityService());
            }
        } else {
            p.sendMessage(ChatColor.YELLOW + "This sector does not have a claim.");
        }
    }

    //Disband sector and delete all SPlayers
    public void disband() {
        plugin.getData().removeSPlayer(Bukkit.getPlayer(this.leader));
        for (UUID id : this.getMembers()) {
            plugin.getData().removeSPlayer(Bukkit.getPlayer(id));
        }
        plugin.getData().removeSector(this);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Sector " + ChatColor.WHITE + this.name + ChatColor.YELLOW + " has been disbanded!");
    }

    //Remove player from sector, whether they left or were kicked. Returns true if player was in the sector, false if player was not.
    public boolean removePlayer(final UUID p) {
        if (p.equals(this.leader)) {
            if (this.dtr <= 0) return false;
            this.disband();
            return true;
        }
        if (this.members.remove(p)) {
            plugin.getData().removeSPlayer(Bukkit.getPlayer(p)); //Remove from FPlayers if online
            this.broadcast(Bukkit.getOfflinePlayer(p).getName() + ChatColor.GOLD + " is no longer in the sector.");
            return true;
        }
        return false;
    }

    //Add player to sector, player must be online to join so we use Player
    public void addPlayer(final Player p) {
        this.members.add(p.getUniqueId());
        plugin.getData().addSPlayer(p, this); //Add player to SPlayers so interactions work.
        this.broadcast(ChatColor.YELLOW + p.getName() + " has joined the sector!");
    }


    //Serialization
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        ArrayList<String> membersStr = new ArrayList<>();
        for (UUID i : this.members) {
            membersStr.add(i.toString());
        }
        map.put("name", this.name);
        map.put("color", Character.toString(this.color.getChar()));
        map.put("leader", this.leader.toString());
        map.put("members", membersStr);
        map.put("description", this.description);
        map.put("dtr", this.dtr);
        map.put("home", this.home);
        map.put("kills", this.kills);
        if (this.claim != null) map.put("claim", this.claim.serialize());
        else map.put("claim", null);
        return map;
    }

    public static Sector deserialize(Map<String, Object> map, Sectors data) {
        return new Sector(map, data);
    }
}
