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
        Location loc = new Location(w, x, 0.0, z);
        return chunkLoaded(loc);
    }

    public static Vector getBlockBounds(Material material) {
        if (MaterialChecks.BED.contains(material)) return new Vector(1.0F, 0.5625F, 1.0F);
        if (MaterialChecks.FENCES.contains(material)) return new Vector(1.0F, 1.5F, 1.0F);
        if (MaterialChecks.CLIMBABLE.contains(material)) return new Vector(0.8625F, 1.0F, 0.8625F);
        if (MaterialChecks.CARPETS.contains(material)) return new Vector(1.0F, 0.0625F, 1.0F);
        if (MaterialChecks.HALFS.contains(material)) return new Vector(1.0F, 0.5F, 1.0F);
        return MaterialChecks.PORTAL.contains(material) ? new Vector(1.0F, 0.8125F, 1.0F) : new Vector(1.0F, 1.0F, 1.0F);
    }

    public static void getTileEntitiesSync(BoundingBox box, Consumer<List<Block>> listConsumer) {
        Tasker.run(() -> listConsumer.accept(box.getCollidingAir()));
    }

    public static long getChunkPair(Chunk chunk) {
        return (long) chunk.getX() << 32 | (long) chunk.getZ() & 4294967295L;
    }

    public static long getChunkPair(int x, int z) {
        return ((long) x & 4294967295L) << 32 | (long) z & 4294967295L;
    }

    public static long getChunkPair(Location location) {
        return (long) (location.getBlockX() >> 4) << 32 | (long) (location.getBlockZ() >> 4) & 4294967295L;
    }
}
