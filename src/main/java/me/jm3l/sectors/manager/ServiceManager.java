package me.jm3l.sectors.manager;

import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.service.MarkerService;
import me.jm3l.sectors.service.PlayerEntityService;

public class ServiceManager {
    private static final PlayerEntityService playerEntityService = new PlayerEntityService();
    private static MarkerService markerService;
    
    public static void initializeServices(Sectors plugin) {
        markerService = new MarkerService(plugin);
    }

    public static PlayerEntityService getPlayerEntityService() {
        return playerEntityService;
    }
    
    public static MarkerService getMarkerService() {
        return markerService;
    }
}
