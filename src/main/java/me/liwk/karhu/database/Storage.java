package me.liwk.karhu.database;

import me.liwk.karhu.check.api.BanWaveX;
import me.liwk.karhu.check.api.BanX;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.data.KarhuPlayer;

import java.util.List;

public interface Storage {
   void init();

   List<ViolationX> getViolations(String var1, Check var2, int var3, int var4, long var5, long var7);

   void setAlerts(String var1, int var2);

   List<ViolationX> getAllViolations(String var1);

   void addAlert(ViolationX var1);

   void addToBanWave(BanWaveX var1);

   List<String> getBanwaveList();

   boolean getAlerts(String var1);

   List<BanX> getRecentBans();

   boolean isInBanwave(String var1);


   void purge(String var1, boolean var2);

   void addBan(BanX var1);

   void loadActiveViolations(String var1, KarhuPlayer var2);

   int getViolationAmount(String var1);

   void removeFromBanWave(String var1);

   int getAllViolationsInStorage();
}
