package me.liwk.karhu.util.pending;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.util.Vector;

@Setter
@Getter
@AllArgsConstructor
public class BlockPlacePending implements Cloneable {
   public Vector blockPosition;
   public int face;
   public long serverTick;
   public Material item;

   @SneakyThrows
   public BlockPlacePending clone() {
       return (BlockPlacePending)super.clone();
   }

}
