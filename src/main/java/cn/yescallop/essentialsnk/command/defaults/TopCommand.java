package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class TopCommand extends CommandBase {

    public TopCommand(EssentialsAPI api) {
        super("top", api);
        commandParameters.clear();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testInGame(sender)) {
            return false;
        }

        Player player = (Player) sender;
        sender.sendMessage(Language.translate("commands.generic.teleporting"));
        player.teleport(essentialsAPI.getHighestStandablePositionAt(player));
        return true;
    }
}
