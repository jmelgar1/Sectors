package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.objects.Sector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class InviteCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.invite";
    }

    @Override
    public String getDescription() {
        return "Invite a player to the sector";
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
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || target.equals(p)) {
            p.sendMessage(ConfigManager.PLAYER_NOT_FOUND);
            return;
        }
        if (plugin.getData().isPlayerInSector(target)) {
            p.sendMessage(Component.text("Player is already in a sector"));
            return;
        }
        if (plugin.getData().hasInvitation(p)) {
            p.sendMessage(Component.text("Player already has an invitation. Wait for it to expire."));
            return;
        }
        plugin.getData().addInvitation(target, s);
        p.sendMessage(ConfigManager.SUCCESS);

        TextComponent inviteHeader =  Component.text("┌──────[ ✉ SECTOR INVITE ✉ ]───────").color(TextColor.color(0x42A5F5));

        TextComponent component1 = Component.text("│ ").color(TextColor.color(0x42A5F5)).append(Component.text("You have been invited to join ").color(TextColor.color(0x26A69A))
            .append(Component.text(s.getName()).color(TextColor.color(0x0277BD))));

        TextComponent component2 = Component.text("│ ").color(TextColor.color(0x42A5F5)).append(Component.text("Type ").color(TextColor.color(0x689F38))
            .append(Component.text("/s accept " + s.getName()).color(TextColor.color(0x4CAF50))
            .append(Component.text(" to accept the invite!").color(TextColor.color(0x689F38)))));

        TextComponent component3 = Component.text("│ ").color(TextColor.color(0x42A5F5)).append(Component.text("Type ").color(TextColor.color(0xE65100))
            .append(Component.text("/s decline " +s.getName()).color(TextColor.color(0xD84315))
                .append(Component.text(" to decline the invite!").color(TextColor.color(0xE65100)))));

        TextComponent inviteFooter =  Component.text("└──────────────────────────").color(TextColor.color(0x42A5F5));

        target.sendMessage(inviteHeader);
        target.sendMessage(component1);
        target.sendMessage(component2);
        target.sendMessage(component3);
        target.sendMessage(inviteFooter);

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getData().expireInvitation(target);
            }
        }.runTaskLaterAsynchronously(plugin, 1200);
    }
}
