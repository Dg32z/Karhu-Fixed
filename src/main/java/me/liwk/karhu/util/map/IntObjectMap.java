package me.liwk.karhu.util.map;

import java.util.Map;

public interface IntObjectMap<V> extends Map<Integer, V> {
   V remove(int var1);

   V get(int var1);

   V put(int var1, V var2);

   boolean containsKey(int var1);

   Iterable<IntObjectMap.PrimitiveEntry<V>> entries();

   public interface PrimitiveEntry<V> {
      int key();

      V value();

      void setValue(V var1);
   }
}
