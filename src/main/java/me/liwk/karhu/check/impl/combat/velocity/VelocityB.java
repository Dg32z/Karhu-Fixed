/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.util.Vector
 */
package me.liwk.karhu.check.impl.combat.velocity;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.pair.Pair;
import me.liwk.karhu.util.pair.Pair3;
import me.liwk.karhu.util.pair.VelocityData5;
import me.liwk.karhu.world.nms.NMSValueParser;
import org.bukkit.util.Vector;

import java.util.HashMap;

@CheckInfo(name="Velocity (B)", category=Category.COMBAT, subCategory=SubCategory.VELOCITY, experimental=false)
public final class VelocityB
extends PacketCheck {
    private double kbZ;
    private double kbX;
    private double allowance;
    private int ticks;
    private int attacks;
    private int bruteforcedAttacks;
    private boolean onGround;
    private boolean attack;
    private boolean jump;

    public VelocityB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private void resetState() {
        this.kbX = 0.0;
        this.kbZ = 0.0;
        this.ticks = 0;
    }

    @Override
    public void handle(Event packet) {
        if (packet instanceof FlyingEvent) {
            Vector tickVel = this.data.getTickedVelocity();
            if (tickVel != null) {
                this.kbX = tickVel.getX();
                this.kbZ = tickVel.getZ();
                this.allowance = 0.001;
                if (this.data.getMoveTicks() <= 1) {
                    this.allowance = this.data.offsetMove() + 0.001;
                }
            }
            if (this.canCheckCondition()) {
                if (this.attack) {
                    this.bruteforceAttack();
                }
                float f4 = 0.91f;
                if (this.onGround) {
                    f4 = this.data.getCurrentFriction();
                }
                this.kbX = Math.abs(this.kbX) < this.data.clamp() ? 0.0 : this.kbX;
                double d = this.kbZ = Math.abs(this.kbZ) < this.data.clamp() ? 0.0 : this.kbZ;
                if (!(this.data.isInWeb() || this.data.isWasInWeb() || this.data.isGliding() || this.data.isRiding() || this.data.elapsed(this.data.getLastSneakEdge()) <= 5 || this.data.isPossiblyTeleporting() || this.data.elapsed(this.data.getLastOnClimbable()) <= 5 || this.data.elapsed(this.data.getLastCollidedWithEntity()) <= 8 || this.data.elapsed(this.data.getLastInLiquid()) <= 5 || this.data.elapsed(this.data.getLastOnBoat()) <= 1 || this.data.elapsed(this.data.getLastCollided()) <= 1 || this.data.elapsed(this.data.getLastCollidedGhost()) <= 1)) {
                    VelocityData5<Float, Float, Float, Boolean, Boolean> data = this.computeKeys(this.kbX, this.kbZ);
                    if (data == null) {
                        this.resetState();
                    } else {
                        boolean reversed;
                        double dClientKb = this.data.deltas.deltaXZ;
                        float strafe = data.getA().floatValue();
                        float forward = data.getX().floatValue();
                        float friction = data.getY().floatValue();
                        boolean fastMath = data.getO();
                        boolean thinkJump = data.getP();
                        if (thinkJump) {
                            float radians = this.data.getLocation().getYaw() * ((float)Math.PI / 180);
                            this.kbX -= (double)(MathHelper.sin(radians) * 0.2f);
                            this.kbZ += (double)(MathHelper.cos(radians) * 0.2f);
                        }
                        this.moveFlying(strafe, forward, friction, fastMath);
                        double dKbZ = this.data.deltas.deltaX / this.kbX;
                        double dKbX = this.data.deltas.deltaZ / this.kbZ;
                        double dKb = MathUtil.hypot(this.kbX, this.kbZ);
                        double diff = dKb - dClientKb;
                        double p = dClientKb / dKb * 100.0;
                        double minPtc = 99.99;
                        minPtc -= this.data.getBukkitPlayer().getMaximumNoDamageTicks() < 10 ? 20.0 : 0.0;
                        minPtc -= this.data.isNewerThan8() ? 20.0 : 0.0;
                        double maxVL = this.data.isNewerThan8() ? 8.0 : 5.0;
                        boolean bl = reversed = dKbZ < -0.05 || dKbX < -0.05;
                        if (thinkJump) {
                            float radians = this.data.getLocation().getYaw() * ((float)Math.PI / 180);
                            this.kbX -= (double)(MathHelper.sin(radians) * 0.2f);
                            this.kbZ += (double)(MathHelper.cos(radians) * 0.2f);
                        }
                        if (p < minPtc && Math.abs(diff) > this.allowance || reversed && !this.data.isJumped()) {
                            this.violations = Math.min(15.0, this.violations + Math.abs(1.975 - Math.abs(dClientKb / dKb)));
                            if (this.violations > maxVL) {
                                this.fail("* Horizontal Modification\n §f* approx pct: §b" + this.format(3, p) + "\n §f* client: §b" + this.format(3, dClientKb) + "\n §f* server: §b" + this.format(3, dKb) + "\n §f* jump: §b" + this.data.isJumped() + " | " + thinkJump + "\n §f* tick: §b" + this.ticks + " | " + this.data.getMoveTicks() + "\n §f* attack: §b" + this.attack + " | " + this.data.getLastAttackTick() + " | " + this.attacks + "\n §f* st/fo/fr: §b" + strafe + " | " + forward + " | " + friction + "\n §f* version: §b" + MathUtil.parseVersion(this.data.getClientVersion()) + "\n §f* reverse: §b" + reversed + " | " + this.format(3, dKbX) + " | " + this.format(3, dKbZ), this.getBanVL(), 60L);
                            }
                            this.debug(String.format("PTC: %.3f, D: %.6f, T: %d, A: %b, R: %b, B: %.2f", p, diff, this.ticks, this.attack, reversed, this.violations));
                            this.resetState();
                        } else {
                            this.violations = Math.max(this.violations - 0.065, 0.0);
                        }
                        this.kbX *= (double)f4;
                        this.kbZ *= (double)f4;
                        if (this.ticks++ >= 8 || this.kbZ == 0.0 && this.kbX == 0.0) {
                            this.resetState();
                        }
                    }
                } else {
                    this.resetState();
                }
            } else {
                this.resetState();
            }
            this.onGround = ((FlyingEvent)packet).isOnGround();
            this.attack = false;
            this.attacks = 0;
            this.bruteforcedAttacks = 0;
        } else if (packet instanceof AttackEvent && ((AttackEvent)packet).isPlayer()) {
            this.attack = true;
            ++this.attacks;
        }
    }

    private void bruteforceAttack() {
        HashMap<Double, Pair3<Double, Double, Integer>> diffs = new HashMap<Double, Pair3<Double, Double, Integer>>();
        double ogX = this.kbX;
        double ogZ = this.kbZ;
        double original = MathUtil.hypot(this.data.deltas.deltaX - this.kbX, this.data.deltas.deltaZ - this.kbZ);
        diffs.put(original, new Pair3<Double, Double, Integer>(this.kbX, this.kbZ, 0));
        int j = 0;
        while (++j <= this.attacks) {
            float friction;
            float forward;
            float strafe;
            Pair<Float, Float> directionAdd;
            double unMovedOgX = ogX *= 0.6;
            double unMovedOgZ = ogZ *= 0.6;
            VelocityData5<Float, Float, Float, Boolean, Boolean> data = this.computeKeys(ogX, ogZ);
            if (data != null && (directionAdd = NMSValueParser.moveFlyingPair2(this.data, strafe = data.getA().floatValue(), forward = data.getX().floatValue(), friction = data.getY().floatValue())) != null) {
                ogX += (double)directionAdd.getX().floatValue();
                ogZ += (double)directionAdd.getY().floatValue();
            }
            double diffMult = MathUtil.hypot(this.data.deltas.deltaX - ogX, this.data.deltas.deltaZ - ogZ);
            diffs.put(diffMult, new Pair3<Double, Double, Integer>(unMovedOgX, unMovedOgZ, j));
            ogX = unMovedOgX;
            ogZ = unMovedOgZ;
        }
        Pair3 pair = (Pair3)diffs.get(diffs.keySet().stream().mapToDouble(d -> d).min().orElse(-420.0));
        if (pair != null) {
            this.kbX = (Double)pair.getX();
            this.kbZ = (Double)pair.getY();
            this.bruteforcedAttacks = (Integer)pair.getZ();
        }
        diffs.clear();
    }

    private void moveFlying(float strafe, float forward, float friction, boolean fastMath) {
        float f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4f) {
            if ((f = MathHelper.sqrt_float(f)) < 1.0f) {
                f = 1.0f;
            }
            f = friction / f;
            float yawRadius = this.data.getLocation().getYaw() * (float)Math.PI / 180.0f;
            float f1 = MathHelper.sin(fastMath, yawRadius);
            float f2 = MathHelper.cos(fastMath, yawRadius);
            this.kbX += (double)((strafe *= f) * f2 - (forward *= f) * f1);
            this.kbZ += (double)(forward * f2 + strafe * f1);
        }
    }

    private VelocityData5<Float, Float, Float, Boolean, Boolean> computeKeys(double x, double z) {
        HashMap<Double, VelocityData5<Float, Float, Float, Boolean, Boolean>> dataAssessments = new HashMap<Double, VelocityData5<Float, Float, Float, Boolean, Boolean>>();
        for (float[] floats : NMSValueParser.KEY_COMBOS) {
            for (boolean using : BOOLEANS) {
                for (boolean sprinting : BOOLEANS) {
                    for (boolean sneaking : BOOLEANS_REVERSED) {
                        for (boolean jump : BOOLEANS_REVERSED) {
                            float strafe = floats[0];
                            float forward = floats[1];
                            float friction = this.moveEntityWithHeading(sprinting).getY().floatValue();
                            if (sneaking) {
                                strafe = (float)((double)strafe * 0.3);
                                forward = (float)((double)forward * 0.3);
                            }
                            if (using) {
                                strafe *= 0.2f;
                                forward *= 0.2f;
                            }
                            boolean didJump = false;
                            if (jump && sprinting && this.onGround) {
                                float radians = this.data.getLocation().getYaw() * ((float)Math.PI / 180);
                                this.kbX -= (double)(MathHelper.sin(radians) * 0.2f);
                                this.kbZ += (double)(MathHelper.cos(radians) * 0.2f);
                                didJump = true;
                            }
                            this.moveFlying(strafe *= 0.98f, forward *= 0.98f, friction, false);
                            double deltaX = this.data.deltas.deltaX - this.kbX;
                            double deltaZ = this.data.deltas.deltaZ - this.kbZ;
                            double offsetH = MathUtil.hypot(deltaX, deltaZ);
                            dataAssessments.put(offsetH, new VelocityData5<Float, Float, Float, Boolean, Boolean>(Float.valueOf(strafe), Float.valueOf(forward), Float.valueOf(friction), false, didJump));
                            this.kbX = x;
                            this.kbZ = z;
                        }
                    }
                }
            }
        }
        double closest = dataAssessments.keySet().stream().mapToDouble(d -> d).min().orElse(3865386.0);
        VelocityData5 result = (VelocityData5)dataAssessments.get(closest);
        dataAssessments.clear();
        return result;
    }

    private boolean canCheckCondition() {
        return this.kbX * this.kbX + this.kbZ * this.kbZ > this.data.offsetMove() + 0.001 && this.data.elapsed(this.data.getLastFlyTick()) > 30;
    }

    private Pair<Float, Float> moveEntityWithHeading(boolean sprint) {
        float f4 = 0.91f;
        float f5 = this.data.getWalkSpeed();
        if (this.onGround) {
            f4 = this.data.getCurrentFriction();
            float f = 0.16277136f / (f4 * f4 * f4);
            if (sprint) {
                f5 += f5 * 0.3f;
            }
            f5 *= f;
        } else {
            f5 = sprint ? 0.025999999f : 0.02f;
        }
        return new Pair<Float, Float>(Float.valueOf(f4), Float.valueOf(f5));
    }
}

