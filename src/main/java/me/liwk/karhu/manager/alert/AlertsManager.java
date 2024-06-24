/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package me.liwk.karhu.manager.alert;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.entity.Player;

import java.util.*;

public final class AlertsManager {
    private final Set<UUID> debugToggled = Collections.synchronizedSet(new HashSet());
    private final Set<UUID> miscDebugToggled = Collections.synchronizedSet(new HashSet());
    private final Set<UUID> alertsToggled = Collections.synchronizedSet(new HashSet());
    private final Set<UUID> setbackToggled = Collections.synchronizedSet(new HashSet());
    private final Set<UUID> mitigationToggled = Collections.synchronizedSet(new HashSet());
    public static final List<UUID> ADMINS = Arrays.asList(UUID.fromString("bd290221-672c-49ef-a653-c8c7f6d4834c"), UUID.fromString("7595bacf-5daf-441c-bd52-92958d527155"), UUID.fromString("aa93f311-db03-4d43-9074-c5082571cb93"), UUID.fromString("4b7c9c57-b138-4045-b7ff-bed33b05ee0e"), UUID.fromString("5754ab81-482c-41b1-b4b6-c6359c23f4bb"));

    public Set<UUID> getAlertsToggled() {
        return this.alertsToggled;
    }

    public Set<UUID> getDebugToggled() {
        return this.debugToggled;
    }

    public boolean hasDebugToggled(Player player) {
        return this.debugToggled.contains(player.getUniqueId());
    }

    public boolean hasAlertsToggled(UUID uuid) {
        return this.alertsToggled.contains(uuid);
    }

    public void setReceiveAlerts(Player player, boolean state) {
        if (!state) {
            this.alertsToggled.remove(player.getUniqueId());
            if (Karhu.getInstance().getConfigManager().isCrackedServer()) {
                Tasker.taskAsync(() -> Karhu.getStorage().setAlerts(player.getName(), 0));
            } else {
                Tasker.taskAsync(() -> Karhu.getStorage().setAlerts(player.getUniqueId().toString(), 0));
            }
        } else {
            this.alertsToggled.add(player.getUniqueId());
            if (Karhu.getInstance().getConfigManager().isCrackedServer()) {
                Tasker.taskAsync(() -> Karhu.getStorage().setAlerts(player.getName(), 1));
            } else {
                Tasker.taskAsync(() -> Karhu.getStorage().setAlerts(player.getUniqueId().toString(), 1));
            }
        }
    }

    public void removeFromList(UUID uuid) {
        this.alertsToggled.remove(uuid);
    }

    public void toggleMiscDebug(Player player) {
        if (!this.miscDebugToggled.remove(player.getUniqueId())) {
            this.miscDebugToggled.add(player.getUniqueId());
        }
    }

    public void toggleMitigation(Player player) {
        if (!this.mitigationToggled.remove(player.getUniqueId())) {
            this.mitigationToggled.add(player.getUniqueId());
        }
    }

    public void toggleDebug(Player player) {
        if (!this.debugToggled.remove(player.getUniqueId())) {
            this.debugToggled.add(player.getUniqueId());
        }
    }

    public void toggleAlerts(Player player) {
        if (!this.alertsToggled.remove(player.getUniqueId())) {
            this.alertsToggled.add(player.getUniqueId());
            if (Karhu.getInstance().getConfigManager().isCrackedServer()) {
                Tasker.taskAsync(() -> Karhu.getStorage().setAlerts(player.getName(), 1));
            } else {
                Tasker.taskAsync(() -> Karhu.getStorage().setAlerts(player.getUniqueId().toString(), 1));
            }
        } else if (Karhu.getInstance().getConfigManager().isCrackedServer()) {
            Tasker.taskAsync(() -> Karhu.getStorage().setAlerts(player.getName(), 0));
        } else {
            Tasker.taskAsync(() -> Karhu.getStorage().setAlerts(player.getUniqueId().toString(), 0));
        }
    }

    public void toggleSetback(Player player) {
        if (!this.setbackToggled.remove(player.getUniqueId())) {
            this.setbackToggled.add(player.getUniqueId());
        }
    }

    public Set<UUID> getMitigationToggled() {
        return this.mitigationToggled;
    }

    public boolean hasSetbackToggled(Player player) {
        return this.setbackToggled.contains(player.getUniqueId());
    }

    public boolean hasMiscDebugToggled(Player player) {
        return this.miscDebugToggled.contains(player.getUniqueId());
    }

    public boolean hasMitigationToggled(Player player) {
        return this.mitigationToggled.contains(player.getUniqueId());
    }

    public Set<UUID> getSetbackToggled() {
        return this.setbackToggled;
    }

    public Set<UUID> getMiscDebugToggled() {
        return this.miscDebugToggled;
    }
}

