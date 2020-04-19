package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

/**
 * Created by CreeperFace on 9. 12. 2016.
 */
public class SetSpawnCommand extends CommandBase {

    public SetSpawnCommand(EssentialsAPI api) {
        super("setspawn", api);
        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testInGame(sender)) {
            return false;
        }

        if (args.length != 0) {
            this.sendUsage(sender);
            return false;
        }

        Player player = (Player) sender;
        player.getLevel().setSpawnLocation(player.round());
        Server.getInstance().setDefaultLevel(player.getLevel());

        player.sendMessage(TextFormat.YELLOW + Language.translate("commands.setspawn.success"));
        Server.getInstance().getLogger().info(TextFormat.YELLOW + "Server's spawn point set to " + TextFormat.AQUA + player.getLevel().getName() + TextFormat.YELLOW + " by " + TextFormat.GREEN + player.getName());
        return true;
    }
}