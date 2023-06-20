package com.fairy.one.event;

import com.fairy.one.One;
import com.fairy.one.block.recipes.OrderOfTheSpaceMachineRecipe;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = One.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
        Registry.register(Registry.RECIPE_TYPE, OrderOfTheSpaceMachineRecipe.Type.ID, OrderOfTheSpaceMachineRecipe.Type.INSTANCE);
    }
}
