package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.Validate;

public class BannerItem extends StandingAndWallBlockItem {
   private static final String PATTERN_PREFIX = "block.minecraft.banner.";

   public BannerItem(Block pStandingBlock, Block pWallBlock, Item.Properties pProperties) {
      super(pStandingBlock, pWallBlock, pProperties);
      Validate.isInstanceOf(AbstractBannerBlock.class, pStandingBlock);
      Validate.isInstanceOf(AbstractBannerBlock.class, pWallBlock);
   }

   public static void appendHoverTextFromBannerBlockEntityTag(ItemStack pStack, List<Component> pTooltipComponents) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      if (compoundtag != null && compoundtag.contains("Patterns")) {
         ListTag listtag = compoundtag.getList("Patterns", 10);

         for(int i = 0; i < listtag.size() && i < 6; ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            DyeColor dyecolor = DyeColor.byId(compoundtag1.getInt("Color"));
            BannerPattern bannerpattern = BannerPattern.byHash(compoundtag1.getString("Pattern"));
            if (bannerpattern != null) {
               net.minecraft.resources.ResourceLocation fileLoc = new net.minecraft.resources.ResourceLocation(bannerpattern.getFilename());
               pTooltipComponents.add((new TranslatableComponent("block." + fileLoc.getNamespace() + ".banner." + fileLoc.getPath() + "." + dyecolor.getName())).withStyle(ChatFormatting.GRAY));
            }
         }

      }
   }

   public DyeColor getColor() {
      return ((AbstractBannerBlock)this.getBlock()).getColor();
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      appendHoverTextFromBannerBlockEntityTag(pStack, pTooltip);
   }
}
