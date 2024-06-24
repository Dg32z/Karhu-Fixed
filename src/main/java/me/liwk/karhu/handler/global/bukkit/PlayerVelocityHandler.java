/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityExplodeEvent
 *  org.bukkit.event.player.PlayerVelocityEvent
 */
package me.liwk.karhu.handler.global.bukkit;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

public class PlayerVelocityHandler
implements Listener {
    @EventHandler(priority=EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event) {
        if (!event.isCancelled()) {
            for (Entity entity : event.getEntity().getNearbyEntities(4.0, 4.0, 4.0)) {
                KarhuPlayer data;
                if (!(entity instanceof Player) || entity.hasMetadata("NPC") || (data = Karhu.getInstance().getDataManager().getPlayerData(((Player)entity).getUniqueId())) == null) continue;
                data.sendTransaction();
                data.setPlayerExplodeCalled(true);
                data.setBrokenVelocityVerify(true);
                data.setPlayerVelocityCalled(true);
            }
        }
    }

    public native void k();

    public native void s();

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        KarhuPlayer data;
        if (!event.isCancelled() && (data = Karhu.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId())) != null) {
            data.sendTransaction();
            data.setBrokenVelocityVerify(!data.hasSentTickFirst);
            data.setPlayerVelocityCalled(true);
        }
    }
}

