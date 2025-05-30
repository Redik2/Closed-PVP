package Modules;

import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.ui.Menus;

public class MenuManager {
    public static int teamNameMenu;

    public static void init()
    {
        teamNameMenu = Menus.registerTextInput((player, text) -> {
            if (text != null)
            {
                player.team().name = text;
                UIManager.chatMessage("chat_notification.new_team_spawn", player.team().coloredName());
            }
            else
            {
                TeamsManager.cancelTeam(player.team());
            }
        });
    }

    public static void callTeamNameMenu(Player player)
    {
        Call.textInput(player.con(), teamNameMenu, LocalizationManager.getFormatted("menu.teamNameMenu.title", player), LocalizationManager.getFormatted("menu.teamNameMenu.text", player), 10, "", false);
    }
}
