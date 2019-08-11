package cn.yescallop.essentialsnk.util;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import com.google.common.base.Preconditions;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Configs implements Closeable {
    private final Map<ConfigType, ConfigData> configs = new ConcurrentHashMap<>();
    private final TaskHandler reloadTaskHandler;

    public Configs(Plugin plugin, Set<ConfigType> configTypes) {
        Preconditions.checkNotNull(configTypes, "configTypes");
        Preconditions.checkArgument(!configTypes.isEmpty(), "configTypes was empty");
        this.reloadTaskHandler = plugin.getServer().getScheduler()
                .scheduleDelayedRepeatingTask(plugin, new ConfigChangeTask(), 600, 600);

        for (ConfigType configType : configTypes) {
            this.configs.put(configType, new ConfigData(configType));
        }
    }

    public void reload() {
        reloadTaskHandler.run(reloadTaskHandler.getLastRunTick());
    }

    public void set(ConfigType configType, String key, Object value) {
        Preconditions.checkNotNull(key, "key");
        this.getConfig(configType).set(key, value);
    }

    public <T> T get(ConfigType configType, String key, T defaultValue) {
        Preconditions.checkNotNull(key, "key");
        return this.getConfig(configType).get(key, defaultValue);
    }

    public boolean exists(ConfigType configType, String key) {
        Preconditions.checkNotNull(key, "key");
        return this.getConfig(configType).exists(key);
    }

    public void remove(ConfigType configType, String key) {
        Preconditions.checkNotNull(key, "key");
        this.getConfig(configType).remove(key);
    }

    public Set<String> getKeys(ConfigType configType) {
        return this.getConfig(configType).getKeys();
    }

    private ConfigData getConfig(ConfigType configType) {
        Preconditions.checkNotNull(configType, "configType");
        ConfigData config = this.configs.get(configType);
        Preconditions.checkArgument(config != null, "ConfigType does not exist");
        return config;
    }

    @Override
    public void close() {
        this.reloadTaskHandler.cancel();
    }

    private class ConfigChangeTask implements Runnable {

        @Override
        public void run() {
            for (ConfigData data : Configs.this.configs.values()) {
                if (!data.changed) {
                    continue;
                }

                data.config.reload();
                for (String key : data.removed) {
                    data.config.remove(key);
                }
                data.config.getRootSection().putAll(data.changeSet);

                data.config.save();

                data.changed = false;
            }
        }
    }

    private static class ConfigData {
        private final Config config;
        private final ConfigSection changeSet;
        private volatile boolean changed;
        private final Set<String> removed = new HashSet<>();

        private ConfigData(ConfigType configType) {
            this.config = new Config(configType.getFile(), configType.getType());
            this.changeSet = new ConfigSection();
        }

        public void set(String key, Object value) {
            this.changeSet.set(key, value);
            this.changed = true;
            this.removed.remove(key);
        }

        @SuppressWarnings("unchecked")
        private <T> T get(String key, T defaultValue) {
            if (this.changed) {
                if (this.changeSet.exists(key)) {
                    return (T) this.changeSet.get(key);
                }
                if (this.removed.contains(key)) {
                    return defaultValue;
                }
            }
            Object object = this.config.get(key);
            if (object == null) {
                return defaultValue;
            }
            return (T) object;
        }

        public boolean exists(String key) {
            if (this.changed) {
                if (this.changeSet.exists(key)) {
                    return true;
                } else if (this.removed.contains(key)) {
                    return false;
                }
            }
            return this.config.exists(key);
        }

        public void remove(String key) {
            this.removed.add(key);
            this.changeSet.remove(key);
            this.changed = true;
        }

        public Set<String> getKeys() {
            Set<String> keys = this.config.getKeys();
            keys.removeAll(this.removed);

            return keys;
        }
    }
}
