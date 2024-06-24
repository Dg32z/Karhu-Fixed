/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.database;

import me.liwk.karhu.check.api.BanWaveX;
import me.liwk.karhu.check.api.BanX;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.data.KarhuPlayer;

import java.util.List;

public interface Storage {
    public void init();

    public List<ViolationX> getViolations(String var1, Check var2, int var3, int var4, long var5, long var7);

    public void setAlerts(String var1, int var2);

    public List<ViolationX> getAllViolations(String var1);

    public void addAlert(ViolationX var1);

    public void addToBanWave(BanWaveX var1);

    public List<String> getBanwaveList();

    public boolean getAlerts(String var1);

    public List<BanX> getRecentBans();

    public boolean isInBanwave(String var1);

    public void purge(String var1, boolean var2);

    public void addBan(BanX var1);

    public void loadActiveViolations(String var1, KarhuPlayer var2);

    public int getViolationAmount(String var1);

    public void removeFromBanWave(String var1);

    public int getAllViolationsInStorage();
}

