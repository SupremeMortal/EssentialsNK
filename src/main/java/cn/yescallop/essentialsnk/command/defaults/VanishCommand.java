package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class VanishCommand extends CommandBase {

    public VanishCommand(EssentialsAPI api) {
        super("vanish", api);
        this.setAliases(new String[]{"v"});
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
        } else {
            if (!sender.hasPermission("essentialsnk.vanish.others")) {
                this.sendPermissionMessage(sender);
                return false;
            }

            player = Server.getInstance().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
                return false;
            }
        }

        String enabled = Language.translate(essentialsAPI.switchVanish(player) ? "commands.generic.enabled" : "commands.generic.disabled");
        player.sendMessage(Language.translate("commands.vanish.success", enabled));
        if (sender != player) {
            sender.sendMessage(Language.translate("commands.vanish.success.other", player.getDisplayName(), enabled));
        }

        return true;
    }
}