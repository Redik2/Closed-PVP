import Modules.CacheManager;
import Modules.CoresManager;
import Modules.GameManager;
import Modules.LocalizationManager;
import Modules.MenuManager;
import Modules.TeamsManager;
import Modules.ZoneManager;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.Vars;
import static mindustry.Vars.state;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import mindustry.world.Tile;

public class Main extends Plugin 
{
    @Override
    public void init()
    {

        GameManager.init();
        CoresManager.init();
        CacheManager.init();
        TeamsManager.init();
        LocalizationManager.init();
        ZoneManager.init();
        MenuManager.init();

        Administration.Config.serverName.set(LocalizationManager.get("server.name", "en"));
        Administration.Config.desc.set(LocalizationManager.get("server.desc", "en"));
        Vars.netServer.admins.setPlayerLimit(10);
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("host", "Start Closed PVP gamemode", arg -> {
            if(state.isGame()){
                Log.err("Already hosting. Type 'stop' to stop hosting first.");
                return;
            }

            GameManager.reload();
            Vars.netServer.openServer();
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("destroy", " ", "Destroy tile under your controlled unit", (args, player) -> {
            Tile tile = player.tileOn();
            if (tile.build != null && tile.build.team() == player.team()) {
                tile.build.kill();
                return;
            } else {
                return;
            }
        });

        handler.<Player>register("spectate", " ", "Destroy your entire base and sets you in spectator mode", (args, player) -> {
            TeamsManager.killTeam(player.team());
        });
    }
}
