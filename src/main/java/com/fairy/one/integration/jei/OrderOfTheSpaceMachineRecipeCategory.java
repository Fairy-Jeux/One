package com.fairy.one.integration.jei;

import com.fairy.one.One;
import com.fairy.one.block.ModBlocks;
import com.fairy.one.block.recipes.OrderOfTheSpaceMachineRecipe;
import com.fairy.one.item.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class OrderOfTheSpaceMachineRecipeCategory implements IRecipeCategory<OrderOfTheSpaceMachineRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(One.MOD_ID, "order_space");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(One.MOD_ID, "textures/gui/order_of_the_space_machine_gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    public OrderOfTheSpaceMachineRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ORDER_OF_THE_SPACE_MACHINE.get()));
    }
    @Override
    public Component getTitle() {
        return new TextComponent("Order Of The Space Machine");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull OrderOfTheSpaceMachineRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 34, 40).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 57, 18).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 103, 18).addIngredients(recipe.getIngredients().get(0));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 60).addItemStack(recipe.getResultItem());
    }

    @Override
    public ResourceLocation getUid() {
        return null;
    }


    @Override
    public Class<? extends OrderOfTheSpaceMachineRecipe> getRecipeClass() {
        return null;
    }
}
