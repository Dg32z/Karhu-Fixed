/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.util.mc.vec;

public class Vec2f {
    public float x;
    public float y;

    public Vec2f() {
    }

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f(Vec2f v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void set(Vec2f v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static float distanceSq(float x1, float y1, float x2, float y2) {
        float var4 = x1 - x2;
        float var5 = y1 - y2;
        return var4 * var4 + var5 * var5;
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float var4 = x1 - x2;
        float var5 = y1 - y2;
        return (float)Math.sqrt(var4 * var4 + var5 * var5);
    }

    public float distanceSq(float vx, float vy) {
        float var3 = vx - this.x;
        float var4 = vy - this.y;
        return var3 * var3 + var4 * var4;
    }

    public float distanceSq(Vec2f v) {
        float vx = v.x - this.x;
        float vy = v.y - this.y;
        return vx * vx + vy * vy;
    }

    public float distance(float vx, float vy) {
        float var3 = vx - this.x;
        float var4 = vy - this.y;
        return (float)Math.sqrt(var3 * var3 + var4 * var4);
    }

    public float distance(Vec2f v) {
        float vx = v.x - this.x;
        float vy = v.y - this.y;
        return (float)Math.sqrt(vx * vx + vy * vy);
    }

    public int hashCode() {
        int bits = 7;
        bits = 31 * bits + Float.floatToIntBits(this.x);
        return 31 * bits + Float.floatToIntBits(this.y);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Vec2f)) {
            return false;
        }
        Vec2f v = (Vec2f)obj;
        return this.x == v.x && this.y == v.y;
    }

    public String toString() {
        return "Vec2f[" + this.x + ", " + this.y + "]";
    }
}

