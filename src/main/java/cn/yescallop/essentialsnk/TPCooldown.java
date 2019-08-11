package cn.yescallop.essentialsnk;

import cn.nukkit.Player;
import cn.nukkit.level.Location;

public class TPCooldown {
    private final Player player;
    private final Location location;
    private final long timestamp;
    private final String message;

    public TPCooldown(Player player, Location location, long timestamp, String message) {
        this.player = player;
        this.location = location;
        this.timestamp = timestamp;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void execute() {
        player.teleport(location);
        player.sendMessage(message);
    }
}
