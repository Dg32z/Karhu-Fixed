/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.check.type;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

public abstract class PositionCheck
extends Check<MovementUpdate> {
    protected static final double JUMP_MOMENTUM = (double)0.42f;
    protected static final double WORLD_GRAVITY = 0.08;
    protected static final double VERTICAL_AIR_FRICTION = (double)0.98f;
    protected static final float JUMP_MOVEMENT_FACTOR = 0.026f;
    protected static final float LAND_MOVEMENT_FACTOR = 0.16277136f;
    protected static final float SPRINT_BOOST = 1.3f;
    protected static final float AIR_FRICTION = 0.91f;
    protected static final double JUMP_BOOST = 0.2;

    public PositionCheck(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(MovementUpdate update) {
    }
}

