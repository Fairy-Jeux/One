package com.fairy.one.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {
    public static final CreativeModeTab ONE_TAB = new CreativeModeTab("onetab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.SPACE.get());
        }
    };
}
