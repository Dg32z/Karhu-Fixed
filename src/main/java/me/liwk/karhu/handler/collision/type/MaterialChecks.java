/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 */
package me.liwk.karhu.handler.collision.type;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class MaterialChecks {
    public static Set<Material> AIR = null;
    public static Set<Material> MOVABLE = null;
    public static Set<Material> SHULKER_BOXES = null;
    public static Set<Material> ICE = null;
    public static Set<Material> SIGNS = null;
    public static Set<Material> HALFS = null;
    public static Set<Material> GRASS = null;
    public static Set<Material> DOORS = null;
    public static Set<Material> TRAPS = null;
    public static Set<Material> LIQUIDS = null;
    public static Set<Material> WATER = null;
    public static Set<Material> LAVA = null;
    public static Set<Material> SEASHIT = null;
    public static Set<Material> FENCES = null;
    public static Set<Material> PANES = null;
    public static Set<Material> WEIRD_SOLID = null;
    public static Set<Material> WEIRD_SOLID_NO_LIQUID = null;
    public static Set<Material> STAIRS = null;
    public static Set<Material> BED = null;
    public static Set<Material> LILY = null;
    public static Set<Material> WEB = null;
    public static Set<Material> SLIME = null;
    public static Set<Material> SOUL = null;
    public static Set<Material> HONEY = null;
    public static Set<Material> BERRIES = null;
    public static Set<Material> SCAFFOLD = null;
    public static Set<Material> CLIMBABLE = null;
    public static Set<Material> REDSTONE = null;
    public static Set<Material> CARPETS = null;
    public static Set<Material> ONETAPS = null;
    public static Set<Material> BUTTONS = null;
    public static Set<Material> TORCHES = null;
    public static Set<Material> RETARD_FACE = null;
    public static Set<Material> PORTAL = null;
    public static Set<Material> POWDERSNOW = null;
    public static Set<Material> DRIP_LEAF = null;
    public static Set<Material> EDIBLE_WITHOUT_HUNGER = null;
    public static Set<Material> SWORDS = null;
    public static Set<Material> BOWS = null;
    public static Set<Material> LIQUID_BUCKETS = null;
    public static Set<Material> CLEARICE = null;
    public static Set<Material> PACKEDICE = null;
    public static Set<Material> FROSTEDICE = null;
    public static Set<Material> BLUEICE = null;

    public static Set<Material> find(String ... array) {
        HashSet<Material> mats = new HashSet<Material>();
        for (String shits : array) {
            for (Material c : Material.values()) {
                if (!c.name().contains(shits)) continue;
                mats.add(c);
            }
        }
        return mats;
    }

    public static Set<Material> fastFind(String ... array) {
        HashSet<Material> mats = new HashSet<Material>();
        for (String shits : array) {
            try {
                Material material = Material.valueOf((String)shits);
                mats.add(material);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return mats;
    }

    static {
        try {
            AIR = MaterialChecks.fastFind("AIR", "CAVE_AIR", "VOID_AIR");
            BED = MaterialChecks.find("BED");
            MOVABLE = MaterialChecks.find("SHULKER_BOX", "PISTON");
            SHULKER_BOXES = MaterialChecks.find("SHULKER_BOX");
            FENCES = MaterialChecks.find("FENCE", "GATE", "WALL", "COBBLE_WALL", "PANE", "THIN");
            PANES = MaterialChecks.find("PANE", "THIN");
            ICE = MaterialChecks.find("ICE", "PACKED");
            GRASS = MaterialChecks.find("GRASS", "FLOWER", "ROSE");
            PORTAL = MaterialChecks.find("PORTAL_FRAME");
            DOORS = MaterialChecks.find("DOOR");
            TRAPS = MaterialChecks.find("TRAP");
            HALFS = MaterialChecks.find("SLAB", "STEP", "DAYLIGHT", "SENSOR", "SNOW", "SKULL", "HEAD", "CAKE", "POT", "BEAN", "COCOA", "ENCH", "STONECUTTER", "LANTERN", "CAMPFIRE", "CANDLE", "PICKLE", "BELL", "AMETHYST", "BED");
            STAIRS = MaterialChecks.find("STAIR");
            SIGNS = MaterialChecks.find("SIGN");
            CARPETS = MaterialChecks.find("CARPET");
            REDSTONE = MaterialChecks.find("DIODE", "REPEATER", "COMPARATOR");
            WEIRD_SOLID = MaterialChecks.find("LILY", "COCOA", "REDSTONE_", "POT", "ROD", "CARPET", "WATER", "BUBBLE", "LAVA", "SKULL", "LADDER", "SNOW", "SCAFFOLD", "DIODE", "REPEATER", "COMPARATOR", "VINE", "CANDLE", "PICKLE", "DRIP_LEAF");
            WEIRD_SOLID_NO_LIQUID = MaterialChecks.find("LILY", "COCOA", "REDSTONE_", "POT", "ROD", "CARPET", "SKULL", "LADDER", "SNOW", "SCAFFOLD", "DIODE", "REPEATER", "COMPARATOR", "VINE", "CANDLE", "PICKLE", "DRIP_LEAF");
            LIQUIDS = MaterialChecks.fastFind("WATER", "STATIONARY_WATER", "LAVA", "STATIONARY_LAVA");
            WATER = MaterialChecks.find("WATER", "STATIONARY_WATER", "BUBBLE_COLUMN");
            LAVA = MaterialChecks.find("LAVA", "STATIONARY_LAVA");
            SEASHIT = MaterialChecks.find("KELP", "SEAGRASS");
            LILY = MaterialChecks.find("LILY");
            DRIP_LEAF = MaterialChecks.find("DRIPLEAF");
            WEB = MaterialChecks.find("WEB");
            SLIME = MaterialChecks.find("SLIME_BLOCK");
            SOUL = MaterialChecks.fastFind("SOUL_SAND");
            HONEY = MaterialChecks.find("HONEY_BLOCK");
            BERRIES = MaterialChecks.find("SWEET");
            SCAFFOLD = MaterialChecks.find("SCAFFOLDING");
            POWDERSNOW = MaterialChecks.find("POWDER_SNOW");
            CLIMBABLE = MaterialChecks.find("VINE", "LADDER", "SCAFFOLDING");
            ONETAPS = MaterialChecks.find("SLIME_BLOCK", "FLOWER", "ROSE", "TORCH");
            BUTTONS = MaterialChecks.find("BUTTON");
            TORCHES = MaterialChecks.find("TORCH");
            RETARD_FACE = MaterialChecks.find("TORCH", "BUTTON", "SIGN");
            EDIBLE_WITHOUT_HUNGER = MaterialChecks.find("GOLDEN_APPLE", "POTION", "BOTTLE", "MILK_BUCKET");
            SWORDS = MaterialChecks.find("SWORD");
            BOWS = MaterialChecks.find("BOW");
            LIQUID_BUCKETS = MaterialChecks.fastFind("WATER_BUCKET", "LAVA_BUCKET");
            CLEARICE = MaterialChecks.fastFind("ICE");
            PACKEDICE = MaterialChecks.fastFind("PACKED_ICE");
            FROSTEDICE = MaterialChecks.fastFind("FROSTED_ICE");
            BLUEICE = MaterialChecks.fastFind("BLUE_ICE");
        }
        catch (Exception var1) {
            var1.printStackTrace();
        }
    }
}

