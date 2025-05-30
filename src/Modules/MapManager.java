package Modules;

import arc.util.Log;
import mindustry.Vars;
import static mindustry.Vars.logic;
import static mindustry.Vars.maps;
import static mindustry.Vars.state;
import static mindustry.Vars.world;
import mindustry.content.Blocks;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import mindustry.world.blocks.storage.CoreBlock;

public class MapManager {
    public static void init()
    {
        
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

        return rules;
    }

    public static void applyCustoms()
    {
        Blocks.coreShard.unitCapModifier = 0;
        Blocks.coreShard.itemCapacity = 0;
        Blocks.coreNucleus.unitCapModifier = 8;
        Blocks.coreFoundation.unitCapModifier = 2;
    }

    public static void reload()
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
                world.loadMap(result, customRules);

                Vars.world.tiles.forEach(tile -> {
                    if (tile.block() instanceof CoreBlock) tile.removeNet();
                });

                state.rules = customRules;

                applyCustoms();

                logic.play();

                Log.info("Map loaded.");
            }catch(MapException e){
                Log.err("@: @", e.map.plainName(), e.getMessage());
            }
        }
    }
}
