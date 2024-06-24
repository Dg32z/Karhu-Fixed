/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.event;

public class SteerEvent
extends Event {
    private final boolean unmount;

    public SteerEvent(boolean unmount) {
        this.unmount = unmount;
    }

    public boolean isUnmount() {
        return this.unmount;
    }
}

