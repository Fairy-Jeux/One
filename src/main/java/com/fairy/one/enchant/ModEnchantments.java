package com.fairy.one.enchant;

import com.fairy.one.One;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, One.MOD_ID);
    public static final RegistryObject<Enchantment> SPACE_ORDER = ENCHANTMENTS.register("space_order", () -> new SpaceOrderEnchantment());



    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
