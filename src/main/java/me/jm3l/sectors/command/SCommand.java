package me.jm3l.sectors.command;

import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.subcommand.ClaimCommand;
import me.jm3l.sectors.command.subcommand.CreateCommand;
import me.jm3l.sectors.command.subcommand.InfoCommand;
import me.jm3l.sectors.command.subcommand.UnclaimCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SCommand implements CommandExecutor {

    private Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    private void sendHelp(Player commandSender) {
        for(Map.Entry<String, SubCommand> e : subCommands.entrySet()){
            SubCommand v = e.getValue();
            String arg = e.getKey();
            if(commandSender.hasPermission(v.getPermission())) commandSender.sendMessage("/s " + arg + " - " + ChatColor.YELLOW + v.getDescription());
            if(commandSender.isOp()) commandSender.sendMessage(ChatColor.GRAY + v.getPermission());
        }
    }

    private final Sectors plugin;
    public SCommand(final Sectors inst) {
        this.plugin = inst;
        subCommands.put("create", new CreateCommand());
        subCommands.put("claim", new ClaimCommand());
        subCommands.put("info", new InfoCommand());
        subCommands.put("unclaim", new UnclaimCommand());
        //subCommandMap.put("desc", new DescCommand());
        //subCommandMap.put("invite", new InviteCommand());
        //subCommandMap.put("accept", new AcceptCommand());
        //subCommandMap.put("kick", new KickCommand());
        //subCommandMap.put("setleader", new SetleaderCommand());
        //subCommandMap.put("leave", new LeaveCommand());
        //subCommandMap.put("who", info);
        //subCommandMap.put("setcolor", new SetColorCommand());
        //subCommandMap.put("list", new ListCommand());
        //subCommandMap.put("sethome", new SetHomeCommand());
        //subCommandMap.put("home", new HomeCommand());
        //subCommandMap.put("notify", new NotifyCommand());
        //subCommandMap.put("reload", new ReloadCommand());
        //subCommandMap.put("version", new VersionCommand());
        //subCommandMap.put("setdtr", new SetDTRCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (args.length == 0) {
            sendHelp(p);
            return true;
        }
        String argument = args[0].toLowerCase();
        String[] pass = Arrays.copyOfRange(args, 1, args.length);
        if (!subCommands.containsKey(argument)) {
            sendHelp(p);
            return true;
        }
        try {
            SubCommand subCmd = subCommands.get(argument);
            if (p.hasPermission(subCmd.getPermission())) {
                subCmd.perform(p, pass, plugin);
            } else {
                p.sendMessage(ConfigManager.NO_PERMISSION);
            }
        } catch (NotInSector ignored) {}
        return true;
    }
}
