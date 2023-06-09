package com.fairy.one.item;

import com.fairy.one.One;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, One.MOD_ID);

    public static final RegistryObject<Item> SPACE = ITEMS.register("space",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));



    public static void register (IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
