package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public enum BannerPattern implements net.minecraftforge.common.IExtensibleEnum {
   BASE("base", "b", false),
   SQUARE_BOTTOM_LEFT("square_bottom_left", "bl"),
   SQUARE_BOTTOM_RIGHT("square_bottom_right", "br"),
   SQUARE_TOP_LEFT("square_top_left", "tl"),
   SQUARE_TOP_RIGHT("square_top_right", "tr"),
   STRIPE_BOTTOM("stripe_bottom", "bs"),
   STRIPE_TOP("stripe_top", "ts"),
   STRIPE_LEFT("stripe_left", "ls"),
   STRIPE_RIGHT("stripe_right", "rs"),
   STRIPE_CENTER("stripe_center", "cs"),
   STRIPE_MIDDLE("stripe_middle", "ms"),
   STRIPE_DOWNRIGHT("stripe_downright", "drs"),
   STRIPE_DOWNLEFT("stripe_downleft", "dls"),
   STRIPE_SMALL("small_stripes", "ss"),
   CROSS("cross", "cr"),
   STRAIGHT_CROSS("straight_cross", "sc"),
   TRIANGLE_BOTTOM("triangle_bottom", "bt"),
   TRIANGLE_TOP("triangle_top", "tt"),
   TRIANGLES_BOTTOM("triangles_bottom", "bts"),
   TRIANGLES_TOP("triangles_top", "tts"),
   DIAGONAL_LEFT("diagonal_left", "ld"),
   DIAGONAL_RIGHT("diagonal_up_right", "rd"),
   DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud"),
   DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud"),
   CIRCLE_MIDDLE("circle", "mc"),
   RHOMBUS_MIDDLE("rhombus", "mr"),
   HALF_VERTICAL("half_vertical", "vh"),
   HALF_HORIZONTAL("half_horizontal", "hh"),
   HALF_VERTICAL_MIRROR("half_vertical_right", "vhr"),
   HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb"),
   BORDER("border", "bo"),
   CURLY_BORDER("curly_border", "cbo"),
   GRADIENT("gradient", "gra"),
   GRADIENT_UP("gradient_up", "gru"),
   BRICKS("bricks", "bri"),
   GLOBE("globe", "glb", true),
   CREEPER("creeper", "cre", true),
   SKULL("skull", "sku", true),
   FLOWER("flower", "flo", true),
   MOJANG("mojang", "moj", true),
   PIGLIN("piglin", "pig", true);

   private static final BannerPattern[] VALUES = values();
   /** @deprecated Use {@link BannerPattern#values()} and get the length from the array **/
   @Deprecated
   public static final int COUNT = VALUES.length;
   /** @deprecated Subtract {@link net.minecraftforge.common.ForgeHooks#getNonPatternItemCount()} from {@link BannerPattern#values()} **/
   @Deprecated
   public static final int PATTERN_ITEM_COUNT = (int)Arrays.stream(VALUES).filter((p_58581_) -> {
      return p_58581_.hasPatternItem;
   }).count();
   /** @deprecated Subtract 1 from {@link net.minecraftforge.common.ForgeHooks#getNonPatternItemCount()} to get the maximum non-pattern item index**/
   @Deprecated
   public static final int AVAILABLE_PATTERNS = COUNT - PATTERN_ITEM_COUNT - 1;
   public final boolean hasPatternItem;
   private final String filename;
   final String hashname;

   private BannerPattern(String pFilename, String pHashname) {
      this(pFilename, pHashname, false);
   }

   private BannerPattern(String pFilename, String pHashname, boolean pHasPatternItem) {
      this.filename = pFilename;
      this.hashname = pHashname;
      this.hasPatternItem = pHasPatternItem;
   }

   public ResourceLocation location(boolean pIsBanner) {
      String s = pIsBanner ? "banner" : "shield";
      ResourceLocation fileLoc = new ResourceLocation(this.getFilename());
      return new ResourceLocation(fileLoc.getNamespace(), "entity/" + s + "/" + fileLoc.getPath());
   }

   public String getFilename() {
      return this.filename;
   }

   public String getHashname() {
      return this.hashname;
   }

   @Nullable
   public static BannerPattern byHash(String pHash) {
      for(BannerPattern bannerpattern : values()) {
         if (bannerpattern.hashname.equals(pHash)) {
            return bannerpattern;
         }
      }

      return null;
   }

   @Nullable
   public static BannerPattern byFilename(String pFilename) {
      for(BannerPattern bannerpattern : values()) {
         if (bannerpattern.filename.equals(pFilename)) {
            return bannerpattern;
         }
      }

      return null;
   }

   public static BannerPattern create(String enumName, String fileNameIn, String hashNameIn) {
      throw new IllegalStateException("Enum not extended");
   }

   public static BannerPattern create(String enumName, String fileNameIn, String hashNameIn, boolean hasPatternItem) {
      throw new IllegalStateException("Enum not extended");
   }

   @Override
   public void init() {
      net.minecraftforge.common.ForgeHooks.refreshBannerPatternData();
   }

   public static class Builder {
      private final List<Pair<BannerPattern, DyeColor>> patterns = Lists.newArrayList();

      public BannerPattern.Builder addPattern(BannerPattern pPattern, DyeColor pColor) {
         return this.addPattern(Pair.of(pPattern, pColor));
      }

      public BannerPattern.Builder addPattern(Pair<BannerPattern, DyeColor> pPattern) {
         this.patterns.add(pPattern);
         return this;
      }

      /**
       * Creates the NBT data for the patterns.
       */
      public ListTag toListTag() {
         ListTag listtag = new ListTag();

         for(Pair<BannerPattern, DyeColor> pair : this.patterns) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("Pattern", (pair.getFirst()).hashname);
            compoundtag.putInt("Color", pair.getSecond().getId());
            listtag.add(compoundtag);
         }

         return listtag;
      }
   }
}
