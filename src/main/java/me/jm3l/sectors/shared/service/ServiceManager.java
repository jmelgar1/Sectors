package me.jm3l.sectors.shared.service;

import me.jm3l.sectors.shared.service.PlayerEntityService;

public class ServiceManager {
    private static final PlayerEntityService playerEntityService = new PlayerEntityService();

    public static PlayerEntityService getPlayerEntityService() {
        return playerEntityService;
    }
}
