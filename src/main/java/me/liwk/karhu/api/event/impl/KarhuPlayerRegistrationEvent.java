/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package me.liwk.karhu.api.event.impl;

import me.liwk.karhu.api.event.KarhuEvent;
import org.bukkit.entity.Player;

public final class KarhuPlayerRegistrationEvent
extends KarhuEvent {
    private final Player player;

    @Override
    public boolean isCancellable() {
        return false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public KarhuPlayerRegistrationEvent(Player player) {
        this.player = player;
    }
}

