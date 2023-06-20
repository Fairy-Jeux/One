package com.fairy.one.integration.jei;

import com.fairy.one.One;
import com.fairy.one.block.ModBlocks;
import com.fairy.one.block.recipes.ModRecipes;
import com.fairy.one.block.recipes.OrderOfTheSpaceMachineRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Objects;

public class JEIOneModPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(One.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new OrderOfTheSpaceMachineRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ORDER_OF_THE_SPACE_MACHINE.get()), ModRecipes.ORDER_OF_THE_SPACE_MACHINE_RECIPE.getId());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<OrderOfTheSpaceMachineRecipe> recipes = rm.getAllRecipesFor(OrderOfTheSpaceMachineRecipe.Type.INSTANCE);
        registration.addRecipes(new RecipeType<>(OrderOfTheSpaceMachineRecipeCategory.UID, OrderOfTheSpaceMachineRecipe.class), recipes);
    }
}
