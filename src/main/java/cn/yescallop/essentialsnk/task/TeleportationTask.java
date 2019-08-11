package cn.yescallop.essentialsnk.task;

import cn.nukkit.scheduler.Task;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.TPCooldown;

import java.util.Iterator;

public class TeleportationTask extends Task {
    private final EssentialsAPI api;

    public TeleportationTask(EssentialsAPI api) {
        this.api = api;
    }

    @Override
    public void onRun(int i) {
        Iterator<TPCooldown> iter = api.getTpCooldowns().iterator();
        long time = System.currentTimeMillis();

        while (iter.hasNext()) {
            TPCooldown cooldown = iter.next();
            if (cooldown.getTimestamp() <= time) {
                cooldown.execute();
                iter.remove();
            }
        }
    }
}
