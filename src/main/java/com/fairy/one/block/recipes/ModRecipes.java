package com.fairy.one.block.recipes;

import com.fairy.one.One;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, One.MOD_ID);

    public static final RegistryObject<RecipeSerializer<OrderOfTheSpaceMachineRecipe>> ORDER_OF_THE_SPACE_MACHINE_RECIPE =
            SERIALIZERS.register("order_space", () -> OrderOfTheSpaceMachineRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
