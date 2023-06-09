package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LogaritmicProgressOption extends ProgressOption {
   public LogaritmicProgressOption(String pCaptionKey, double pMinValue, double pMaxValue, float pSteps, Function<Options, Double> pGetter, BiConsumer<Options, Double> pSetter, BiFunction<Options, ProgressOption, Component> pToString) {
      super(pCaptionKey, pMinValue, pMaxValue, pSteps, pGetter, pSetter, pToString);
   }

   public double toPct(double pValue) {
      return Math.log(pValue / this.minValue) / Math.log(this.maxValue / this.minValue);
   }

   public double toValue(double pValue) {
      return this.minValue * Math.pow(Math.E, Math.log(this.maxValue / this.minValue) * pValue);
   }
}