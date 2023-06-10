package com.fairy.one.item;

import com.fairy.one.One;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, One.MOD_ID);

    public static final RegistryObject<Item> SPACE = ITEMS.register("space",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));

    public static final RegistryObject<Item> SPACE_STICK = ITEMS.register("space_stick",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));

    public static final RegistryObject<Item> SPACE_SWORD = ITEMS.register("space_sword",
            () -> new SwordItem(ModTiers.SPACE, 5, 4f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));

    public static final RegistryObject<Item> SPACE_HELMET = ITEMS.register("space_helmet",
            () -> new ArmorItem(ModArmorMaterials.SPACE, EquipmentSlot.HEAD,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SPACE_CHESTPLATE = ITEMS.register("space_chestplate",
            () -> new ArmorItem(ModArmorMaterials.SPACE, EquipmentSlot.CHEST,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SPACE_LEGGING = ITEMS.register("space_leggings",
            () -> new ArmorItem(ModArmorMaterials.SPACE, EquipmentSlot.LEGS,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SPACE_BOOTS = ITEMS.register("space_boots",
            () -> new ArmorItem(ModArmorMaterials.SPACE, EquipmentSlot.FEET,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));

    public static void register (IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
