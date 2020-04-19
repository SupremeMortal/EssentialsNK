package cn.yescallop.essentialsnk.command.defaults.home;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

import java.util.OptionalInt;

public class SetHomeCommand extends CommandBase {

    public SetHomeCommand(EssentialsAPI api) {
        super("sethome", api);
        this.setAliases(new String[]{"createhome", "newhome"});
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("home", CommandParamType.TEXT, false)
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

        if (args[0].toLowerCase().equals("bed")) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.sethome.bed"));
            return false;
        } else if (args[0].trim().equals("")) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.sethome.empty"));
            return false;
        }

        Player player = (Player) sender;
        OptionalInt allowedHomes = essentialsAPI.getAllowedHomes(player);
        if (allowedHomes.isPresent()) {
            int currentHomesCount = essentialsAPI.getHomesList(player).length;
            if (currentHomesCount >= allowedHomes.getAsInt()) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.sethome.limit"));
                return true;
            }
        }

        sender.sendMessage(essentialsAPI.setHome(player, args[0].toLowerCase(), player) ?
                Language.translate("commands.sethome.updated", args[0]) :
                Language.translate("commands.sethome.success", args[0])
        );

        return true;
    }
}