/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.check.type;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;

public abstract class PacketCheck
extends Check<Event> {
    public PacketCheck(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public abstract void handle(Event var1);

    public boolean checkClick() {
        return !this.data.isPlacing() && !this.data.isHasDig() && !this.data.isUsingItem();
    }
}

