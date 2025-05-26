package Modules;

import arc.util.Log;
import static mindustry.Vars.logic;
import static mindustry.Vars.maps;
import static mindustry.Vars.netServer;
import static mindustry.Vars.state;
import static mindustry.Vars.world;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.maps.Map;
import mindustry.maps.MapException;

public class MapManager {
    public void init()
    {
        
    }

    
     public static Rules generateRules()
     {
        Rules rules = new Rules();
        
        // Кастомные настройки
        rules.pvp = true;
        rules.waves = false;
        rules.pvpAutoPause = false;
        rules.enemyCoreBuildRadius = 0f;
        rules.unitCap = 0;
        
        return rules;
     }

    public void reload()
    {
        Rules customRules = generateRules();

        Map result;
        result = maps.getShuffleMode().next(Gamemode.editor, state.map);
        if(result != null){
            Log.info("Randomized next map to be @.", result.plainName());
        }
        Log.info("Loading map...");

        logic.reset();
        if(result != null){
            try{
                world.loadMap(result, result.rules(customRules));
                state.rules = result.rules(customRules);
                logic.play();

                Log.info("Map loaded.");

                netServer.openServer();
            }catch(MapException e){
                Log.err("@: @", e.map.plainName(), e.getMessage());
            }
        }
    }
}
