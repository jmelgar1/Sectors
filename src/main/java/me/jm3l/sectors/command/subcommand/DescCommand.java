package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.objects.Sector;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class DescCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.desc";
    }

    @Override
    public String getDescription() {
        return "Set your sector description";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(p);
        if (!s.getLeader().equals(p.getUniqueId())) {
            p.sendMessage(ConfigManager.MUST_BE_LEADER);
            return;
        }
        if (args.length == 0) {
            p.sendMessage(ConfigManager.MISSING_ARGUMENT);
            return;
        }
        String desc = String.join(" ", args);
        if (desc.toCharArray().length <= ConfigManager.MAX_DESCRIPTION_LENGTH) {
            s.setDescription(String.join(" ", args));
            p.sendMessage(ConfigManager.SUCCESS);
        } else {
            p.sendMessage(Component.text("Description exceeds maximum length."));
        }
    }
}
