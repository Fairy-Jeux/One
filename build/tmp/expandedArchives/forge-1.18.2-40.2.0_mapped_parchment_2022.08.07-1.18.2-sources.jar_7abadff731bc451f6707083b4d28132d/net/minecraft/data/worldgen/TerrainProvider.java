package net.minecraft.data.worldgen;

import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.biome.TerrainShaper;

public class TerrainProvider {
   public static TerrainShaper overworld(boolean pTransform) {
      return TerrainShaper.overworld(pTransform);
   }

   public static TerrainShaper caves() {
      return new TerrainShaper(CubicSpline.constant(0.0F), CubicSpline.constant(0.0F), CubicSpline.constant(0.0F));
   }

   public static TerrainShaper floatingIslands() {
      return new TerrainShaper(CubicSpline.constant(0.0F), CubicSpline.constant(0.0F), CubicSpline.constant(0.0F));
   }

   public static TerrainShaper nether() {
      return new TerrainShaper(CubicSpline.constant(0.0F), CubicSpline.constant(0.0F), CubicSpline.constant(0.0F));
   }

   public static TerrainShaper end() {
      return new TerrainShaper(CubicSpline.constant(0.0F), CubicSpline.constant(1.0F), CubicSpline.constant(0.0F));
   }
}