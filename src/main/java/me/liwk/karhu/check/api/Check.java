/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.ComponentBuilder
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.permissions.ServerOperator
 */
package me.liwk.karhu.check.api;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.setback.Setbacks;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.manager.ConfigManager;
import me.liwk.karhu.manager.alert.AlertsManager;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.APICaller;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.bungee.BungeeAPI;
import me.liwk.karhu.util.discord.Webhook;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.player.BlockUtil;
import me.liwk.karhu.util.task.Tasker;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class Check<T> {
    protected final KarhuPlayer data;
    protected final Karhu karhu;
    protected final ConfigManager cfg;
    private int maxvl = 25;
    private int setbacks;
    private String name;
    private String desc;
    private String credits;
    private Category category;
    private SubCategory subCategory;
    private boolean subCheck;
    private boolean silent;
    private boolean experimental;
    private boolean setback = true;
    private CheckInfo checkInfo;
    protected double violations;
    protected double subVl;
    private boolean didFail;
    private long lastFlag;
    private long now;
    private long nowNano;
    private Location flagLocation = null;
    protected static final boolean[] BOOLEANS = new boolean[]{true, false};
    protected static final boolean[] BOOLEANS_REVERSED = new boolean[]{false, true};

    public Check(KarhuPlayer data, Karhu karhu) {
        this.data = data;
        this.karhu = karhu;
        this.cfg = karhu.getConfigManager();
        this.name = this.getCheckInfo().name();
        this.desc = this.getCheckInfo().desc();
        this.credits = this.getCheckInfo().credits();
        this.category = this.getCheckInfo().category();
        this.subCategory = this.getCheckInfo().subCategory();
        this.subCheck = this.getCheckInfo().subCheck();
        this.silent = this.getCheckInfo().silent();
        this.experimental = this.getCheckInfo().experimental();
    }

    public String getName() {
        return this.name;
    }

    protected final String format(int places, Object obj) {
        return String.format("%." + places + "f", obj);
    }

    public void debug(String formatted) {
        String debugForm = "§7[§b" + this.name + "§7] §9" + this.data.getName() + " §f" + formatted;
        this.karhu.getAlertsManager().getDebugToggled().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(admin -> admin.sendMessage(debugForm));
    }

    public void setName(String name) {
        this.name = name;
    }

    public final void fail(String debug, long time) {
        this.fail(debug, this.getBanVL(), time);
    }

    public final void fail(String debug, int maxvl, long time) {
        if (Karhu.getInstance().getConfigManager().isPullback()) {
            this.flagLocation = this.data.getLastLocation().toLocation(this.data.getWorld());
        }
        if (!((long)this.data.getPositionPackets() < Math.min(800L, Karhu.getInstance().getConfigManager().getExemptTicksJoin()) || this.data.getPositionPackets() < 150 && this.data.deltas.deltaXZ > 20.0 && this.data.getTransactionPing() == 0L)) {
            this.didFail = true;
            this.maxvl = maxvl;
            this.now = System.currentTimeMillis();
            this.nowNano = System.nanoTime();
            Player player = this.data.getBukkitPlayer();
            if (!this.karhu.getConfigManager().isBypass() || !player.hasPermission("karhu.bypass")) {
                if (this.karhu.isServerLagging(this.now) || this.karhu.hasRecentlyDropped(1000L)) {
                    if (this.now - this.karhu.lastPerformanceAnnounce > 10000L) {
                        String var19;
                        this.karhu.lastPerformanceAnnounce = this.now;
                        switch (var19 = this.karhu.getConfigManager().getLagWarnDisplay().toUpperCase()) {
                            case "CONSOLE": {
                                String msg = this.karhu.getConfigManager().getLagWarnMsg().replaceAll("%prefix%", this.karhu.getConfigManager().getPrefix()).replaceAll("%player%", player.getName());
                                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)msg));
                                break;
                            }
                            case "CHAT": {
                                Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(staff -> {
                                    String string = this.karhu.getConfigManager().getLagWarnMsg().replaceAll("%prefix%", this.karhu.getConfigManager().getPrefix()).replaceAll("%player%", player.getName());
                                    staff.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)string));
                                });
                            }
                        }
                    }
                } else if (!(this.data.isBanned() || this.subCheck || (this.subCategory == SubCategory.AUTOCLICKER && !this.name.equals("AutoClicker (A)") || this.subCategory == SubCategory.BADPACKETS || this.subCategory == SubCategory.KILLAURA) && this.data.isNewerThan8() && (double)(this.nowNano - this.data.lastFlying) / 1000000.0 > 55.0)) {
                    String locationParsed = this.format(2, this.data.getLocation().getX()) + "," + this.format(2, this.data.getLocation().getY()) + "," + this.format(2, this.data.getLocation().getZ());
                    String worldParsed = this.data.getWorld().getName();
                    int tempviolations = this.data.getViolations(this, time * 1000L) + 1;
                    boolean autoban = Karhu.getInstance().getCheckState().isAutoban(this.name);
                    boolean banwave = Karhu.getInstance().getCheckState().isBanwave(this.name);
                    String cmd = this.karhu.getConfigManager().getAlertClickCommand();
                    String checkNameFormatted = this.experimental ? this.name + this.cfg.getExpIcon() : this.name;
                    checkNameFormatted = !autoban ? checkNameFormatted + this.cfg.getNoPunishIcon() : checkNameFormatted;
                    TextComponent hover = new TextComponent(this.karhu.getConfigManager().getPrefix() + this.karhu.getConfigManager().getAlertMessage().replaceAll("%player%", this.data.getName()).replaceAll("%version%", this.data.getClientVersion().toString().replaceAll("_", ".").replaceAll("v.", "")).replaceAll("%brand%", this.data.getBrand()).replaceAll("%ping%", String.valueOf(this.data.getTransactionPing())).replaceAll("%tps%", String.valueOf(this.karhu.getTPS())).replaceAll("%check%", checkNameFormatted).replaceAll("%experimental%", MathUtil.booleanToString(this.experimental)).replaceAll("%vl%", String.valueOf(tempviolations)));
                    if (cmd != null) {
                        hover.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd.replace("%player%", player.getName())));
                    }
                    String finalDebug = ChatColor.translateAlternateColorCodes((char)'&', (String)this.karhu.getConfigManager().getAlertHoverMessage().replaceAll("%info%", debug.replaceAll("§b", Karhu.getInstance().getConfigManager().getAlertHoverMessageHighlight())).replaceAll("%player%", this.data.getName()).replaceAll("%ping%", String.valueOf(this.data.getTransactionPing())).replaceAll("%world%", player.getWorld().getName()).replaceAll("%ticks%", String.valueOf(this.data.getTotalTicks())).replaceAll("%loc%", locationParsed).replaceAll("%client%", this.data.getCleanBrand()).replaceAll("%check%", checkNameFormatted).replaceAll("%experimental%", MathUtil.booleanToString(this.experimental)).replaceAll("%version%", this.data.getClientVersion().toString().replaceAll("_", ".").replaceAll("v.", "")).replaceAll("%time%", String.valueOf(this.now - this.lastFlag)).replaceAll("%tps%", String.valueOf(this.karhu.getTPS())));
                    hover.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(finalDebug).create()));
                    String alert = this.karhu.getConfigManager().getPrefix() + this.karhu.getConfigManager().getAlertMessage().replaceAll("%player%", player.getName()).replaceAll("%version%", this.data.getClientVersion().toString().replaceAll("_", ".").replaceAll("v.", "")).replaceAll("%brand%", this.data.getCleanBrand()).replaceAll("%client%", this.data.getCleanBrand()).replaceAll("%ping%", String.valueOf(this.data.getTransactionPing())).replaceAll("%tps%", String.valueOf(this.karhu.getTPS())).replaceAll("%check%", checkNameFormatted).replaceAll("%vl%", String.valueOf(tempviolations)).replaceAll("%maxvl%", String.valueOf(maxvl));
                    String databaseUser = Karhu.getInstance().getConfigManager().isCrackedServer() ? player.getName() : player.getUniqueId().toString();
                    boolean autobanDisable = false;
                    if (Karhu.isAPIAvailable()) {
                        if (APICaller.callAlert(this.data.getBukkitPlayer(), this.getCheckInfo(), this, debug, (BaseComponent)hover, tempviolations, maxvl, this.data.getTransactionPing())) {
                            this.data.addViolation(this);
                            int violations = this.data.getViolations(this, time * 1000L);
                            this.data.setCheckVl(violations, this);
                            this.handleAlert(player, debug, alert, (BaseComponent)hover, violations);
                            if (this.karhu.getConfigManager().isPullback() && (this.category == Category.MOVEMENT || this.category == Category.PACKET || this.category == Category.WORLD)) {
                                this.failSilent();
                            }
                            if (violations == this.getBanwaveVL() && banwave) {
                                Tasker.taskAsync(() -> Karhu.getInstance().getWaveManager().addToWave(databaseUser, this.name));
                            }
                            if (violations >= maxvl && autoban && this.karhu.getConfigManager().isAutoban() && !this.data.isBanned() && !player.hasPermission("karhu.bypass.ban")) {
                                if (!autobanDisable) {
                                    Karhu.storage.addAlert(new ViolationX(databaseUser, checkNameFormatted, violations, this.now, debug + " [PUNISHED]", locationParsed, worldParsed, this.data.getTransactionPing(), this.karhu.getTPS()));
                                    Karhu.storage.addBan(new BanX(databaseUser, checkNameFormatted, this.now, debug, this.data.getTransactionPing(), this.karhu.getTPS()));
                                }
                                this.handlePunishment(player);
                                if (this.karhu.getConfigManager().isDiscordAlert() && !autobanDisable && this.karhu.getConfigManager().isSendBans()) {
                                    this.karhu.getDiscordThread().execute(() -> this.handleDiscord(player, this.name, debug, violations, true));
                                }
                            } else {
                                Karhu.storage.addAlert(new ViolationX(databaseUser, checkNameFormatted, violations, this.now, debug, locationParsed, worldParsed, this.data.getTransactionPing(), this.karhu.getTPS()));
                            }
                        }
                    } else {
                        this.data.addViolation(this);
                        int violations = this.data.getViolations(this, time * 1000L);
                        this.data.setCheckVl(violations, this);
                        this.handleAlert(player, debug, alert, (BaseComponent)hover, violations);
                        if (this.karhu.getConfigManager().isPullback() && (this.category == Category.MOVEMENT || this.category == Category.WORLD)) {
                            this.failSilent();
                            if (this.category == Category.MOVEMENT || this.name.equals("Timer (A)")) {
                                this.data.getLocation().setCheats(true);
                            }
                        }
                        if (violations == this.getBanwaveVL() && banwave) {
                            Tasker.taskAsync(() -> Karhu.getInstance().getWaveManager().addToWave(databaseUser, this.name));
                        }
                        if (violations >= maxvl && autoban && this.karhu.getConfigManager().isAutoban() && !this.data.isBanned() && !player.hasPermission("karhu.bypass.ban")) {
                            if (!autobanDisable) {
                                Karhu.storage.addAlert(new ViolationX(databaseUser, checkNameFormatted, violations, this.now, debug + " [PUNISHED]", locationParsed, worldParsed, this.data.getTransactionPing(), this.karhu.getTPS()));
                                Karhu.storage.addBan(new BanX(databaseUser, checkNameFormatted, this.now, debug, this.data.getTransactionPing(), this.karhu.getTPS()));
                            }
                            this.handlePunishment(player);
                            if (this.karhu.getConfigManager().isDiscordAlert() && !autobanDisable && this.karhu.getConfigManager().isSendBans()) {
                                this.karhu.getDiscordThread().execute(() -> this.handleDiscord(player, this.name, debug, violations, true));
                            }
                        } else {
                            Karhu.storage.addAlert(new ViolationX(databaseUser, checkNameFormatted, violations, this.now, debug, locationParsed, worldParsed, this.data.getTransactionPing(), this.karhu.getTPS()));
                        }
                    }
                    if (this.category == Category.MOVEMENT) {
                        this.data.setDidFlagMovement(true);
                        this.data.setLastMovementFlag(this.data.getTotalTicks());
                    }
                }
            }
        }
    }

    public abstract void handle(T var1);

    public String getDesc() {
        return this.desc;
    }

    public KarhuPlayer getData() {
        return this.data;
    }

    public double getViolations() {
        return this.violations;
    }

    public void setViolations(double violations) {
        this.violations = violations;
    }

    public void disallowMove(boolean glide) {
        if (this.data.isInitialized() && this.canSetbackStrict()) {
            this.cancel(glide);
        }
    }

    public void setCheckInfo(CheckInfo checkInfo) {
        this.checkInfo = checkInfo;
    }

    public void setLastFlag(long lastFlag) {
        this.lastFlag = lastFlag;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public int getSetbacks() {
        return this.setbacks;
    }

    public void setNowNano(long nowNano) {
        this.nowNano = nowNano;
    }

    public int getMaxvl() {
        return this.maxvl;
    }

    public void setMaxvl(int maxvl) {
        this.maxvl = maxvl;
    }

    public boolean canSetbackStrict() {
        return BlockUtil.chunkLoaded(this.data.getLastLocation().toLocation(this.data.getWorld())) && BlockUtil.chunkLoaded(this.data.getLastLastLocation().toLocation(this.data.getWorld())) && BlockUtil.chunkLoaded(this.data.getLocation().toLocation(this.data.getWorld())) && !this.data.isPossiblyTeleporting() && !this.data.getBukkitPlayer().isDead() && this.data.getTotalTicks() > 40;
    }

    public long getNowNano() {
        return this.nowNano;
    }

    public String getCredits() {
        return this.credits;
    }

    public void setDidFail(boolean didFail) {
        this.didFail = didFail;
    }

    public void setFlagLocation(Location flagLocation) {
        this.flagLocation = flagLocation;
    }

    public SubCategory getSubCategory() {
        return this.subCategory;
    }

    public Karhu getKarhu() {
        return this.karhu;
    }

    public void setSubCheck(boolean subCheck) {
        this.subCheck = subCheck;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void pullback(CustomLocation location) {
        if (!this.data.isDidFlagMovement()) {
            Player player = this.data.getBukkitPlayer();
            if (location != null) {
                CustomLocation setback = Setbacks.forgeToRotatedLocation(location, this.data);
                if (this.canSetback()) {
                    MiscellaneousAlertPoster.postSetback(this.data.getName() + " | §c" + this.name + " flying desynced");
                    this.data.setDidFlagMovement(true);
                    this.data.setLastMovementFlag(this.data.getTotalTicks());
                    if (Karhu.isAPIAvailable()) {
                        if (APICaller.callPullback(player, this.getCheckInfo(), this, setback.toLocation(this.data.getWorld()))) {
                            Tasker.run(() -> this.data.teleport(setback));
                        }
                    } else {
                        Tasker.run(() -> this.data.teleport(setback));
                    }
                }
            }
        }
    }

    public void pullback(Location location) {
        if (!this.data.isDidFlagMovement()) {
            Player player = this.data.getBukkitPlayer();
            if (location != null) {
                Location setback = Setbacks.forgeToRotatedLocation(location, this.data);
                if (this.canSetback()) {
                    MiscellaneousAlertPoster.postSetback(this.data.getName() + " | §c" + this.name + " flying desynced");
                    this.data.setDidFlagMovement(true);
                    this.data.setLastMovementFlag(this.data.getTotalTicks());
                    if (Karhu.isAPIAvailable()) {
                        if (APICaller.callPullback(player, this.getCheckInfo(), this, setback)) {
                            Tasker.run(() -> this.data.teleport(setback));
                        }
                    } else {
                        Tasker.run(() -> this.data.teleport(setback));
                    }
                }
            }
        }
    }

    public void pullback() {
        if (!this.data.isDidFlagMovement()) {
            Player player = this.data.getBukkitPlayer();
            CustomLocation setback = Setbacks.forgeToRotatedLocation(this.data.getSafeSetback(), this.data);
            if (this.canSetback()) {
                if (Karhu.isAPIAvailable()) {
                    if (APICaller.callPullback(player, this.getCheckInfo(), this, setback.toLocation(this.data.getWorld()))) {
                        Tasker.run(() -> this.data.teleport(setback));
                    }
                } else {
                    Tasker.run(() -> this.data.teleport(setback));
                }
            }
        }
    }

    public ConfigManager getCfg() {
        return this.cfg;
    }

    protected boolean canClick() {
        return !this.data.isHasDig() && !this.data.isPlacing() && !this.data.isUsingItem() && !this.data.isSpectating();
    }

    public Location getFlagLocation() {
        return this.flagLocation;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setSetback(boolean setback) {
        this.setback = setback;
    }

    public double getSubVl() {
        return this.subVl;
    }

    public long getLastFlag() {
        return this.lastFlag;
    }

    public boolean isDidFail() {
        return this.didFail;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public void setExperimental(boolean experimental) {
        this.experimental = experimental;
    }

    public boolean isSubCheck() {
        return this.subCheck;
    }

    protected void decrease(double decrease) {
        this.violations = Math.max(0.0, this.violations - decrease);
    }

    public void setSetbacks(int setbacks) {
        this.setbacks = setbacks;
    }

    public boolean canSetback() {
        return BlockUtil.chunkLoaded(this.data.getLastLocation().toLocation(this.data.getWorld())) && BlockUtil.chunkLoaded(this.data.getLastLastLocation().toLocation(this.data.getWorld())) && BlockUtil.chunkLoaded(this.data.getLocation().toLocation(this.data.getWorld())) && !this.data.isPossiblyTeleporting() && !this.data.getBukkitPlayer().isDead() && !this.data.isDidFlagMovement() && this.data.getTotalTicks() > 40;
    }

    public boolean isExperimental() {
        return this.experimental;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public void debugMisc(String formatted) {
        String debugForm = "§7[§b" + this.name + "§7] §9" + this.data.getName() + " §f" + formatted;
        this.karhu.getAlertsManager().getMiscDebugToggled().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(admin -> admin.sendMessage(debugForm));
    }

    public Location fixSetback(Location location) {
        World world = this.data.getWorld();
        Location pushedOut = Setbacks.moveOutOfBlockSafely(location.getX(), location.getZ(), this.data);
        if (pushedOut == null) {
            location = this.data.getLocation().distance(this.data.getLastLastLocation()) <= 5.0 ? this.data.getLastLastLocation().toLocation(world) : this.data.getLastLocation().toLocation(world);
            return Setbacks.forgeToRotatedLocation(location, this.data);
        }
        return Setbacks.forgeToRotatedLocation(pushedOut, this.data);
    }

    public void setSubVl(double subVl) {
        this.subVl = subVl;
    }

    public boolean isSetback() {
        return this.setback;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return this.category;
    }

    public void cancel(boolean glide) {
        if (glide || this.data.elapsed(this.data.getLastMovementFlag()) > 1 || this.data.isPossiblyTeleporting()) {
            Player player = this.data.getBukkitPlayer();
            Location teleport = Setbacks.forgeToRotatedLocation(this.data.getSafeSetback().toLocation(this.data.getWorld()), this.data);
            MiscellaneousAlertPoster.postSetback(this.data.getName() + ChatColor.RED + " " + (glide ? " chunk lag " : this.name + " limit exceeded ") + teleport.toVector() + " tp: " + this.data.getTeleportManager().teleportTicks);
            if (this.canSetbackStrict2(teleport)) {
                if (Karhu.isAPIAvailable()) {
                    if (APICaller.callPullback(player, this.getCheckInfo(), this, teleport)) {
                        Tasker.run(() -> this.data.teleport(teleport));
                    }
                } else {
                    Tasker.run(() -> this.data.teleport(teleport));
                }
                this.data.setDidFlagMovement(true);
                this.data.setLastMovementFlag(this.data.getTotalTicks());
            }
        }
    }

    protected double increase(double increase) {
        this.violations += increase;
        return this.violations;
    }

    public boolean isSilent() {
        return this.silent;
    }

    public final CheckInfo getCheckInfo() {
        return this.checkInfo == null ? this.getClass().getAnnotation(CheckInfo.class) : this.checkInfo;
    }

    public void handleDiscord(Player player, String check, String data, int violations, boolean punish) {
        block16: {
            String serverName;
            String hookURL = this.karhu.getConfigManager().getConfig().getString("discord.alert-webhook-url");
            Webhook discord = new Webhook(hookURL);
            boolean showWorld = this.karhu.getConfigManager().getConfig().getBoolean("discord.show-world");
            boolean showStats = this.karhu.getConfigManager().getConfig().getBoolean("discord.show-statistics");
            boolean showIcon = this.karhu.getConfigManager().getConfig().getBoolean("discord.show-icon-thumbnail");
            boolean sendPunish = this.karhu.getConfigManager().isSendBans();
            boolean sendAlerts = this.karhu.getConfigManager().isSendAlerts();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            discord.setUsername(this.karhu.getConfigManager().getName());
            discord.setTts(false);
            String string = serverName = this.karhu.getConfigManager().getServerName().equalsIgnoreCase("Karhu") ? "Karhu (edit by changing server-name in config.yml)" : this.karhu.getConfigManager().getServerName();
            if (!punish) {
                if (sendAlerts) {
                    if (showIcon) {
                        discord.addEmbed(new Webhook.EmbedObject().setTitle("```" + player.getName() + " " + player.getUniqueId() + "``` | " + check + " (x" + violations + ")").setThumbnail("https://minotar.net/avatar/" + player.getName() + "/50.png").setDescription(ChatColor.stripColor((String)data.replaceAll("\n", " "))).setColor(Color.CYAN).addField("Server: ", serverName, true).addField("Info", (showWorld ? "W: " + player.getWorld().getName() + " | C: " + this.format(1, player.getLocation().getX()) + "/" + this.format(1, player.getLocation().getY()) + "/" + this.format(1, player.getLocation().getZ()) : " C: " + this.format(1, player.getLocation().getX()) + "/" + this.format(1, player.getLocation().getY()) + "/" + this.format(1, player.getLocation().getZ())) + (showStats ? " | TPS: " + Karhu.getInstance().getTPS() + " | Ping: " + this.data.getTransactionPing() + "ms | CL: " + this.data.getCleanBrand() + " | V: " + MathUtil.parseVersion(this.data.getClientVersion()) : " | T: " + this.data.getTotalTicks()), false).addField("Date", dtf.format(now), false));
                    } else {
                        discord.addEmbed(new Webhook.EmbedObject().setTitle("```" + player.getName() + " " + player.getUniqueId() + "``` | " + check + " (x" + violations + ")").setDescription(ChatColor.stripColor((String)data.replaceAll("\n", " "))).setColor(Color.CYAN).addField("Server: ", serverName, true).addField("Info", (showWorld ? "W: " + player.getWorld().getName() + " | C: " + this.format(1, player.getLocation().getX()) + "/" + this.format(1, player.getLocation().getY()) + "/" + this.format(1, player.getLocation().getZ()) : " C: " + this.format(1, player.getLocation().getX()) + "/" + this.format(1, player.getLocation().getY()) + "/" + this.format(1, player.getLocation().getZ())) + (showStats ? " | TPS: " + Karhu.getInstance().getTPS() + " | Ping: " + this.data.getTransactionPing() + "ms | CL: " + this.data.getCleanBrand() + " | V: " + MathUtil.parseVersion(this.data.getClientVersion()) : " | T: " + this.data.getTotalTicks()), false).addField("Date", dtf.format(now), false));
                    }
                    try {
                        discord.execute();
                    }
                    catch (IOException var18) {
                        if (var18.toString().contains("429")) {
                            this.karhu.getPlug().getLogger().warning("Unable to post discord webhook: 429 Too many requests");
                        } else if (!var18.getMessage().contains("no protocol")) {
                            this.karhu.getPlug().getLogger().warning("Unable to post discord webhook: " + var18.getMessage());
                        }
                    }
                }
            } else if (sendPunish) {
                if (showIcon) {
                    discord.addEmbed(new Webhook.EmbedObject().setTitle("```" + player.getName() + " " + player.getUniqueId() + "``` | " + check + " (x" + violations + ")").setThumbnail("https://minotar.net/avatar/" + player.getName() + "/50.png").setDescription(ChatColor.stripColor((String)data.replaceAll("\n", " "))).setColor(Color.RED).addField("Server: ", serverName, true).addField("Info", (showWorld ? "W: " + player.getWorld().getName() + " | C: " + this.format(1, player.getLocation().getX()) + "/" + this.format(1, player.getLocation().getY()) + "/" + this.format(1, player.getLocation().getZ()) : " C: " + this.format(1, player.getLocation().getX()) + "/" + this.format(1, player.getLocation().getY()) + "/" + this.format(1, player.getLocation().getZ())) + (showStats ? " | TPS: " + Karhu.getInstance().getTPS() + " | Ping: " + this.data.getTransactionPing() + "ms | CL: " + this.data.getCleanBrand() + " | V: " + MathUtil.parseVersion(this.data.getClientVersion()) : " | T: " + this.data.getTotalTicks()), false).addField("Date", dtf.format(now), false));
                } else {
                    discord.addEmbed(new Webhook.EmbedObject().setTitle("```" + player.getName() + " " + player.getUniqueId() + "``` | " + check + " (x" + violations + ")").setDescription(ChatColor.stripColor((String)data.replaceAll("\n", " "))).setColor(Color.RED).addField("Server: ", serverName, true).addField("Info", (showWorld ? "W: " + player.getWorld().getName() + " | C: " + this.format(1, player.getLocation().getX()) + "/" + this.format(1, player.getLocation().getY()) + "/" + this.format(1, player.getLocation().getZ()) : " C: " + this.format(1, player.getLocation().getX()) + "/" + this.format(1, player.getLocation().getY()) + "/" + this.format(1, player.getLocation().getZ())) + (showStats ? " | TPS: " + Karhu.getInstance().getTPS() + " | Ping: " + this.data.getTransactionPing() + "ms | CL: " + this.data.getCleanBrand() + " | V: " + MathUtil.parseVersion(this.data.getClientVersion()) : " | T: " + this.data.getTotalTicks()), false).addField("Date", dtf.format(now), false));
                }
                try {
                    discord.execute();
                }
                catch (IOException var17) {
                    if (var17.toString().contains("429")) {
                        this.karhu.getPlug().getLogger().warning("Unable to post discord webhook: 429 Too many requests");
                    }
                    if (var17.getMessage().contains("no protocol")) break block16;
                    this.karhu.getPlug().getLogger().warning("Unable to post discord webhook: " + var17.getMessage());
                }
            }
        }
    }

    private void handlePunishment(Player player) {
        block17: {
            List<String> banCMD;
            long delayTime;
            block16: {
                List<String> banCMD2;
                delayTime = this.karhu.getConfigManager().getCommandDelay();
                if (Karhu.isAPIAvailable()) break block16;
                if (Karhu.getInstance().getConfigManager().isDisallowFlagsAfterPunish()) {
                    this.data.setBanned(true);
                }
                if (this.karhu.getConfigManager().isPunishBroadcast()) {
                    Bukkit.broadcastMessage((String)ChatColor.translateAlternateColorCodes((char)'&', (String)Karhu.getInstance().getConfigManager().getConfig().getString("Punishments.message").replaceAll("%check%", this.name).replaceAll("%player%", this.data.getName())));
                }
                boolean firstLoop = true;
                List<String> list = banCMD2 = Karhu.getInstance().getCheckState().isBanning(this) ? Karhu.getInstance().getConfigManager().getPunishmentsBan() : Karhu.getInstance().getConfigManager().getPunishmentsKick();
                if (!Karhu.getInstance().getConfigManager().isBungeeCommand()) {
                    for (String ban : banCMD2) {
                        if (firstLoop) {
                            Tasker.run(() -> Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), ban.replaceAll("%player%", player.getName()).replaceAll("%check%", this.name)));
                            firstLoop = false;
                            continue;
                        }
                        Tasker.runTaskLater(() -> Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), ban.replaceAll("%player%", player.getName()).replaceAll("%check%", this.name)), 20L * delayTime);
                    }
                } else {
                    for (String ban : banCMD2) {
                        if (firstLoop) {
                            Tasker.run(() -> BungeeAPI.sendCommand(ban.replaceAll("%player%", player.getName()).replaceAll("%check%", this.name)));
                            firstLoop = false;
                            continue;
                        }
                        Tasker.runTaskLater(() -> BungeeAPI.sendCommand(ban.replaceAll("%player%", player.getName()).replaceAll("%check%", this.name)), 20L * delayTime);
                    }
                }
                break block17;
            }
            if (!APICaller.callBan(player, this.getCheckInfo(), this)) break block17;
            if (Karhu.getInstance().getConfigManager().isDisallowFlagsAfterPunish()) {
                this.data.setBanned(true);
            }
            if (this.karhu.getConfigManager().isPunishBroadcast()) {
                Bukkit.broadcastMessage((String)ChatColor.translateAlternateColorCodes((char)'&', (String)Karhu.getInstance().getConfigManager().getConfig().getString("Punishments.message").replaceAll("%check%", this.name).replaceAll("%player%", this.data.getName())));
            }
            boolean firstLoop = true;
            List<String> list = banCMD = Karhu.getInstance().getCheckState().isBanning(this) ? Karhu.getInstance().getConfigManager().getPunishmentsBan() : Karhu.getInstance().getConfigManager().getPunishmentsKick();
            if (!Karhu.getInstance().getConfigManager().isBungeeCommand()) {
                for (String ban : banCMD) {
                    if (firstLoop) {
                        Tasker.run(() -> Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), ban.replaceAll("%player%", player.getName()).replaceAll("%check%", this.name)));
                        firstLoop = false;
                        continue;
                    }
                    Tasker.runTaskLater(() -> Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), ban.replaceAll("%player%", player.getName()).replaceAll("%check%", this.name)), 20L * delayTime);
                }
            } else {
                for (String ban : banCMD) {
                    if (firstLoop) {
                        Tasker.run(() -> BungeeAPI.sendCommand(ban.replaceAll("%player%", player.getName()).replaceAll("%check%", this.name)));
                        firstLoop = false;
                        continue;
                    }
                    Tasker.runTaskLater(() -> BungeeAPI.sendCommand(ban.replaceAll("%player%", player.getName()).replaceAll("%check%", this.name)), 20L * delayTime);
                }
            }
        }
    }

    private void handleAlert(Player player, String debug, String text, BaseComponent hover, int violations) {
        String cmd = this.karhu.getConfigManager().getAlertClickCommand();
        if (cmd != null) {
            int modulo;
            if (this.now - this.lastFlag > Karhu.getInstance().getConfigManager().getAlertDelay()) {
                if (!this.karhu.getConfigManager().isHoverlessAlert()) {
                    if (this.karhu.getConfigManager().isSpigotApiAlert()) {
                        for (UUID uuid : this.karhu.getAlertsManager().getAlertsToggled()) {
                            Player staff = Bukkit.getPlayer((UUID)uuid);
                            if (!this.karhu.getConfigManager().isSpigotApiAlert() || staff == null) continue;
                            if (!staff.hasPermission("karhu.hover-debug") && !AlertsManager.ADMINS.contains(staff.getUniqueId())) {
                                staff.sendMessage(text);
                                continue;
                            }
                            if (this.karhu.getConfigManager().isSpigotApiAlert()) {
                                staff.spigot().sendMessage(hover);
                                continue;
                            }
                            staff.spigot().sendMessage(hover);
                        }
                    }
                } else {
                    this.karhu.getAlertsManager().getAlertsToggled().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(staffx -> staffx.sendMessage(text));
                }
                this.lastFlag = this.now;
            }
            if (this.karhu.getConfigManager().isDiscordAlert() && this.karhu.getConfigManager().isSendAlerts() && violations % (modulo = this.karhu.getConfigManager().getConfig().getInt("discord.post-vl-rate")) == 0) {
                this.karhu.getDiscordThread().execute(() -> this.handleDiscord(player, this.name, debug, violations, false));
            }
            if (this.karhu.getConfigManager().isBungeeAlert() && violations % this.karhu.getConfigManager().getBungeePostRate() == 0) {
                BungeeAPI.sendAlert(this.experimental ? this.name + this.cfg.getExpIcon() : this.name + "#" + violations + "#" + player.getName());
            }
        }
    }

    public int getBanVL() {
        return Karhu.getInstance().getCheckState().getCheckVl(this.name);
    }

    public boolean canSetbackStrict2(Location location) {
        if (BlockUtil.chunkLoaded(location) && location.getBlock().getType().isSolid()) {
            location = this.fixSetback(location.clone());
        }
        if (location.getWorld() != this.data.getWorld()) {
            location = this.fixSetback(this.data.getLocation().toLocation(this.data.getWorld()));
        }
        return BlockUtil.chunkLoaded(location) && !location.getBlock().getType().isSolid() && !this.data.isWasWasInUnloadedChunk() && !this.data.isWasInUnloadedChunk() && !this.data.isInUnloadedChunk() && !this.data.isPossiblyTeleporting() && !this.data.getBukkitPlayer().isDead() && this.data.getTotalTicks() > 40;
    }

    public int getBanwaveVL() {
        return Karhu.getInstance().getCheckState().getBanwaveVl(this.name);
    }

    protected void failSilent() {
        if (this.data.isInitialized()) {
            this.pullback();
        }
    }

    public long getNow() {
        return this.now;
    }
}

