package Modules;

import java.util.ArrayList;
import java.util.Random;

import arc.Events;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

public class TeamsManager {
    static Team spectatorTeam = Team.get(255);

    public static void init()
    {
        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            PlayerCache playerCache = CacheManager.getPlayerCache(player.uuid());

            player.team(playerCache.team);
        });

        arc.util.Timer.schedule(() -> {
            Groups.player.forEach(player -> {
                if (!player.team().active()) player.team(spectatorTeam);
            });
        }, 0, 0.1f);

        arc.util.Timer.schedule(() -> {
            for (Team team : Team.all)
            {
                if (team.id < 10) continue;
                if (team.data().cores.size == 0 && !team.name.startsWith("team"))
                {
                    killTeam(team);
                    team.name = "team#" + Integer.toString(team.id);
                }
            }
        }, 0, 0.1f);
    }
    
    public static Team getNewTeam()
    {
        ArrayList<Team> emptyTeams = new ArrayList<>();

        for (Team team : Team.all)
        {
            if (team.id < 10) continue;
            if (!team.active()) emptyTeams.add(team);
        }

        return emptyTeams.get(new Random().nextInt(emptyTeams.size()));
    }

    public static void killTeam(Team team)
    {
        team.data().destroyToDerelict();
        team.data().units.forEach(unit -> {
           unit.kill();
        });
        UIManager.chatMessage("chat_notification.team_destroyed", team.coloredName());
    }

    public static void cancelTeam(Team team)
    {
        for (CoreBuild core : team.cores())
        {
            core.tile().setNet(Blocks.air);
        }
        team.data().units.forEach(unit -> {
           Call.unitDespawn(unit);
        });
    }
}
