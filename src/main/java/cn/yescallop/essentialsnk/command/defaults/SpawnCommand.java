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

/**
 * Created by CreeperFace on 9. 12. 2016.
 */
public class SpawnCommand extends CommandBase {

    public SpawnCommand(EssentialsAPI api) {
        super("spawn", api);
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, true)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testInGame(sender)) {
            return false;
        }

        if (args.length == 1 && !sender.hasPermission("essentialsnk.spawn.others")) {
            this.sendPermissionMessage(sender);
            return false;
        }

        if (args.length > 1) {
            this.sendUsage(sender);
            return false;
        }

        if (essentialsAPI.hasCooldown(sender)) {
            return true;
        }

        Player player;

        if (args.length == 0) {
            player = (Player) sender;
        } else {
            player = Server.getInstance().getPlayer(args[0]);
        }

        if (player == null || !player.isOnline()) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
            return false;
        }

        essentialsAPI.onTP(player, essentialsAPI.getServer().getDefaultLevel().getSpawnLocation(), Language.translate("commands.generic.teleporting"));
        if (args.length == 1) {
            sender.sendMessage(Language.translate("commands.generic.teleporting"));
        }

        return true;
    }
}