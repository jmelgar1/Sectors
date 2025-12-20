package me.jm3l.sectors.admin.commands;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.core.command.SubCommand;
import me.jm3l.sectors.core.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ReloadCommand implements SubCommand {
    @Override
    public String getPermission() {
        return "sec.admin";
    }

    @Override
    public String getDescription() {
        return "Reload config.yml";
    }

    @Override
    public void perform(Player player, String[] args, Sectors plugin) {
        plugin.reloadConfig();
        ConfigManager.loadConfig(plugin.getConfig());
        player.sendMessage(Component.text("Reloading Sectors config.yml"));
    }
}
