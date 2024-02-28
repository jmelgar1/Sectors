package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.ChatColor;
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
        Sector s = plugin.getData().getSectorOrError(p);
        if (!s.getLeader().equals(p.getUniqueId())) {
            p.sendMessage(ConfigManager.MUST_BE_LEADER);
            return;
        }
        if (s.hasClaim()) {
            s.setClaim(null);
            p.sendMessage(ConfigManager.SUCCESS);
            s.broadcast(ChatColor.RED + "Your sector no longer has a claim!");
        } else {
            p.sendMessage(ChatColor.RED + "You do not have a claim");
        }
    }
}
