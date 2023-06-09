package com.mojang.blaze3d.font;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GlyphInfo {
   float getAdvance();

   default float getAdvance(boolean pBold) {
      return this.getAdvance() + (pBold ? this.getBoldOffset() : 0.0F);
   }

   default float getBearingX() {
      return 0.0F;
   }

   default float getBearingY() {
      return 0.0F;
   }

   default float getBoldOffset() {
      return 1.0F;
   }

   default float getShadowOffset() {
      return 1.0F;
   }
}