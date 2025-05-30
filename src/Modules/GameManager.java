package Modules;

import java.util.ArrayList;

import arc.Events;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import static mindustry.Vars.logic;
import static mindustry.Vars.maps;
import static mindustry.Vars.state;
import static mindustry.Vars.world;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import mindustry.net.WorldReloader;
import mindustry.world.blocks.storage.CoreBlock;

public class GameManager {
    public static void init()
    {
        applyCustoms();
        arc.util.Timer.schedule(() -> {
            ZoneManager.draw();
        }, 0f, 0.1f);

        Events.on(CustomEvents.ZoneClosed.class, event -> {
            ArrayList<Team> activeTeams = new ArrayList<>();
            for (Team team : Team.all)
            {
                if (team.active()) activeTeams.add(team);
            }

            if (activeTeams.size() == 1)
            {
                ZoneManager.stopZoneClosing();
                Timer.schedule(() -> GameManager.reload(), 10);
                Groups.player.forEach(player -> {
                    mindustry.gen.Call.infoMessage(player.con(), LocalizationManager.getFormatted("gameover.team_wins", player, activeTeams.get(0).coloredName()));
                });
            }
            else if (activeTeams.isEmpty())
            {
                GameManager.reload();
            }
        });
    }

    
    public static Rules generateRules()
    {
        Rules rules = new Rules();
        
        // Кастомные настройки
        rules.pvp = true;
        rules.waves = false;
        rules.waveTimer = false;
        rules.pvpAutoPause = false;
        rules.enemyCoreBuildRadius = 240f;
        rules.unitCap = 8;
        rules.canGameOver = false;
        rules.infiniteResources = true;
        rules.planet = Planets.sun;
        rules.modeName = "CPvP";

        return rules;
    }

    public static void applyCustoms()
    {
        Blocks.coreShard.unitCapModifier = 0;
        Blocks.coreShard.itemCapacity = 0;

        Blocks.coreFoundation.unitCapModifier = 2;
        
        Blocks.coreNucleus.unitCapModifier = 8;
    }

    public static void reload()
    {
        Rules customRules = generateRules();
        WorldReloader reloader = new WorldReloader();

        Map result;
        result = maps.getShuffleMode().next(Gamemode.editor, state.map);
        if(result != null){
            Log.info("Randomized next map to be @.", result.plainName());
        }
        Log.info("Loading map...");

        if(result != null){
            try{
                reloader.begin();
                logic.reset();

                world.loadMap(result, result.rules(customRules));
                
                ZoneManager.stopZoneClosing();
                ZoneManager.resetZone();

                state.rules = customRules;

                logic.play();
                reloader.end();

                mindustry.gen.Call.setCameraPosition(Vars.world.width() * 4, Vars.world.height() * 4);

                Vars.world.tiles.forEach(tile -> {
                    if (tile.block() instanceof CoreBlock) tile.removeNet();
                });

                ZoneManager.startZoneClosing(1);

                Log.info("Map loaded.");
            }catch(MapException e){
                Log.err("@: @", e.map.plainName(), e.getMessage());
            }
        }
    }
}
