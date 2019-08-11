package cn.yescallop.essentialsnk;

import cn.nukkit.Player;
import cn.nukkit.level.Location;

public class TPRequest {

    private final long startTime;
    private final Player from;
    private final Player to;
    private final Location location;
    private final boolean isTo;

    public TPRequest(long startTime, Player from, Player to, Location location, boolean isTo) {
        this.startTime = startTime;
        this.from = from;
        this.to = to;
        this.location = location;
        this.isTo = isTo;
    }

    public long getStartTime() {
        return startTime;
    }

    public Player getFrom() {
        return from;
    }

    public Player getTo() {
        return to;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isTo() {
        return isTo;
    }
}