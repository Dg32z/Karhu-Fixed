/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.util.Vector
 */
package me.liwk.karhu.util.player;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class MovementUtils {
    public static double getHorizontalDistanceSpeed(Location to, Location from, Player p) {
        double x = Math.abs(to.getX()) - Math.abs(from.getX());
        double z = Math.abs(to.getZ()) - Math.abs(from.getZ());
        return Math.sqrt(x * x + z * z);
    }

    public static double offset(Vector from, Vector to) {
        from.setY(0);
        to.setY(0);
        return to.subtract(from).length();
    }

    public static int getDepthStriderLevel(Player player) {
        if (player.getInventory().getBoots() != null) {
            Enchantment enchLegacy = Enchantment.getByName((String)"DEPTH_STRIDER");
            Enchantment enchModern = Enchantment.getByName((String)"depth_strider");
            if (enchLegacy != null && MovementUtils.hasEnchantment(player.getInventory().getBoots(), enchLegacy)) {
                return (Integer)player.getInventory().getBoots().getEnchantments().get(enchLegacy);
            }
            if (enchModern != null && MovementUtils.hasEnchantment(player.getInventory().getBoots(), enchModern)) {
                return (Integer)player.getInventory().getBoots().getEnchantments().get(enchModern);
            }
        }
        return 0;
    }

    public static int getSoulSpeedLevel(Player player) {
        if (player.getInventory().getBoots() != null && MovementUtils.hasEnchantment(player.getInventory().getBoots(), Enchantment.getByName((String)"SOUL_SPEED"))) {
            return (Integer)player.getInventory().getBoots().getEnchantments().get(Enchantment.getByName((String)"SOUL_SPEED"));
        }
        return 0;
    }

    public static boolean hasEnchantment(ItemStack item, Enchantment enchantment) {
        return item.getEnchantments().containsKey(enchantment);
    }

    public static boolean searchEnchant(Player player, Enchantment enchantment) {
        for (ItemStack stack : player.getInventory()) {
            if (stack == null || stack.getEnchantments().isEmpty() || !stack.getEnchantments().containsKey(enchantment)) continue;
            return true;
        }
        return false;
    }

    public static int getEnchantmentLevel(ItemStack item, Enchantment enchantment) {
        return item.getEnchantmentLevel(enchantment);
    }
}

