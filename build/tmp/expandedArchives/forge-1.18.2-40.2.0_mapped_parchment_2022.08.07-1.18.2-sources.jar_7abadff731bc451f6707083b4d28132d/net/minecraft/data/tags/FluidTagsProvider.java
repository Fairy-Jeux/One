package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider extends TagsProvider<Fluid> {
   /** @deprecated Forge: Use the {@link #FluidTagsProvider(DataGenerator, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant} */
   @Deprecated
   public FluidTagsProvider(DataGenerator pGenerator) {
      super(pGenerator, Registry.FLUID);
   }
   public FluidTagsProvider(DataGenerator pGenerator, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pGenerator, Registry.FLUID, modId, existingFileHelper);
   }

   protected void addTags() {
      this.tag(FluidTags.WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
      this.tag(FluidTags.LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public String getName() {
      return "Fluid Tags";
   }
}
