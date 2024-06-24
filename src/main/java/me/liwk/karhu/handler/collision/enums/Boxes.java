/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.handler.collision.enums;

public enum Boxes {
    BOAT(0.6f, 1.5f),
    PLAYER(1.8f, 0.6f),
    CROUCH(1.5f, 0.6f);

    private final float height;
    private final float width;

    private Boxes(float h, float w) {
        this.width = w / 2.0f;
        this.height = h;
    }

    public float getHeight() {
        return this.height;
    }

    public float getWidth() {
        return this.width;
    }
}

