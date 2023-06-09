package net.minecraft.world.level.levelgen.placement;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public abstract class RepeatingPlacement extends PlacementModifier {
   protected abstract int count(Random pRandom, BlockPos pPos);

   public Stream<BlockPos> getPositions(PlacementContext pContext, Random pRandom, BlockPos pPos) {
      return IntStream.range(0, this.count(pRandom, pPos)).mapToObj((p_191912_) -> {
         return pPos;
      });
   }
}