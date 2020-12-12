package cn.yescallop.essentialsnk.command.defaults.teleport;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class TPHereCommand extends CommandBase {

    public TPHereCommand(EssentialsAPI api) {
        super("tphere", api);
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testInGame(sender)) {
            return false;
        }

        if (args.length < 1) {
            this.sendUsage(sender);
            return false;
        }

        Player player = (Player) sender;
        Player target = Server.getInstance().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
            return false;
        }

        target.teleport(player);
        target.sendMessage(Language.translate("commands.tphere.other", player.getDisplayName()));
        sender.sendMessage(Language.translate("commands.tphere.success", target.getDisplayName()));
        return true;
    }
}