package me.jm3l.sectors.utilities;

import me.jm3l.sectors.Sectors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class ClaimWand {
    private final ItemStack wand;
    private static NamespacedKey key;
    public ItemStack getWand(){
        return this.wand;
    }

    public ClaimWand(final Sectors plugin) {
        key = new NamespacedKey(plugin, "wand");
        wand = new ItemStack(Material.CARROT_ON_A_STICK, 1);
        final ItemMeta meta = wand.getItemMeta();
        assert meta != null;
        meta.setLore(Arrays.asList("Left Click - Position 1", "Right Click - Position 2"));
        meta.setDisplayName("Claiming Wand");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
    }

    public boolean isWand(ItemStack s){
        if(s == null) return false;
        if(!s.getType().equals(Material.CARROT_ON_A_STICK)) return false;
        ItemMeta meta = s.getItemMeta();
        assert meta != null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(key, PersistentDataType.BYTE);
    }
}
