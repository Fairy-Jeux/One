package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ConfiguredStructureFeature<FC extends FeatureConfiguration, F extends StructureFeature<FC>> {
   public static final Codec<ConfiguredStructureFeature<?, ?>> DIRECT_CODEC = Registry.STRUCTURE_FEATURE.byNameCodec().dispatch((p_65410_) -> {
      return p_65410_.feature;
   }, StructureFeature::configuredStructureCodec);
   public static final Codec<Holder<ConfiguredStructureFeature<?, ?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Codec<HolderSet<ConfiguredStructureFeature<?, ?>>> LIST_CODEC = RegistryCodecs.homogeneousList(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC);
   public final F feature;
   public final FC config;
   public final HolderSet<Biome> biomes;
   public final Map<MobCategory, StructureSpawnOverride> spawnOverrides;
   public final boolean adaptNoise;

   public ConfiguredStructureFeature(F pFeature, FC pConfig, HolderSet<Biome> pBiomes, boolean pAdaptNoise, Map<MobCategory, StructureSpawnOverride> pSpawnOverrides) {
      this.feature = pFeature;
      this.config = pConfig;
      this.biomes = pBiomes;
      this.adaptNoise = pAdaptNoise;
      this.spawnOverrides = pSpawnOverrides;
   }

   public StructureStart generate(RegistryAccess pRegistryAcess, ChunkGenerator pChunkGenerator, BiomeSource pBiomeSource, StructureManager pStructureManager, long pSeed, ChunkPos pChunkPos, int p_204714_, LevelHeightAccessor pLevel, Predicate<Holder<Biome>> pBiomePredicate) {
      Optional<PieceGenerator<FC>> optional = this.feature.pieceGeneratorSupplier().createGenerator(new PieceGeneratorSupplier.Context<>(pChunkGenerator, pBiomeSource, pSeed, pChunkPos, this.config, pLevel, pBiomePredicate, pStructureManager, pRegistryAcess));
      if (optional.isPresent()) {
         StructurePiecesBuilder structurepiecesbuilder = new StructurePiecesBuilder();
         WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
         worldgenrandom.setLargeFeatureSeed(pSeed, pChunkPos.x, pChunkPos.z);
         optional.get().generatePieces(structurepiecesbuilder, new PieceGenerator.Context<>(this.config, pChunkGenerator, pStructureManager, pChunkPos, pLevel, worldgenrandom, pSeed));
         StructureStart structurestart = new StructureStart(this, pChunkPos, p_204714_, structurepiecesbuilder.build());
         if (structurestart.isValid()) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public HolderSet<Biome> biomes() {
      return this.biomes;
   }

   public BoundingBox adjustBoundingBox(BoundingBox p_209754_) {
      return this.adaptNoise ? p_209754_.inflatedBy(12) : p_209754_;
   }
}