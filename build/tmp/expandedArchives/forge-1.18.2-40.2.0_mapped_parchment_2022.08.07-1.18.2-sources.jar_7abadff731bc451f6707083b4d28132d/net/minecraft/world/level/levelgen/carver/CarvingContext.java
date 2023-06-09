package net.minecraft.world.level.levelgen.carver;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
   private final NoiseBasedChunkGenerator generator;
   private final RegistryAccess registryAccess;
   private final NoiseChunk noiseChunk;

   public CarvingContext(NoiseBasedChunkGenerator pGenerator, RegistryAccess pRegistryAccess, LevelHeightAccessor pLevel, NoiseChunk pNoiseChunk) {
      super(pGenerator, pLevel);
      this.generator = pGenerator;
      this.registryAccess = pRegistryAccess;
      this.noiseChunk = pNoiseChunk;
   }

   /** @deprecated */
   @Deprecated
   public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> pBiomeMapper, ChunkAccess pAccess, BlockPos pPos, boolean pHasFluid) {
      return this.generator.topMaterial(this, pBiomeMapper, pAccess, this.noiseChunk, pPos, pHasFluid);
   }

   /** @deprecated */
   @Deprecated
   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }
}