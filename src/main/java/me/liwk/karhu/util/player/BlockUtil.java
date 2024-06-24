/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Chunk
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.util.Vector
 */
package me.liwk.karhu.util.player;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Consumer;

public final class BlockUtil {
    public static boolean chunkLoaded(Location loc) {
        return Karhu.getInstance().getChunkManager().isChunkLoaded(loc);
    }

    public static boolean chunkLoaded(World w, int x, int z) {
        Location loc = new Location(w, (double)x, 0.0, (double)z);
        return BlockUtil.chunkLoaded(loc);
    }

    public static Vector getBlockBounds(Material material) {
        if (MaterialChecks.BED.contains(material)) {
            return new Vector(1.0f, 0.5625f, 1.0f);
        }
        if (MaterialChecks.FENCES.contains(material)) {
            return new Vector(1.0f, 1.5f, 1.0f);
        }
        if (MaterialChecks.CLIMBABLE.contains(material)) {
            return new Vector(0.8625f, 1.0f, 0.8625f);
        }
        if (MaterialChecks.CARPETS.contains(material)) {
            return new Vector(1.0f, 0.0625f, 1.0f);
        }
        if (MaterialChecks.HALFS.contains(material)) {
            return new Vector(1.0f, 0.5f, 1.0f);
        }
        return MaterialChecks.PORTAL.contains(material) ? new Vector(1.0f, 0.8125f, 1.0f) : new Vector(1.0f, 1.0f, 1.0f);
    }

    public static void getTileEntitiesSync(BoundingBox box, Consumer<List<Block>> listConsumer) {
        Tasker.run(() -> listConsumer.accept(box.getCollidingAir()));
    }

    public static long getChunkPair(Chunk chunk) {
        return (long)chunk.getX() << 32 | (long)chunk.getZ() & 0xFFFFFFFFL;
    }

    public static long getChunkPair(int x, int z) {
        return ((long)x & 0xFFFFFFFFL) << 32 | (long)z & 0xFFFFFFFFL;
    }

    public static long getChunkPair(Location location) {
        return (long)(location.getBlockX() >> 4) << 32 | (long)(location.getBlockZ() >> 4) & 0xFFFFFFFFL;
    }
}

