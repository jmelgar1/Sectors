package me.jm3l.sectors.home.commands;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.core.command.SubCommand;
import me.jm3l.sectors.sector.exceptions.NotInSector;
import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.sector.Sector;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HomeCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "hcf.player.home";
    }

    @Override
    public String getDescription() {
        return "Go to your faction's home";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(p);
        p.sendMessage(ConfigManager.TELEPORT_PENDING);

        //create pending teleport
        int r = new BukkitRunnable() {
            @Override
            public void run() {
                s.tpHome(p);
                plugin.getData().getPendingTeleports().remove(p);
            }
        }.runTaskLater(plugin, ConfigManager.TELEPORT_DELAY).getTaskId();
        plugin.getData().getPendingTeleports().put(p, r);
    }
}
