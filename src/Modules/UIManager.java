package Modules;

import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Tile;

public class UIManager {
    public static void init()
    {

    }

    public static void label(float worldx, float worldy, String key, Player player, Object... args)
    {
        Call.label(player.con(), LocalizationManager.getFormatted(key, player, args), 3f, worldx, worldy);
    }

    public static void label(Tile tile, String key, Player player, Object... args)
    {
        label(tile.worldx(), tile.worldy(), key, player, args);
    }
}
