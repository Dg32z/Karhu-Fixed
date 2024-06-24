/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package me.liwk.karhu.manager;

import com.github.retrooper.packetevents.protocol.player.User;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager {
    private final Map<UUID, KarhuPlayer> playerDataMap = new ConcurrentHashMap<UUID, KarhuPlayer>();
    private final Karhu karhu;

    public PlayerDataManager(Karhu karhu) {
        this.karhu = karhu;
    }

    public KarhuPlayer add(UUID uuid, long now) {
        return this.playerDataMap.put(uuid, new KarhuPlayer(uuid, this.karhu, now));
    }

    public KarhuPlayer remove(UUID uuid) {
        KarhuPlayer data = this.getPlayerData(uuid);
        if (data != null) {
            data.setRemovingObject(true);
            Karhu.getInstance().getThreadManager().shutdownThread(data);
        }
        return this.playerDataMap.remove(uuid);
    }

    public Map<UUID, KarhuPlayer> getPlayerDataMap() {
        return this.playerDataMap;
    }

    public KarhuPlayer getPlayerData(User user) {
        return this.playerDataMap.get(user.getUUID());
    }

    public KarhuPlayer getPlayerData(Player player) {
        return this.playerDataMap.get(player.getUniqueId());
    }

    public KarhuPlayer getPlayerData(UUID uuid) {
        return this.playerDataMap.get(uuid);
    }
}

