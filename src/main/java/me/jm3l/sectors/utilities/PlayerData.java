package me.jm3l.sectors.utilities;

import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.objects.Sector;
import me.jm3l.sectors.objects.claim.ClaimSelection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
    public PlayerData(){
        sectors = new ArrayList<>();
        sPlayers = new HashMap<>();
        invites = new HashMap<>();
        selections = new HashMap<>();
        pendingTeleports = new HashMap<>();

        // In case of server /reload
        for(Player p : Bukkit.getOnlinePlayers()){
            for (Sector s : getSectors()) {
                if (s.hasMember(p)) addSPlayer(p, s); break;
            }
        }
    }
    /*
   Pending teleports
    */
    private HashMap<Player, Integer> pendingTeleports;
    public HashMap<Player, Integer> getPendingTeleports(){
        return pendingTeleports;
    }

    /*
   Loaded sectors
    */
    private ArrayList<Sector> sectors;
    public ArrayList<Sector> getSectors(){
        return sectors;
    }

    public void addSector(Sector s){
        sectors.add(s);
    }

    public void removeSector(Sector s){
        sectors.remove(s);
        while(invites.values().remove(s));
    }

    public Sector getSector(String name){
        for(Sector s : sectors){
            if(s.getName().equalsIgnoreCase(name)) return s;
        }
        for(Player p : sPlayers.keySet()){
            if(p.getName().equalsIgnoreCase(name)) return getSector(p);
        }
        return null;
    }

    public Sector getSector(Player p){
        return sPlayers.get(p);
    }

    public Sector getSector(OfflinePlayer p){
        if(p.isOnline()) return getSector((Player) p);
        UUID id = p.getUniqueId();
        for(Sector s : getSectors()){
            if(s.getMembers().contains(id) || s.getLeader().equals(id)) return s;
        }
        return null;
    }
    public Sector getSectorOrError(Player p) throws NotInSector {
        if(!sPlayers.containsKey(p)){
            p.sendMessage(ConfigManager.NOT_IN_SECTOR);
            throw new NotInSector();
        }
        return sPlayers.get(p);
    }
    public Sector getSectorRawName(String sectorName){
        for(Sector s : sectors){
            if(s.getName().equalsIgnoreCase(sectorName)) return s;
        }
        return null;
    }
    /*
       Online players & sector association
        */
    private HashMap<Player, Sector> sPlayers;
    public void addSPlayer(Player p, Sector s){
        sPlayers.put(p, s);
    }
    public void removeSPlayer(Player p){
        sPlayers.remove(p);
    }
    public boolean isPlayerInSector(Player p){
        return sPlayers.containsKey(p);
    }

    /*
    Online players with active invitations
     */
    private HashMap<Player, Sector> invites;
    public boolean hasInvitation(Player p){
        if(invites.containsKey(p)) return true;
        return false;
    }
    public void addInvitation(Player p, Sector s) {invites.put(p, s);}
    public void expireInvitation(Player p) {invites.remove(p);}
    public boolean acceptInvite(Player p){
        if(invites.containsKey(p)){
            if(invites.get(p).getMembers().size() >= ConfigManager.MAX_MEMBERS) return false;
            invites.get(p).addPlayer(p);
            return true;
        }
        return false;
    }

    /*
    Claim Selections
     */
    private HashMap<Player, ClaimSelection> selections;
    public HashMap<Player, ClaimSelection> getSelections(){
        return selections;
    }
    public ClaimSelection getSelection(Player p){
        return selections.get(p);
    }
    public boolean hasSelection(Player p){
        if(selections.containsKey(p)){
            ClaimSelection s = selections.get(p);
            if(s.pos1() == null || s.pos2() == null) return false;
            return true;
        }
        return false;
    }
}
