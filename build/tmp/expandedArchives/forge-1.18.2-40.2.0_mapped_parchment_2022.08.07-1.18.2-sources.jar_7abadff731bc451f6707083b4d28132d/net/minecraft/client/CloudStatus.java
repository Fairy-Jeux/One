package net.minecraft.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum CloudStatus {
   OFF("options.off"),
   FAST("options.clouds.fast"),
   FANCY("options.clouds.fancy");

   private final String key;

   private CloudStatus(String pKey) {
      this.key = pKey;
   }

   public String getKey() {
      return this.key;
   }
}