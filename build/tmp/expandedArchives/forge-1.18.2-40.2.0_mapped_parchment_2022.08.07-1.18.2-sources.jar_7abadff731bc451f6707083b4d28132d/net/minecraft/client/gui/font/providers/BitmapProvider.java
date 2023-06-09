package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BitmapProvider implements GlyphProvider {
   static final Logger LOGGER = LogUtils.getLogger();
   private final NativeImage image;
   private final Int2ObjectMap<BitmapProvider.Glyph> glyphs;

   BitmapProvider(NativeImage pImage, Int2ObjectMap<BitmapProvider.Glyph> pGlyphs) {
      this.image = pImage;
      this.glyphs = pGlyphs;
   }

   public void close() {
      this.image.close();
   }

   @Nullable
   public RawGlyph getGlyph(int pCharacter) {
      return this.glyphs.get(pCharacter);
   }

   public IntSet getSupportedGlyphs() {
      return IntSets.unmodifiable(this.glyphs.keySet());
   }

   @OnlyIn(Dist.CLIENT)
   public static class Builder implements GlyphProviderBuilder {
      private final ResourceLocation texture;
      private final List<int[]> chars;
      private final int height;
      private final int ascent;

      public Builder(ResourceLocation pTexture, int pHeight, int pAscent, List<int[]> pChars) {
         this.texture = new ResourceLocation(pTexture.getNamespace(), "textures/" + pTexture.getPath());
         this.chars = pChars;
         this.height = pHeight;
         this.ascent = pAscent;
      }

      public static BitmapProvider.Builder fromJson(JsonObject pJson) {
         int i = GsonHelper.getAsInt(pJson, "height", 8);
         int j = GsonHelper.getAsInt(pJson, "ascent");
         if (j > i) {
            throw new JsonParseException("Ascent " + j + " higher than height " + i);
         } else {
            List<int[]> list = Lists.newArrayList();
            JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, "chars");

            for(int k = 0; k < jsonarray.size(); ++k) {
               String s = GsonHelper.convertToString(jsonarray.get(k), "chars[" + k + "]");
               int[] aint = s.codePoints().toArray();
               if (k > 0) {
                  int l = ((int[])list.get(0)).length;
                  if (aint.length != l) {
                     throw new JsonParseException("Elements of chars have to be the same length (found: " + aint.length + ", expected: " + l + "), pad with space or \\u0000");
                  }
               }

               list.add(aint);
            }

            if (!list.isEmpty() && ((int[])list.get(0)).length != 0) {
               return new BitmapProvider.Builder(new ResourceLocation(GsonHelper.getAsString(pJson, "file")), i, j, list);
            } else {
               throw new JsonParseException("Expected to find data in chars, found none.");
            }
         }
      }

      @Nullable
      public GlyphProvider create(ResourceManager pResourceManager) {
         try {
            Resource resource = pResourceManager.getResource(this.texture);

            BitmapProvider bitmapprovider;
            try {
               NativeImage nativeimage = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream());
               int i = nativeimage.getWidth();
               int j = nativeimage.getHeight();
               int k = i / ((int[])this.chars.get(0)).length;
               int l = j / this.chars.size();
               float f = (float)this.height / (float)l;
               Int2ObjectMap<BitmapProvider.Glyph> int2objectmap = new Int2ObjectOpenHashMap<>();

               for(int i1 = 0; i1 < this.chars.size(); ++i1) {
                  int j1 = 0;

                  for(int k1 : this.chars.get(i1)) {
                     int l1 = j1++;
                     if (k1 != 0 && k1 != 32) {
                        int i2 = this.getActualGlyphWidth(nativeimage, k, l, l1, i1);
                        BitmapProvider.Glyph bitmapprovider$glyph = int2objectmap.put(k1, new BitmapProvider.Glyph(f, nativeimage, l1 * k, i1 * l, k, l, (int)(0.5D + (double)((float)i2 * f)) + 1, this.ascent));
                        if (bitmapprovider$glyph != null) {
                           BitmapProvider.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(k1), this.texture);
                        }
                     }
                  }
               }

               bitmapprovider = new BitmapProvider(nativeimage, int2objectmap);
            } catch (Throwable throwable1) {
               if (resource != null) {
                  try {
                     resource.close();
                  } catch (Throwable throwable) {
                     throwable1.addSuppressed(throwable);
                  }
               }

               throw throwable1;
            }

            if (resource != null) {
               resource.close();
            }

            return bitmapprovider;
         } catch (IOException ioexception) {
            throw new RuntimeException(ioexception.getMessage());
         }
      }

      private int getActualGlyphWidth(NativeImage pNativeImage, int pCharWidth, int pCharHeightInsp, int pColumn, int pRow) {
         int i;
         for(i = pCharWidth - 1; i >= 0; --i) {
            int j = pColumn * pCharWidth + i;

            for(int k = 0; k < pCharHeightInsp; ++k) {
               int l = pRow * pCharHeightInsp + k;
               if (pNativeImage.getLuminanceOrAlpha(j, l) != 0) {
                  return i + 1;
               }
            }
         }

         return i + 1;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static final class Glyph implements RawGlyph {
      private final float scale;
      private final NativeImage image;
      private final int offsetX;
      private final int offsetY;
      private final int width;
      private final int height;
      private final int advance;
      private final int ascent;

      Glyph(float pSacle, NativeImage pImage, int pOffsetX, int pOffsetY, int pWidth, int pHeight, int pAdvance, int pAscent) {
         this.scale = pSacle;
         this.image = pImage;
         this.offsetX = pOffsetX;
         this.offsetY = pOffsetY;
         this.width = pWidth;
         this.height = pHeight;
         this.advance = pAdvance;
         this.ascent = pAscent;
      }

      public float getOversample() {
         return 1.0F / this.scale;
      }

      public int getPixelWidth() {
         return this.width;
      }

      public int getPixelHeight() {
         return this.height;
      }

      public float getAdvance() {
         return (float)this.advance;
      }

      public float getBearingY() {
         return RawGlyph.super.getBearingY() + 7.0F - (float)this.ascent;
      }

      public void upload(int pXOffset, int pYOffset) {
         this.image.upload(0, pXOffset, pYOffset, this.offsetX, this.offsetY, this.width, this.height, false, false);
      }

      public boolean isColored() {
         return this.image.format().components() > 1;
      }
   }
}