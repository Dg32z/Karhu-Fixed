/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.check.type;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

public abstract class RotationCheck
extends Check<MovementUpdate> {
    public RotationCheck(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(MovementUpdate update) {
    }
}

