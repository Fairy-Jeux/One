package com.fairy.one.enchant;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
public class SpaceOrderEnchantment extends Enchantment {
        public SpaceOrderEnchantment(EquipmentSlot... slots) {
            super(Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE, slots);
        }

        @Override
        public int getDamageProtection(int level, DamageSource source) {
            return level * 1;
        }
    }