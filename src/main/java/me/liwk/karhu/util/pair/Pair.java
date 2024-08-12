package me.liwk.karhu.util.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Pair<X, Y> {
   private X x;
   private Y y;
}
