/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.handler.interfaces;

import me.liwk.karhu.util.location.CustomLocation;

public interface ICrashHandler {
    public void handleClientKeepAlive();

    public void handleFlying(boolean var1, boolean var2, CustomLocation var3, CustomLocation var4);

    public void handleArm();

    public void handleWindowClick(int var1, int var2, int var3, int var4);

    public void handleSlot();

    public void handleCustomPayload();

    public void handlePlace();
}

