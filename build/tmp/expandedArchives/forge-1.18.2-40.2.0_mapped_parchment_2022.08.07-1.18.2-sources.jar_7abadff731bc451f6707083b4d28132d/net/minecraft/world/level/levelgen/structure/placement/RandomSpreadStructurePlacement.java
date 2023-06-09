package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public record RandomSpreadStructurePlacement(int spacing, int separation, RandomSpreadType spreadType, int salt, Vec3i locateOffset) implements StructurePlacement {
   public static final Codec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.<RandomSpreadStructurePlacement>mapCodec((p_204996_) -> {
      return p_204996_.group(Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing), Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation), RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(RandomSpreadStructurePlacement::salt), Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(RandomSpreadStructurePlacement::locateOffset)).apply(p_204996_, RandomSpreadStructurePlacement::new);
   }).flatXmap((p_205002_) -> {
      return p_205002_.spacing <= p_205002_.separation ? DataResult.error("Spacing has to be larger than separation") : DataResult.success(p_205002_);
   }, DataResult::success).codec();

   public RandomSpreadStructurePlacement(int pSpacing, int pSeperation, RandomSpreadType pSpreadType, int pSalt) {
      this(pSpacing, pSeperation, pSpreadType, pSalt, Vec3i.ZERO);
   }

   public ChunkPos getPotentialFeatureChunk(long p_204992_, int p_204993_, int p_204994_) {
      int i = this.spacing();
      int j = this.separation();
      int k = Math.floorDiv(p_204993_, i);
      int l = Math.floorDiv(p_204994_, i);
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureWithSalt(p_204992_, k, l, this.salt());
      int i1 = i - j;
      int j1 = this.spreadType().evaluate(worldgenrandom, i1);
      int k1 = this.spreadType().evaluate(worldgenrandom, i1);
      return new ChunkPos(k * i + j1, l * i + k1);
   }

   public boolean isFeatureChunk(ChunkGenerator pGenerator, long pSeed, int pX, int pZ) {
      ChunkPos chunkpos = this.getPotentialFeatureChunk(pSeed, pX, pZ);
      return chunkpos.x == pX && chunkpos.z == pZ;
   }

   public StructurePlacementType<?> type() {
      return StructurePlacementType.RANDOM_SPREAD;
   }
}