package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.entity.Player;

public class UnclaimCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.unclaim";
    }

    @Override
    public String getDescription() {
        return "Undo your claim";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector sector = plugin.getData().getSectorOrError(p);
        if (!sector.getLeader().equals(p.getUniqueId())) {
            p.sendMessage(ConfigManager.MUST_BE_LEADER);
            return;
        }
        if (sector.hasClaim()) {
            sector.setClaim(null);
            p.sendMessage(ConfigManager.SUCCESS);
            sector.broadcast(ConfigManager.SECTOR_UNCLAIM);
        } else {
            p.sendMessage(ConfigManager.SECTOR_NO_CLAIM);
        }
    }
}
