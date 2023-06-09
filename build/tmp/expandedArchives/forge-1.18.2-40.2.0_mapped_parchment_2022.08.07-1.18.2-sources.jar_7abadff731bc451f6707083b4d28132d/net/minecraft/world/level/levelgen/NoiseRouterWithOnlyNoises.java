package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record NoiseRouterWithOnlyNoises(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise, DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise, DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction depth, DensityFunction ridges, DensityFunction initialDensityWithoutJaggedness, DensityFunction finalDensity, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) {
   public static final Codec<NoiseRouterWithOnlyNoises> CODEC = RecordCodecBuilder.create((p_209602_) -> {
      return p_209602_.group(field("barrier", NoiseRouterWithOnlyNoises::barrierNoise), field("fluid_level_floodedness", NoiseRouterWithOnlyNoises::fluidLevelFloodednessNoise), field("fluid_level_spread", NoiseRouterWithOnlyNoises::fluidLevelSpreadNoise), field("lava", NoiseRouterWithOnlyNoises::lavaNoise), field("temperature", NoiseRouterWithOnlyNoises::temperature), field("vegetation", NoiseRouterWithOnlyNoises::vegetation), field("continents", NoiseRouterWithOnlyNoises::continents), field("erosion", NoiseRouterWithOnlyNoises::erosion), field("depth", NoiseRouterWithOnlyNoises::depth), field("ridges", NoiseRouterWithOnlyNoises::ridges), field("initial_density_without_jaggedness", NoiseRouterWithOnlyNoises::initialDensityWithoutJaggedness), field("final_density", NoiseRouterWithOnlyNoises::finalDensity), field("vein_toggle", NoiseRouterWithOnlyNoises::veinToggle), field("vein_ridged", NoiseRouterWithOnlyNoises::veinRidged), field("vein_gap", NoiseRouterWithOnlyNoises::veinGap)).apply(p_209602_, NoiseRouterWithOnlyNoises::new);
   });

   private static RecordCodecBuilder<NoiseRouterWithOnlyNoises, DensityFunction> field(String p_209606_, Function<NoiseRouterWithOnlyNoises, DensityFunction> p_209607_) {
      return DensityFunction.HOLDER_HELPER_CODEC.fieldOf(p_209606_).forGetter(p_209607_);
   }

   public NoiseRouterWithOnlyNoises mapAll(DensityFunction.Visitor pVisitor) {
      return new NoiseRouterWithOnlyNoises(this.barrierNoise.mapAll(pVisitor), this.fluidLevelFloodednessNoise.mapAll(pVisitor), this.fluidLevelSpreadNoise.mapAll(pVisitor), this.lavaNoise.mapAll(pVisitor), this.temperature.mapAll(pVisitor), this.vegetation.mapAll(pVisitor), this.continents.mapAll(pVisitor), this.erosion.mapAll(pVisitor), this.depth.mapAll(pVisitor), this.ridges.mapAll(pVisitor), this.initialDensityWithoutJaggedness.mapAll(pVisitor), this.finalDensity.mapAll(pVisitor), this.veinToggle.mapAll(pVisitor), this.veinRidged.mapAll(pVisitor), this.veinGap.mapAll(pVisitor));
   }
}