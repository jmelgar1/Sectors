package me.jm3l.sectors.manager;

import me.jm3l.sectors.service.PlayerEntityService;

public class ServiceManager {
    private static final PlayerEntityService playerEntityService = new PlayerEntityService();

    public static PlayerEntityService getPlayerEntityService() {
        return playerEntityService;
    }
}
