package net.minecraft.world.level.levelgen.placement;

import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public abstract class PlacementFilter extends PlacementModifier {
   public final Stream<BlockPos> getPositions(PlacementContext pContext, Random pRandom, BlockPos pPos) {
      return this.shouldPlace(pContext, pRandom, pPos) ? Stream.of(pPos) : Stream.of();
   }

   protected abstract boolean shouldPlace(PlacementContext pContext, Random pRandom, BlockPos pPos);
}