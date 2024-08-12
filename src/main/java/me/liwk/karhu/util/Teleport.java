package me.liwk.karhu.util;

import lombok.Setter;

public class Teleport {
   public final TeleportPosition position;
   @Setter
   public boolean accepted = false;
   @Setter
   public boolean moved = false;

   public Teleport(TeleportPosition position) {
      this.position = position;
   }

}
