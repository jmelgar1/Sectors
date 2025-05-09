package me.jm3l.sectors.command.wand.util;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.wand.ClaimWand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class ClaimToolInventoryUtilities {
    public static void fillHotbarWithWand(Player player, Map<UUID, ItemStack[]> savedHotbars, Sectors plugin) {
        ItemStack[] hotbar = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            hotbar[i] = player.getInventory().getItem(i);
        }
        savedHotbars.put(player.getUniqueId(), hotbar);

        ItemStack wand = new ClaimWand(plugin).getWand();
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, wand.clone());
        }
    }

    public static void restoreHotbar(Player player, Map<UUID, ItemStack[]> savedHotbars, Sectors plugin) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && plugin.getClaimWand().isWand(item)) {
                player.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
        }

        ItemStack[] savedItems = savedHotbars.remove(player.getUniqueId());
        if (savedItems != null) {
            for (int i = 0; i < 9; i++) {
                player.getInventory().setItem(i, savedItems[i]);
            }
        }
    }
}
