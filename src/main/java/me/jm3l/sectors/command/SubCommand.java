package me.jm3l.sectors.command;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.exceptions.NotInSector;
import org.bukkit.entity.Player;

public interface SubCommand {
    abstract String getDescription();
    abstract String getPermission();
    abstract void perform(Player p, String[] args, Sectors plugin) throws NotInSector;
}
