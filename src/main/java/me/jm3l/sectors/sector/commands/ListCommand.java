package me.jm3l.sectors.sector.commands;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.core.command.SubCommand;
import me.jm3l.sectors.sector.Sector;
import me.jm3l.sectors.sector.exceptions.NotInSector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class ListCommand implements SubCommand {
    private static final int ENTRIES_PER_PAGE = 10;

    @Override
    public String getPermission() {
        return "sec.player.list";
    }

    @Override
    public String getDescription() {
        return "List all sectors by member count";
    }

    @Override
    public void perform(Player player, String[] args, Sectors plugin) throws NotInSector {
        ArrayList<Sector> sectors = plugin.getData().getSectors();

        if (sectors.isEmpty()) {
            player.sendMessage(Component.text("No sectors found.").color(TextColor.color(0xE57373)));
            return;
        }

        // Parse page number from args (default to 1)
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid page number. Usage: /s list [page]").color(TextColor.color(0xE57373)));
                return;
            }
        }

        // Sort sectors by total member count (descending)
        sectors.sort(Comparator.comparingInt((Sector s) -> {
            return s.getMembers().size() + 1; // +1 for the leader
        }).reversed());

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) sectors.size() / ENTRIES_PER_PAGE);
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, sectors.size());

        // Header with page number
        player.sendMessage(Component.text("┌──────[ Sector List - Page " + page + "/" + totalPages + " ]──────◓")
            .color(TextColor.color(0xEF9A9A)));

        // Display sectors for current page
        for (int i = startIndex; i < endIndex; i++) {
            Sector sector = sectors.get(i);
            int onlineCount = 0;
            int totalCount = sector.getMembers().size() + 1; // +1 for the leader

            // Count online members
            if (Bukkit.getPlayer(sector.getLeader()) != null) {
                onlineCount++;
            }
            for (UUID memberId : sector.getMembers()) {
                if (Bukkit.getPlayer(memberId) != null) {
                    onlineCount++;
                }
            }

            int offlineCount = totalCount - onlineCount;

            // Format: "│ 1. Sector name (online/offline)"
            Component sectorName = Component.text(sector.getName())
                .color(TextColor.color(0xEEEEEE))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/s info " + sector.getName()))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                    Component.text("Click to view sector info").color(TextColor.color(0x9E9E9E))
                ));

            Component message = Component.text("│ ").color(TextColor.color(0xEF9A9A))
                .append(Component.text((i + 1) + ". ").color(TextColor.color(0xFFF9C4)))
                .append(sectorName)
                .append(Component.text(" (").color(TextColor.color(0x9E9E9E)))
                .append(Component.text(String.valueOf(onlineCount)).color(TextColor.color(0x66BB6A)))
                .append(Component.text("/").color(TextColor.color(0x9E9E9E)))
                .append(Component.text(String.valueOf(offlineCount)).color(TextColor.color(0xEF5350)))
                .append(Component.text(")").color(TextColor.color(0x9E9E9E)));

            player.sendMessage(message);
        }

        // Footer with navigation buttons
        Component footer = Component.text("└──").color(TextColor.color(0xEF9A9A));

        // Add back button if not on first page
        if (page > 1) {
            Component backButton = Component.text("[ ← Back ]")
                .color(TextColor.color(0x64B5F6))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/s list " + (page - 1)))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                    Component.text("Go to page " + (page - 1)).color(TextColor.color(0x9E9E9E))
                ));
            footer = footer.append(backButton).append(Component.text(" ").color(TextColor.color(0xEF9A9A)));
        }

        // Add next button if not on last page
        if (page < totalPages) {
            Component nextButton = Component.text("[ Next → ]")
                .color(TextColor.color(0x64B5F6))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/s list " + (page + 1)))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                    Component.text("Go to page " + (page + 1)).color(TextColor.color(0x9E9E9E))
                ));
            footer = footer.append(nextButton).append(Component.text(" ").color(TextColor.color(0xEF9A9A)));
        }

        footer = footer.append(Component.text("──◒").color(TextColor.color(0xEF9A9A)));
        player.sendMessage(footer);
    }
}
