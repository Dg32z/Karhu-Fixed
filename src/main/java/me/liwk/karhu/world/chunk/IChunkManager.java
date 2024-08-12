package me.liwk.karhu.world.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.liwk.karhu.util.gui.Callback;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Map;

public interface IChunkManager {
   void onChunkLoad(Chunk var1);

   void removeWorld(World var1);

   void onChunkUnload(Chunk var1);

   void unloadAll();

   Block getChunkBlockAt(Location var1);

   int getCacheSize(World var1);

   boolean isChunkLoaded(Location var1);

   void getChunk(Location var1, Callback<Chunk> var2);

   Map<World, Long2ObjectMap<Chunk>> getLoadedChunks();

   void addWorld(World var1);
}
