/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.api.event.registry;

import me.liwk.karhu.api.event.KarhuEvent;
import me.liwk.karhu.api.event.KarhuListener;

public interface KarhuEventListenerRegistry {
    public boolean fireEvent(KarhuEvent var1);

    public void shutdown();

    public void addListener(KarhuListener var1);

    public void removeListener(KarhuListener var1);
}

