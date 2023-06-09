package net.minecraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
   public static void spawnParticlesOnBlockFaces(Level pLevel, BlockPos pPos, ParticleOptions pParticle, UniformInt p_144966_) {
      for(Direction direction : Direction.values()) {
         int i = p_144966_.sample(pLevel.random);

         for(int j = 0; j < i; ++j) {
            spawnParticleOnFace(pLevel, pPos, direction, pParticle);
         }
      }

   }

   public static void spawnParticlesAlongAxis(Direction.Axis pAxis, Level pLevel, BlockPos pPos, double p_144971_, ParticleOptions pParticle, UniformInt p_144973_) {
      Vec3 vec3 = Vec3.atCenterOf(pPos);
      boolean flag = pAxis == Direction.Axis.X;
      boolean flag1 = pAxis == Direction.Axis.Y;
      boolean flag2 = pAxis == Direction.Axis.Z;
      int i = p_144973_.sample(pLevel.random);

      for(int j = 0; j < i; ++j) {
         double d0 = vec3.x + Mth.nextDouble(pLevel.random, -1.0D, 1.0D) * (flag ? 0.5D : p_144971_);
         double d1 = vec3.y + Mth.nextDouble(pLevel.random, -1.0D, 1.0D) * (flag1 ? 0.5D : p_144971_);
         double d2 = vec3.z + Mth.nextDouble(pLevel.random, -1.0D, 1.0D) * (flag2 ? 0.5D : p_144971_);
         double d3 = flag ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
         double d4 = flag1 ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
         double d5 = flag2 ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
         pLevel.addParticle(pParticle, d0, d1, d2, d3, d4, d5);
      }

   }

   public static void spawnParticleOnFace(Level pLevel, BlockPos pPos, Direction pFace, ParticleOptions pParticle) {
      Vec3 vec3 = Vec3.atCenterOf(pPos);
      int i = pFace.getStepX();
      int j = pFace.getStepY();
      int k = pFace.getStepZ();
      double d0 = vec3.x + (i == 0 ? Mth.nextDouble(pLevel.random, -0.5D, 0.5D) : (double)i * 0.55D);
      double d1 = vec3.y + (j == 0 ? Mth.nextDouble(pLevel.random, -0.5D, 0.5D) : (double)j * 0.55D);
      double d2 = vec3.z + (k == 0 ? Mth.nextDouble(pLevel.random, -0.5D, 0.5D) : (double)k * 0.55D);
      double d3 = i == 0 ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
      double d4 = j == 0 ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
      double d5 = k == 0 ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
      pLevel.addParticle(pParticle, d0, d1, d2, d3, d4, d5);
   }
}