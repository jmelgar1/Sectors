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

public class DemoteCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.demote";
    }

    @Override
    public String getDescription() {
        return "Demote an officer to regular member";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(p);

        // Only the leader can demote
        if (!s.getLeader().equals(p.getUniqueId())) {
            p.sendMessage(ConfigManager.MUST_BE_LEADER);
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
            p.sendMessage(Component.text("You are the leader, not an officer.").color(TextColor.color(0xE57373)));
            return;
        }

        // Check if target is in the sector
        if (!s.hasMember(target)) {
            p.sendMessage(Component.text("That player is not in your sector.").color(TextColor.color(0xE57373)));
            return;
        }

        // Check if target is an officer
        if (!s.isOfficer(target)) {
            p.sendMessage(Component.text(target.getName() + " is not an officer.").color(TextColor.color(0xE57373)));
            return;
        }

        // Demote the player
        s.removeOfficer(target.getUniqueId());
        s.getMembers().add(target.getUniqueId());

        p.sendMessage(Component.text("You demoted " + target.getName() + " to regular member.").color(TextColor.color(0x4CAF50)));
        target.sendMessage(Component.text("You have been demoted to regular member in " + s.getName() + ".").color(TextColor.color(0xFFA726)));
        s.broadcast(Component.text(target.getName() + " has been demoted to regular member.").color(TextColor.color(0x9E9E9E)));
    }
}
