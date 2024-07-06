/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.chat.BaseComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 */
package me.liwk.karhu.manager.alert;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.discord.Webhook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class MiscellaneousAlertPoster {
    public static void postMisc(String debug, KarhuPlayer data, String type) {
        String message = Karhu.getInstance().getConfigManager().getMiscPrefix() + debug;
        Karhu.getInstance().getAlertsManager().getAlertsToggled().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(staff -> staff.sendMessage(message));
        if (type.contains("Crash")) {
            MiscellaneousAlertPoster.handleDiscord(debug, data);
        }
    }

    public static void postMiscPrivate(String msg) {
        Karhu.getInstance().getAlertsManager().getMiscDebugToggled().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(admin -> admin.sendMessage(msg));
    }

    public static void postMitigation(String msg) {
        Karhu.getInstance().getAlertsManager().getMitigationToggled().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(admin -> admin.sendMessage(msg));
    }

    public static void postSetback(String msg) {
        Karhu.getInstance().getAlertsManager().getSetbackToggled().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(admin -> admin.sendMessage(msg));
    }

    public static void handleDiscord(String data, KarhuPlayer pdata) {
        block5: {
            Player player = pdata.getBukkitPlayer();
            String hookURL = Karhu.getInstance().getConfigManager().getConfig().getString("discord.crash-webhook-url");
            Webhook discord = new Webhook(hookURL);
            boolean showWorld = Karhu.getInstance().getConfigManager().getConfig().getBoolean("discord.show-world");
            boolean showStats = Karhu.getInstance().getConfigManager().getConfig().getBoolean("discord.show-statistics");
            boolean showIcon = Karhu.getInstance().getConfigManager().getConfig().getBoolean("discord.show-icon-thumbnail");
            discord.setUsername(Karhu.getInstance().getConfigManager().getName());
            discord.setTts(false);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            if (showIcon) {
                discord.addEmbed(new Webhook.EmbedObject().setTitle("```" + player.getName() + "``` | Crash ").setThumbnail("https://minotar.net/avatar/" + player.getName() + "/50.png").setDescription(ChatColor.stripColor((String)data.replaceAll("\n", ""))).setColor(Color.ORANGE).addField("Info", (showWorld ? "World: " + player.getWorld().getName() + " | Coords: " + MiscellaneousAlertPoster.format(player.getLocation().getX()) + "/" + MiscellaneousAlertPoster.format(player.getLocation().getY()) + "/" + MiscellaneousAlertPoster.format(player.getLocation().getZ()) : " Coords: " + MiscellaneousAlertPoster.format(player.getLocation().getX()) + "/" + MiscellaneousAlertPoster.format(player.getLocation().getY()) + "/" + MiscellaneousAlertPoster.format(player.getLocation().getZ())) + (showStats ? " | TPS: " + Karhu.getInstance().getTPS() + " | Ping: " + pdata.getTransactionPing() + "ms" : ""), false).addField("Date", dtf.format(now), false));
            } else {
                discord.addEmbed(new Webhook.EmbedObject().setTitle("```" + player.getName() + "``` | Crash ").setDescription(ChatColor.stripColor((String)data.replaceAll("\n", ""))).setColor(Color.ORANGE).addField("Info", (showWorld ? "World: " + player.getWorld().getName() + " | Coords: " + MiscellaneousAlertPoster.format(player.getLocation().getX()) + "/" + MiscellaneousAlertPoster.format(player.getLocation().getY()) + "/" + MiscellaneousAlertPoster.format(player.getLocation().getZ()) : " Coords: " + MiscellaneousAlertPoster.format(player.getLocation().getX()) + "/" + MiscellaneousAlertPoster.format(player.getLocation().getY()) + "/" + MiscellaneousAlertPoster.format(player.getLocation().getZ())) + (showStats ? " | TPS: " + Karhu.getInstance().getTPS() + " | Ping: " + pdata.getTransactionPing() + "ms" : ""), false).addField("Date", dtf.format(now), false));
            }
            try {
                discord.execute();
            }
            catch (IOException var11) {
                if (var11.toString().contains("429")) {
                    Karhu.getInstance().getPlug().getLogger().warning("Unable to post discord webhook: 429 Too many requests");
                }
                if (var11.getMessage().contains("no protocol")) break block5;
                Karhu.getInstance().getPlug().getLogger().warning("Unable to post discord webhook: " + var11.getMessage());
            }
        }
    }

    private static String format(Object obj) {
        return String.format("%." + 1 + "f", obj);
    }
}

