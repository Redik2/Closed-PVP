import Modules.MapManager;
import arc.util.CommandHandler;
import arc.util.Log;
import static mindustry.Vars.state;
import mindustry.mod.Plugin;

public class Main extends Plugin 
{
    MapManager mapManager = new MapManager();

    @Override
    public void init()
    {
        mapManager.init();
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("host", "Start Closed PVP gamemode", arg -> {
            if(state.isGame()){
                Log.err("Already hosting. Type 'stop' to stop hosting first.");
                return;
            }

            mapManager.reload();
        });
    }
}
