package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placement) {
   public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create((p_191788_) -> {
      return p_191788_.group(ConfiguredFeature.CODEC.fieldOf("feature").forGetter((p_204928_) -> {
         return p_204928_.feature;
      }), PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter((p_191796_) -> {
         return p_191796_.placement;
      })).apply(p_191788_, PlacedFeature::new);
   });
   public static final Codec<Holder<PlacedFeature>> CODEC = RegistryFileCodec.create(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Codec<HolderSet<PlacedFeature>> LIST_CODEC = RegistryCodecs.homogeneousList(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Codec<List<HolderSet<PlacedFeature>>> LIST_OF_LISTS_CODEC = RegistryCodecs.homogeneousList(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC, true).listOf();

   public boolean place(WorldGenLevel pLevel, ChunkGenerator pGenerator, Random p_191785_, BlockPos p_191786_) {
      return this.placeWithContext(new PlacementContext(pLevel, pGenerator, Optional.empty()), p_191785_, p_191786_);
   }

   public boolean placeWithBiomeCheck(WorldGenLevel pLevel, ChunkGenerator pGenerator, Random p_191809_, BlockPos p_191810_) {
      return this.placeWithContext(new PlacementContext(pLevel, pGenerator, Optional.of(this)), p_191809_, p_191810_);
   }

   private boolean placeWithContext(PlacementContext pContext, Random p_191799_, BlockPos p_191800_) {
      Stream<BlockPos> stream = Stream.of(p_191800_);

      for(PlacementModifier placementmodifier : this.placement) {
         stream = stream.flatMap((p_191805_) -> {
            return placementmodifier.getPositions(pContext, p_191799_, p_191805_);
         });
      }

      ConfiguredFeature<?, ?> configuredfeature = this.feature.value();
      MutableBoolean mutableboolean = new MutableBoolean();
      stream.forEach((p_191794_) -> {
         if (configuredfeature.place(pContext.getLevel(), pContext.generator(), p_191799_, p_191794_)) {
            mutableboolean.setTrue();
         }

      });
      return mutableboolean.isTrue();
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return this.feature.value().getFeatures();
   }

   public String toString() {
      return "Placed " + this.feature;
   }

   static record test(int a) {
   }
}