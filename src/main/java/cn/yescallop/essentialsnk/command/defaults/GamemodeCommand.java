package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class GamemodeCommand extends CommandBase {

    public GamemodeCommand(EssentialsAPI api) {
        super("gamemode", api);
        this.setAliases(new String[]{"gm", "gma", "gmc", "gms", "gmsp", "gmt", "adventure", "creative", "survival", "spectator", "viewer"});

        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("mode", CommandParamType.INT, false),
                new CommandParameter("player", CommandParamType.TARGET, true)
        });
        this.commandParameters.put("byString", new CommandParameter[]{
                new CommandParameter("mode", new String[]{"survival", "s", "creative", "c",
                        "adventure", "a", "spectator", "sp", "viewer", "view", "v"}),
                new CommandParameter("player", CommandParamType.TARGET, true)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }
        Player player;
        int gamemode;
        if (label.toLowerCase().equals("gamemode") || label.toLowerCase().equals("gm")) {
            if (args.length == 0 || args.length > 2) {
                this.sendUsage(sender, label);
                return false;
            }
            if (args.length == 1) {
                if (!this.testIngame(sender)) {
                    return false;
                }
                player = (Player) sender;
            } else {
                if (!sender.hasPermission("essentialsnk.gamemode.others")) {
                    this.sendPermissionMessage(sender);
                    return false;
                }
                player = api.getServer().getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[1]));
                    return false;
                }
            }
            gamemode = Server.getGamemodeFromString(args[0]);
            if (gamemode == -1) {
                sender.sendMessage(TextFormat.RED + Language.translate("commands.gamemode.invalid", args[0]));
                return false;
            }
        } else {
            if (args.length > 1) {
                this.sendUsage(sender, label);
                return false;
            }
            if (args.length == 0) {
                if (!this.testIngame(sender)) {
                    return false;
                }
                player = (Player) sender;
            } else {
                if (!sender.hasPermission("essentialsnk.gamemode.others")) {
                    this.sendPermissionMessage(sender);
                    return false;
                }
                player = api.getServer().getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.player.notfound", args[0]));
                    return false;
                }
            }
            switch (label.toLowerCase()) {
                case "gms":
                case "survival":
                case "s":
                    gamemode = Player.SURVIVAL;
                    break;
                case "creative":
                case "c":
                case "gmc":
                    gamemode = Player.CREATIVE;
                    break;
                case "adventure":
                case "a":
                case "gma":
                    gamemode = Player.ADVENTURE;
                    break;
                case "spectator":
                case "viewer":
                case "view":
                case "sp":
                case "v":
                case "gmsp":
                case "gmt":
                    gamemode = Player.SPECTATOR;
                    break;
                default:
                    return false;
            }
        }
        player.setGamemode(gamemode);
        String gamemodeStr = Server.getGamemodeString(gamemode);
        player.sendMessage(Language.translate("commands.gamemode.success", gamemodeStr));
        if (sender != player) {
            sender.sendMessage(Language.translate("commands.gamemode.success.other", player.getDisplayName(), gamemodeStr));
        }
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        String usage;
        if (label.toLowerCase().equals("gamemode") || label.toLowerCase().equals("gm")) {
            usage = Language.translate("commands.gamemode.usage1", label.toLowerCase());
        } else {
            usage = Language.translate("commands.gamemode.usage2", label.toLowerCase());
        }
        sender.sendMessage(new TranslationContainer("commands.generic.usage", usage));
    }
}
