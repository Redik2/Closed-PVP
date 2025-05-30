package Modules;

import java.util.HashMap;

import arc.Events;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.gen.Player;

public class CacheManager {
    static HashMap<String, PlayerCache> playerCache;

    public static void init() {
        playerCache = new HashMap<>();

        Events.on(EventType.PlayerConnect.class, event -> {
            Player player = event.player;
            Log.info("Create chache for " + player.uuid());
            if (!playerCache.containsKey(player.uuid()))
            {
                playerCache.put(player.uuid(), new PlayerCache());
            }
        });
    }

    public static PlayerCache getPlayerCache(String uuid)
    {
        return playerCache.getOrDefault(uuid, new PlayerCache());
    }

}
