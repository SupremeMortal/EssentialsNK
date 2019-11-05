package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class MessageCommand extends CommandBase {

    public MessageCommand(EssentialsAPI api) {
        super("message", api);
        this.setAliases(new String[]{"m", "msg", "w", "whisper", "tell", "privatemessage", "pm"});

        // command parameters
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("message", CommandParamType.TEXT, false)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }
        if (args.length < 2) {
            return false;
        }

        Player player = api.getServer().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
            return false;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }

        sender.sendMessage(Language.translate("commands.message.toformat", player.getName(), builder.toString()));
        player.sendMessage(Language.translate("commands.message.fromformat", sender.getName(), builder.toString()));

        api.getLastMessagedPlayers().put(sender.getName(), player.getName());
        api.getLastMessagedPlayers().put(player.getName(), sender.getName());
        return true;
    }
}
