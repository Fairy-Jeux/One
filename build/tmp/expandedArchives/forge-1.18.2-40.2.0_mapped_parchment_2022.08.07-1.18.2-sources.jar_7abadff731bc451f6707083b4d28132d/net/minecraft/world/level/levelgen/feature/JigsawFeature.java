package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;

public class JigsawFeature extends StructureFeature<JigsawConfiguration> {
   public JigsawFeature(Codec<JigsawConfiguration> pCodec, int pStartY, boolean pDoExpansionHack, boolean pProjectStartToHeightmap, Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> p_197096_) {
      super(pCodec, (p_197102_) -> {
         if (!p_197096_.test(p_197102_)) {
            return Optional.empty();
         } else {
            BlockPos blockpos = new BlockPos(p_197102_.chunkPos().getMinBlockX(), pStartY, p_197102_.chunkPos().getMinBlockZ());
            Pools.bootstrap();
            return JigsawPlacement.addPieces(p_197102_, PoolElementStructurePiece::new, blockpos, pDoExpansionHack, pProjectStartToHeightmap);
         }
      });
   }
}