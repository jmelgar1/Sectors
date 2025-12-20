package me.jm3l.sectors.home.commands;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.core.command.SubCommand;
import me.jm3l.sectors.sector.exceptions.NotInSector;
import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.sector.Sector;
import org.bukkit.entity.Player;

public class SethomeCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.sethome";
    }

    @Override
    public String getDescription() {
        return "Set your sector's home";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(p);
        if (!s.getLeader().equals(p.getUniqueId())) {
            p.sendMessage(ConfigManager.MUST_BE_LEADER);
            return;
        }
        s.setHome(p);
    }
}
