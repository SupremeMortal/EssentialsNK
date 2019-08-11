package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class RealNameCommand extends CommandBase {

    public RealNameCommand(EssentialsAPI api) {
        super("realname", api);

        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }
        if (args.length != 1) {
            this.sendUsage(sender);
            return false;
        }
        Player player = api.getServer().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
            return false;
        }
        sender.sendMessage(Language.translate("commands.realname.success", player.getDisplayName(), player.getName()));
        return true;
    }
}
