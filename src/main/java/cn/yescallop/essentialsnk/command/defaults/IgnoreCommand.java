package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class IgnoreCommand extends CommandBase {

    public IgnoreCommand(EssentialsAPI api) {
        super("ignore", api);
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
    }


    @Override
    @SuppressWarnings("deprecation")
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testInGame(sender)) {
            return false;
        }

        if (args.length == 0) {
            this.sendUsage(sender);
            return false;
        }

        Player player = (Player) sender;
        IPlayer toIgnore = Server.getInstance().getPlayer(args[0]);

        if (toIgnore == null) {
            toIgnore = Server.getInstance().getOfflinePlayer(args[0]);

            if (toIgnore.getUniqueId() == null) {
                this.sendUsage(sender);
                return false;
            }
        }

        if (toIgnore.equals(player)) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.ignore.self"));
        }

        sender.sendMessage(essentialsAPI.ignore(player.getUniqueId(), toIgnore.getUniqueId()) ? "Successfully ignored" : "Successfully un-ignored");
        return true;
    }
}