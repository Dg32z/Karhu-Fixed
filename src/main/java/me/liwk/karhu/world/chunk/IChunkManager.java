/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  org.bukkit.Chunk
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.block.Block
 */
package me.liwk.karhu.world.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.liwk.karhu.util.gui.Callback;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Map;

public interface IChunkManager {
    public void onChunkLoad(Chunk var1);

    public void removeWorld(World var1);

    public void onChunkUnload(Chunk var1);

    public void unloadAll();

    public Block getChunkBlockAt(Location var1);

    public int getCacheSize(World var1);

    public boolean isChunkLoaded(Location var1);

    public void getChunk(Location var1, Callback<Chunk> var2);

    public Map<World, Long2ObjectMap<Chunk>> getLoadedChunks();

    public void addWorld(World var1);
}

