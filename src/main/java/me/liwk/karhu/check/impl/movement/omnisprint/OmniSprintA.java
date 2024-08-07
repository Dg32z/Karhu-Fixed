/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.util.Vector
 */
package me.liwk.karhu.check.impl.movement.omnisprint;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.update.MovementUpdate;
import org.bukkit.util.Vector;

@CheckInfo(name="OmniSprint (A)", category=Category.MOVEMENT, subCategory=SubCategory.SPEED, experimental=true)
public final class OmniSprintA
extends PositionCheck {
    public OmniSprintA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(MovementUpdate e) {
        if (this.data.deltas.deltaXZ > 0.2 && this.data.getVelocityHorizontal() == 0.0 && !this.data.isSpectating() && !this.data.isOnLiquid()) {
            double move = MathUtil.getVectorSpeed(e.getFrom(), e.getTo()).distanceSquared(MathUtil.getDirection(this.data));
            double baseSpeed = (double)PlayerUtil.getBaseSpeedAttribute(this.data, 2.0f) + 0.03;
            double jumpHeight = PlayerUtil.getJumpHeight(this.data);
            if (this.data.deltas.motionY >= jumpHeight - 0.03125) {
                float yawRadians = this.data.getLocation().yaw * (float)Math.PI / 180.0f;
                baseSpeed += Math.hypot(MathHelper.sin(yawRadians) * 0.2f, MathHelper.cos(yawRadians) * 0.2f);
            }
            if (!this.data.isOnGroundPacket()) {
                baseSpeed += 0.05;
            }
            if (this.data.getClientAirTicks() <= 3) {
                baseSpeed += 0.04;
            }
            double p = (baseSpeed += this.data.elapsed(this.data.getLastOnIce()) <= 3 ? 0.8 : 0.0) / move * 100.0;
            double addition = this.data.isSprinting() && this.data.isWasSprinting() ? 1.2 : 0.4;
            float[] keyPair = MathUtil.getStrafeForward(e.from, e.to, this.data);
            Vector moveChange = MathUtil.getMoveChange(e.from, e.to, this.data);
            Vector yawDirection = MathUtil.getDirection(e.to.yaw, 0.0f);
            double angle = MathUtil.angle(yawDirection, moveChange);
            if (!(move > baseSpeed && this.data.deltas.deltaXZ > baseSpeed && keyPair[0] <= 0.0f && angle > 1.1 && this.data.elapsed(this.data.getLastFlyTick()) > 30 && this.data.elapsed(this.data.getLastPistonPush()) > 3 && !this.data.recentlyTeleported(5))) {
                this.violations = Math.max(this.violations - 0.05, 0.0);
            } else {
                this.violations += addition;
                if (violations > 7.0) {
                    this.fail("* Sprinting in impossible direction\n §f* speed §b" + this.format(3, p) + "\n §f* dXZ §b" + this.format(3, this.data.deltas.deltaXZ) + "\n §f* moved §b" + this.format(3, move) + " | " + this.format(3, angle) + "\n §f* sp/lsp §b" + this.data.isSprinting() + " | " + this.data.isWasSprinting() + "\n §f* expected §b" + baseSpeed, this.getMaxvl(), 125L);
                }
            }
        }
    }
}

