/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.loot;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Implementation that defines what a global loot modifier must implement in order to be functional.
 * {@link LootModifier} Supplies base functionality; most modders should only need to extend that.<br/>
 * Requires an {@link GlobalLootModifierSerializer} to be registered via json (see forge:loot_modifiers/global_loot_modifiers).
 */
public interface IGlobalLootModifier {
    /**
     * Applies the modifier to the list of generated loot. This function needs to be responsible for
     * checking ILootConditions as well.
     * @param generatedLoot the list of ItemStacks that will be dropped, generated by loot tables
     * @param context the LootContext, identical to what is passed to loot tables
     * @return modified loot drops
     */
    @Nonnull
    List<ItemStack> apply(List<ItemStack> generatedLoot, LootContext context);
}
