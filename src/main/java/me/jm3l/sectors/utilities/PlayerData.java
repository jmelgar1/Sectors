package me.jm3l.sectors.utilities;

import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.objects.Sector;
import me.jm3l.sectors.objects.claim.ClaimSelection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
    private final HashMap<Player, Integer> pendingTeleports;
    public HashMap<Player, Integer> getPendingTeleports(){
        return pendingTeleports;
    }

    /*
   Loaded sectors
    */
    private final ArrayList<Sector> sectors;
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
        return invites.containsKey(p);
    }
    public void addInvitation(Player p, Sector s) {invites.put(p, s);}
    public void expireInvitation(Player p) {
        p.sendMessage(Component.text("Sector invite expired!").color(TextColor.color(0xF44336)));
        invites.remove(p);
    }

    public boolean handleInvite(Player p, Sector s, boolean accept) {
        if (invites.containsKey(p)) {
            Sector sector = invites.get(p);
            if (sector.equals(s)) {
                if (accept) {
                    if (sector.getMembers().size() >= ConfigManager.MAX_MEMBERS) {return false;}
                    sector.addPlayer(p);
                }
                invites.remove(p);
                return true;
            } else {
                p.sendMessage(Component.text("You do not have an invitation from this sector!"));
            }
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
            return s.pos1() != null && s.pos2() != null;
        }
        return false;
    }
}
