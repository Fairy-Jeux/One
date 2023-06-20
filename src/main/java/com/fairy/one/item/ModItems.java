package com.fairy.one.item;

import com.fairy.one.One;
import com.fairy.one.item.custom.DamageSuperSwordItem;
import com.fairy.one.item.custom.DamageSwordItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, One.MOD_ID);

    public static final RegistryObject<Item> SPACE = ITEMS.register("space",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));

    public static final RegistryObject<Item> SUPER_SPACE = ITEMS.register("super_space",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));

    public static final RegistryObject<Item> SPACE_STICK = ITEMS.register("space_stick",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));

    public static final RegistryObject<Item> SPACE_SWORD = ITEMS.register("space_sword",
            () -> new DamageSwordItem(ModTiers.SPACE, 5, 4f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SPACE_PICKAXE = ITEMS.register("space_pickaxe",
            () -> new PickaxeItem(ModTiers.SPACE, 1, 1f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SPACE_SHOVEL = ITEMS.register("space_shovel",
            () -> new ShovelItem(ModTiers.SPACE, 0, 1f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SPACE_AXE = ITEMS.register("space_axe",
            () -> new AxeItem(ModTiers.SPACE, 4, 0f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SPACE_HOE = ITEMS.register("space_hoe",
            () -> new HoeItem(ModTiers.SPACE, 0, 0f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SUPER_SPACE_SWORD = ITEMS.register("super_space_sword",
            () -> new DamageSuperSwordItem(ModTiers.SPACE, 5, 4f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SUPER_SPACE_PICKAXE = ITEMS.register("super_space_pickaxe",
            () -> new PickaxeItem(ModTiers.SPACE, 1, 1f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SUPER_SPACE_SHOVEL = ITEMS.register("super_space_shovel",
            () -> new ShovelItem(ModTiers.SPACE, 0, 1f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SUPER_SPACE_AXE = ITEMS.register("super_space_axe",
            () -> new AxeItem(ModTiers.SPACE, 4, 0f,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SUPER_SPACE_HOE = ITEMS.register("super_space_hoe",
            () -> new HoeItem(ModTiers.SPACE, 0, 0f,
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

    public static final RegistryObject<Item> SUPER_SPACE_HELMET = ITEMS.register("super_space_helmet",
            () -> new ArmorItem(ModArmorMaterials.SPACE, EquipmentSlot.HEAD,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SUPER_SPACE_CHESTPLATE = ITEMS.register("super_space_chestplate",
            () -> new ArmorItem(ModArmorMaterials.SPACE, EquipmentSlot.CHEST,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SUPER_SPACE_LEGGING = ITEMS.register("super_space_leggings",
            () -> new ArmorItem(ModArmorMaterials.SPACE, EquipmentSlot.LEGS,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> SUPER_SPACE_BOOTS = ITEMS.register("super_space_boots",
            () -> new ArmorItem(ModArmorMaterials.SPACE, EquipmentSlot.FEET,
                    new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));
    public static final RegistryObject<Item> DRAGON_HEART = ITEMS.register("dragon_heart",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ONE_TAB)));

    public static void register (IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
