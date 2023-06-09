package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleDescription {
   @Nullable
   private final List<ResourceLocation> textures;

   private ParticleDescription(@Nullable List<ResourceLocation> pTextures) {
      this.textures = pTextures;
   }

   @Nullable
   public List<ResourceLocation> getTextures() {
      return this.textures;
   }

   public static ParticleDescription fromJson(JsonObject pJson) {
      JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, "textures", (JsonArray)null);
      List<ResourceLocation> list;
      if (jsonarray != null) {
         list = Streams.stream(jsonarray).map((p_107284_) -> {
            return GsonHelper.convertToString(p_107284_, "texture");
         }).map(ResourceLocation::new).collect(ImmutableList.toImmutableList());
      } else {
         list = null;
      }

      return new ParticleDescription(list);
   }
}