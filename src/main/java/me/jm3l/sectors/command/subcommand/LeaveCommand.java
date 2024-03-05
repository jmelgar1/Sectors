package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.objects.Sector;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.player.leave";
    }

    @Override
    public String getDescription() {
        return "Leave your sector";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(p);
        if (!s.removePlayer(p.getUniqueId(), false)) {
            p.sendMessage(Component.text("Failed to leave the sector. (Are you the leader or is the sector raidable?)"));
        }
    }
}
