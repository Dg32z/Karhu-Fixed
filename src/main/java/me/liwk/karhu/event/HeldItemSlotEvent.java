/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.event;

import lombok.Getter;

@Getter
public class HeldItemSlotEvent
extends Event {
    private final int slot;

    public HeldItemSlotEvent(int slot) {
        this.slot = slot;
    }

}

