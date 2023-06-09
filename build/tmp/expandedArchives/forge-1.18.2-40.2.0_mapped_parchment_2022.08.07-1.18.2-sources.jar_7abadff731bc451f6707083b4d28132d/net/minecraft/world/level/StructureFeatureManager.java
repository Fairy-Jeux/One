package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureFeatureManager {
   private final LevelAccessor level;
   private final WorldGenSettings worldGenSettings;
   private final StructureCheck structureCheck;

   public StructureFeatureManager(LevelAccessor pLevel, WorldGenSettings pWorldGenSettings, StructureCheck pStructureCheck) {
      this.level = pLevel;
      this.worldGenSettings = pWorldGenSettings;
      this.structureCheck = pStructureCheck;
   }

   public StructureFeatureManager forWorldGenRegion(WorldGenRegion pRegion) {
      if (pRegion.getLevel() != this.level) {
         throw new IllegalStateException("Using invalid feature manager (source level: " + pRegion.getLevel() + ", region: " + pRegion);
      } else {
         return new StructureFeatureManager(pRegion, this.worldGenSettings, this.structureCheck);
      }
   }

   public List<StructureStart> startsForFeature(SectionPos pSectionPos, Predicate<ConfiguredStructureFeature<?, ?>> pStructurePredicate) {
      Map<ConfiguredStructureFeature<?, ?>, LongSet> map = this.level.getChunk(pSectionPos.x(), pSectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
      Builder<StructureStart> builder = ImmutableList.builder();

      for(Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : map.entrySet()) {
         ConfiguredStructureFeature<?, ?> configuredstructurefeature = entry.getKey();
         if (pStructurePredicate.test(configuredstructurefeature)) {
            this.fillStartsForFeature(configuredstructurefeature, entry.getValue(), builder::add);
         }
      }

      return builder.build();
   }

   public List<StructureStart> startsForFeature(SectionPos pSectionPos, ConfiguredStructureFeature<?, ?> pStructure) {
      LongSet longset = this.level.getChunk(pSectionPos.x(), pSectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(pStructure);
      Builder<StructureStart> builder = ImmutableList.builder();
      this.fillStartsForFeature(pStructure, longset, builder::add);
      return builder.build();
   }

   public void fillStartsForFeature(ConfiguredStructureFeature<?, ?> pConfigured, LongSet pStructureRefs, Consumer<StructureStart> pStartConsumer) {
      for(long i : pStructureRefs) {
         SectionPos sectionpos = SectionPos.of(new ChunkPos(i), this.level.getMinSection());
         StructureStart structurestart = this.getStartForFeature(sectionpos, pConfigured, this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_STARTS));
         if (structurestart != null && structurestart.isValid()) {
            pStartConsumer.accept(structurestart);
         }
      }

   }

   @Nullable
   public StructureStart getStartForFeature(SectionPos pSectionPos, ConfiguredStructureFeature<?, ?> pStructure, FeatureAccess pReader) {
      return pReader.getStartForFeature(pStructure);
   }

   public void setStartForFeature(SectionPos pSectionPos, ConfiguredStructureFeature<?, ?> pStructure, StructureStart pStart, FeatureAccess pReader) {
      pReader.setStartForFeature(pStructure, pStart);
   }

   public void addReferenceForFeature(SectionPos pSectionPos, ConfiguredStructureFeature<?, ?> pStructure, long pChunkValue, FeatureAccess pReader) {
      pReader.addReferenceForFeature(pStructure, pChunkValue);
   }

   public boolean shouldGenerateFeatures() {
      return this.worldGenSettings.generateFeatures();
   }

   public StructureStart getStructureAt(BlockPos pPos, ConfiguredStructureFeature<?, ?> pStructure) {
      for(StructureStart structurestart : this.startsForFeature(SectionPos.of(pPos), pStructure)) {
         if (structurestart.getBoundingBox().isInside(pPos)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public StructureStart getStructureWithPieceAt(BlockPos pPos, ResourceKey<ConfiguredStructureFeature<?, ?>> pKey) {
      ConfiguredStructureFeature<?, ?> configuredstructurefeature = this.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).get(pKey);
      return configuredstructurefeature == null ? StructureStart.INVALID_START : this.getStructureWithPieceAt(pPos, configuredstructurefeature);
   }

   public StructureStart getStructureWithPieceAt(BlockPos pPos, ConfiguredStructureFeature<?, ?> pConfiguredFeature) {
      for(StructureStart structurestart : this.startsForFeature(SectionPos.of(pPos), pConfiguredFeature)) {
         if (this.structureHasPieceAt(pPos, structurestart)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public boolean structureHasPieceAt(BlockPos pPos, StructureStart pStart) {
      for(StructurePiece structurepiece : pStart.getPieces()) {
         if (structurepiece.getBoundingBox().isInside(pPos)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasAnyStructureAt(BlockPos pPos) {
      SectionPos sectionpos = SectionPos.of(pPos);
      return this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
   }

   public Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllStructuresAt(BlockPos pPos) {
      SectionPos sectionpos = SectionPos.of(pPos);
      return this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
   }

   public StructureCheckResult checkStructurePresence(ChunkPos pPos, ConfiguredStructureFeature<?, ?> pConfiguredFeature, boolean pSkipKnownStructures) {
      return this.structureCheck.checkStart(pPos, pConfiguredFeature, pSkipKnownStructures);
   }

   public void addReference(StructureStart pStart) {
      pStart.addReference();
      this.structureCheck.incrementReference(pStart.getChunkPos(), pStart.getFeature());
   }

   public RegistryAccess registryAccess() {
      return this.level.registryAccess();
   }
}