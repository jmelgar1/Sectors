package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.entity.Player;

public class DeclineCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.decline";
    }

    @Override
    public String getDescription() {
        return "Decline an invitation to a sector";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) {
        if (args.length == 0) {
            p.sendMessage(ConfigManager.MISSING_ARGUMENT);
            return;
        }

        Sector target = plugin.getData().getSector(args[0]);
        if (target != null) {
            if(plugin.getData().handleInvite(p, target, false)){
                p.sendMessage(ConfigManager.SUCCESS);
            } else {
                p.sendMessage(ConfigManager.NO_INVITE);
            }
        }
    }
}
