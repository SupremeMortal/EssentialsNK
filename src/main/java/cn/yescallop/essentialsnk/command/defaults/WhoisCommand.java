package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.LoginChainData;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Pattern;

public class WhoisCommand extends CommandBase {
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    public WhoisCommand(EssentialsAPI api) {
        super("whois", api);
        this.setAliases(new String[]{"whoiss"});
        this.setUsage("/whois <name|uuid>");
        this.setPermission("essentialsnk.whois");

        this.commandParameters.clear();
        this.commandParameters.put("player", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
        this.commandParameters.put("uuid", new CommandParameter[]{
                new CommandParameter("uuid", CommandParamType.STRING, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) && !this.testIngame(sender)) {
            return false;
        }
        if (args.length > 1 || args.length == 0 && !(sender instanceof Player)) {
            return false;
        }

        IPlayer player;
        if (args.length == 0) {
            player = (Player) sender;
        } else if (sender.hasPermission("essentialsnk.whois.other")) {
            try {
                if (UUID_PATTERN.matcher(args[0]).matches()) {
                    player = sender.getServer().getOfflinePlayer(UUID.fromString(args[0]));
                } else {
                    player = sender.getServer().getOfflinePlayer(args[0]);
                }
            } catch (Exception e) {
                player = null;
            }
        } else {
            this.sendPermissionMessage(sender);
            return true;
        }

        if (player == null || player.getFirstPlayed() == null) {
            sender.sendMessage(Language.translate("commands.whois.notfound"));
            return true;
        }

        StringJoiner message = new StringJoiner("\n");

        Date firstJoined = new Date(player.getFirstPlayed() * 1000);
        Date lastJoined = new Date(player.getLastPlayed() * 1000);

        String name = player.getName() == null ? "N/A" : player.getName();
        message.add(Language.translate("commands.whois.header", name));

        message.add(Language.translate("commands.whois.join.first", DATE_FORMAT.format(firstJoined)));
        message.add(Language.translate("commands.whois.join.last", DATE_FORMAT.format(lastJoined)));

        UUID uuid = player.getUniqueId() == null ? new UUID(0, 0) : player.getUniqueId();
        message.add(Language.translate("commands.whois.uuid", uuid.toString()));

        // Online data
        Player onlinePlayer = null;
        if (player.isOnline()) {
            onlinePlayer = player.getPlayer();
        }

        if (onlinePlayer != null) {
            LoginChainData loginChainData = onlinePlayer.getLoginChainData();
            String xuid = loginChainData.isXboxAuthed() ? loginChainData.getXUID() : "N/A (Offline)";
            message.add(Language.translate("commands.whois.xuid", xuid));

            InetSocketAddress socketAddress = new InetSocketAddress(onlinePlayer.getAddress(), onlinePlayer.getPort());
            String address = socketAddress.getAddress().getHostAddress();
            String hostname = socketAddress.getAddress().getCanonicalHostName();
            if (hostname.equals(address)) {
                hostname = "N/A";
            }

            message.add(Language.translate("commands.whois.ip.address", address));
            message.add(Language.translate("commands.whois.ip.reverse", hostname));

            DeviceOS deviceOS = DeviceOS.values()[loginChainData.getDeviceOS()];
            message.add(Language.translate("commands.whois.device.os", deviceOS.name));

            String deviceModel = loginChainData.getDeviceModel();
            message.add(Language.translate("commands.whois.device.model", deviceModel));


            String[] langCountry = loginChainData.getLanguageCode().split("_");
            Locale locale = new Locale(langCountry[0], langCountry[1]);
            message.add(Language.translate("commands.whois.language", locale.getDisplayName()));

            message.add(Language.translate("commands.whois.vanished", api.isVanished(onlinePlayer)));

            message.add(Language.translate("commands.whois.gamemode", Server.getGamemodeString(onlinePlayer.getGamemode())));

            message.add(Language.translate("commands.whois.health", onlinePlayer.getHealth() + "/" + onlinePlayer.getMaxHealth()));

            message.add(Language.translate("commands.whois.exp", onlinePlayer.getExperience()));
        }

        message.add(Language.translate("commands.whois.op", sender.getServer().isOp(player.getName())));

        sender.sendMessage(message.toString());

        return true;
    }

    private enum DeviceOS {
        UNKNOWN("Unknown"),
        ANDROID("Android"),
        IOS("iOS"),
        OSX("MacOS"),
        FIREOS("FireOS"),
        GEARVR("GearVR"),
        HOLOLENS("HoloLens"),
        WIN10("UWP"),
        WIN32("Windows"),
        DEDICATED("Dedicated"),
        ORBIS("PlayStation 4"),
        NX("Nintendo Switch");

        private final String name;

        DeviceOS(String name) {
            this.name = name;
        }
    }
}
