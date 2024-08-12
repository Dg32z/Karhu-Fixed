package me.liwk.karhu.util;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class BanData {
    public String license;
    public String karhuVer;
    public String serverVer;
    public double tps;
    public Player playerObj;
    public String player;
    public String type;
    public String client;
    public String sessionTime;
    public String coordinates;
    public long ping;
    public String logLink;
}
