/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.event;

public class VelocityEvent
extends Event {
    private final double x;
    private final double y;
    private final double z;
    private final int eid;

    public VelocityEvent(double x, double y, double z, int eid) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.eid = eid;
    }

    public double getZ() {
        return this.z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getEid() {
        return this.eid;
    }
}

