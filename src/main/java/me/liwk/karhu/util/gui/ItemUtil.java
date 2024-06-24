/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.SkullMeta
 */
package me.liwk.karhu.util.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Objects;

public class ItemUtil {
    public static ItemStack makeItem(Material mat, int amount, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        meta.setLore(lore);
        item.setAmount(amount);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(Material mat, short damage, int amount, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        meta.setLore(lore);
        item.setAmount(amount);
        item.setItemMeta(meta);
        item.setDurability(damage);
        return item;
    }

    public static ItemStack makeItem(Material mat, int amount, String displayName) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        item.setAmount(amount);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeSkullItem(String target, int amount, String displayName, boolean legacy, List<String> lore) {
        ItemStack item = legacy ? new ItemStack(Objects.requireNonNull(Material.getMaterial("SKULL_ITEM")), amount, (short) 3) : new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        if (meta != null) {
            meta.setOwner(target);
        }
        if (meta != null) {
            meta.setDisplayName(displayName);
        }
        if (meta != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(Material mat, int amount) {
        return new ItemStack(mat, amount);
    }

    public static ItemStack makeItem(Material mat) {
        return new ItemStack(mat);
    }
}

