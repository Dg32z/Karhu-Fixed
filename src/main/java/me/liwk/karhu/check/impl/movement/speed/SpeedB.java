/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.util.Vector
 */
package me.liwk.karhu.check.impl.movement.speed;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.update.MovementUpdate;
import me.liwk.karhu.world.nms.NMSValueParser;
import org.bukkit.util.Vector;

@CheckInfo(name="Speed (B)", category=Category.MOVEMENT, subCategory=SubCategory.SPEED, experimental=false)
public final class SpeedB
extends PositionCheck {
    private Vector lastMove = new Vector();
    private double shitZeroPointThree;
    private double holdVelocity;

    public SpeedB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(MovementUpdate e) {
        boolean velocity;
        double deltaX = e.to.x - e.from.x;
        double deltaY = e.to.y - e.from.y;
        double deltaZ = e.to.z - e.from.z;
        boolean bl = velocity = this.data.getTickedVelocity() != null;
        if (!(this.data.deltas.deltaXZ < 0.2) && this.checkValid(velocity)) {
            double threshold;
            float movementSpeed = this.data.getWalkSpeed();
            float movementSpeedSP = movementSpeed + movementSpeed * 0.3f;
            float friction = 0.91f;
            float force = 0.02f;
            float forceSprint = 0.026f;
            Vector move = new Vector(deltaX, 0.0, deltaZ);
            Vector compLastMove = e.fromFrom.ground ? this.lastMove.clone().multiply(this.data.getLastTickFriction()) : this.lastMove.clone().multiply(0.91f);
            Vector plainComp = compLastMove.clone();
            if (this.data.getLastAttackTick() <= 1 && this.data.getLastTarget() != null) {
                compLastMove.multiply(0.6);
            }
            if (e.from.ground && !e.to.ground && deltaY >= 0.0) {
                float f = e.to.yaw * ((float)Math.PI / 180);
                compLastMove.add(new Vector((double)(-MathHelper.sin(f)) * 0.2, 0.0, (double)MathHelper.cos(f) * 0.2));
            }
            if (e.from.ground) {
                friction = this.data.getCurrentFriction();
                force = movementSpeed * 0.16277136f / (friction * friction * friction);
                forceSprint = movementSpeedSP * 0.16277136f / (friction * friction * friction);
            }
            double d = threshold = this.data.getMoveTicks() <= 3 ? 0.0325 : 0.0105;
            if (this.data.deltas.deltaXZ < 0.25 && this.data.getMoveTicks() <= 2 && e.from.ground) {
                threshold += 0.2;
            }
            if (this.data.elapsed(this.data.getLastCollidedWithEntity()) <= 10) {
                threshold += 0.1;
            }
            if (this.data.elapsed(this.data.getLastInBerry()) <= 2) {
                threshold += 0.1;
            }
            if (this.data.isOnHoney()) {
                threshold += 0.15;
            }
            if (this.data.getTeleportManager().teleportTicks <= 2) {
                threshold += 0.3;
            }
            if (this.data.elapsed(this.data.getLastSneakEdge()) <= 3) {
                threshold = this.data.getTickedVelocity() != null ? (threshold += this.data.getVelocityHorizontal() + 0.5) : (threshold += 0.5);
            }
            if (this.data.elapsed(this.data.getLastVelocityTaken()) <= 3 && !e.from.ground) {
                threshold += this.holdVelocity * 2.5 + 0.6;
            }
            double tMult = Math.max(Karhu.getInstance().getConfigManager().getSpeedBMult(), 1.0001);
            if (this.data.deltas.deltaXZ > 0.2) {
                Vector subtracted = move.clone().subtract(compLastMove);
                double bestNormal = Math.min(this.getBest(subtracted, false, forceSprint, true), this.getBest(subtracted, false, force, false));
                double bestBlocking = Math.min(this.getBest(subtracted, true, forceSprint, true), this.getBest(subtracted, true, force, false));
                if (bestNormal > threshold * tMult && bestBlocking > threshold * tMult) {
                    Vector subtractedPlain = move.clone().subtract(plainComp);
                    double bestNormal2 = Math.min(this.getBest(subtractedPlain, false, forceSprint, true), this.getBest(subtractedPlain, false, force, false));
                    double bestBlocking2 = Math.min(this.getBest(subtractedPlain, true, forceSprint, true), this.getBest(subtractedPlain, true, force, false));
                    if (bestNormal2 > threshold * tMult && bestBlocking2 > threshold * tMult) {
                        if (this.data.getMoveTicks() <= 1) {
                            this.shitZeroPointThree += 1.0;
                            if (shitZeroPointThree > 3.0) {
                                this.disallowMove(false);
                                this.decrease(0.005);
                            }
                            this.lastMove = new Vector(deltaX, 0.0, deltaZ);
                        } else {
                            this.shitZeroPointThree = Math.min(0.0, this.shitZeroPointThree - 0.1);
                        }
                        double closest = Math.min(Math.min(bestNormal, bestNormal2), Math.min(bestBlocking, bestBlocking2));
                        double addition = Math.min(7.5, Math.max(1.0, closest * 50.0));
                        if (this.data.elapsed(this.data.getLastFlyTick()) <= 8) {
                            if (this.data.isConfirmingFlying() && !this.data.getBukkitPlayer().getAllowFlight() && this.data.elapsed(this.data.getLastConfirmingState()) > 3) {
                                // empty if block
                            }
                        } else {
                            double d3;
                            int required = bestNormal < 0.06 && this.data.elapsed(this.data.getLastSneakTick()) <= 3 ? 50 : 35;
                            this.violations += addition;
                            if (violations >= (double)required) {
                                this.fail(String.format("* Invalid move vector:\n %.3f %.3f %.2f %.3f %.3f %b %b", bestNormal, bestNormal2, Float.valueOf(forceSprint), threshold, Float.valueOf(friction), e.from.ground, velocity), 300L);
                            } else if (bestNormal > 0.2 && bestBlocking2 > 0.2 && !this.data.isConfirmingVelocity()) {
                                this.disallowMove(false);
                            }
                            this.debug(String.format("%.3f %.3f %.2f %.3f %.3f %b %b", bestNormal, bestNormal2, Float.valueOf(forceSprint), threshold, Float.valueOf(friction), e.from.ground, velocity));
                        }
                    } else {
                        this.decrease(0.1);
                    }
                } else {
                    this.decrease(0.1);
                }
            } else {
                this.decrease(0.01);
            }
            this.lastMove = new Vector(deltaX, 0.0, deltaZ);
            if (this.data.getTickedVelocity() != null) {
                this.holdVelocity = this.data.getVelocityHorizontal();
            }
        } else {
            this.lastMove = new Vector(deltaX, 0.0, deltaZ);
        }
    }

    public Vector moveFlying(float strafe, float forward, boolean blocking, boolean sneaking, float friction) {
        float f;
        if (sneaking) {
            strafe *= 0.3f;
            forward *= 0.3f;
        }
        if (blocking) {
            strafe *= 0.2f;
            forward *= 0.2f;
        }
        if ((f = (strafe *= 0.98f) * strafe + (forward *= 0.98f) * forward) >= 1.0E-4f) {
            if ((f = MathHelper.sqrt_float(f)) < 1.0f) {
                f = 1.0f;
            }
            f = friction / f;
            float f1 = MathHelper.sin(this.data.getLocation().getYaw() * (float)Math.PI / 180.0f);
            float f2 = MathHelper.cos(this.data.getLocation().getYaw() * (float)Math.PI / 180.0f);
            float xAdd = (strafe *= f) * f2 - (forward *= f) * f1;
            float zAdd = forward * f2 + strafe * f1;
            return new Vector(xAdd, 0.0f, zAdd);
        }
        return new Vector(0, 0, 0);
    }

    private boolean checkValid(boolean velocity) {
        return !velocity && !this.data.isSpectating() && !this.data.isPossiblyTeleporting() && this.data.elapsed(this.data.getLastCollided()) > 2 && this.data.elapsed(this.data.getLastCollidedGhost()) > 2 && this.data.elapsed(this.data.getLastGlide()) >= 30 && this.data.elapsed(this.data.getLastRiptide()) >= 30 && this.data.elapsed(this.data.getLastInLiquid()) > 2 && !this.data.isInBed() && !this.data.isLastInBed() && this.data.elapsed(this.data.getLastOnSlime()) > 3 && this.data.elapsed(this.data.getLastOnSoul()) > 3 && this.data.elapsed(this.data.getLastInWeb()) > 3 && !this.data.isOnScaffolding() && this.data.elapsed(this.data.getLastPistonPush()) > 3 && this.data.elapsed(this.data.getLastOnClimbable()) > 3;
    }

    private double getBest(Vector move, boolean blocking, float friction, boolean sprint) {
        double lowestMatch = Double.MAX_VALUE;
        for (float[] floats : NMSValueParser.KEY_COMBOS) {
            for (boolean sneaking : BOOLEANS_REVERSED) {
                float strafe = floats[0];
                float forward = floats[1];
                Vector moveFlying = this.moveFlying(strafe, forward, blocking, sneaking, friction);
                double diffX = Math.abs(move.getX() - moveFlying.getX());
                double diffZ = Math.abs(move.getZ() - moveFlying.getZ());
                double[] diffXZ = new double[]{diffX, diffZ};
                lowestMatch = Math.min(lowestMatch, MathUtil.hypot(diffXZ));
            }
        }
        return lowestMatch;
    }
}

