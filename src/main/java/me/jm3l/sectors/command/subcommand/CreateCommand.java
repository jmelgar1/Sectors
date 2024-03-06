package me.jm3l.sectors.command.subcommand;

import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.objects.Sector;
import me.jm3l.sectors.utilities.PlayerData;
import me.jm3l.sectors.utilities.text.MessageUtilities;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class CreateCommand implements SubCommand {

    @Override
    public String getDescription() {
        return "Create a sector";
    }

    @Override
    public String getPermission() {
        return "sec.player.create";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) {
        PlayerData data = plugin.getData();
        if (data.isPlayerInSector(p)) {
            p.sendMessage(ConfigManager.ALREADY_IN_SECTOR);
            return;
        }
        if (args.length == 0) {
            p.sendMessage(ConfigManager.MISSING_ARGUMENT);
            return;
        }
        if (args[0].length() > ConfigManager.MAX_SECTOR_NAME) {
            p.sendMessage(ConfigManager.NAME_TOO_LONG);
            return;
        }
        if (data.getSectorRawName(args[0]) != null) {
            p.sendMessage(ConfigManager.NAME_TAKEN);
            return;
        }
        Pattern pattern = Pattern.compile("[^a-zA-Z]");
        boolean hasBadChar = pattern.matcher(args[0]).find();
        if (!hasBadChar) {
            Sector newSec = new Sector(args[0], p, plugin);
            data.addSector(newSec);
            Bukkit.broadcast(MessageUtilities.createSectorIcon(
                TextColor.color(255,111,0))
                .append(LegacyComponentSerializer.legacyAmpersand()
                .deserialize((ConfigManager.SECTOR_FOUNDED
                .replaceAll("\\{sector}", args[0])))));
        } else {
            p.sendMessage(ConfigManager.INVALID_NAME);
        }
    }
}
