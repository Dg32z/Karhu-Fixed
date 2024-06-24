/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.file.FileConfiguration
 */
package me.liwk.karhu.api.check;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.impl.combat.aimassist.*;
import me.liwk.karhu.check.impl.combat.aimassist.analysis.*;
import me.liwk.karhu.check.impl.combat.autoclicker.*;
import me.liwk.karhu.check.impl.combat.hitbox.HitboxA;
import me.liwk.karhu.check.impl.combat.killaura.*;
import me.liwk.karhu.check.impl.combat.reach.ReachA;
import me.liwk.karhu.check.impl.combat.velocity.VelocityA;
import me.liwk.karhu.check.impl.combat.velocity.VelocityB;
import me.liwk.karhu.check.impl.movement.fly.*;
import me.liwk.karhu.check.impl.movement.inventory.InventoryA;
import me.liwk.karhu.check.impl.movement.inventory.InventoryB;
import me.liwk.karhu.check.impl.movement.motion.*;
import me.liwk.karhu.check.impl.movement.omnisprint.OmniSprintA;
import me.liwk.karhu.check.impl.movement.speed.SpeedA;
import me.liwk.karhu.check.impl.movement.speed.SpeedB;
import me.liwk.karhu.check.impl.movement.speed.SpeedC;
import me.liwk.karhu.check.impl.movement.step.StepA;
import me.liwk.karhu.check.impl.movement.vehicle.VehicleFly;
import me.liwk.karhu.check.impl.movement.water.JesusA;
import me.liwk.karhu.check.impl.movement.water.JesusB;
import me.liwk.karhu.check.impl.packet.badpackets.*;
import me.liwk.karhu.check.impl.packet.pingspoof.PingA;
import me.liwk.karhu.check.impl.packet.timer.TimerA;
import me.liwk.karhu.check.impl.packet.timer.TimerB;
import me.liwk.karhu.check.impl.packet.timer.TimerC;
import me.liwk.karhu.check.impl.world.block.*;
import me.liwk.karhu.check.impl.world.ground.GroundA;
import me.liwk.karhu.check.impl.world.ground.GroundB;
import me.liwk.karhu.check.impl.world.ground.GroundC;
import me.liwk.karhu.check.impl.world.scaffold.*;
import me.liwk.karhu.manager.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CheckState {
    private final Set<Class<? extends Check>> checkClasses = new HashSet<Class<? extends Check>>();
    private final Map<String, Boolean> enabledMap = new ConcurrentHashMap<String, Boolean>();
    private final Map<String, Boolean> autobanMap = new ConcurrentHashMap<String, Boolean>();
    private final Map<String, Boolean> banwaveMap = new ConcurrentHashMap<String, Boolean>();
    private final Map<String, Integer> vlMap = new ConcurrentHashMap<String, Integer>();
    private final Map<String, Integer> banwaveVlMap = new ConcurrentHashMap<String, Integer>();
    private final Map<String, Integer> pullbackVlMap = new ConcurrentHashMap<String, Integer>();

    public boolean isEnabled(String name) {
        return this.enabledMap.getOrDefault(name, true);
    }

    public void setEnabled(String name, boolean b) {
        this.enabledMap.put(name, b);
    }

    public Map<String, Boolean> getAutobanMap() {
        return this.autobanMap;
    }

    public Set<Class<? extends Check>> getCheckClasses() {
        return this.checkClasses;
    }

    public Map<String, Integer> getVlMap() {
        return this.vlMap;
    }

    public Map<String, Boolean> getEnabledMap() {
        return this.enabledMap;
    }

    public Set<Class<? extends Check>> loadOrGetClasses() {
        if (this.checkClasses.isEmpty()) {
            this.checkClasses.add(AimAssistA.class);
            this.checkClasses.add(AimAssistB.class);
            this.checkClasses.add(AimAssistC.class);
            this.checkClasses.add(AimAssistD.class);
            this.checkClasses.add(AimAssistE.class);
            this.checkClasses.add(AimAssistF.class);
            this.checkClasses.add(AimAssistG.class);
            this.checkClasses.add(AimAssistH.class);
            this.checkClasses.add(AimAssistI.class);
            this.checkClasses.add(AimAssistJ.class);
            this.checkClasses.add(AimAssistM.class);
            this.checkClasses.add(AimAssistN.class);
            this.checkClasses.add(AnalysisA.class);
            this.checkClasses.add(AnalysisB.class);
            this.checkClasses.add(AnalysisC.class);
            this.checkClasses.add(AnalysisD.class);
            this.checkClasses.add(AnalysisE.class);
            this.checkClasses.add(AnalysisF.class);
            this.checkClasses.add(AutoClickerA.class);
            this.checkClasses.add(AutoClickerB.class);
            this.checkClasses.add(AutoClickerC.class);
            this.checkClasses.add(AutoClickerD.class);
            this.checkClasses.add(AutoClickerE.class);
            this.checkClasses.add(AutoClickerF.class);
            this.checkClasses.add(AutoClickerG.class);
            this.checkClasses.add(AutoClickerH.class);
            this.checkClasses.add(AutoClickerI.class);
            this.checkClasses.add(AutoClickerJ.class);
            this.checkClasses.add(AutoClickerK.class);
            this.checkClasses.add(AutoClickerL.class);
            this.checkClasses.add(AutoClickerP.class);
            this.checkClasses.add(AutoClickerU.class);
            this.checkClasses.add(AutoClickerW.class);
            this.checkClasses.add(ReachA.class);
            this.checkClasses.add(HitboxA.class);
            this.checkClasses.add(VelocityA.class);
            this.checkClasses.add(VelocityB.class);
            this.checkClasses.add(KillauraA.class);
            this.checkClasses.add(KillauraB.class);
            this.checkClasses.add(KillauraC.class);
            this.checkClasses.add(KillauraE.class);
            this.checkClasses.add(KillauraF.class);
            this.checkClasses.add(KillauraG.class);
            this.checkClasses.add(KillauraH.class);
            this.checkClasses.add(KillauraI.class);
            this.checkClasses.add(KillauraJ.class);
            this.checkClasses.add(KillauraK.class);
            this.checkClasses.add(KillauraM.class);
            this.checkClasses.add(KillauraN.class);
            this.checkClasses.add(FlyA.class);
            this.checkClasses.add(FlyB.class);
            this.checkClasses.add(FlyC.class);
            this.checkClasses.add(FlyD.class);
            this.checkClasses.add(FlyE.class);
            this.checkClasses.add(FlyF.class);
            this.checkClasses.add(VehicleFly.class);
            this.checkClasses.add(JesusA.class);
            this.checkClasses.add(JesusB.class);
            this.checkClasses.add(InventoryA.class);
            this.checkClasses.add(InventoryB.class);
            this.checkClasses.add(SpeedA.class);
            this.checkClasses.add(SpeedB.class);
            this.checkClasses.add(SpeedC.class);
            this.checkClasses.add(OmniSprintA.class);
            this.checkClasses.add(MotionA.class);
            this.checkClasses.add(MotionB.class);
            this.checkClasses.add(MotionC.class);
            this.checkClasses.add(MotionD.class);
            this.checkClasses.add(MotionE.class);
            this.checkClasses.add(MotionF.class);
            this.checkClasses.add(MotionI.class);
            this.checkClasses.add(MotionJ.class);
            this.checkClasses.add(StepA.class);
            this.checkClasses.add(TimerA.class);
            this.checkClasses.add(TimerB.class);
            this.checkClasses.add(TimerC.class);
            this.checkClasses.add(BadPacketsA.class);
            this.checkClasses.add(BadPacketsB.class);
            this.checkClasses.add(BadPacketsC.class);
            this.checkClasses.add(BadPacketsD.class);
            this.checkClasses.add(BadPacketsE.class);
            this.checkClasses.add(BadPacketsF.class);
            this.checkClasses.add(BadPacketsG.class);
            this.checkClasses.add(BadPacketsH.class);
            this.checkClasses.add(BadPacketsI.class);
            this.checkClasses.add(BadPacketsJ.class);
            this.checkClasses.add(BadPacketsK.class);
            this.checkClasses.add(BadPacketsM.class);
            this.checkClasses.add(BadPacketsN.class);
            this.checkClasses.add(BadPacketsO.class);
            this.checkClasses.add(BadPacketsP.class);
            this.checkClasses.add(BadPacketsQ.class);
            this.checkClasses.add(BadPacketsR.class);
            this.checkClasses.add(PingA.class);
            this.checkClasses.add(ScaffoldA.class);
            this.checkClasses.add(ScaffoldB.class);
            this.checkClasses.add(ScaffoldC.class);
            this.checkClasses.add(ScaffoldD.class);
            this.checkClasses.add(ScaffoldE.class);
            this.checkClasses.add(ScaffoldF.class);
            this.checkClasses.add(ScaffoldG.class);
            this.checkClasses.add(ScaffoldH.class);
            this.checkClasses.add(ScaffoldI.class);
            this.checkClasses.add(ScaffoldJ.class);
            this.checkClasses.add(ScaffoldK.class);
            this.checkClasses.add(ScaffoldL.class);
            this.checkClasses.add(ScaffoldM.class);
            this.checkClasses.add(ScaffoldN.class);
            this.checkClasses.add(ScaffoldO.class);
            this.checkClasses.add(ScaffoldP.class);
            this.checkClasses.add(ScaffoldQ.class);
            this.checkClasses.add(ScaffoldR.class);
            this.checkClasses.add(FastBreakA.class);
            this.checkClasses.add(FastBreakB.class);
            this.checkClasses.add(FastBreakC.class);
            this.checkClasses.add(GhostBreak.class);
            this.checkClasses.add(BlockReach.class);
            this.checkClasses.add(NoLookBreak.class);
            this.checkClasses.add(GroundA.class);
            this.checkClasses.add(GroundB.class);
            this.checkClasses.add(GroundC.class);
        }
        return this.checkClasses;
    }

    public void initConfig(FileConfiguration checkConfiguration) {
        for (Class<? extends Check> chs : this.checkClasses) {
            CheckInfo annotation = chs.getAnnotation(CheckInfo.class);
            if (annotation.silent()) continue;
            String name = annotation.name();
            String category = annotation.category().name();
            boolean exp = annotation.experimental();
            String[] idk = name.contains(" ") ? name.split(" ") : new String[]{name, "(A)"};
            String realTypeName = idk[0];
            String typeChars = idk[1].replaceAll("[^a-zA-Z0-9]", "");
            if (!checkConfiguration.isSet(category + "." + realTypeName + "." + typeChars + ".enabled")) {
                if (exp) {
                    checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".enabled", (Object)false);
                } else {
                    checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".enabled", (Object)true);
                }
            }
            if (!checkConfiguration.isSet(category + "." + realTypeName + "." + typeChars + ".autoban")) {
                if (exp) {
                    checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".autoban", (Object)false);
                } else {
                    checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".autoban", (Object)true);
                }
            }
            if (!checkConfiguration.isSet(category + "." + realTypeName + "." + typeChars + ".punish-vl")) {
                if (exp) {
                    checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".punish-vl", (Object)30);
                } else {
                    checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".punish-vl", (Object)20);
                }
            }
            if (!checkConfiguration.isSet(category + "." + realTypeName + "." + typeChars + ".mode")) {
                if (exp) {
                    checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".mode", (Object)"KICK");
                } else {
                    checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".mode", (Object)"BAN");
                }
            }
            if (!checkConfiguration.isSet(category + "." + realTypeName + "." + typeChars + ".banwave")) {
                checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".banwave", (Object)false);
            }
            if (!checkConfiguration.isSet(category + "." + realTypeName + "." + typeChars + ".banwave-vl")) {
                checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".banwave-vl", (Object)10);
            }
            int banVL = checkConfiguration.getInt(category + "." + realTypeName + "." + typeChars + ".punish-vl");
            int banwaveVL = checkConfiguration.getInt(category + "." + realTypeName + "." + typeChars + ".banwave-vl");
            boolean enabled = checkConfiguration.getBoolean(category + "." + realTypeName + "." + typeChars + ".enabled");
            boolean autoban = checkConfiguration.getBoolean(category + "." + realTypeName + "." + typeChars + ".autoban");
            boolean banwave = checkConfiguration.getBoolean(category + "." + realTypeName + "." + typeChars + ".banwave");
            this.enabledMap.put(name, enabled);
            this.autobanMap.put(name, autoban);
            this.banwaveMap.put(name, banwave);
            this.vlMap.put(name, banVL);
            this.banwaveVlMap.put(name, banwaveVL);
        }
    }

    public boolean isBanning(Check check) {
        CheckInfo annotation = check.getCheckInfo();
        String name = annotation.name();
        String category = annotation.category().name();
        String[] idk = name.contains(" ") ? name.split(" ") : new String[]{name, "(A)"};
        String realTypeName = idk[0];
        String typeChars = idk[1].replaceAll("[^a-zA-Z0-9]", "");
        String mode = Karhu.getInstance().getConfigManager().getChecks().getString(category + "." + realTypeName + "." + typeChars + ".mode");
        return mode != null && mode.equalsIgnoreCase("BAN");
    }

    public boolean isBanwave(String name) {
        return this.banwaveMap.getOrDefault(name, false);
    }

    public Map<String, Boolean> getBanwaveMap() {
        return this.banwaveMap;
    }

    public Map<String, Integer> getPullbackVlMap() {
        return this.pullbackVlMap;
    }

    public int getCheckVl(String name) {
        return this.vlMap.getOrDefault(name, 20);
    }

    public void updateChecks() {
        ConfigManager checkConfig = Karhu.getInstance().getConfigManager();
        FileConfiguration checkConfiguration = checkConfig.getChecks();
        for (Class<? extends Check> chs : this.checkClasses) {
            CheckInfo annotation = chs.getAnnotation(CheckInfo.class);
            String name = annotation.name();
            String category = annotation.category().name();
            String[] idk = name.contains(" ") ? name.split(" ") : new String[]{name, "(A)"};
            String realTypeName = idk[0];
            String typeChars = idk[1].replaceAll("[^a-zA-Z0-9]", "");
            boolean enabled = checkConfiguration.getBoolean(category + "." + realTypeName + "." + typeChars + ".enabled", true);
            boolean autoban = checkConfiguration.getBoolean(category + "." + realTypeName + "." + typeChars + ".autoban", true);
            boolean banwave = checkConfiguration.getBoolean(category + "." + realTypeName + "." + typeChars + ".banwave", false);
            this.enabledMap.put(name, enabled);
            this.autobanMap.put(name, autoban);
            this.banwaveMap.put(name, banwave);
            this.updateVls(name, category);
        }
    }

    public void setPunishVl(String name, int amount) {
        this.vlMap.put(name, amount);
    }

    public boolean isAutoban(String name) {
        return this.autobanMap.getOrDefault(name, true);
    }

    public void updateVls(String name, String category) {
        String[] idk = name.contains(" ") ? name.split(" ") : new String[]{name, "(A)"};
        String realTypeName = idk[0];
        String typeChars = idk[1].replaceAll("[^a-zA-Z0-9]", "");
        if (!Karhu.getInstance().getConfigManager().getChecks().isSet(category + "." + realTypeName + "." + typeChars + ".punish-vl")) {
            Karhu.getInstance().getConfigManager().getChecks().set(category + "." + realTypeName + "." + typeChars + ".punish-vl", (Object)20);
        }
        if (!Karhu.getInstance().getConfigManager().getChecks().isSet(category + "." + realTypeName + "." + typeChars + ".banwave-vl")) {
            Karhu.getInstance().getConfigManager().getChecks().set(category + "." + realTypeName + "." + typeChars + ".banwave-vl", (Object)10);
        }
        int banVL = Karhu.getInstance().getConfigManager().getChecks().getInt(category + "." + realTypeName + "." + typeChars + ".punish-vl");
        int banwaveVL = Karhu.getInstance().getConfigManager().getChecks().getInt(category + "." + realTypeName + "." + typeChars + ".banwave-vl");
        this.vlMap.put(name, banVL);
        this.banwaveVlMap.put(name, banwaveVL);
    }

    public Map<String, Integer> getBanwaveVlMap() {
        return this.banwaveVlMap;
    }

    public void setAutoban(String name, boolean b) {
        this.autobanMap.put(name, b);
    }

    public int getBanwaveVl(String name) {
        return this.banwaveVlMap.getOrDefault(name, 10);
    }
}

