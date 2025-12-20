package me.jm3l.sectors.membership.commands;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.core.command.SubCommand;
import me.jm3l.sectors.sector.exceptions.NotInSector;
import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.sector.Sector;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SetleaderCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.setleader";
    }

    @Override
    public String getDescription() {
        return "Transfer sector leadership to another player.";
    }

    @Override
    public void perform(Player player, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(player);
        if (!s.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ConfigManager.MUST_BE_LEADER);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ConfigManager.PLAYER_NOT_FOUND);
            return;
        }
        if (!s.getMembers().contains(target.getUniqueId())) {
            player.sendMessage(Component.text("Player is not in your sector"));
            return;
        }
        s.setLeader(target.getUniqueId());
    }
}
