package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
   private static final Codec<DensityFunction> CODEC = Registry.DENSITY_FUNCTION_TYPES.byNameCodec().dispatch(DensityFunction::codec, Function.identity());
   protected static final double MAX_REASONABLE_NOISE_VALUE = 1000000.0D;
   static final Codec<Double> NOISE_VALUE_CODEC = Codec.doubleRange(-1000000.0D, 1000000.0D);
   public static final Codec<DensityFunction> DIRECT_CODEC = Codec.either(NOISE_VALUE_CODEC, CODEC).xmap((p_208274_) -> {
      return p_208274_.map(DensityFunctions::constant, Function.identity());
   }, (p_208392_) -> {
      if (p_208392_ instanceof DensityFunctions.Constant) {
         DensityFunctions.Constant densityfunctions$constant = (DensityFunctions.Constant)p_208392_;
         return Either.left(densityfunctions$constant.value());
      } else {
         return Either.right(p_208392_);
      }
   });

   public static Codec<? extends DensityFunction> bootstrap(Registry<Codec<? extends DensityFunction>> pRegistry) {
      register(pRegistry, "blend_alpha", DensityFunctions.BlendAlpha.CODEC);
      register(pRegistry, "blend_offset", DensityFunctions.BlendOffset.CODEC);
      register(pRegistry, "beardifier", DensityFunctions.BeardifierMarker.CODEC);
      register(pRegistry, "old_blended_noise", BlendedNoise.CODEC);

      for(DensityFunctions.Marker.Type densityfunctions$marker$type : DensityFunctions.Marker.Type.values()) {
         register(pRegistry, densityfunctions$marker$type.getSerializedName(), densityfunctions$marker$type.codec);
      }

      register(pRegistry, "noise", DensityFunctions.Noise.CODEC);
      register(pRegistry, "end_islands", DensityFunctions.EndIslandDensityFunction.CODEC);
      register(pRegistry, "weird_scaled_sampler", DensityFunctions.WeirdScaledSampler.CODEC);
      register(pRegistry, "shifted_noise", DensityFunctions.ShiftedNoise.CODEC);
      register(pRegistry, "range_choice", DensityFunctions.RangeChoice.CODEC);
      register(pRegistry, "shift_a", DensityFunctions.ShiftA.CODEC);
      register(pRegistry, "shift_b", DensityFunctions.ShiftB.CODEC);
      register(pRegistry, "shift", DensityFunctions.Shift.CODEC);
      register(pRegistry, "blend_density", DensityFunctions.BlendDensity.CODEC);
      register(pRegistry, "clamp", DensityFunctions.Clamp.CODEC);

      for(DensityFunctions.Mapped.Type densityfunctions$mapped$type : DensityFunctions.Mapped.Type.values()) {
         register(pRegistry, densityfunctions$mapped$type.getSerializedName(), densityfunctions$mapped$type.codec);
      }

      register(pRegistry, "slide", DensityFunctions.Slide.CODEC);

      for(DensityFunctions.TwoArgumentSimpleFunction.Type densityfunctions$twoargumentsimplefunction$type : DensityFunctions.TwoArgumentSimpleFunction.Type.values()) {
         register(pRegistry, densityfunctions$twoargumentsimplefunction$type.getSerializedName(), densityfunctions$twoargumentsimplefunction$type.codec);
      }

      register(pRegistry, "spline", DensityFunctions.Spline.CODEC);
      register(pRegistry, "terrain_shaper_spline", DensityFunctions.TerrainShaperSpline.CODEC);
      register(pRegistry, "constant", DensityFunctions.Constant.CODEC);
      return register(pRegistry, "y_clamped_gradient", DensityFunctions.YClampedGradient.CODEC);
   }

   private static Codec<? extends DensityFunction> register(Registry<Codec<? extends DensityFunction>> pRegistry, String pName, Codec<? extends DensityFunction> pCodec) {
      return Registry.register(pRegistry, pName, pCodec);
   }

   static <A, O> Codec<O> singleArgumentCodec(Codec<A> pCodec, Function<A, O> pFromFunction, Function<O, A> pToFunction) {
      return pCodec.fieldOf("argument").xmap(pFromFunction, pToFunction).codec();
   }

   static <O> Codec<O> singleFunctionArgumentCodec(Function<DensityFunction, O> pFromFunction, Function<O, DensityFunction> pTpFunction) {
      return singleArgumentCodec(DensityFunction.HOLDER_HELPER_CODEC, pFromFunction, pTpFunction);
   }

   static <O> Codec<O> doubleFunctionArgumentCodec(BiFunction<DensityFunction, DensityFunction, O> pFromFunction, Function<O, DensityFunction> pPrimary, Function<O, DensityFunction> pSecondary) {
      return RecordCodecBuilder.create((p_208359_) -> {
         return p_208359_.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(pPrimary), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(pSecondary)).apply(p_208359_, pFromFunction);
      });
   }

   static <O> Codec<O> makeCodec(MapCodec<O> pMapCodec) {
      return pMapCodec.codec();
   }

   private DensityFunctions() {
   }

   public static DensityFunction interpolated(DensityFunction pWrapped) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Interpolated, pWrapped);
   }

   public static DensityFunction flatCache(DensityFunction pWrapped) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.FlatCache, pWrapped);
   }

   public static DensityFunction cache2d(DensityFunction pWrapped) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Cache2D, pWrapped);
   }

   public static DensityFunction cacheOnce(DensityFunction pWrapped) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheOnce, pWrapped);
   }

   public static DensityFunction cacheAllInCell(DensityFunction pWrapped) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheAllInCell, pWrapped);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> pNoiseData, @Deprecated double pXzScale, double pYScale, double p_208340_, double p_208341_) {
      return mapFromUnitTo(new DensityFunctions.Noise(pNoiseData, (NormalNoise)null, pXzScale, pYScale), p_208340_, p_208341_);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> pNoiseData, double pYScale, double p_208334_, double p_208335_) {
      return mappedNoise(pNoiseData, 1.0D, pYScale, p_208334_, p_208335_);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> pNoiseData, double p_208329_, double p_208330_) {
      return mappedNoise(pNoiseData, 1.0D, 1.0D, p_208329_, p_208330_);
   }

   public static DensityFunction shiftedNoise2d(DensityFunction pShiftX, DensityFunction pShiftZ, double pXzScale, Holder<NormalNoise.NoiseParameters> pNoiseData) {
      return new DensityFunctions.ShiftedNoise(pShiftX, zero(), pShiftZ, pXzScale, 0.0D, pNoiseData, (NormalNoise)null);
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> pNoiseData) {
      return noise(pNoiseData, 1.0D, 1.0D);
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> pNoiseData, double pXzScale, double pYScale) {
      return new DensityFunctions.Noise(pNoiseData, (NormalNoise)null, pXzScale, pYScale);
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> pNoiseData, double pYScale) {
      return noise(pNoiseData, 1.0D, pYScale);
   }

   public static DensityFunction rangeChoice(DensityFunction pInput, double pMinInclusive, double pMaxExclusive, DensityFunction pWhenInRange, DensityFunction pWhenOutOfRange) {
      return new DensityFunctions.RangeChoice(pInput, pMinInclusive, pMaxExclusive, pWhenInRange, pWhenOutOfRange);
   }

   public static DensityFunction shiftA(Holder<NormalNoise.NoiseParameters> pNoiseData) {
      return new DensityFunctions.ShiftA(pNoiseData, (NormalNoise)null);
   }

   public static DensityFunction shiftB(Holder<NormalNoise.NoiseParameters> pNoiseData) {
      return new DensityFunctions.ShiftB(pNoiseData, (NormalNoise)null);
   }

   public static DensityFunction shift(Holder<NormalNoise.NoiseParameters> pNoiseData) {
      return new DensityFunctions.Shift(pNoiseData, (NormalNoise)null);
   }

   public static DensityFunction blendDensity(DensityFunction pInput) {
      return new DensityFunctions.BlendDensity(pInput);
   }

   public static DensityFunction endIslands(long pSeed) {
      return new DensityFunctions.EndIslandDensityFunction(pSeed);
   }

   public static DensityFunction weirdScaledSampler(DensityFunction pInput, Holder<NormalNoise.NoiseParameters> pNoiseData, DensityFunctions.WeirdScaledSampler.RarityValueMapper pRarityValueMapper) {
      return new DensityFunctions.WeirdScaledSampler(pInput, pNoiseData, (NormalNoise)null, pRarityValueMapper);
   }

   public static DensityFunction slide(NoiseSettings pSettings, DensityFunction pInput) {
      return new DensityFunctions.Slide(pSettings, pInput);
   }

   public static DensityFunction add(DensityFunction pArgument1, DensityFunction pArgument2) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, pArgument1, pArgument2);
   }

   public static DensityFunction mul(DensityFunction pArgument1, DensityFunction pArgument2) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL, pArgument1, pArgument2);
   }

   public static DensityFunction min(DensityFunction pArgument1, DensityFunction pArgument2) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MIN, pArgument1, pArgument2);
   }

   public static DensityFunction max(DensityFunction pArgument1, DensityFunction pArgument2) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MAX, pArgument1, pArgument2);
   }

   public static DensityFunction terrainShaperSpline(DensityFunction pContinentalness, DensityFunction pErosion, DensityFunction pWeirdness, DensityFunctions.TerrainShaperSpline.SplineType pSpline, double pMinValue, double pMaxValue) {
      return new DensityFunctions.TerrainShaperSpline(pContinentalness, pErosion, pWeirdness, (TerrainShaper)null, pSpline, pMinValue, pMaxValue);
   }

   public static DensityFunction zero() {
      return DensityFunctions.Constant.ZERO;
   }

   public static DensityFunction constant(double p_208265_) {
      return new DensityFunctions.Constant(p_208265_);
   }

   public static DensityFunction yClampedGradient(int pFromY, int pToY, double pFromValue, double pToValue) {
      return new DensityFunctions.YClampedGradient(pFromY, pToY, pFromValue, pToValue);
   }

   public static DensityFunction map(DensityFunction pInput, DensityFunctions.Mapped.Type pType) {
      return DensityFunctions.Mapped.create(pType, pInput);
   }

   private static DensityFunction mapFromUnitTo(DensityFunction pDensityFunction, double pFromY, double pToY) {
      double d0 = (pFromY + pToY) * 0.5D;
      double d1 = (pToY - pFromY) * 0.5D;
      return add(constant(d0), mul(constant(d1), pDensityFunction));
   }

   public static DensityFunction blendAlpha() {
      return DensityFunctions.BlendAlpha.INSTANCE;
   }

   public static DensityFunction blendOffset() {
      return DensityFunctions.BlendOffset.INSTANCE;
   }

   public static DensityFunction lerp(DensityFunction pMinFunction, DensityFunction pMaxFunction, DensityFunction pDeltaFunction) {
      DensityFunction densityfunction = cacheOnce(pMinFunction);
      DensityFunction densityfunction1 = add(mul(densityfunction, constant(-1.0D)), constant(1.0D));
      return add(mul(pMaxFunction, densityfunction1), mul(pDeltaFunction, densityfunction));
   }

   static record Ap2(DensityFunctions.TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue) implements DensityFunctions.TwoArgumentSimpleFunction {
      public double compute(DensityFunction.FunctionContext p_208410_) {
         double d0 = this.argument1.compute(p_208410_);
         double d1;
         switch(this.type) {
         case ADD:
            d1 = d0 + this.argument2.compute(p_208410_);
            break;
         case MAX:
            d1 = d0 > this.argument2.maxValue() ? d0 : Math.max(d0, this.argument2.compute(p_208410_));
            break;
         case MIN:
            d1 = d0 < this.argument2.minValue() ? d0 : Math.min(d0, this.argument2.compute(p_208410_));
            break;
         case MUL:
            d1 = d0 == 0.0D ? 0.0D : d0 * this.argument2.compute(p_208410_);
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         return d1;
      }

      public void fillArray(double[] p_208414_, DensityFunction.ContextProvider p_208415_) {
         this.argument1.fillArray(p_208414_, p_208415_);
         switch(this.type) {
         case ADD:
            double[] adouble = new double[p_208414_.length];
            this.argument2.fillArray(adouble, p_208415_);

            for(int k = 0; k < p_208414_.length; ++k) {
               p_208414_[k] += adouble[k];
            }
            break;
         case MAX:
            double d3 = this.argument2.maxValue();

            for(int l = 0; l < p_208414_.length; ++l) {
               double d4 = p_208414_[l];
               p_208414_[l] = d4 > d3 ? d4 : Math.max(d4, this.argument2.compute(p_208415_.forIndex(l)));
            }
            break;
         case MIN:
            double d2 = this.argument2.minValue();

            for(int j = 0; j < p_208414_.length; ++j) {
               double d1 = p_208414_[j];
               p_208414_[j] = d1 < d2 ? d1 : Math.min(d1, this.argument2.compute(p_208415_.forIndex(j)));
            }
            break;
         case MUL:
            for(int i = 0; i < p_208414_.length; ++i) {
               double d0 = p_208414_[i];
               p_208414_[i] = d0 == 0.0D ? 0.0D : d0 * this.argument2.compute(p_208415_.forIndex(i));
            }
         }

      }

      public DensityFunction mapAll(DensityFunction.Visitor p_208412_) {
         return p_208412_.apply(DensityFunctions.TwoArgumentSimpleFunction.create(this.type, this.argument1.mapAll(p_208412_), this.argument2.mapAll(p_208412_)));
      }

      public double minValue() {
         return this.minValue;
      }

      public double maxValue() {
         return this.maxValue;
      }

      public DensityFunctions.TwoArgumentSimpleFunction.Type type() {
         return this.type;
      }

      public DensityFunction argument1() {
         return this.argument1;
      }

      public DensityFunction argument2() {
         return this.argument2;
      }
   }

   protected static enum BeardifierMarker implements DensityFunctions.BeardifierOrMarker {
      INSTANCE;

      public double compute(DensityFunction.FunctionContext p_208515_) {
         return 0.0D;
      }

      public void fillArray(double[] p_208517_, DensityFunction.ContextProvider p_208518_) {
         Arrays.fill(p_208517_, 0.0D);
      }

      public double minValue() {
         return 0.0D;
      }

      public double maxValue() {
         return 0.0D;
      }
   }

   public interface BeardifierOrMarker extends DensityFunction.SimpleFunction {
      Codec<DensityFunction> CODEC = Codec.unit(DensityFunctions.BeardifierMarker.INSTANCE);

      default Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static enum BlendAlpha implements DensityFunction.SimpleFunction {
      INSTANCE;

      public static final Codec<DensityFunction> CODEC = Codec.unit(INSTANCE);

      public double compute(DensityFunction.FunctionContext p_208536_) {
         return 1.0D;
      }

      public void fillArray(double[] p_208538_, DensityFunction.ContextProvider p_208539_) {
         Arrays.fill(p_208538_, 1.0D);
      }

      public double minValue() {
         return 1.0D;
      }

      public double maxValue() {
         return 1.0D;
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   static record BlendDensity(DensityFunction input) implements DensityFunctions.TransformerWithContext {
      static final Codec<DensityFunctions.BlendDensity> CODEC = DensityFunctions.singleFunctionArgumentCodec(DensityFunctions.BlendDensity::new, DensityFunctions.BlendDensity::input);

      public double transform(DensityFunction.FunctionContext p_208553_, double p_208554_) {
         return p_208553_.getBlender().blendDensity(p_208553_, p_208554_);
      }

      public DensityFunction mapAll(DensityFunction.Visitor p_208556_) {
         return p_208556_.apply(new DensityFunctions.BlendDensity(this.input.mapAll(p_208556_)));
      }

      public double minValue() {
         return Double.NEGATIVE_INFINITY;
      }

      public double maxValue() {
         return Double.POSITIVE_INFINITY;
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public DensityFunction input() {
         return this.input;
      }
   }

   protected static enum BlendOffset implements DensityFunction.SimpleFunction {
      INSTANCE;

      public static final Codec<DensityFunction> CODEC = Codec.unit(INSTANCE);

      public double compute(DensityFunction.FunctionContext p_208573_) {
         return 0.0D;
      }

      public void fillArray(double[] p_208575_, DensityFunction.ContextProvider p_208576_) {
         Arrays.fill(p_208575_, 0.0D);
      }

      public double minValue() {
         return 0.0D;
      }

      public double maxValue() {
         return 0.0D;
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static record Clamp(DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
      private static final MapCodec<DensityFunctions.Clamp> DATA_CODEC = RecordCodecBuilder.mapCodec((p_208597_) -> {
         return p_208597_.group(DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(DensityFunctions.Clamp::input), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min").forGetter(DensityFunctions.Clamp::minValue), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max").forGetter(DensityFunctions.Clamp::maxValue)).apply(p_208597_, DensityFunctions.Clamp::new);
      });
      public static final Codec<DensityFunctions.Clamp> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double transform(double pValue) {
         return Mth.clamp(pValue, this.minValue, this.maxValue);
      }

      public DensityFunction mapAll(DensityFunction.Visitor pVisitor) {
         return new DensityFunctions.Clamp(this.input.mapAll(pVisitor), this.minValue, this.maxValue);
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public DensityFunction input() {
         return this.input;
      }

      public double minValue() {
         return this.minValue;
      }

      public double maxValue() {
         return this.maxValue;
      }
   }

   static record Constant(double value) implements DensityFunction.SimpleFunction {
      static final Codec<DensityFunctions.Constant> CODEC = DensityFunctions.singleArgumentCodec(DensityFunctions.NOISE_VALUE_CODEC, DensityFunctions.Constant::new, DensityFunctions.Constant::value);
      static final DensityFunctions.Constant ZERO = new DensityFunctions.Constant(0.0D);

      public double compute(DensityFunction.FunctionContext p_208615_) {
         return this.value;
      }

      public void fillArray(double[] p_208617_, DensityFunction.ContextProvider p_208618_) {
         Arrays.fill(p_208617_, this.value);
      }

      public double minValue() {
         return this.value;
      }

      public double maxValue() {
         return this.value;
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
      public static final Codec<DensityFunctions.EndIslandDensityFunction> CODEC = Codec.unit(new DensityFunctions.EndIslandDensityFunction(0L));
      final SimplexNoise islandNoise;

      public EndIslandDensityFunction(long pSeed) {
         RandomSource randomsource = new LegacyRandomSource(pSeed);
         randomsource.consumeCount(17292);
         this.islandNoise = new SimplexNoise(randomsource);
      }

      public double compute(DensityFunction.FunctionContext pContext) {
         return ((double)TheEndBiomeSource.getHeightValue(this.islandNoise, pContext.blockX() / 8, pContext.blockZ() / 8) - 8.0D) / 128.0D;
      }

      public double minValue() {
         return -0.84375D;
      }

      public double maxValue() {
         return 0.5625D;
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static record HolderHolder(Holder<DensityFunction> function) implements DensityFunction {
      public double compute(DensityFunction.FunctionContext p_208641_) {
         return this.function.value().compute(p_208641_);
      }

      public void fillArray(double[] p_208645_, DensityFunction.ContextProvider p_208646_) {
         this.function.value().fillArray(p_208645_, p_208646_);
      }

      public DensityFunction mapAll(DensityFunction.Visitor p_208643_) {
         return p_208643_.apply(new DensityFunctions.HolderHolder(new Holder.Direct<>(this.function.value().mapAll(p_208643_))));
      }

      public double minValue() {
         return this.function.value().minValue();
      }

      public double maxValue() {
         return this.function.value().maxValue();
      }

      public Codec<? extends DensityFunction> codec() {
         throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
      }
   }

   protected static record Mapped(DensityFunctions.Mapped.Type type, DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
      public static DensityFunctions.Mapped create(DensityFunctions.Mapped.Type pType, DensityFunction pInput) {
         double d0 = pInput.minValue();
         double d1 = transform(pType, d0);
         double d2 = transform(pType, pInput.maxValue());
         return pType != DensityFunctions.Mapped.Type.ABS && pType != DensityFunctions.Mapped.Type.SQUARE ? new DensityFunctions.Mapped(pType, pInput, d1, d2) : new DensityFunctions.Mapped(pType, pInput, Math.max(0.0D, d0), Math.max(d1, d2));
      }

      private static double transform(DensityFunctions.Mapped.Type pType, double pValue) {
         double d1;
         switch(pType) {
         case ABS:
            d1 = Math.abs(pValue);
            break;
         case SQUARE:
            d1 = pValue * pValue;
            break;
         case CUBE:
            d1 = pValue * pValue * pValue;
            break;
         case HALF_NEGATIVE:
            d1 = pValue > 0.0D ? pValue : pValue * 0.5D;
            break;
         case QUARTER_NEGATIVE:
            d1 = pValue > 0.0D ? pValue : pValue * 0.25D;
            break;
         case SQUEEZE:
            double d0 = Mth.clamp(pValue, -1.0D, 1.0D);
            d1 = d0 / 2.0D - d0 * d0 * d0 / 24.0D;
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         return d1;
      }

      public double transform(double pValue) {
         return transform(this.type, pValue);
      }

      public DensityFunctions.Mapped mapAll(DensityFunction.Visitor pVisitor) {
         return create(this.type, this.input.mapAll(pVisitor));
      }

      public Codec<? extends DensityFunction> codec() {
         return this.type.codec;
      }

      public DensityFunction input() {
         return this.input;
      }

      public double minValue() {
         return this.minValue;
      }

      public double maxValue() {
         return this.maxValue;
      }

      static enum Type implements StringRepresentable {
         ABS("abs"),
         SQUARE("square"),
         CUBE("cube"),
         HALF_NEGATIVE("half_negative"),
         QUARTER_NEGATIVE("quarter_negative"),
         SQUEEZE("squeeze");

         private final String name;
         final Codec<DensityFunctions.Mapped> codec = DensityFunctions.singleFunctionArgumentCodec((p_208700_) -> {
            return DensityFunctions.Mapped.create(this, p_208700_);
         }, DensityFunctions.Mapped::input);

         private Type(String pName) {
            this.name = pName;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   protected static record Marker(DensityFunctions.Marker.Type type, DensityFunction wrapped) implements DensityFunctions.MarkerOrMarked {
      public double compute(DensityFunction.FunctionContext pContext) {
         return this.wrapped.compute(pContext);
      }

      public void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider) {
         this.wrapped.fillArray(pArray, pContextProvider);
      }

      public DensityFunction mapAll(DensityFunction.Visitor pVisitor) {
         return pVisitor.apply(new DensityFunctions.Marker(this.type, this.wrapped.mapAll(pVisitor)));
      }

      public double minValue() {
         return this.wrapped.minValue();
      }

      public double maxValue() {
         return this.wrapped.maxValue();
      }

      public DensityFunctions.Marker.Type type() {
         return this.type;
      }

      public DensityFunction wrapped() {
         return this.wrapped;
      }

      static enum Type implements StringRepresentable {
         Interpolated("interpolated"),
         FlatCache("flat_cache"),
         Cache2D("cache_2d"),
         CacheOnce("cache_once"),
         CacheAllInCell("cache_all_in_cell");

         private final String name;
         final Codec<DensityFunctions.MarkerOrMarked> codec = DensityFunctions.singleFunctionArgumentCodec((p_208740_) -> {
            return new DensityFunctions.Marker(this, p_208740_);
         }, DensityFunctions.MarkerOrMarked::wrapped);

         private Type(String pName) {
            this.name = pName;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   public interface MarkerOrMarked extends DensityFunction {
      DensityFunctions.Marker.Type type();

      DensityFunction wrapped();

      default Codec<? extends DensityFunction> codec() {
         return this.type().codec;
      }
   }

   static record MulOrAdd(DensityFunctions.MulOrAdd.Type specificType, DensityFunction input, double minValue, double maxValue, double argument) implements DensityFunctions.TwoArgumentSimpleFunction, DensityFunctions.PureTransformer {
      public DensityFunctions.TwoArgumentSimpleFunction.Type type() {
         return this.specificType == DensityFunctions.MulOrAdd.Type.MUL ? DensityFunctions.TwoArgumentSimpleFunction.Type.MUL : DensityFunctions.TwoArgumentSimpleFunction.Type.ADD;
      }

      public DensityFunction argument1() {
         return DensityFunctions.constant(this.argument);
      }

      public DensityFunction argument2() {
         return this.input;
      }

      public double transform(double p_208759_) {
         double d0;
         switch(this.specificType) {
         case MUL:
            d0 = p_208759_ * this.argument;
            break;
         case ADD:
            d0 = p_208759_ + this.argument;
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         return d0;
      }

      public DensityFunction mapAll(DensityFunction.Visitor p_208761_) {
         DensityFunction densityfunction = this.input.mapAll(p_208761_);
         double d0 = densityfunction.minValue();
         double d1 = densityfunction.maxValue();
         double d2;
         double d3;
         if (this.specificType == DensityFunctions.MulOrAdd.Type.ADD) {
            d2 = d0 + this.argument;
            d3 = d1 + this.argument;
         } else if (this.argument >= 0.0D) {
            d2 = d0 * this.argument;
            d3 = d1 * this.argument;
         } else {
            d2 = d1 * this.argument;
            d3 = d0 * this.argument;
         }

         return new DensityFunctions.MulOrAdd(this.specificType, densityfunction, d2, d3, this.argument);
      }

      public DensityFunction input() {
         return this.input;
      }

      public double minValue() {
         return this.minValue;
      }

      public double maxValue() {
         return this.maxValue;
      }

      static enum Type {
         MUL,
         ADD;
      }
   }

   protected static record Noise(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise, double xzScale, double yScale) implements DensityFunction.SimpleFunction {
      public static final MapCodec<DensityFunctions.Noise> DATA_CODEC = RecordCodecBuilder.mapCodec((p_208798_) -> {
         return p_208798_.group(NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.Noise::noiseData), Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.Noise::xzScale), Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.Noise::yScale)).apply(p_208798_, DensityFunctions.Noise::createUnseeded);
      });
      public static final Codec<DensityFunctions.Noise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public static DensityFunctions.Noise createUnseeded(Holder<NormalNoise.NoiseParameters> p_208802_, @Deprecated double p_208803_, double p_208804_) {
         return new DensityFunctions.Noise(p_208802_, (NormalNoise)null, p_208803_, p_208804_);
      }

      public double compute(DensityFunction.FunctionContext pContext) {
         return this.noise == null ? 0.0D : this.noise.getValue((double)pContext.blockX() * this.xzScale, (double)pContext.blockY() * this.yScale, (double)pContext.blockZ() * this.xzScale);
      }

      public double minValue() {
         return -this.maxValue();
      }

      public double maxValue() {
         return this.noise == null ? 2.0D : this.noise.maxValue();
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   interface PureTransformer extends DensityFunction {
      DensityFunction input();

      default double compute(DensityFunction.FunctionContext pContext) {
         return this.transform(this.input().compute(pContext));
      }

      default void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider) {
         this.input().fillArray(pArray, pContextProvider);

         for(int i = 0; i < pArray.length; ++i) {
            pArray[i] = this.transform(pArray[i]);
         }

      }

      double transform(double pValue);
   }

   static record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) implements DensityFunction {
      public static final MapCodec<DensityFunctions.RangeChoice> DATA_CODEC = RecordCodecBuilder.mapCodec((p_208837_) -> {
         return p_208837_.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.RangeChoice::input), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_inclusive").forGetter(DensityFunctions.RangeChoice::minInclusive), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_exclusive").forGetter(DensityFunctions.RangeChoice::maxExclusive), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_in_range").forGetter(DensityFunctions.RangeChoice::whenInRange), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_out_of_range").forGetter(DensityFunctions.RangeChoice::whenOutOfRange)).apply(p_208837_, DensityFunctions.RangeChoice::new);
      });
      public static final Codec<DensityFunctions.RangeChoice> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double compute(DensityFunction.FunctionContext pContext) {
         double d0 = this.input.compute(pContext);
         return d0 >= this.minInclusive && d0 < this.maxExclusive ? this.whenInRange.compute(pContext) : this.whenOutOfRange.compute(pContext);
      }

      public void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider) {
         this.input.fillArray(pArray, pContextProvider);

         for(int i = 0; i < pArray.length; ++i) {
            double d0 = pArray[i];
            if (d0 >= this.minInclusive && d0 < this.maxExclusive) {
               pArray[i] = this.whenInRange.compute(pContextProvider.forIndex(i));
            } else {
               pArray[i] = this.whenOutOfRange.compute(pContextProvider.forIndex(i));
            }
         }

      }

      public DensityFunction mapAll(DensityFunction.Visitor pVisitor) {
         return pVisitor.apply(new DensityFunctions.RangeChoice(this.input.mapAll(pVisitor), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(pVisitor), this.whenOutOfRange.mapAll(pVisitor)));
      }

      public double minValue() {
         return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
      }

      public double maxValue() {
         return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   static record Shift(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
      static final Codec<DensityFunctions.Shift> CODEC = DensityFunctions.singleArgumentCodec(NormalNoise.NoiseParameters.CODEC, (p_208868_) -> {
         return new DensityFunctions.Shift(p_208868_, (NormalNoise)null);
      }, DensityFunctions.Shift::noiseData);

      public double compute(DensityFunction.FunctionContext pContext) {
         return this.compute((double)pContext.blockX(), (double)pContext.blockY(), (double)pContext.blockZ());
      }

      public DensityFunctions.ShiftNoise withNewNoise(NormalNoise pOffsetNoise) {
         return new DensityFunctions.Shift(this.noiseData, pOffsetNoise);
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public Holder<NormalNoise.NoiseParameters> noiseData() {
         return this.noiseData;
      }

      @Nullable
      public NormalNoise offsetNoise() {
         return this.offsetNoise;
      }
   }

   protected static record ShiftA(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
      static final Codec<DensityFunctions.ShiftA> CODEC = DensityFunctions.singleArgumentCodec(NormalNoise.NoiseParameters.CODEC, (p_208888_) -> {
         return new DensityFunctions.ShiftA(p_208888_, (NormalNoise)null);
      }, DensityFunctions.ShiftA::noiseData);

      public double compute(DensityFunction.FunctionContext pContext) {
         return this.compute((double)pContext.blockX(), 0.0D, (double)pContext.blockZ());
      }

      public DensityFunctions.ShiftNoise withNewNoise(NormalNoise pOffsetNoise) {
         return new DensityFunctions.ShiftA(this.noiseData, pOffsetNoise);
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public Holder<NormalNoise.NoiseParameters> noiseData() {
         return this.noiseData;
      }

      @Nullable
      public NormalNoise offsetNoise() {
         return this.offsetNoise;
      }
   }

   protected static record ShiftB(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
      static final Codec<DensityFunctions.ShiftB> CODEC = DensityFunctions.singleArgumentCodec(NormalNoise.NoiseParameters.CODEC, (p_208908_) -> {
         return new DensityFunctions.ShiftB(p_208908_, (NormalNoise)null);
      }, DensityFunctions.ShiftB::noiseData);

      public double compute(DensityFunction.FunctionContext pContext) {
         return this.compute((double)pContext.blockZ(), (double)pContext.blockX(), 0.0D);
      }

      public DensityFunctions.ShiftNoise withNewNoise(NormalNoise pOffsetNoise) {
         return new DensityFunctions.ShiftB(this.noiseData, pOffsetNoise);
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public Holder<NormalNoise.NoiseParameters> noiseData() {
         return this.noiseData;
      }

      @Nullable
      public NormalNoise offsetNoise() {
         return this.offsetNoise;
      }
   }

   interface ShiftNoise extends DensityFunction.SimpleFunction {
      Holder<NormalNoise.NoiseParameters> noiseData();

      @Nullable
      NormalNoise offsetNoise();

      default double minValue() {
         return -this.maxValue();
      }

      default double maxValue() {
         NormalNoise normalnoise = this.offsetNoise();
         return (normalnoise == null ? 2.0D : normalnoise.maxValue()) * 4.0D;
      }

      default double compute(double pX, double pY, double pZ) {
         NormalNoise normalnoise = this.offsetNoise();
         return normalnoise == null ? 0.0D : normalnoise.getValue(pX * 0.25D, pY * 0.25D, pZ * 0.25D) * 4.0D;
      }

      DensityFunctions.ShiftNoise withNewNoise(NormalNoise pOffsetNoise);
   }

   protected static record ShiftedNoise(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise) implements DensityFunction {
      private static final MapCodec<DensityFunctions.ShiftedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec((p_208943_) -> {
         return p_208943_.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(DensityFunctions.ShiftedNoise::shiftX), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(DensityFunctions.ShiftedNoise::shiftY), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(DensityFunctions.ShiftedNoise::shiftZ), Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.ShiftedNoise::xzScale), Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.ShiftedNoise::yScale), NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.ShiftedNoise::noiseData)).apply(p_208943_, DensityFunctions.ShiftedNoise::createUnseeded);
      });
      public static final Codec<DensityFunctions.ShiftedNoise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public static DensityFunctions.ShiftedNoise createUnseeded(DensityFunction p_208949_, DensityFunction p_208950_, DensityFunction p_208951_, double p_208952_, double p_208953_, Holder<NormalNoise.NoiseParameters> p_208954_) {
         return new DensityFunctions.ShiftedNoise(p_208949_, p_208950_, p_208951_, p_208952_, p_208953_, p_208954_, (NormalNoise)null);
      }

      public double compute(DensityFunction.FunctionContext pContext) {
         if (this.noise == null) {
            return 0.0D;
         } else {
            double d0 = (double)pContext.blockX() * this.xzScale + this.shiftX.compute(pContext);
            double d1 = (double)pContext.blockY() * this.yScale + this.shiftY.compute(pContext);
            double d2 = (double)pContext.blockZ() * this.xzScale + this.shiftZ.compute(pContext);
            return this.noise.getValue(d0, d1, d2);
         }
      }

      public void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider) {
         pContextProvider.fillAllDirectly(pArray, this);
      }

      public DensityFunction mapAll(DensityFunction.Visitor pVisitor) {
         return pVisitor.apply(new DensityFunctions.ShiftedNoise(this.shiftX.mapAll(pVisitor), this.shiftY.mapAll(pVisitor), this.shiftZ.mapAll(pVisitor), this.xzScale, this.yScale, this.noiseData, this.noise));
      }

      public double minValue() {
         return -this.maxValue();
      }

      public double maxValue() {
         return this.noise == null ? 2.0D : this.noise.maxValue();
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static record Slide(@Nullable NoiseSettings settings, DensityFunction input) implements DensityFunctions.TransformerWithContext {
      public static final Codec<DensityFunctions.Slide> CODEC = DensityFunctions.singleFunctionArgumentCodec((p_208985_) -> {
         return new DensityFunctions.Slide((NoiseSettings)null, p_208985_);
      }, DensityFunctions.Slide::input);

      public double transform(DensityFunction.FunctionContext pContext, double pValue) {
         return this.settings == null ? pValue : NoiseRouterData.applySlide(this.settings, pValue, (double)pContext.blockY());
      }

      public DensityFunction mapAll(DensityFunction.Visitor pVisitor) {
         return pVisitor.apply(new DensityFunctions.Slide(this.settings, this.input.mapAll(pVisitor)));
      }

      public double minValue() {
         return this.settings == null ? this.input.minValue() : Math.min(this.input.minValue(), Math.min(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
      }

      public double maxValue() {
         return this.settings == null ? this.input.maxValue() : Math.max(this.input.maxValue(), Math.max(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public DensityFunction input() {
         return this.input;
      }
   }

   public static record Spline(CubicSpline<TerrainShaper.PointCustom> spline, double minValue, double maxValue) implements DensityFunction {
      private static final MapCodec<DensityFunctions.Spline> DATA_CODEC = RecordCodecBuilder.mapCodec((p_211713_) -> {
         return p_211713_.group(TerrainShaper.SPLINE_CUSTOM_CODEC.fieldOf("spline").forGetter(DensityFunctions.Spline::spline), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_value").forGetter(DensityFunctions.Spline::minValue), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_value").forGetter(DensityFunctions.Spline::maxValue)).apply(p_211713_, DensityFunctions.Spline::new);
      });
      public static final Codec<DensityFunctions.Spline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double compute(DensityFunction.FunctionContext pContext) {
         return Mth.clamp((double)this.spline.apply(TerrainShaper.makePoint(pContext)), this.minValue, this.maxValue);
      }

      public void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider) {
         pContextProvider.fillAllDirectly(pArray, this);
      }

      public DensityFunction mapAll(DensityFunction.Visitor pVisitor) {
         return pVisitor.apply(new DensityFunctions.Spline(this.spline.mapAll((p_211720_) -> {
            Object object;
            if (p_211720_ instanceof TerrainShaper.CoordinateCustom) {
               TerrainShaper.CoordinateCustom terrainshaper$coordinatecustom = (TerrainShaper.CoordinateCustom)p_211720_;
               object = terrainshaper$coordinatecustom.mapAll(pVisitor);
            } else {
               object = p_211720_;
            }

            return (ToFloatFunction<TerrainShaper.PointCustom>)object;
         }), this.minValue, this.maxValue));
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public double minValue() {
         return this.minValue;
      }

      public double maxValue() {
         return this.maxValue;
      }
   }

   /** @deprecated */
   @Deprecated
   public static record TerrainShaperSpline(DensityFunction continentalness, DensityFunction erosion, DensityFunction weirdness, @Nullable TerrainShaper shaper, DensityFunctions.TerrainShaperSpline.SplineType spline, double minValue, double maxValue) implements DensityFunction {
      private static final MapCodec<DensityFunctions.TerrainShaperSpline> DATA_CODEC = RecordCodecBuilder.mapCodec((p_209014_) -> {
         return p_209014_.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("continentalness").forGetter(DensityFunctions.TerrainShaperSpline::continentalness), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("erosion").forGetter(DensityFunctions.TerrainShaperSpline::erosion), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("weirdness").forGetter(DensityFunctions.TerrainShaperSpline::weirdness), DensityFunctions.TerrainShaperSpline.SplineType.CODEC.fieldOf("spline").forGetter(DensityFunctions.TerrainShaperSpline::spline), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_value").forGetter(DensityFunctions.TerrainShaperSpline::minValue), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_value").forGetter(DensityFunctions.TerrainShaperSpline::maxValue)).apply(p_209014_, DensityFunctions.TerrainShaperSpline::createUnseeded);
      });
      public static final Codec<DensityFunctions.TerrainShaperSpline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public static DensityFunctions.TerrainShaperSpline createUnseeded(DensityFunction p_209020_, DensityFunction p_209021_, DensityFunction p_209022_, DensityFunctions.TerrainShaperSpline.SplineType p_209023_, double p_209024_, double p_209025_) {
         return new DensityFunctions.TerrainShaperSpline(p_209020_, p_209021_, p_209022_, (TerrainShaper)null, p_209023_, p_209024_, p_209025_);
      }

      public double compute(DensityFunction.FunctionContext pContext) {
         return this.shaper == null ? 0.0D : Mth.clamp((double)this.spline.spline.apply(this.shaper, TerrainShaper.makePoint((float)this.continentalness.compute(pContext), (float)this.erosion.compute(pContext), (float)this.weirdness.compute(pContext))), this.minValue, this.maxValue);
      }

      public void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider) {
         for(int i = 0; i < pArray.length; ++i) {
            pArray[i] = this.compute(pContextProvider.forIndex(i));
         }

      }

      public DensityFunction mapAll(DensityFunction.Visitor pVisitor) {
         return pVisitor.apply(new DensityFunctions.TerrainShaperSpline(this.continentalness.mapAll(pVisitor), this.erosion.mapAll(pVisitor), this.weirdness.mapAll(pVisitor), this.shaper, this.spline, this.minValue, this.maxValue));
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public double minValue() {
         return this.minValue;
      }

      public double maxValue() {
         return this.maxValue;
      }

      interface Spline {
         float apply(TerrainShaper pShaper, TerrainShaper.Point pPoint);
      }

      public static enum SplineType implements StringRepresentable {
         OFFSET("offset", TerrainShaper::offset),
         FACTOR("factor", TerrainShaper::factor),
         JAGGEDNESS("jaggedness", TerrainShaper::jaggedness);

         private static final Map<String, DensityFunctions.TerrainShaperSpline.SplineType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(DensityFunctions.TerrainShaperSpline.SplineType::getSerializedName, (p_209059_) -> {
            return p_209059_;
         }));
         public static final Codec<DensityFunctions.TerrainShaperSpline.SplineType> CODEC = StringRepresentable.fromEnum(DensityFunctions.TerrainShaperSpline.SplineType::values, BY_NAME::get);
         private final String name;
         final DensityFunctions.TerrainShaperSpline.Spline spline;

         private SplineType(String pName, DensityFunctions.TerrainShaperSpline.Spline pSpline) {
            this.name = pName;
            this.spline = pSpline;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   interface TransformerWithContext extends DensityFunction {
      DensityFunction input();

      default double compute(DensityFunction.FunctionContext pContext) {
         return this.transform(pContext, this.input().compute(pContext));
      }

      default void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider) {
         this.input().fillArray(pArray, pContextProvider);

         for(int i = 0; i < pArray.length; ++i) {
            pArray[i] = this.transform(pContextProvider.forIndex(i), pArray[i]);
         }

      }

      double transform(DensityFunction.FunctionContext pContext, double pValue);
   }

   interface TwoArgumentSimpleFunction extends DensityFunction {
      Logger LOGGER = LogUtils.getLogger();

      static DensityFunctions.TwoArgumentSimpleFunction create(DensityFunctions.TwoArgumentSimpleFunction.Type pType, DensityFunction pArgument1, DensityFunction pArgument2) {
         double d0 = pArgument1.minValue();
         double d1 = pArgument2.minValue();
         double d2 = pArgument1.maxValue();
         double d3 = pArgument2.maxValue();
         if (pType == DensityFunctions.TwoArgumentSimpleFunction.Type.MIN || pType == DensityFunctions.TwoArgumentSimpleFunction.Type.MAX) {
            boolean flag = d0 >= d3;
            boolean flag1 = d1 >= d2;
            if (flag || flag1) {
               LOGGER.warn("Creating a " + pType + " function between two non-overlapping inputs: " + pArgument1 + " and " + pArgument2);
            }
         }

         double d6;
         switch(pType) {
         case ADD:
            d6 = d0 + d1;
            break;
         case MAX:
            d6 = Math.max(d0, d1);
            break;
         case MIN:
            d6 = Math.min(d0, d1);
            break;
         case MUL:
            d6 = d0 > 0.0D && d1 > 0.0D ? d0 * d1 : (d2 < 0.0D && d3 < 0.0D ? d2 * d3 : Math.min(d0 * d3, d2 * d1));
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         double d5 = d6;
         switch(pType) {
         case ADD:
            d6 = d2 + d3;
            break;
         case MAX:
            d6 = Math.max(d2, d3);
            break;
         case MIN:
            d6 = Math.min(d2, d3);
            break;
         case MUL:
            d6 = d0 > 0.0D && d1 > 0.0D ? d2 * d3 : (d2 < 0.0D && d3 < 0.0D ? d0 * d1 : Math.max(d0 * d1, d2 * d3));
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         double d4 = d6;
         if (pType == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL || pType == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
            if (pArgument1 instanceof DensityFunctions.Constant) {
               DensityFunctions.Constant densityfunctions$constant1 = (DensityFunctions.Constant)pArgument1;
               return new DensityFunctions.MulOrAdd(pType == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL, pArgument2, d5, d4, densityfunctions$constant1.value);
            }

            if (pArgument2 instanceof DensityFunctions.Constant) {
               DensityFunctions.Constant densityfunctions$constant = (DensityFunctions.Constant)pArgument2;
               return new DensityFunctions.MulOrAdd(pType == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL, pArgument1, d5, d4, densityfunctions$constant.value);
            }
         }

         return new DensityFunctions.Ap2(pType, pArgument1, pArgument2, d5, d4);
      }

      DensityFunctions.TwoArgumentSimpleFunction.Type type();

      DensityFunction argument1();

      DensityFunction argument2();

      default Codec<? extends DensityFunction> codec() {
         return this.type().codec;
      }

      public static enum Type implements StringRepresentable {
         ADD("add"),
         MUL("mul"),
         MIN("min"),
         MAX("max");

         final Codec<DensityFunctions.TwoArgumentSimpleFunction> codec = DensityFunctions.doubleFunctionArgumentCodec((p_209092_, p_209093_) -> {
            return DensityFunctions.TwoArgumentSimpleFunction.create(this, p_209092_, p_209093_);
         }, DensityFunctions.TwoArgumentSimpleFunction::argument1, DensityFunctions.TwoArgumentSimpleFunction::argument2);
         private final String name;

         private Type(String pName) {
            this.name = pName;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   protected static record WeirdScaledSampler(DensityFunction input, Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise, DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper) implements DensityFunctions.TransformerWithContext {
      private static final MapCodec<DensityFunctions.WeirdScaledSampler> DATA_CODEC = RecordCodecBuilder.mapCodec((p_208438_) -> {
         return p_208438_.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.WeirdScaledSampler::input), NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.WeirdScaledSampler::noiseData), DensityFunctions.WeirdScaledSampler.RarityValueMapper.CODEC.fieldOf("rarity_value_mapper").forGetter(DensityFunctions.WeirdScaledSampler::rarityValueMapper)).apply(p_208438_, DensityFunctions.WeirdScaledSampler::createUnseeded);
      });
      public static final Codec<DensityFunctions.WeirdScaledSampler> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public static DensityFunctions.WeirdScaledSampler createUnseeded(DensityFunction p_208445_, Holder<NormalNoise.NoiseParameters> p_208446_, DensityFunctions.WeirdScaledSampler.RarityValueMapper p_208447_) {
         return new DensityFunctions.WeirdScaledSampler(p_208445_, p_208446_, (NormalNoise)null, p_208447_);
      }

      public double transform(DensityFunction.FunctionContext pContext, double pValue) {
         if (this.noise == null) {
            return 0.0D;
         } else {
            double d0 = this.rarityValueMapper.mapper.get(pValue);
            return d0 * Math.abs(this.noise.getValue((double)pContext.blockX() / d0, (double)pContext.blockY() / d0, (double)pContext.blockZ() / d0));
         }
      }

      public DensityFunction mapAll(DensityFunction.Visitor pVisitor) {
         this.input.mapAll(pVisitor);
         return pVisitor.apply(new DensityFunctions.WeirdScaledSampler(this.input.mapAll(pVisitor), this.noiseData, this.noise, this.rarityValueMapper));
      }

      public double minValue() {
         return 0.0D;
      }

      public double maxValue() {
         return this.rarityValueMapper.maxRarity * (this.noise == null ? 2.0D : this.noise.maxValue());
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public DensityFunction input() {
         return this.input;
      }

      public static enum RarityValueMapper implements StringRepresentable {
         TYPE1("type_1", NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0D),
         TYPE2("type_2", NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D, 3.0D);

         private static final Map<String, DensityFunctions.WeirdScaledSampler.RarityValueMapper> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(DensityFunctions.WeirdScaledSampler.RarityValueMapper::getSerializedName, (p_208475_) -> {
            return p_208475_;
         }));
         public static final Codec<DensityFunctions.WeirdScaledSampler.RarityValueMapper> CODEC = StringRepresentable.fromEnum(DensityFunctions.WeirdScaledSampler.RarityValueMapper::values, BY_NAME::get);
         private final String name;
         final Double2DoubleFunction mapper;
         final double maxRarity;

         private RarityValueMapper(String pName, Double2DoubleFunction pMapper, double pMaxRarity) {
            this.name = pName;
            this.mapper = pMapper;
            this.maxRarity = pMaxRarity;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   static record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction {
      private static final MapCodec<DensityFunctions.YClampedGradient> DATA_CODEC = RecordCodecBuilder.mapCodec((p_208494_) -> {
         return p_208494_.group(Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("from_y").forGetter(DensityFunctions.YClampedGradient::fromY), Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("to_y").forGetter(DensityFunctions.YClampedGradient::toY), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("from_value").forGetter(DensityFunctions.YClampedGradient::fromValue), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("to_value").forGetter(DensityFunctions.YClampedGradient::toValue)).apply(p_208494_, DensityFunctions.YClampedGradient::new);
      });
      public static final Codec<DensityFunctions.YClampedGradient> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double compute(DensityFunction.FunctionContext pContext) {
         return Mth.clampedMap((double)pContext.blockY(), (double)this.fromY, (double)this.toY, this.fromValue, this.toValue);
      }

      public double minValue() {
         return Math.min(this.fromValue, this.toValue);
      }

      public double maxValue() {
         return Math.max(this.fromValue, this.toValue);
      }

      public Codec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }
}