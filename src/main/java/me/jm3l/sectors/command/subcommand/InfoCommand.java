package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InfoCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.info";
    }

    @Override
    public String getDescription() {
        return "View a sector's info";
    }

    @Override
    public void perform(Player player, String[] args, Sectors plugin) throws NotInSector {
        // case when /s info is done with no arguments
        if (args.length == 0) {
            Sector s = plugin.getData().getSectorOrError(player);
            s.showInfo(player);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            if (plugin.getData().isPlayerInSector(target)) {
                plugin.getData().getSector(target).showInfo(player);
                return;
            }
        }
        Sector targetS = plugin.getData().getSector(args[0]);
        if (targetS != null) {
            targetS.showInfo(player);
            return;
        }
        player.sendMessage(ConfigManager.NOT_A_PLAYER_OR_SECTOR);
    }
}
