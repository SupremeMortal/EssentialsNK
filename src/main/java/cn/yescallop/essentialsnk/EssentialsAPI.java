package cn.yescallop.essentialsnk;

import cn.nukkit.AdventureSettings;
import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.weather.EntityLightning;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmor;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.yescallop.essentialsnk.util.ConfigType;
import cn.yescallop.essentialsnk.util.Configs;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssentialsAPI {

    private static final long TP_EXPIRATION = TimeUnit.MINUTES.toMillis(1);
    private static final Pattern COOLDOWN_PATTERN = Pattern.compile("^essentialsnk\\.cooldown\\.([0-9]+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TP_COOLDOWN_PATTERN = Pattern.compile("^essentialsnk\\.tp\\.cooldown\\.([0-9]+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOMES_PERMISSION_PATTERN = Pattern.compile("^essentialsnk\\.homes\\.([0-9]+)$", Pattern.CASE_INSENSITIVE);
    public static final Integer[] NON_SOLID_BLOCKS = new Integer[]{Block.AIR, Block.SAPLING, Block.WATER, Block.STILL_WATER, Block.LAVA, Block.STILL_LAVA, Block.COBWEB, Block.TALL_GRASS, Block.BUSH, Block.DANDELION,
            Block.POPPY, Block.BROWN_MUSHROOM, Block.RED_MUSHROOM, Block.TORCH, Block.FIRE, Block.WHEAT_BLOCK, Block.SIGN_POST, Block.WALL_SIGN, Block.SUGARCANE_BLOCK,
            Block.PUMPKIN_STEM, Block.MELON_STEM, Block.VINE, Block.CARROT_BLOCK, Block.POTATO_BLOCK, Block.DOUBLE_PLANT};
    private static EssentialsAPI instance = null;
    private static Duration THIRTY_DAYS = Duration.ZERO.plusDays(30);
    private Vector3 temporalVector = new Vector3();
    private EssentialsNK plugin;
    private final Map<CommandSender, Long> cooldown = new IdentityHashMap<>();
    private final List<TPCooldown> tpCooldowns = new ArrayList<>();
    private Map<Player, Location> playerLastLocation = new HashMap<>();
    private Map<Integer, TPRequest> tpRequests = new ConcurrentHashMap<>();
    private List<Player> vanishedPlayers = new ArrayList<>();
    private Map<String, String> lastMessagedPlayers = new HashMap<>();

    private final ConfigType homeConfig;
    private final ConfigType warpConfig;
    private final ConfigType muteConfig;
    private final ConfigType ignoreConfig;
    private final Configs configs;

    public EssentialsAPI(EssentialsNK plugin) {
        instance = this;
        this.plugin = plugin;

        this.homeConfig = new ConfigType(new File(plugin.getDataFolder(), "home.yml"), Config.YAML);
        this.warpConfig = new ConfigType(new File(plugin.getDataFolder(), "warp.yml"), Config.YAML);
        this.muteConfig = new ConfigType(new File(plugin.getDataFolder(), "mute.yml"), Config.YAML);
        this.ignoreConfig = new ConfigType(new File(plugin.getDataFolder(), "ignore.yml"), Config.YAML);
        Set<ConfigType> configTypes = ImmutableSet.of(this.homeConfig, this.warpConfig, this.muteConfig, this.ignoreConfig);
        this.configs = new Configs(plugin, configTypes);

        this.plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(this.plugin, new TeleportationExpireTask(),
                20, 20, true);
    }

    public static EssentialsAPI getInstance() {
        return instance;
    }

    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public void reload() {
        this.configs.reload();
    }

    public Server getServer() {
        return plugin.getServer();
    }

    public PluginLogger getLogger() {
        return this.plugin.getLogger();
    }

    public void setLastLocation(Player player, Location pos) {
        if (pos.getLevel() != null && pos.getLevel().getProvider() != null) {
            this.playerLastLocation.put(player, pos);
        }
    }

    public Location getLastLocation(Player player) {
        Location lastLoc = this.playerLastLocation.get(player);
        if (lastLoc != null && lastLoc.getLevel().getProvider() == null) {
            this.playerLastLocation.remove(player);
            return null;
        }
        return lastLoc;
    }

    public boolean hasCooldown(CommandSender sender) {
        long cooldown = Long.MAX_VALUE;
        for (PermissionAttachmentInfo info : sender.getEffectivePermissions().values()) {
            Matcher matcher = COOLDOWN_PATTERN.matcher(info.getPermission());
            if (matcher.find()) {
                int time = Integer.parseInt(matcher.group(1));
                if (time < cooldown) {
                    cooldown = time;
                }
            }
        }

        if (!sender.isOp() && cooldown < Long.MAX_VALUE) {
            long currentTime = System.currentTimeMillis();
            long lastCooldown = this.cooldown.getOrDefault(sender, -1L) + TimeUnit.SECONDS.toMillis(cooldown);

            if (currentTime > lastCooldown) {
                this.cooldown.put(sender, currentTime);
            } else {
                long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastCooldown - currentTime);
                sender.sendMessage(Language.translate("commands.generic.cooldown", timeLeft));
                return true;
            }
        }
        return false;
    }

    private OptionalInt hasTPCooldown(Player player) {
        int cooldown = Integer.MAX_VALUE;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions().values()) {
            Matcher matcher = TP_COOLDOWN_PATTERN.matcher(info.getPermission());
            if (matcher.find()) {
                int time = Integer.parseInt(matcher.group(1));
                if (time < cooldown) {
                    cooldown = time;
                }
            }
        }

        if (!player.isOp() && cooldown < Integer.MAX_VALUE) {
            return OptionalInt.of(cooldown);
        }
        return OptionalInt.empty();
    }

    public OptionalInt getAllowedHomes(Player player) {
        int homes = 0;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions().values()) {
            Matcher matcher = HOMES_PERMISSION_PATTERN.matcher(info.getPermission());
            if (matcher.find()) {
                int newHomes = Integer.parseInt(matcher.group(1));
                if (homes < newHomes) {
                    homes = newHomes;
                }
            }
        }

        if (!player.isOp() && homes > 0) {
            return OptionalInt.of(homes);
        }
        return OptionalInt.empty();
    }

    public void onTP(Player player, Position position, String message) {
        this.onTP(player, position.getLocation(), message);
    }

    public void onTP(Player player, Location location, String message) {
        OptionalInt cooldown = hasTPCooldown(player);

        if (cooldown.isPresent()) {
            player.sendMessage(Language.translate("commands.generic.teleporation.cooldown", cooldown.getAsInt()));
            tpCooldowns.add(new TPCooldown(player, location,
                    System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(cooldown.getAsInt()), message));
        } else {
            player.teleport(location);
            player.sendMessage(message);
        }
    }

    public List<TPCooldown> getTpCooldowns() {
        return tpCooldowns;
    }

    public boolean switchCanFly(Player player) {
        boolean canFly = !this.canFly(player);
        this.setCanFly(player, canFly);
        return canFly;
    }

    public boolean canFly(Player player) {
        return player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT);
    }

    public void setCanFly(Player player, boolean canFly) {
        player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, canFly);
        player.getAdventureSettings().update();
    }

    public boolean switchVanish(Player player) {
        boolean vanished = this.isVanished(player);
        if (vanished) {
            this.setVanished(player, false);
            vanishedPlayers.remove(player);
        } else {
            this.setVanished(player, true);
            vanishedPlayers.add(player);
        }
        return !vanished;
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player);
    }

    public void setVanished(Player player, boolean vanished) {
        for (Player p : this.getServer().getOnlinePlayers().values()) {
            if (vanished) {
                p.hidePlayer(player);
            } else {
                p.showPlayer(player);
            }
        }
    }

    public boolean isRepairable(Item item) {
        return item instanceof ItemTool || item instanceof ItemArmor;
    }

    public void strikeLighting(Position pos) {
        FullChunk chunk = pos.getLevel().getChunk((int) pos.getX() >> 4, (int) pos.getZ() >> 4);
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("", pos.getX()))
                        .add(new DoubleTag("", pos.getY()))
                        .add(new DoubleTag("", pos.getZ())))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", 0))
                        .add(new FloatTag("", 0)));
        EntityLightning lightning = new EntityLightning(chunk, nbt);
        lightning.spawnToAll();
    }

    private static int getHashCode(Player from, Player to, boolean isTo) {
        return from.hashCode() + to.hashCode() + Boolean.hashCode(isTo);
    }

    public void requestTP(Player from, Player to, boolean isTo) {
        this.tpRequests.put(getHashCode(from, to, isTo), new TPRequest(System.currentTimeMillis(), from, to, isTo ? to.getLocation() : from.getLocation(), isTo));
    }

    public List<TPRequest> getTPRequestsTo(Player player) {
        List<TPRequest> requests = new ArrayList<>();
        for (TPRequest request : this.tpRequests.values()) {
            if (request.getRecipient() == player) {
                requests.add(request);
            }
        }
        return requests;
    }

    public TPRequest getLatestTPRequestTo(Player player) {
        TPRequest latest = null;
        for (TPRequest request : this.tpRequests.values()) {
            if (request.getRecipient() == player && (latest == null || request.getRequestTime() > latest.getRequestTime())) {
                latest = request;
            }
        }
        return latest;
    }

    public TPRequest getTPRequestBetween(Player from, Player to) {
        int key;
        if (this.tpRequests.containsKey(key = getHashCode(from, to, true)) || this.tpRequests.containsKey(key = getHashCode(from, to, false))) {
            return this.tpRequests.get(key);
        }
        return null;
    }

    public boolean hasTPRequestBetween(Player from, Player to) {
        return this.tpRequests.containsKey(getHashCode(from, to, true)) || this.tpRequests.containsKey(getHashCode(from, to, false));
    }

    public void removeTPRequestBetween(Player from, Player to) {
        this.tpRequests.remove(getHashCode(from, to, true));
        this.tpRequests.remove(getHashCode(from, to, false));

    }

    public void removeTPRequest(Player player) {
        this.tpRequests.values().removeIf(request -> request.getSender() == player || request.getRecipient() == player);
    }

    public boolean ignore(UUID player, UUID toIgnore) {
        String playerId = player.toString();
        String toIgnoreId = toIgnore.toString();
        Map<String, Object> ignores = this.configs.get(this.ignoreConfig, playerId, new HashMap<>());
        boolean add = !ignores.containsKey(toIgnoreId);
        if (add) {
            ignores.put(toIgnoreId, null);
        } else {
            ignores.remove(toIgnoreId);
        }
        this.configs.set(this.ignoreConfig, playerId, ignores);
        return add;
    }

    public boolean isIgnoring(UUID player, UUID target) {
        String playerId = player.toString();
        String targetId = target.toString();

        Map<String, Object> ignores = this.configs.get(this.ignoreConfig, playerId, null);

        return ignores != null && ignores.containsKey(targetId);
    }

    public boolean setHome(Player player, String name, Location pos) {
        return setHome(player.getUniqueId(), name, pos);
    }

    public boolean setHome(IPlayer player, String name, Location pos) {
        return setHome(player.getUniqueId(), name, pos);
    }

    public boolean setHome(UUID uuid, String name, Location location) {

        checkAndUpdateLegacyHomes(uuid);
        Map<String, Object> map = getHomeMap(uuid, true);

        boolean replaced = map.containsKey(name);
        List home = Arrays.asList(location.level.getFolderName(), location.x, location.y, location.z, location.yaw, location.pitch);
        map.put(name, home);
        return replaced;
    }

    public Location getHome(Player player, String name) {
        return getHome(player.getUniqueId(), name);
    }

    public Location getHome(IPlayer player, String name) {
        return getHome(player.getUniqueId(), name);
    }

    public Location getHome(UUID uuid, String name) {
        checkAndUpdateLegacyHomes(uuid);
        @SuppressWarnings("unchecked") Map<String, List<Object>> map = (Map) getHomeMap(uuid, false);
        List<Object> home = map.get(name);
        if (home == null || home.size() != 6) {
            return null;
        }
        return new Location((double) home.get(1), (double) home.get(2), (double) home.get(3), (double) home.get(4), (double) home.get(5), this.getServer().getLevelByName((String) home.get(0)));
    }

    public void removeHome(Player player, String name) {
        removeHome(player.getUniqueId(), name);
    }

    public void removeHome(IPlayer player, String name) {
        removeHome(player.getUniqueId(), name);
    }

    public void removeHome(UUID uuid, String name) {
        checkAndUpdateLegacyHomes(uuid);
        getHomeMap(uuid, true).remove(name);
    }

    public String[] getHomesList(Player player) {
        return getHomesList(player.getUniqueId());
    }

    public String[] getHomesList(IPlayer player) {
        return getHomesList(player.getUniqueId());
    }

    public String[] getHomesList(UUID uuid) {
        checkAndUpdateLegacyHomes(uuid);
        String[] list = getHomeMap(uuid, false).keySet().toArray(new String[0]);
        Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    public boolean isHomeExists(Player player, String name) {
        return isHomeExists(player.getUniqueId(), name);
    }

    public boolean isHomeExists(IPlayer player, String name) {
        return isHomeExists(player.getUniqueId(), name);
    }

    public boolean isHomeExists(UUID uuid, String name) {
        checkAndUpdateLegacyHomes(uuid);
        return getHomeMap(uuid, false).containsKey(name);
    }

    private Map<String, Object> getHomeMap(UUID uuid, boolean set) {
        ConfigSection section = this.configs.get(this.homeConfig, uuid.toString(), new ConfigSection());
        if (set) {
            this.configs.set(this.homeConfig, uuid.toString(), section);
        }
        return section;
    }

    private void checkAndUpdateLegacyHomes(UUID uuid) {
        IPlayer player = getServer().getOfflinePlayer(uuid);
        if (player == null) {
            return;
        }
        String uuidString = player.getUniqueId().toString();
        String name = player.getName().toLowerCase();
        if (this.configs.exists(this.homeConfig, name)) {
            if (this.configs.exists(this.homeConfig, uuidString)) {
                ConfigSection newSection = this.configs.get(this.homeConfig, uuidString, new ConfigSection());
                ConfigSection oldSection = this.configs.get(this.homeConfig, name, new ConfigSection());
                oldSection.getSection(name).forEach((s, o) -> {
                    if (!newSection.containsKey(s)) {
                        newSection.put(s, o);
                    }
                });
                this.configs.remove(this.homeConfig, name);
            } else {
                this.configs.set(this.homeConfig, uuidString, this.configs.get(this.homeConfig, name, new ConfigSection()));
            }
        }
    }

    public boolean setWarp(String name, Location pos) {
        boolean replaced = this.configs.exists(this.warpConfig, name);
        List home = Arrays.asList(pos.level.getFolderName(), pos.x, pos.y, pos.z, pos.yaw, pos.pitch);
        this.configs.set(this.warpConfig, name, home);
        return replaced;
    }

    public Location getWarp(String name) {
        List warp = this.configs.get(this.warpConfig, name, Collections.emptyList());
        if (warp.size() != 6) {
            return null;
        }
        return new Location((double) warp.get(1), (double) warp.get(2), (double) warp.get(3), (double) warp.get(4), (double) warp.get(5), this.getServer().getLevelByName((String) warp.get(0)));
    }

    public void removeWarp(String name) {
        this.configs.remove(this.warpConfig, name);
    }

    public String[] getWarpsList() {
        String[] list = this.configs.getKeys(this.warpConfig).toArray(new String[0]);
        Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    public boolean isWarpExists(String name) {
        return this.configs.exists(this.warpConfig, name);
    }

    public Position getStandablePositionAt(Position pos) {
        int x = pos.getFloorX();
        int y = pos.getFloorY() + 1;
        int z = pos.getFloorZ();
        for (; y <= 128; y++) {
            if (!pos.level.getBlock(this.temporalVector.setComponents(x, y, z)).isSolid() && !pos.level.getBlock(this.temporalVector.setComponents(x, y + 1, z)).isSolid()) {
                return new Position(x + 0.5, pos.level.getBlock(this.temporalVector.setComponents(x, y - 1, z)).getBoundingBox().getMaxY(), z + 0.5, pos.level);
            }
        }
        return null;
    }

    public Position getHighestStandablePositionAt(Position pos) {
        int x = pos.getFloorX();
        int z = pos.getFloorZ();
        for (int y = 127; y >= 0; y--) {
            if (pos.level.getBlock(this.temporalVector.setComponents(x, y, z)).isSolid()) {
                return new Position(x + 0.5, pos.level.getBlock(this.temporalVector.setComponents(x, y, z)).getBoundingBox().getMaxY(), z + 0.5, pos.level);
            }
        }
        return null;
    }

    public boolean mute(Player player, int d, int h, int m, int s) {
        return this.mute(player, Duration.ZERO.plusDays(d).plusHours(h).plusMinutes(m).plusSeconds(s));
    }

    public boolean mute(Player player, Duration duration) {
        return mute(player.getUniqueId(), duration);
    }

    //for peace
    public boolean mute(IPlayer player, int d, int h, int m, int s) {
        return this.mute(player, Duration.ZERO.plusDays(d).plusHours(h).plusMinutes(m).plusSeconds(s));
    }

    //for peace too -- lmlstarqaq
    public boolean mute(IPlayer player, Duration duration) {
        return mute(player.getUniqueId(), duration);
    }

    public boolean mute(UUID uuid, int d, int h, int m, int s) {
        return this.mute(uuid, Duration.ZERO.plusDays(d).plusHours(h).plusMinutes(m).plusSeconds(s));
    }

    public boolean mute(UUID uuid, Duration duration) {
        checkAndUpdateLegacyMute(uuid);
        if (duration.isNegative() || duration.isZero()) return false;
        // t>30 => (t!=30 && t>=30) => (t!=30 && t-30>=0) => (t!=30 && !(t-30<0))
        if (duration.toDays() != 30 && !(duration.minus(THIRTY_DAYS).isNegative())) return false; // t>30
        this.configs.set(this.muteConfig, uuid.toString(), Timestamp.valueOf(LocalDateTime.now().plus(duration)).getTime() / 1000);
        return true;
    }

    public Integer getRemainingTimeToUnmute(Player player) {
        return getRemainingTimeToUnmute(player.getUniqueId());
    }

    public Integer getRemainingTimeToUnmute(IPlayer player) {
        return getRemainingTimeToUnmute(player.getUniqueId());
    }

    public Integer getRemainingTimeToUnmute(UUID uuid) {
        checkAndUpdateLegacyMute(uuid);
        Long time = this.configs.get(this.muteConfig, uuid.toString(), 0L);
        return time == null ? null : (int) (time - Timestamp.valueOf(LocalDateTime.now()).getTime() / 1000);
    }

    public boolean isMuted(Player player) {
        return isMuted(player.getUniqueId());
    }

    public boolean isMuted(IPlayer player) {
        return isMuted(player.getUniqueId());
    }

    public boolean isMuted(UUID uuid) {
        checkAndUpdateLegacyMute(uuid);
        Integer time = this.getRemainingTimeToUnmute(uuid);
        if (time == null) return false;
        if (time <= 0) {
            this.unmute(uuid);
            return false;
        }
        return true;
    }

    public String getMuteTimeMessage(int d, int h, int m, int s) {
        return getDurationString(Duration.ZERO.plusDays(d).plusHours(h).plusMinutes(m).plusSeconds(s));
    }

    public String getUnmuteTimeMessage(Player player) {
        Integer time = this.getRemainingTimeToUnmute(player);
        return getDurationString(Duration.ofSeconds(time));
    }

    public String getUnmuteTimeMessage(IPlayer player) {
        Integer time = this.getRemainingTimeToUnmute(player);
        return getDurationString(Duration.ofSeconds(time));
    }

    public void unmute(Player player) {
        unmute(player.getUniqueId());
    }

    public void unmute(IPlayer player) {
        unmute(player.getUniqueId());
    }

    public void unmute(UUID uuid) {
        checkAndUpdateLegacyMute(uuid);
        this.configs.remove(this.muteConfig, uuid.toString());
    }

    /**
     * Returns a map of the last player another player
     * sent a message to.
     *
     * @return a map of the last player another player sent a message to
     */
    public Map<String, String> getLastMessagedPlayers() {
        return lastMessagedPlayers;
    }

    private void checkAndUpdateLegacyMute(UUID uuid) {
        IPlayer player = getServer().getOfflinePlayer(uuid);
        if (player == null) {
            return;
        }
        String uuidString = player.getUniqueId().toString();
        String name = player.getName().toLowerCase();
        if (this.configs.exists(this.muteConfig, name)) {
            if (this.configs.exists(this.muteConfig, uuidString)) {
                ConfigSection newSection = this.configs.get(this.muteConfig, uuidString, new ConfigSection());
                ConfigSection oldSection = this.configs.get(this.muteConfig, name, new ConfigSection());
                oldSection.forEach((s, o) -> {
                    if (!newSection.containsKey(s)) {
                        newSection.put(s, o);
                    }
                });
                this.configs.remove(this.muteConfig, name);
            } else {
                this.configs.set(this.muteConfig, uuidString, this.configs.get(this.muteConfig, name, new ConfigSection()));
            }
        }
    }

    // Scallop: Thanks lmlstarqaq
    // %0 days %1 hours %2 minutes %3 seconds, language localized.
    public String getDurationString(Duration duration) {
        if (duration == null) return "null";
        long d = duration.toDays();
        long h = duration.toHours() % 24;
        long m = duration.toMinutes() % 60;
        long s = duration.getSeconds() % 60;
        String d1 = "", h1 = "", m1 = "", s1 = "";
        //Singulars and plurals. Maybe necessary for English or other languages. 虽然中文似乎没有名词的单复数 -- lmlstarqaq
        if (d > 1) d1 = Language.translate("commands.generic.days", d);
        else if (d > 0) d1 = Language.translate("commands.generic.day", d);
        if (h > 1) h1 = Language.translate("commands.generic.hours", h);
        else if (h > 0) h1 = Language.translate("commands.generic.hour", h);
        if (m > 1) m1 = Language.translate("commands.generic.minutes", m);
        else if (m > 0) m1 = Language.translate("commands.generic.minute", m);
        if (s > 1) s1 = Language.translate("commands.generic.seconds", s);
        else if (s > 0) s1 = Language.translate("commands.generic.second", s);
        //In some languages, times are read from SECONDS to HOURS, which should be noticed.
        return Language.translate("commands.generic.time.format", d1, h1, m1, s1).trim().replaceAll(" +", " ");
    }

    private class TeleportationExpireTask implements Runnable {

        @Override
        public void run() {
            Iterator<TPRequest> requestIterator = EssentialsAPI.this.tpRequests.values().iterator();

            long currentTime = System.currentTimeMillis();

            while (requestIterator.hasNext()) {
                TPRequest request = requestIterator.next();

                long expirationTime = request.getRequestTime() + TP_EXPIRATION;
                if (expirationTime <= currentTime) {
                    requestIterator.remove();
                }
            }
        }
    }
}