package cn.yescallop.essentialsnk.command.defaults.teleport;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.TPRequest;
import cn.yescallop.essentialsnk.command.CommandBase;

public class TPAcceptCommand extends CommandBase {

    public TPAcceptCommand(EssentialsAPI api) {
        super("tpaccept", api);
        this.setAliases(new String[]{"tpyes", "tpc"});
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, true)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testInGame(sender)) {
            return false;
        }

        Player to = (Player) sender;
        if (essentialsAPI.getLatestTPRequestTo(to) == null) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.tpaccept.noRequest"));
            return false;
        }

        TPRequest request;
        Player from;
        if (args.length == 0) {
            if ((request = essentialsAPI.getLatestTPRequestTo(to)) == null) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.tpaccept.unavailable"));
                return false;
            }

            from = request.getSender();
        } else {
            from = Server.getInstance().getPlayer(args[0]);
            if (from == null) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
                return false;
            }

            if ((request = essentialsAPI.getTPRequestBetween(from, to)) != null) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.tpaccept.noRequestFrom", from.getDisplayName()));
                return false;
            }
        }

        if (request == null) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.tpaccept.noRequest"));
            return false;
        }

        from.sendMessage(Language.translate("commands.tpaccept.accepted", to.getDisplayName()));
        sender.sendMessage(Language.translate("commands.generic.teleporting"));

        essentialsAPI.onTP(request.isTo() ? from : to, request.getLocation(), Language.translate("commands.generic.teleporting"));
        essentialsAPI.removeTPRequestBetween(from, to);
        return true;
    }
}