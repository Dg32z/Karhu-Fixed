/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package me.liwk.karhu.util;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    private static final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public static Class<?> blockPosition = null;
    private static Class<?> iBlockData = null;
    private static final Class<?> craftWorld = ReflectionUtil.getCBClass("CraftWorld");
    private static final Class<?> worldServer = ReflectionUtil.getNMSClass("WorldServer");

    public static boolean canDestroyBlock(KarhuPlayer data, Block block) {
        Object inventory = ReflectionUtil.getVanillaInventory(data.getBukkitPlayer());
        return (Boolean)ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("PlayerInventory"), "b", ReflectionUtil.getNMSClass("Block")), inventory, Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_12_2) ? ReflectionUtil.getBlockData(block) : ReflectionUtil.getVanillaBlock(block));
    }

    public static Object getVanillaInventory(Player player) {
        return ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(ReflectionUtil.getCBClass("inventory.CraftInventoryPlayer"), "getInventory", new Class[0]), player.getInventory(), new Object[0]);
    }

    public static float getDestroySpeed(Block block, KarhuPlayer data) {
        Object item = ReflectionUtil.getVanillaItem(data.getStackInHand());
        return ((Float)(Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8) ? ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("Item"), "getDestroySpeed", ReflectionUtil.getNMSClass("ItemStack"), ReflectionUtil.getNMSClass("IBlockData")), item, ReflectionUtil.getVanillaItemStack(data.getStackInHand()), ReflectionUtil.getBlockData(block)) : ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("Item"), "getDestroySpeed", ReflectionUtil.getNMSClass("ItemStack"), ReflectionUtil.getNMSClass("Block")), item, ReflectionUtil.getVanillaItemStack(data.getStackInHand()), ReflectionUtil.getVanillaBlock(block)))).floatValue();
    }

    public static float getBlockDurability(Block block) {
        Object vanillaBlock = ReflectionUtil.getVanillaBlock(block);
        if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_16)) {
            Object getType = ReflectionUtil.getBlockData(block);
            Object blockData = ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("Block"), "getBlockData", new Class[0]), vanillaBlock, new Object[0]);
            return ((Float)ReflectionUtil.getFieldValue(ReflectionUtil.getFieldByName(iBlockData, "strength"), blockData)).floatValue();
        }
        return ((Float)ReflectionUtil.getFieldValue(ReflectionUtil.getFieldByName(ReflectionUtil.getNMSClass("Block"), "strength"), vanillaBlock)).floatValue();
    }

    public static Object getVanillaBlock(Block block) {
        if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            Object world = ReflectionUtil.getWorldHandle(block.getWorld());
            return ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(worldServer, "getType", Integer.TYPE, Integer.TYPE, Integer.TYPE), world, block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
        }
        Object getType = ReflectionUtil.getBlockData(block);
        return ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(iBlockData, "getBlock", new Class[0]), getType, new Object[0]);
    }

    private static Object getBlockData(Block block) {
        try {
            if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
                Object world = ReflectionUtil.getWorldHandle(block.getWorld());
                return ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(worldServer, "getType", Integer.TYPE, Integer.TYPE, Integer.TYPE), world, block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
            }
            Object bPos = blockPosition.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
            Object world = ReflectionUtil.getWorldHandle(block.getWorld());
            return ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(worldServer, "getType", blockPosition), world, bPos);
        }
        catch (Exception var31) {
            var31.printStackTrace();
            return null;
        }
    }

    public static Object getVanillaItem(ItemStack itemStack) {
        return ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("ItemStack"), "getItem", new Class[0]), ReflectionUtil.getVanillaItemStack(itemStack), new Object[0]);
    }

    public static Object getVanillaItemStack(ItemStack itemStack) {
        return ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(ReflectionUtil.getCBClass("inventory.CraftItemStack"), "asNMSCopy", ReflectionUtil.getClass("org.bukkit.inventory.ItemStack")), itemStack, itemStack);
    }

    public static Object getMethodValue(Method method, Object object, Object ... args) {
        try {
            return method.invoke(object, args);
        }
        catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?> ... args) {
        try {
            Method method = clazz.getMethod(methodName, args);
            method.setAccessible(true);
            return method;
        }
        catch (Exception var41) {
            var41.printStackTrace();
            return null;
        }
    }

    public static Class<?> getNMSClass(String string) {
        return ReflectionUtil.getClass("net.minecraft.server." + version + "." + string);
    }

    public static Class<?> getCBClass(String string) {
        return ReflectionUtil.getClass("org.bukkit.craftbukkit." + version + "." + string);
    }

    public static Class<?> getClass(String string) {
        try {
            return Class.forName(string);
        }
        catch (ClassNotFoundException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static Object getFieldValue(Field field, Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        }
        catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName) != null ? clazz.getDeclaredField(fieldName) : clazz.getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }
        catch (Exception var31) {
            var31.printStackTrace();
            return null;
        }
    }

    public static Object getWorldHandle(World world) {
        return ReflectionUtil.getMethodValue(ReflectionUtil.getMethod(craftWorld, "getHandle", new Class[0]), world, new Object[0]);
    }

    static {
        if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_8)) {
            iBlockData = ReflectionUtil.getNMSClass("IBlockData");
            blockPosition = ReflectionUtil.getNMSClass("BlockPosition");
        }
    }
}

