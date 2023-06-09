package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public enum PackCompatibility {
   TOO_OLD("old"),
   TOO_NEW("new"),
   COMPATIBLE("compatible");

   private final Component description;
   private final Component confirmation;

   private PackCompatibility(String pType) {
      this.description = (new TranslatableComponent("pack.incompatible." + pType)).withStyle(ChatFormatting.GRAY);
      this.confirmation = new TranslatableComponent("pack.incompatible.confirm." + pType);
   }

   public boolean isCompatible() {
      return this == COMPATIBLE;
   }

   public static PackCompatibility forFormat(int pVersion, PackType pType) {
      int i = pType.getVersion(SharedConstants.getCurrentVersion());
      if (pVersion < i) {
         return TOO_OLD;
      } else {
         return pVersion > i ? TOO_NEW : COMPATIBLE;
      }
   }

   public static PackCompatibility forMetadata(PackMetadataSection p_143886_, PackType pType) {
      return forFormat(p_143886_.getPackFormat(pType), pType);
   }

   public Component getDescription() {
      return this.description;
   }

   public Component getConfirmation() {
      return this.confirmation;
   }
}
