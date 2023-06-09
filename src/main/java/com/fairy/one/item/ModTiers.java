package com.fairy.one.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
public class ModTiers {
    public static final ForgeTier SPACE = new ForgeTier(4, 5000, 15.0f,
            4.0f, 22, BlockTags.NEEDS_DIAMOND_TOOL,
            () -> Ingredient.of(ModItems.SPACE.get()));
}
