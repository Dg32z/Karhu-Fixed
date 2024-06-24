package me.liwk.karhu.check.api;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.impl.combat.aimassist.*;
import me.liwk.karhu.check.impl.combat.aimassist.analysis.*;
import me.liwk.karhu.check.impl.combat.autoclicker.*;
import me.liwk.karhu.check.impl.combat.hitbox.HitboxA;
import me.liwk.karhu.check.impl.combat.killaura.*;
import me.liwk.karhu.check.impl.combat.reach.ReachA;
import me.liwk.karhu.check.impl.combat.velocity.VelocityA;
import me.liwk.karhu.check.impl.combat.velocity.VelocityB;
import me.liwk.karhu.check.impl.mouse.Mouse;
import me.liwk.karhu.check.impl.mouse.Sensitivity;
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
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.APICaller;
import me.liwk.karhu.util.benchmark.Benchmark;
import me.liwk.karhu.util.benchmark.BenchmarkType;
import me.liwk.karhu.util.benchmark.KarhuBenchmarker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CheckManager {
    private final Check[] checks;
    private final KarhuPlayer kp;
    private final List<RotationCheck> rotationChecks;
    private final List<PositionCheck> positionChecks;
    private final List<PacketCheck> packetChecks;

    public CheckManager(KarhuPlayer karhuPlayer, Karhu karhu) {
        this.kp = karhuPlayer;
        List<Check> c = Arrays.asList(
                new AutoClickerA(karhuPlayer, karhu),
                new AutoClickerB(karhuPlayer, karhu),
                new AutoClickerC(karhuPlayer, karhu),
                new AutoClickerD(karhuPlayer, karhu),
                new AutoClickerE(karhuPlayer, karhu),
                new AutoClickerF(karhuPlayer, karhu),
                new AutoClickerG(karhuPlayer, karhu),
                new AutoClickerH(karhuPlayer, karhu),
                new AutoClickerI(karhuPlayer, karhu),
                new AutoClickerJ(karhuPlayer, karhu),
                new AutoClickerK(karhuPlayer, karhu),
                new AutoClickerL(karhuPlayer, karhu),
                new AutoClickerP(karhuPlayer, karhu),
                new AutoClickerU(karhuPlayer, karhu),
                new AutoClickerW(karhuPlayer, karhu),
                new VelocityA(karhuPlayer, karhu),
                new VelocityB(karhuPlayer, karhu),
                new ReachA(karhuPlayer, karhu),
                new HitboxA(karhuPlayer, karhu),
                new AimAssistA(karhuPlayer, karhu),
                new AimAssistB(karhuPlayer, karhu),
                new AimAssistC(karhuPlayer, karhu),
                new AimAssistD(karhuPlayer, karhu),
                new AimAssistE(karhuPlayer, karhu),
                new AimAssistF(karhuPlayer, karhu),
                new AimAssistG(karhuPlayer, karhu),
                new AimAssistH(karhuPlayer, karhu),
                new AimAssistI(karhuPlayer, karhu),
                new AimAssistJ(karhuPlayer, karhu),
                new AimAssistM(karhuPlayer, karhu),
                new AimAssistN(karhuPlayer, karhu),
                new AnalysisA(karhuPlayer, karhu),
                new AnalysisB(karhuPlayer, karhu),
                new AnalysisC(karhuPlayer, karhu),
                new AnalysisD(karhuPlayer, karhu),
                new AnalysisE(karhuPlayer, karhu),
                new AnalysisF(karhuPlayer, karhu),
                new KillauraA(karhuPlayer, karhu),
                new KillauraB(karhuPlayer, karhu),
                new KillauraC(karhuPlayer, karhu),
                new KillauraE(karhuPlayer, karhu),
                new KillauraF(karhuPlayer, karhu),
                new KillauraG(karhuPlayer, karhu),
                new KillauraH(karhuPlayer, karhu),
                new KillauraI(karhuPlayer, karhu),
                new KillauraJ(karhuPlayer, karhu),
                new KillauraK(karhuPlayer, karhu),
                new KillauraM(karhuPlayer, karhu),
                new KillauraN(karhuPlayer, karhu),
                new FlyA(karhuPlayer, karhu),
                new FlyB(karhuPlayer, karhu),
                new FlyC(karhuPlayer, karhu),
                new FlyD(karhuPlayer, karhu),
                new FlyE(karhuPlayer, karhu),
                new FlyF(karhuPlayer, karhu),
                new VehicleFly(karhuPlayer, karhu),
                new MotionA(karhuPlayer, karhu),
                new MotionB(karhuPlayer, karhu),
                new MotionC(karhuPlayer, karhu),
                new MotionD(karhuPlayer, karhu),
                new MotionE(karhuPlayer, karhu),
                new MotionF(karhuPlayer, karhu),
                new MotionI(karhuPlayer, karhu),
                new MotionJ(karhuPlayer, karhu),
                new StepA(karhuPlayer, karhu),
                new SpeedA(karhuPlayer, karhu),
                new SpeedB(karhuPlayer, karhu),
                new SpeedC(karhuPlayer, karhu),
                new OmniSprintA(karhuPlayer, karhu),
                new JesusA(karhuPlayer, karhu),
                new JesusB(karhuPlayer, karhu),
                new InventoryA(karhuPlayer, karhu),
                new InventoryB(karhuPlayer, karhu),
                new BadPacketsA(karhuPlayer, karhu),
                new BadPacketsB(karhuPlayer, karhu),
                new BadPacketsC(karhuPlayer, karhu),
                new BadPacketsD(karhuPlayer, karhu),
                new BadPacketsE(karhuPlayer, karhu),
                new BadPacketsF(karhuPlayer, karhu),
                new BadPacketsG(karhuPlayer, karhu),
                new BadPacketsH(karhuPlayer, karhu),
                new BadPacketsI(karhuPlayer, karhu),
                new BadPacketsJ(karhuPlayer, karhu),
                new BadPacketsK(karhuPlayer, karhu),
                new BadPacketsM(karhuPlayer, karhu),
                new BadPacketsN(karhuPlayer, karhu),
                new BadPacketsO(karhuPlayer, karhu),
                new BadPacketsQ(karhuPlayer, karhu),
                new BadPacketsR(karhuPlayer, karhu),
                new PingA(karhuPlayer, karhu),
                new TimerA(karhuPlayer, karhu),
                new TimerB(karhuPlayer, karhu),
                new TimerC(karhuPlayer, karhu),
                new ScaffoldA(karhuPlayer, karhu),
                new ScaffoldB(karhuPlayer, karhu),
                new ScaffoldC(karhuPlayer, karhu),
                new ScaffoldD(karhuPlayer, karhu),
                new ScaffoldE(karhuPlayer, karhu),
                new ScaffoldF(karhuPlayer, karhu),
                new ScaffoldG(karhuPlayer, karhu),
                new ScaffoldH(karhuPlayer, karhu),
                new ScaffoldI(karhuPlayer, karhu),
                new ScaffoldJ(karhuPlayer, karhu),
                new ScaffoldK(karhuPlayer, karhu),
                new ScaffoldL(karhuPlayer, karhu),
                new ScaffoldM(karhuPlayer, karhu),
                new ScaffoldN(karhuPlayer, karhu),
                new ScaffoldO(karhuPlayer, karhu),
                new ScaffoldP(karhuPlayer, karhu),
                new ScaffoldQ(karhuPlayer, karhu),
                new ScaffoldR(karhuPlayer, karhu),
                new FastBreakA(karhuPlayer, karhu),
                new FastBreakB(karhuPlayer, karhu),
                new FastBreakC(karhuPlayer, karhu),
                new GhostBreak(karhuPlayer, karhu),
                new BlockReach(karhuPlayer, karhu),
                new NoLookBreak(karhuPlayer, karhu),
                new GroundA(karhuPlayer, karhu),
                new GroundB(karhuPlayer, karhu),
                new GroundC(karhuPlayer, karhu),
                new Sensitivity(karhuPlayer, karhu),
                new Mouse(karhuPlayer, karhu)
        );
        this.checks = c.toArray(new Check[0]);
        this.packetChecks = this.getAllOfType(PacketCheck.class);
        this.positionChecks = this.getAllOfType(PositionCheck.class);
        this.rotationChecks = this.getAllOfType(RotationCheck.class);
    }

    public void runChecks(List<? extends Check>  paskat, Object e, Object packet) {
        long start = System.nanoTime();

        for(Check c : paskat) {
            if (Karhu.getInstance().getCheckState().isEnabled(c.getName()) || c.isSilent()) {
                if (Karhu.isAPIAvailable()) {
                    if (APICaller.callPreCheck(c.getCheckInfo(), c, this.kp.getBukkitPlayer(), packet)) {
                        c.setDidFail(false);
                        c.handle(e);
                        APICaller.callPostCheck(this.kp.getBukkitPlayer(), c.getCheckInfo(), c, packet);
                    }
                } else {
                    c.setDidFail(false);
                    c.handle(e);
                }
            }
        }

        Benchmark profileData = KarhuBenchmarker.getProfileData(BenchmarkType.CHECKS);
        profileData.insertResult(start, System.nanoTime());
    }

    public Check[] getChecks() {
        return this.checks;
    }

    public int checkAmount() {
        return this.checks.length;
    }

    public <T> T getCheck(Class<T> clazz) {
        return (T)Arrays.stream(this.checks).filter(check -> check.getClass() == clazz).findFirst().orElse(null);
    }

    private <T> List<T> getAllOfType(Class<T> clazz) {
        return (List<T>) Arrays.stream(this.checks).filter(clazz::isInstance).collect(Collectors.toList());
    }

    public List<RotationCheck> getRotationChecks() {
        return this.rotationChecks;
    }

    public List<PositionCheck> getPositionChecks() {
        return this.positionChecks;
    }

    public List<PacketCheck> getPacketChecks() {
        return this.packetChecks;
    }
}
