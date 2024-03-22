package cn.yescallop.essentialsnk.command.defaults.teleport;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.TPRequest;
import cn.yescallop.essentialsnk.command.CommandBase;

public class TPDenyCommand extends CommandBase {

    public TPDenyCommand(EssentialsAPI api) {
        super("tpdeny", api);
        this.setAliases(new String[]{"tpno"});

        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("player", CommandParamType.TARGET, true)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }
        if (!this.testIngame(sender)) {
            return false;
        }
        if (args.length > 1) {
            this.sendUsage(sender);
            return false;
        }
        Player to = (Player) sender;
        TPRequest request;
        Player from;
        if (args.length == 0) {
            request = api.getLatestTPRequestTo(to);
            if (request == null) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.tpaccept.noRequest"));
                return false;
            }
            from = request.getSender();
        } else {
            from = api.getServer().getPlayer(args[0]);
            if (from == null) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
                return false;
            }
            if ((request = api.getTPRequestBetween(from, to)) == null) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.tpaccept.noRequestFrom", from.getDisplayName()));
                return false;
            }
        }
        if (sender != request.getRecipient()) {
            return true;
        }
        from.sendMessage(Language.translate("commands.tpdeny.denied", to.getDisplayName()));
        sender.sendMessage(Language.translate("commands.tpdeny.success", to.getDisplayName()));
        api.removeTPRequestBetween(from, to);
        return true;
    }
}
