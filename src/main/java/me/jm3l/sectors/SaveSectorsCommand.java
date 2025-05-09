package me.jm3l.sectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SaveSectorsCommand implements CommandExecutor {

    private final Sectors plugin;

    public SaveSectorsCommand(Sectors plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.hasPermission("sec.admin")){
            plugin.getSectorsFile().saveSectors();
        }
        return true;
    }
}