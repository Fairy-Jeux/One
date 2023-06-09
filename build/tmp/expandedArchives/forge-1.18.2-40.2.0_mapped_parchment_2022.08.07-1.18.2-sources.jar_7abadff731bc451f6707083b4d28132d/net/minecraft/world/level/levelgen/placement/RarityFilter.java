package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;

public class RarityFilter extends PlacementFilter {
   public static final Codec<RarityFilter> CODEC = ExtraCodecs.POSITIVE_INT.fieldOf("chance").xmap(RarityFilter::new, (p_191907_) -> {
      return p_191907_.chance;
   }).codec();
   private final int chance;

   private RarityFilter(int p_191899_) {
      this.chance = p_191899_;
   }

   public static RarityFilter onAverageOnceEvery(int pChance) {
      return new RarityFilter(pChance);
   }

   protected boolean shouldPlace(PlacementContext pContext, Random pRandom, BlockPos pPos) {
      return pRandom.nextFloat() < 1.0F / (float)this.chance;
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.RARITY_FILTER;
   }
}