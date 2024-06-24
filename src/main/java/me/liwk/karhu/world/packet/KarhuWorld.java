/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 */
package me.liwk.karhu.world.packet;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.player.BlockUtil;
import me.liwk.karhu.world.packet.util.WrappedChunk;

import java.util.Map;

public class KarhuWorld {
    public final KarhuPlayer data;
    public final Map<Long, WrappedChunk> chunks = new Long2ObjectOpenHashMap(81, 0.5f);

    public void updateBlock(int x, int y, int z, int blockID) {
        WrappedChunk wrappedChunk = this.getChunk(x >> 4, z >> 4);
    }

    public WrappedChunk getChunk(int chunkX, int chunkZ) {
        long chunkPosition = BlockUtil.getChunkPair(chunkX, chunkZ);
        return this.chunks.get(chunkPosition);
    }

    public KarhuWorld(KarhuPlayer data) {
        this.data = data;
    }
}

