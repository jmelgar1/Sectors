package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KickCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.kick";
    }

    @Override
    public String getDescription() {
        return "Kick a player from the sector.";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(p);
        if (args.length == 0) {
            p.sendMessage(ConfigManager.MISSING_ARGUMENT);
            return;
        }
        UUID target = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
        if (target.equals(p.getUniqueId())) {
            p.sendMessage(ConfigManager.CANT_KICK_YOURSELF);
            return;
        }
        if (s.removePlayer(target, true)) {
            p.sendMessage(ConfigManager.SUCCESS);
        } else {
            p.sendMessage(ConfigManager.PLAYER_NOT_FOUND);
        }
    }
}
