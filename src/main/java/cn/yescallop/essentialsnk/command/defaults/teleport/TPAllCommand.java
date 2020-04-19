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

public class TPAllCommand extends CommandBase {

    public TPAllCommand(EssentialsAPI api) {
        super("tpall", api);
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, true)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        Player player;
        if (args.length == 0) {
            if (!this.testInGame(sender)) {
                return false;
            }

            player = (Player) sender;
        } else if (args.length == 1) {
            player = Server.getInstance().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
                return false;
            }
        } else {
            this.sendUsage(sender);
            return false;
        }

        Server.getInstance().getOnlinePlayers().forEach((uuid, target) -> {
            if (!target.equals(player)) {
                target.teleport(player);
                target.sendMessage(Language.translate("commands.tpall.other", player.getDisplayName()));
            }
        });

        player.sendMessage(Language.translate("commands.tpall.success"));
        return true;
    }
}