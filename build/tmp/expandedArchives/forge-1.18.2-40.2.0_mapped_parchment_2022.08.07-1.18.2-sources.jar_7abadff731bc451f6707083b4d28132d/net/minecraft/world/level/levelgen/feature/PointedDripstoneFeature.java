package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;

public class PointedDripstoneFeature extends Feature<PointedDripstoneConfiguration> {
   public PointedDripstoneFeature(Codec<PointedDripstoneConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> pContext) {
      LevelAccessor levelaccessor = pContext.level();
      BlockPos blockpos = pContext.origin();
      Random random = pContext.random();
      PointedDripstoneConfiguration pointeddripstoneconfiguration = pContext.config();
      Optional<Direction> optional = getTipDirection(levelaccessor, blockpos, random);
      if (optional.isEmpty()) {
         return false;
      } else {
         BlockPos blockpos1 = blockpos.relative(optional.get().getOpposite());
         createPatchOfDripstoneBlocks(levelaccessor, random, blockpos1, pointeddripstoneconfiguration);
         int i = random.nextFloat() < pointeddripstoneconfiguration.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(levelaccessor.getBlockState(blockpos.relative(optional.get()))) ? 2 : 1;
         DripstoneUtils.growPointedDripstone(levelaccessor, blockpos, optional.get(), i, false);
         return true;
      }
   }

   private static Optional<Direction> getTipDirection(LevelAccessor p_191069_, BlockPos p_191070_, Random pRandom) {
      boolean flag = DripstoneUtils.isDripstoneBase(p_191069_.getBlockState(p_191070_.above()));
      boolean flag1 = DripstoneUtils.isDripstoneBase(p_191069_.getBlockState(p_191070_.below()));
      if (flag && flag1) {
         return Optional.of(pRandom.nextBoolean() ? Direction.DOWN : Direction.UP);
      } else if (flag) {
         return Optional.of(Direction.DOWN);
      } else {
         return flag1 ? Optional.of(Direction.UP) : Optional.empty();
      }
   }

   private static void createPatchOfDripstoneBlocks(LevelAccessor p_191073_, Random p_191074_, BlockPos p_191075_, PointedDripstoneConfiguration p_191076_) {
      DripstoneUtils.placeDripstoneBlockIfPossible(p_191073_, p_191075_);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (!(p_191074_.nextFloat() > p_191076_.chanceOfDirectionalSpread)) {
            BlockPos blockpos = p_191075_.relative(direction);
            DripstoneUtils.placeDripstoneBlockIfPossible(p_191073_, blockpos);
            if (!(p_191074_.nextFloat() > p_191076_.chanceOfSpreadRadius2)) {
               BlockPos blockpos1 = blockpos.relative(Direction.getRandom(p_191074_));
               DripstoneUtils.placeDripstoneBlockIfPossible(p_191073_, blockpos1);
               if (!(p_191074_.nextFloat() > p_191076_.chanceOfSpreadRadius3)) {
                  BlockPos blockpos2 = blockpos1.relative(Direction.getRandom(p_191074_));
                  DripstoneUtils.placeDripstoneBlockIfPossible(p_191073_, blockpos2);
               }
            }
         }
      }

   }
}