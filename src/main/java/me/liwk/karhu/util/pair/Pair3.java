package me.liwk.karhu.util.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Pair3<X, Y, Z> {
   private X x;
   private Y y;
   private Z z;
}
