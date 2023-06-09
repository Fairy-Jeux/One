package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class RuleProcessor extends StructureProcessor {
   public static final Codec<RuleProcessor> CODEC = ProcessorRule.CODEC.listOf().fieldOf("rules").xmap(RuleProcessor::new, (p_74306_) -> {
      return p_74306_.rules;
   }).codec();
   private final ImmutableList<ProcessorRule> rules;

   public RuleProcessor(List<? extends ProcessorRule> p_74296_) {
      this.rules = ImmutableList.copyOf(p_74296_);
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader pLevel, BlockPos p_74300_, BlockPos pPos, StructureTemplate.StructureBlockInfo pBlockInfo, StructureTemplate.StructureBlockInfo pRelativeBlockInfo, StructurePlaceSettings pSettings) {
      Random random = new Random(Mth.getSeed(pRelativeBlockInfo.pos));
      BlockState blockstate = pLevel.getBlockState(pRelativeBlockInfo.pos);

      for(ProcessorRule processorrule : this.rules) {
         if (processorrule.test(pRelativeBlockInfo.state, blockstate, pBlockInfo.pos, pRelativeBlockInfo.pos, pPos, random)) {
            return new StructureTemplate.StructureBlockInfo(pRelativeBlockInfo.pos, processorrule.getOutputState(), processorrule.getOutputTag());
         }
      }

      return pRelativeBlockInfo;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.RULE;
   }
}