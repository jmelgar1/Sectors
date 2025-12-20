package me.jm3l.sectors;

import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.sector.storage.SectorFile;
import me.jm3l.sectors.core.command.SCommand;
import me.jm3l.sectors.core.command.SaveSectorsCommand;
import me.jm3l.sectors.claim.wand.ClaimWand;
import me.jm3l.sectors.claim.wand.ClaimToolEvents;
import me.jm3l.sectors.claim.events.ClaimProtectionEvents;
import me.jm3l.sectors.claim.events.ClaimToolInteractionEvents;
import me.jm3l.sectors.events.EventDataManager;
import me.jm3l.sectors.events.PlayerSessionEvents;
import me.jm3l.sectors.sector.events.SectorBoundaryEvents;
import me.jm3l.sectors.sector.Sector;
import me.jm3l.sectors.claim.Claim;
import me.jm3l.sectors.claim.visual.ClaimParticleTask;
import me.jm3l.sectors.shared.data.PlayerData;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.retrooper.packetevents.PacketEvents;

import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

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

    private SectorFile sectorsFile;
    public SectorFile getSectorsFile(){
        return sectorsFile;
    }

    private EventDataManager eventDataManager;
    public EventDataManager getEventDataManager(){
        return this.eventDataManager;
    }

    private ClaimToolEvents claimToolEvents;
    public ClaimToolEvents getClaimToolEvents(){
        return this.claimToolEvents;
    }

    private ClaimParticleTask claimParticleTask;
    public ClaimParticleTask getClaimParticleTask(){
        return this.claimParticleTask;
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        //On Bukkit, calling this here is essential, hence the name "load"
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        // Initialize data managers and core systems
        this.eventDataManager = new EventDataManager();
        this.claimToolEvents = new ClaimToolEvents(this);
        this.claimParticleTask = new ClaimParticleTask(this);
        this.claimWand = new ClaimWand(this);
        this.playerData = new PlayerData();

        // Register commands
        getCommand("sectors").setExecutor(new SCommand(this));
        getCommand("savesectors").setExecutor(new SaveSectorsCommand(this));

        // Load configuration
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        ConfigManager.loadConfig(this.getConfig());

        // Load sector data
        sectorsFile = new SectorFile(this);
        sectorsFile.loadSectors();
        ConfigurationSerialization.registerClass(Sector.class);
        ConfigurationSerialization.registerClass(Claim.class);

        // Register all event listeners
        getServer().getPluginManager().registerEvents(new ClaimProtectionEvents(this), this);
        getServer().getPluginManager().registerEvents(new ClaimToolInteractionEvents(this, eventDataManager), this);
        getServer().getPluginManager().registerEvents(new PlayerSessionEvents(this, eventDataManager), this);
        getServer().getPluginManager().registerEvents(new SectorBoundaryEvents(this, eventDataManager), this);
        getServer().getPluginManager().registerEvents(claimToolEvents, this);

        // Start tasks
        claimParticleTask.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable() {
        sectorsFile.saveSectors();
        PacketEvents.getAPI().terminate();
    }
}
