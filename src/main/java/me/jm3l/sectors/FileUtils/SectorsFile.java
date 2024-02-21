package me.jm3l.sectors.FileUtils;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.objects.Sector;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SectorsFile {
    private Sectors plugin;

    public SectorsFile(final Sectors plugin){
        this.plugin = plugin;
    }

    private File sectorsFile;
    private FileConfiguration sectorsData;
    public void loadSectors(){
        sectorsFile = new File(plugin.getDataFolder(),"sectors.yml");
        if(!sectorsFile.exists()){
            sectorsFile.getParentFile().mkdir();
            try{
                sectorsFile.createNewFile();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        sectorsData = YamlConfiguration.loadConfiguration(sectorsFile);
        if(!sectorsData.getKeys(false).isEmpty()){
            for(String key : sectorsData.getKeys(false)){
                Sector loadedSec = Sector.deserialize(sectorsData.getConfigurationSection(key).getValues(false), plugin);
                plugin.getData().addSector(loadedSec);
            }
        }
    }

    public void saveSectors(){

        //Keep track of sectors saved so we can delete any sectors from the file that weren't saved (meaning they were deleted).
        List<String> savedSecs = new ArrayList<>();

        for(Sector s : plugin.getData().getSectors()){
            sectorsData.set(s.getName(), s.serialize());
            savedSecs.add(s.getName());
        }
        try {
            sectorsData.save(sectorsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Delete any disbanded sectors
        for(String key : sectorsData.getKeys(false)){
            if(!savedSecs.contains(key)){
                sectorsData.set(key, null);
            }
        }

        try {
            sectorsData.save(sectorsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
