package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class OreBlock extends Block {
   private final UniformInt xpRange;

   public OreBlock(BlockBehaviour.Properties pProperties) {
      this(pProperties, UniformInt.of(0, 0));
   }

   public OreBlock(BlockBehaviour.Properties pProperties, UniformInt pXpRange) {
      super(pProperties);
      this.xpRange = pXpRange;
   }

   /**
    * Perform side-effects from block dropping, such as creating silverfish
    */
   public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack);
   }

   @Override
   public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader reader, BlockPos pos, int fortune, int silktouch) {
      return silktouch == 0 ? this.xpRange.sample(RANDOM) : 0;
   }
}
