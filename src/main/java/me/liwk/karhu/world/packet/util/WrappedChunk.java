package me.liwk.karhu.world.packet.util;

import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WrappedChunk {
    public final int x;
    public final int z;
    public BaseChunk[] chunks;
}
