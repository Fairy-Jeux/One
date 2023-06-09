package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.blending.Blender;

public interface DensityFunction {
   Codec<DensityFunction> DIRECT_CODEC = DensityFunctions.DIRECT_CODEC;
   Codec<Holder<DensityFunction>> CODEC = RegistryFileCodec.create(Registry.DENSITY_FUNCTION_REGISTRY, DIRECT_CODEC);
   Codec<DensityFunction> HOLDER_HELPER_CODEC = CODEC.xmap(DensityFunctions.HolderHolder::new, (p_208226_) -> {
      if (p_208226_ instanceof DensityFunctions.HolderHolder) {
         DensityFunctions.HolderHolder densityfunctions$holderholder = (DensityFunctions.HolderHolder)p_208226_;
         return densityfunctions$holderholder.function();
      } else {
         return new Holder.Direct<>(p_208226_);
      }
   });

   double compute(DensityFunction.FunctionContext pContext);

   void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider);

   DensityFunction mapAll(DensityFunction.Visitor pVisitor);

   double minValue();

   double maxValue();

   Codec<? extends DensityFunction> codec();

   default DensityFunction clamp(double pMinValue, double pMaxValue) {
      return new DensityFunctions.Clamp(this, pMinValue, pMaxValue);
   }

   default DensityFunction abs() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.ABS);
   }

   default DensityFunction square() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUARE);
   }

   default DensityFunction cube() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.CUBE);
   }

   default DensityFunction halfNegative() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.HALF_NEGATIVE);
   }

   default DensityFunction quarterNegative() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.QUARTER_NEGATIVE);
   }

   default DensityFunction squeeze() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUEEZE);
   }

   public interface ContextProvider {
      DensityFunction.FunctionContext forIndex(int pArrayIndex);

      void fillAllDirectly(double[] pValues, DensityFunction pFunction);
   }

   public interface FunctionContext {
      int blockX();

      int blockY();

      int blockZ();

      default Blender getBlender() {
         return Blender.empty();
      }
   }

   public interface SimpleFunction extends DensityFunction {
      default void fillArray(double[] p_208241_, DensityFunction.ContextProvider p_208242_) {
         p_208242_.fillAllDirectly(p_208241_, this);
      }

      default DensityFunction mapAll(DensityFunction.Visitor p_208239_) {
         return p_208239_.apply(this);
      }
   }

   public static record SinglePointContext(int blockX, int blockY, int blockZ) implements DensityFunction.FunctionContext {
      public int blockX() {
         return this.blockX;
      }

      public int blockY() {
         return this.blockY;
      }

      public int blockZ() {
         return this.blockZ;
      }
   }

   public interface Visitor extends Function<DensityFunction, DensityFunction> {
   }
}