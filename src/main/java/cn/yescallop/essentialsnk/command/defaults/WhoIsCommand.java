package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.command.CommandBase;

import java.text.DecimalFormat;
import java.util.Date;

public class WhoIsCommand extends CommandBase {

    public WhoIsCommand(EssentialsAPI api) {
        super("whois", api);
        this.setAliases(new String[]{"whoiss"});

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("name", CommandParamType.STRING, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) && !this.testIngame(sender)) {
            return false;
        }

        if (args.length < 1) {
            this.sendUsage(sender);
        } else {

            Player target = sender.getServer().getPlayer(args[0]);

            if (target != null && target.isOnline()) {

                Player player = (Player) sender;
                Date firstJoined = new Date(target.getFirstPlayed() * 1000);
                String targetLevel = target.getLevel().getName();

                double x = target.getLocation().getX();
                double y = target.getLocation().getY();
                double z = target.getLocation().getZ();
                
                player.sendMessage(TextFormat.YELLOW + "===== WhoIS " + TextFormat.WHITE + target.getName() + " =====");
                player.sendMessage(TextFormat.GOLD + "UUID " + TextFormat.WHITE + target.getUniqueId());
                player.sendMessage(TextFormat.GOLD + "Health " + TextFormat.WHITE + target.getHealth() + "/20");
                player.sendMessage(TextFormat.GOLD + "EXP " + TextFormat.WHITE + target.getExperience());
                player.sendMessage(TextFormat.GOLD + "Location " + TextFormat.WHITE + "(" + targetLevel + ", x" + x + ", y" + y + ", z" + z);
                player.sendMessage(TextFormat.GOLD + "First Joined " + TextFormat.WHITE + firstJoined);
                player.sendMessage(TextFormat.GOLD + "IP Address " + TextFormat.WHITE + "/" + target.getAddress());
                player.sendMessage(TextFormat.GOLD + "GameMode " + TextFormat.WHITE + target.getGamemode());
                player.sendMessage(TextFormat.GOLD + "Vanished " + TextFormat.WHITE + api.isVanished(target));
                player.sendMessage(TextFormat.GOLD + "OP " + TextFormat.WHITE + target.isOp());
                player.sendMessage(TextFormat.GOLD + "WhiteListed " + TextFormat.WHITE + target.isWhitelisted());

                if (api.isMuted(target)) {
                    player.sendMessage(TextFormat.GOLD + "Muted " + api.getRemainingTimeToUnmute(target));
                } else {
                    player.sendMessage(TextFormat.GOLD + "Muted " + TextFormat.WHITE + api.isMuted(target));
                }

            } else {
                sender.sendMessage("Player was not found...");
            }
        }
        return true;
    }
}
