package Modules;

import mindustry.game.Team;

public class PlayerCache {
    Team team;
    long lastRespawn;

    public PlayerCache()
    {
        this.team = TeamsManager.spectatorTeam;
        this.lastRespawn = 0;
    }
}
