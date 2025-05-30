package Modules;

import java.util.ArrayList;
import java.util.Random;

import arc.Events;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class TeamsManager {
    static Team spectatorTeam = Team.get(255);

    public static void init()
    {
        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            PlayerCache playerCache = CacheManager.getPlayerCache(player.uuid());

            player.team(playerCache.team);
        });
    }
    
    public static Team getNewTeam()
    {
        ArrayList<Team> emptyTeams = new ArrayList<>();

        for (Team team : Team.all)
        {
            if (!team.active()) emptyTeams.add(team);
        }

        return emptyTeams.get(new Random().nextInt(emptyTeams.size()));
    }

    public static void killTeam(Team team)
    {
        //team.data().destroyToDerelict();
        //team.data().units.forEach(unit -> {
        //    unit.kill();
        //});
        for (PlayerCache cache : CacheManager.playerCache.values())
        {
            if (cache.team == team) cache.team = TeamsManager.spectatorTeam;
            Groups.player.forEach(player -> {
                player.clearUnit();
                player.team(TeamsManager.spectatorTeam);
            });
        }
    }
}
