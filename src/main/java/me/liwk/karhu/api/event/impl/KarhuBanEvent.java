/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package me.liwk.karhu.api.event.impl;

import me.liwk.karhu.api.data.CheckData;
import me.liwk.karhu.api.event.KarhuEvent;
import org.bukkit.entity.Player;

public final class KarhuBanEvent
extends KarhuEvent {
    private final Player player;
    private final CheckData check;

    public Player getPlayer() {
        return this.player;
    }

    public CheckData getCheck() {
        return this.check;
    }

    public KarhuBanEvent(Player player, CheckData check) {
        this.player = player;
        this.check = check;
    }
}

