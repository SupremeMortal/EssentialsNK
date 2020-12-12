package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class SpeedCommand extends CommandBase {

    public SpeedCommand(EssentialsAPI api) {
        super("speed", api);
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("multiplier", CommandParamType.INT, false),
                new CommandParameter("player", CommandParamType.TARGET, true)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length == 0 || args.length > 2) {
            this.sendUsage(sender);
            return false;
        }

        int speed;
        try {
            speed = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.number.invalidinteger", args[0]));
            return false;
        }

        Player player;
        if (args.length == 2) {
            if (!sender.hasPermission("essentialsnk.speed.others")) {
                this.sendPermissionMessage(sender);
                return false;
            }

            player = Server.getInstance().getPlayer(args[1]);
        } else if (!this.testInGame(sender)) {
            return false;
        } else {
            player = (Player) sender;
        }

        if (player == null) {
            sender.sendMessage(Language.translate("commands.generic.player.notfound", args[0]));
            return false;
        }

        player.removeEffect(Effect.SPEED);
        if (speed != 0) {
            player.addEffect(
                    Effect.getEffect(Effect.SPEED)
                            .setAmplifier(speed)
                            .setDuration(Integer.MAX_VALUE)
            );
        }

        sender.sendMessage(sender.equals(player) ? Language.translate("commands.speed.success", speed) : Language.translate("commands.speed.success.other", player.getDisplayName(), speed));
        return true;
    }
}