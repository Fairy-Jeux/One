package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public final class FluidTags {
   public static final TagKey<Fluid> WATER = create("water");
   public static final TagKey<Fluid> LAVA = create("lava");

   private FluidTags() {
   }

   private static TagKey<Fluid> create(String pName) {
      return TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(pName));
   }

   public static TagKey<Fluid> create(ResourceLocation name) {
      return TagKey.create(Registry.FLUID_REGISTRY, name);
   }
}
