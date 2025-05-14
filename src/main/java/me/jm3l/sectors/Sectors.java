package me.jm3l.sectors;

import me.jm3l.sectors.manager.ConfigManager;
import me.jm3l.sectors.manager.ServiceManager;
import me.jm3l.sectors.FileUtils.SectorsFile;
import me.jm3l.sectors.command.SCommand;
import me.jm3l.sectors.command.wand.ClaimWand;
import me.jm3l.sectors.command.wand.events.ClaimToolEvents;
import me.jm3l.sectors.events.Events;
import me.jm3l.sectors.objects.Sector;
import me.jm3l.sectors.objects.claim.Claim;
import me.jm3l.sectors.runnables.ClaimParticleTask;
import me.jm3l.sectors.utilities.PlayerData;
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

    private SectorsFile sectorsFile;
    public SectorsFile getSectorsFile(){
        return sectorsFile;
    }

    private Events events;
    public Events getEvents(){
        return this.events;
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
        
        // Initialize services
        ServiceManager.initializeServices(this);
        
        this.claimToolEvents = new ClaimToolEvents(this);
        this.claimParticleTask = new ClaimParticleTask(this);
        this.events = new Events(this);
        this.claimWand = new ClaimWand(this);
        this.playerData = new PlayerData();
        getCommand("sectors").setExecutor(new SCommand(this));
        getCommand("savesectors").setExecutor(new SaveSectorsCommand(this));
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        ConfigManager.loadConfig(this.getConfig());
        sectorsFile = new SectorsFile(this);
        sectorsFile.loadSectors();
        ConfigurationSerialization.registerClass(Sector.class);
        ConfigurationSerialization.registerClass(Claim.class);
        getServer().getPluginManager().registerEvents(events, this);
        getServer().getPluginManager().registerEvents(claimToolEvents, this);
        claimParticleTask.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable() {
        sectorsFile.saveSectors();
        PacketEvents.getAPI().terminate();
    }
}
