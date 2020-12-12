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

public class TPAHereCommand extends CommandBase {

    public TPAHereCommand(EssentialsAPI api) {
        super("tpahere", api);
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

        if (essentialsAPI.hasCooldown(sender)) {
            return true;
        }

        Player player = Server.getInstance().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
            return false;
        }

        if (sender.equals(player)) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.tpa.self"));
            return false;
        }

        if (!essentialsAPI.isIgnoring(player.getUniqueId(), ((Player) sender).getUniqueId())) {
            essentialsAPI.requestTP((Player) sender, player, false);
            player.sendMessage(Language.translate("commands.tpahere.invite", sender.getName()));
            sender.sendMessage(Language.translate("commands.tpa.success", player.getDisplayName()));
        } else {
            sender.sendMessage(Language.translate("commands.tpdeny.denied", player.getDisplayName()));
        }

        return true;
    }
}