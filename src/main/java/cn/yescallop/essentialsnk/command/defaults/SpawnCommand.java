package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
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

        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("player", CommandParamType.TARGET, true)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length == 0 && !this.testIngame(sender)) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.ingame"));
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

        if (api.hasCooldown(sender)) {
            return true;
        }

        Player p;

        if (args.length == 0) {
            p = (Player) sender;
        } else {
            p = getAPI().getServer().getPlayer(args[0]);
        }

        if (p == null || !p.isOnline()) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
            return false;
        }

        api.onTP(p, api.getServer().getDefaultLevel().getSpawnLocation(), Language.translate("commands.generic.teleporting"));
        if (args.length == 1) {
            sender.sendMessage(Language.translate("commands.generic.teleporting"));
        }
        return true;
    }
}
