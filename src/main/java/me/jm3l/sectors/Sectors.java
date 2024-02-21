package me.jm3l.sectors;

import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.FileUtils.SectorsFile;
import me.jm3l.sectors.command.SCommand;
import me.jm3l.sectors.events.Events;
import me.jm3l.sectors.objects.Claim;
import me.jm3l.sectors.objects.Sector;
import me.jm3l.sectors.utilities.ClaimWand;
import me.jm3l.sectors.utilities.PlayerData;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Sectors extends JavaPlugin {

    private ClaimWand claimWand;
    public ClaimWand getClaimWand(){ return claimWand; }
    public ItemStack getWand(){
        return claimWand.getWand();
    }

    private PlayerData playerData;
    public PlayerData getData(){
        return this.playerData;
    }

    private SectorsFile sectorsFile;
    public SectorsFile getSectorsFile(){
        return sectorsFile;
    }

    @Override
    public void onEnable() {
        this.claimWand = new ClaimWand(this);
        this.playerData = new PlayerData();
        getCommand("sectors").setExecutor(new SCommand(this));

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        ConfigManager.loadConfig(this.getConfig());
        sectorsFile = new SectorsFile(this);
        sectorsFile.loadSectors();
        ConfigurationSerialization.registerClass(Sector.class);
        ConfigurationSerialization.registerClass(Claim.class);
        getServer().getPluginManager().registerEvents(new Events(this), this);

        System.out.println("[ON] Sector Enabled");
    }

    @Override
    public void onDisable() {
        //sectorsFile.saveSectors();
        System.out.println("[OFF] Sector Disabled");
    }
}
