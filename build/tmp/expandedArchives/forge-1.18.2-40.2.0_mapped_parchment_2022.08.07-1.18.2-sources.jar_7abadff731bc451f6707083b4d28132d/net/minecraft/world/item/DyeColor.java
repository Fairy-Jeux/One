package net.minecraft.world.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MaterialColor;

public enum DyeColor implements StringRepresentable {
   WHITE(0, "white", 16383998, MaterialColor.SNOW, 15790320, 16777215),
   ORANGE(1, "orange", 16351261, MaterialColor.COLOR_ORANGE, 15435844, 16738335),
   MAGENTA(2, "magenta", 13061821, MaterialColor.COLOR_MAGENTA, 12801229, 16711935),
   LIGHT_BLUE(3, "light_blue", 3847130, MaterialColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
   YELLOW(4, "yellow", 16701501, MaterialColor.COLOR_YELLOW, 14602026, 16776960),
   LIME(5, "lime", 8439583, MaterialColor.COLOR_LIGHT_GREEN, 4312372, 12582656),
   PINK(6, "pink", 15961002, MaterialColor.COLOR_PINK, 14188952, 16738740),
   GRAY(7, "gray", 4673362, MaterialColor.COLOR_GRAY, 4408131, 8421504),
   LIGHT_GRAY(8, "light_gray", 10329495, MaterialColor.COLOR_LIGHT_GRAY, 11250603, 13882323),
   CYAN(9, "cyan", 1481884, MaterialColor.COLOR_CYAN, 2651799, 65535),
   PURPLE(10, "purple", 8991416, MaterialColor.COLOR_PURPLE, 8073150, 10494192),
   BLUE(11, "blue", 3949738, MaterialColor.COLOR_BLUE, 2437522, 255),
   BROWN(12, "brown", 8606770, MaterialColor.COLOR_BROWN, 5320730, 9127187),
   GREEN(13, "green", 6192150, MaterialColor.COLOR_GREEN, 3887386, 65280),
   RED(14, "red", 11546150, MaterialColor.COLOR_RED, 11743532, 16711680),
   BLACK(15, "black", 1908001, MaterialColor.COLOR_BLACK, 1973019, 0);

   private static final DyeColor[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(DyeColor::getId)).toArray((p_41067_) -> {
      return new DyeColor[p_41067_];
   });
   private static final Int2ObjectOpenHashMap<DyeColor> BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap<>(Arrays.stream(values()).collect(Collectors.toMap((p_41064_) -> {
      return p_41064_.fireworkColor;
   }, (p_41056_) -> {
      return p_41056_;
   })));
   private final int id;
   private final String name;
   private final MaterialColor color;
   private final float[] textureDiffuseColors;
   private final int fireworkColor;
   private final net.minecraft.tags.TagKey<Item> tag;
   private final int textColor;

   private DyeColor(int pId, String pName, int pTextureColor, MaterialColor pColor, int pFireworkColor, int pTextColor) {
      this.id = pId;
      this.name = pName;
      this.color = pColor;
      this.textColor = pTextColor;
      int i = (pTextureColor & 16711680) >> 16;
      int j = (pTextureColor & '\uff00') >> 8;
      int k = (pTextureColor & 255) >> 0;
      this.tag = net.minecraft.tags.ItemTags.create(new net.minecraft.resources.ResourceLocation("forge", "dyes/" + pName));
      this.textureDiffuseColors = new float[]{(float)i / 255.0F, (float)j / 255.0F, (float)k / 255.0F};
      this.fireworkColor = pFireworkColor;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   /**
    * Gets an array containing 3 floats ranging from 0.0 to 1.0: the red, green, and blue components of the
    * corresponding color.
    */
   public float[] getTextureDiffuseColors() {
      return this.textureDiffuseColors;
   }

   public MaterialColor getMaterialColor() {
      return this.color;
   }

   public int getFireworkColor() {
      return this.fireworkColor;
   }

   public int getTextColor() {
      return this.textColor;
   }

   public static DyeColor byId(int pColorId) {
      if (pColorId < 0 || pColorId >= BY_ID.length) {
         pColorId = 0;
      }

      return BY_ID[pColorId];
   }

   public static DyeColor byName(String pTranslationKey, DyeColor pFallback) {
      for(DyeColor dyecolor : values()) {
         if (dyecolor.name.equals(pTranslationKey)) {
            return dyecolor;
         }
      }

      return pFallback;
   }

   @Nullable
   public static DyeColor byFireworkColor(int pFireworkColor) {
      return BY_FIREWORK_COLOR.get(pFireworkColor);
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public net.minecraft.tags.TagKey<Item> getTag() {
      return tag;
   }

   @Nullable
   public static DyeColor getColor(ItemStack stack) {
      if (stack.getItem() instanceof DyeItem)
         return ((DyeItem)stack.getItem()).getDyeColor();

      for (DyeColor color : BY_ID) {
         if (stack.is(color.getTag()))
             return color;
      }

      return null;
   }
}
