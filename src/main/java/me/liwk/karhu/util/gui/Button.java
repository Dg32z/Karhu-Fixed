/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 */
package me.liwk.karhu.util.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Button {
    public int pos;
    public Inventory inv;
    public ItemStack item;
    public int page;

    @Deprecated
    public Button(Inventory inv, int pos, ItemStack item) {
        this.inv = inv;
        this.pos = pos;
        this.item = item;
        this.page = 1;
    }

    public Button(int page, int pos, ItemStack item) {
        this.page = page;
        this.pos = pos;
        this.item = item;
    }

    public abstract void onClick(Player var1, ClickType var2);
}

