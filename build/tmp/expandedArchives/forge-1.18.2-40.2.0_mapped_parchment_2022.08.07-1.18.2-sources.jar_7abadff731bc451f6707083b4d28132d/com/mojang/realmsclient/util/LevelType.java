package com.mojang.realmsclient.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum LevelType {
   DEFAULT(0, new TranslatableComponent("generator.default")),
   FLAT(1, new TranslatableComponent("generator.flat")),
   LARGE_BIOMES(2, new TranslatableComponent("generator.large_biomes")),
   AMPLIFIED(3, new TranslatableComponent("generator.amplified"));

   private final int index;
   private final Component name;

   private LevelType(int pIndex, Component pName) {
      this.index = pIndex;
      this.name = pName;
   }

   public Component getName() {
      return this.name;
   }

   public int getDtoIndex() {
      return this.index;
   }
}