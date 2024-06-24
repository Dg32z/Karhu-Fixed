/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.ComponentBuilder
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package me.liwk.karhu.command.sub;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.command.CommandAPI;
import me.liwk.karhu.manager.ConfigManager;
import me.liwk.karhu.manager.alert.AlertsManager;
import me.liwk.karhu.util.framework.Command;
import me.liwk.karhu.util.framework.CommandArgs;
import me.liwk.karhu.util.framework.CommandFramework;
import me.liwk.karhu.util.text.TextUtils;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class LogsCommand
extends CommandAPI {
    public LogsCommand(CommandFramework k) {
        super(k);
    }

    private String findUUID(String arg) {
        if (Karhu.getInstance().getConfigManager().isCrackedServer()) {
            Player target = Bukkit.getPlayer((String)arg);
            return target != null ? arg : arg;
        }
        Player target = Bukkit.getPlayer((String)arg);
        return target != null ? target.getUniqueId().toString() : Bukkit.getOfflinePlayer((String)arg).getUniqueId().toString();
    }

    @Command(name="logs", permission="karhu.logs")
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (command.getSender() instanceof Player) {
            ConfigManager cfg = Karhu.getInstance().getConfigManager();
            if (command.getLabel().equalsIgnoreCase("logs") && (!(command.getSender() instanceof Player) || player.hasPermission("karhu.logs"))) {
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)Karhu.getInstance().getPlug(), () -> {
                    if (args.length >= 1) {
                        int page;
                        String uuid = this.findUUID(args[0]);
                        List<ViolationX> vls = Karhu.storage.getViolations(uuid, null, page = args.length == 2 ? Integer.parseInt(args[1]) : 0, 10, -1L, -1L);
                        if (vls.isEmpty()) {
                            player.sendMessage("§cPlayer has no logs!");
                            if (!Karhu.getInstance().getConfigManager().isCrackedServer()) {
                                return;
                            }
                            uuid = Bukkit.getOfflinePlayer((String)args[0]).getName();
                            vls = Karhu.storage.getViolations(uuid, null, page, 10, -1L, -1L);
                            if (vls.isEmpty()) {
                                player.sendMessage("§cPlayer has no logs!");
                                return;
                            }
                        }
                        int maxPages = Karhu.storage.getAllViolations(uuid).size() / 10;
                        player.sendMessage("§7Showing logs of " + cfg.getLogsHighlight() + args[0] + " §7(§a" + page + "§7/§2" + maxPages + "§7)");
                        for (ViolationX v : vls) {
                            TextComponent msg;
                            String textMsg;
                            if (!v.data.contains("PUNISHED")) {
                                textMsg = "§7* " + cfg.getLogsHighlight() + v.type + " §7VL: " + cfg.getLogsHighlight() + TextUtils.format(v.vl, 1) + " §7(§a" + TextUtils.formatMillis(System.currentTimeMillis() - v.time) + " ago§7)";
                                msg = new TextComponent("§7* " + cfg.getLogsHighlight() + v.type + " §7VL: " + cfg.getLogsHighlight() + TextUtils.format(v.vl, 1) + " §7(§a" + TextUtils.formatMillis(System.currentTimeMillis() - v.time) + " ago§7)");
                                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes((char)'&', (String)v.data.replaceAll("§b", Karhu.getInstance().getConfigManager().getAlertHoverMessageHighlight())) + "\n" + cfg.getLogsHighlight() + v.ping + "§7ms, " + cfg.getLogsHighlight() + v.TPS + "TPS").create()));
                                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/karhu teleport " + v.location + " " + v.world));
                                if (!Karhu.getInstance().getConfigManager().getConfig().getBoolean("hoverless-alert") && (player.hasPermission("karhu.hover-debug") || AlertsManager.ADMINS.contains(player.getUniqueId()))) {
                                    if (Karhu.getInstance().getConfigManager().getConfig().getBoolean("spigot-api-alert")) {
                                        player.spigot().sendMessage((BaseComponent)msg);
                                        continue;
                                    }
                                    player.spigot().sendMessage((BaseComponent)msg);
                                    continue;
                                }
                                player.sendMessage(textMsg);
                                continue;
                            }
                            textMsg = "§7* " + cfg.getLogsBan() + v.type + " §7VL: " + cfg.getLogsBan() + TextUtils.format(v.vl, 1) + " §7(" + cfg.getLogsBan() + TextUtils.formatMillis(System.currentTimeMillis() - v.time) + " ago§7)";
                            msg = new TextComponent("§7* " + cfg.getLogsBan() + v.type + " §7VL: " + cfg.getLogsBan() + TextUtils.format(v.vl, 1) + " §7(" + cfg.getLogsBan() + TextUtils.formatMillis(System.currentTimeMillis() - v.time) + " ago§7)");
                            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes((char)'&', (String)v.data.replaceAll("§b", Karhu.getInstance().getConfigManager().getAlertHoverMessageHighlight())) + "\n" + cfg.getLogsHighlight() + v.ping + "§7ms, " + cfg.getLogsHighlight() + v.TPS + "TPS").create()));
                            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/karhu teleport " + v.location + " " + v.world));
                            if (!Karhu.getInstance().getConfigManager().getConfig().getBoolean("hoverless-alert") && (player.hasPermission("karhu.hover-debug") || AlertsManager.ADMINS.contains(player.getUniqueId()))) {
                                if (Karhu.getInstance().getConfigManager().getConfig().getBoolean("spigot-api-alert")) {
                                    player.spigot().sendMessage((BaseComponent)msg);
                                    continue;
                                }
                                player.spigot().sendMessage((BaseComponent)msg);
                                continue;
                            }
                            player.sendMessage(textMsg);
                        }
                    } else {
                        command.getSender().sendMessage("§c/" + Karhu.getInstance().getConfigManager().getName().toLowerCase() + " logs <player> <page>");
                    }
                });
            }
        }
    }
}

