package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface PositionalRandomFactory {
   default RandomSource at(BlockPos p_189315_) {
      return this.at(p_189315_.getX(), p_189315_.getY(), p_189315_.getZ());
   }

   default RandomSource fromHashOf(ResourceLocation p_189319_) {
      return this.fromHashOf(p_189319_.toString());
   }

   RandomSource fromHashOf(String pName);

   RandomSource at(int pX, int pY, int pZ);

   @VisibleForTesting
   void parityConfigString(StringBuilder pBuilder);
}