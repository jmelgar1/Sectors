package me.jm3l.sectors.membership.commands;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.core.command.SubCommand;
import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.sector.Sector;
import me.jm3l.sectors.sector.exceptions.NotInSector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KickCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.kick";
    }

    @Override
    public String getDescription() {
        return "Kick a player from the sector";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(p);

        // Only officers and leaders can kick
        if (!s.isOfficerOrLeader(p)) {
            p.sendMessage(Component.text("You must be an officer or leader to kick players.").color(TextColor.color(0xE57373)));
            return;
        }

        if (args.length == 0) {
            p.sendMessage(ConfigManager.MISSING_ARGUMENT);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(ConfigManager.PLAYER_NOT_FOUND);
            return;
        }

        if (target.equals(p)) {
            p.sendMessage(Component.text("You cannot kick yourself. Use /s leave instead.").color(TextColor.color(0xE57373)));
            return;
        }

        // Check if target is in the sector
        if (!s.hasMember(target)) {
            p.sendMessage(Component.text("That player is not in your sector.").color(TextColor.color(0xE57373)));
            return;
        }

        // Check if target is the leader
        if (s.getLeader().equals(target.getUniqueId())) {
            p.sendMessage(Component.text("You cannot kick the leader.").color(TextColor.color(0xE57373)));
            return;
        }

        // Officers cannot kick other officers, only leaders can
        if (s.isOfficer(target) && !s.getLeader().equals(p.getUniqueId())) {
            p.sendMessage(Component.text("Only the leader can kick officers.").color(TextColor.color(0xE57373)));
            return;
        }

        // Kick the player
        s.removePlayer(target.getUniqueId(), true);
        p.sendMessage(Component.text("You kicked " + target.getName() + " from the sector.").color(TextColor.color(0x4CAF50)));
        target.sendMessage(Component.text("You have been kicked from " + s.getName() + ".").color(TextColor.color(0xE57373)));
    }
}
