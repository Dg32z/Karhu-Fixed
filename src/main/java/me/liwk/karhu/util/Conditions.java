/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.util;

public final class Conditions {
    public static void notNull(Object o, String msg) {
        if (o == null) {
            throw new IllegalArgumentException(msg);
        }
    }
}

