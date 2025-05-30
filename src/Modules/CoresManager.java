package Modules;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

public class CoresManager {
    static int respawnCooldown = 10000;
    public static void init()
    {
        Events.on(EventType.TapEvent.class, event -> {
            Player player = event.player;
            PlayerCache cache = CacheManager.getPlayerCache(player.uuid());
            if (player.team().active()) return;
            Tile tile = event.tile;

            Team newTeam = TeamsManager.getNewTeam();

            Object result = validPlaceCore(tile, newTeam);
            if (System.currentTimeMillis() - cache.lastRespawn < respawnCooldown)
            {
                UIManager.label(tile, "error.too_fast", player, (respawnCooldown - System.currentTimeMillis() + cache.lastRespawn) / 1000);
            }
            else if (result instanceof Tile)
            {
                UIManager.label(tile, "error.not_valid_tile", player);
            }
            else if (result instanceof CoreBuild)
            {
                UIManager.label(tile, "error.core_build_radius_cross", player);
                mindustry.gen.Call.effect(player.con(), Fx.overdriveWave, ((CoreBuild) result).x, ((CoreBuild) result).y, Vars.state.rules.enemyCoreBuildRadius * 2, Color.valueOf("#ff3030ff"));
            }
            else
            {
                cache.team = newTeam;
                cache.lastRespawn = System.currentTimeMillis();
                player.team(newTeam);

                tile.setNet(Blocks.coreNucleus, player.team(), 0);
                sync_unitCap(newTeam);
                mindustry.gen.Call.effect(Fx.overdriveWave, tile.worldx(), tile.worldy(), Vars.state.rules.enemyCoreBuildRadius, newTeam.color);
                MenuManager.callTeamNameMenu(player);
            }
        });

        Events.on(EventType.BlockDestroyEvent.class, event -> {
            if (event.tile.block() instanceof CoreBlock)
            {
                sync_unitCap(event.tile.team());
            }
        });

        Events.on(EventType.CoreChangeEvent.class, event -> {
            Time.run(5f / 60f, () -> sync_unitCap(event.core.team()));
            
        });

        Vars.netServer.admins.addActionFilter(action ->
        {
            if (action.type != Administration.ActionType.placeBlock) return true;
            if (action.block != Blocks.vault) return true;

            Object result = validPlaceCore(action.tile, action.player.team());
            if (result instanceof CoreBuild) {
                mindustry.gen.Call.effect(action.player.con(), Fx.overdriveWave, ((CoreBuild) result).x, ((CoreBuild) result).y, Vars.state.rules.enemyCoreBuildRadius * 2, Color.valueOf("#ff3030ff"));
                UIManager.label(action.tile, "error.core_build_radius_cross", action.player);
                action.tile.setNet(Blocks.air);
                return false;
            }

            return true;
        });

        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            if (!event.breaking && event.tile.block() == Blocks.vault)
            {
                Core.app.post(() -> {
                    event.tile.setNet(Blocks.coreShard, event.tile.build.team(), 0);
                });
                Groups.player.each(player -> {
                    mindustry.gen.Call.effect(Fx.overdriveWave, event.tile.worldx(), event.tile.worldy(), Vars.state.rules.enemyCoreBuildRadius, event.team.color);
                });

                
                sync_unitCap(event.team);
            }
        });
    }

    public static Object validPlaceCore(Tile tile, Team team) {
        // Проверка перекрытия с другими ядрами
        CoreBuild closestCore = null;
        float dst = 0;
        int count = 0;
        for (Team checkTeam : Team.all) {
            if (team == checkTeam) continue;
            for (CoreBuild core : checkTeam.cores())
            {
                count++;
                Call.label(Integer.toString(count), 5, core.x, core.y);
                if (core.within(tile.worldx(), tile.worldy(), Vars.state.rules.enemyCoreBuildRadius * 2))
                {
                    float new_dst = core.dst2(tile.worldx(), tile.worldy());
                    if (closestCore == null || dst > new_dst)
                    {
                        closestCore = core;
                        dst = new_dst;
                    }
                }
            }
        }
        if (closestCore != null) return closestCore;

        // Проверка физической возможности размещения
        if (!Build.validPlace(Blocks.multiplicativeReconstructor, team, tile.x, tile.y, 0)) {
            return tile; // Нельзя разместить на этом тайле
        }

        return 0;
    }

    public static void sync_unitCap(Team team)
    {
        Rules rules = Vars.state.rules.copy();
        int vanillaUnitCap = 0;
        for (CoreBlock.CoreBuild core : team.cores())
        {
            if (core.block() == Blocks.coreShard) vanillaUnitCap += 8;
            else if (core.block() == Blocks.coreFoundation) vanillaUnitCap += 16;
            else if (core.block() == Blocks.coreNucleus) vanillaUnitCap += 24;
        }
        rules.unitCap = team.data().unitCap - vanillaUnitCap + Vars.state.rules.unitCap;
        team.data().players.forEach(player ->
        {
            Call.setRules(player.con(), rules);
        });
    }
}
