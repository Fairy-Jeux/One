package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   protected final DataGenerator generator;
   protected final Registry<T> registry;
   protected final Map<ResourceLocation, Tag.Builder> builders = Maps.newLinkedHashMap();
   protected final String modId;
   protected final net.minecraftforge.common.data.ExistingFileHelper existingFileHelper;
   private final net.minecraftforge.common.data.ExistingFileHelper.IResourceType resourceType;

   /**
    * @see #TagsProvider(DataGenerator, Registry, String, net.minecraftforge.common.data.ExistingFileHelper)
    * @deprecated Forge: Use the mod id variant
    */
   @Deprecated
   protected TagsProvider(DataGenerator pGenerator, Registry<T> pRegistry) {
      this(pGenerator, pRegistry, "vanilla", null);
   }
   protected TagsProvider(DataGenerator pGenerator, Registry<T> pRegistry, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      this.generator = pGenerator;
      this.registry = pRegistry;
      this.modId = modId;
      this.existingFileHelper = existingFileHelper;
      this.resourceType = new net.minecraftforge.common.data.ExistingFileHelper.ResourceType(net.minecraft.server.packs.PackType.SERVER_DATA, ".json", TagManager.getTagDir(pRegistry.key()));
   }

   protected abstract void addTags();

   /**
    * Performs this provider's action.
    */
   public void run(HashCache pCache) {
      this.builders.clear();
      this.addTags();
      this.builders.forEach((p_176835_, p_176836_) -> {
         List<Tag.BuilderEntry> list = p_176836_.getEntries().filter((p_176832_) -> {
            return !p_176832_.entry().verifyIfPresent(this.registry::containsKey, this.builders::containsKey);
         }).filter(this::missing).collect(Collectors.toList()); // Forge: Add validation via existing resources
         if (!list.isEmpty()) {
            throw new IllegalArgumentException(String.format("Couldn't define tag %s as it is missing following references: %s", p_176835_, list.stream().map(Objects::toString).collect(Collectors.joining(","))));
         } else {
            JsonObject jsonobject = p_176836_.serializeToJson();
            Path path = this.getPath(p_176835_);
            if (path == null) return; // Forge: Allow running this data provider without writing it. Recipe provider needs valid tags.

            try {
               String s = GSON.toJson((JsonElement)jsonobject);
               String s1 = SHA1.hashUnencodedChars(s).toString();
               if (!Objects.equals(pCache.getHash(path), s1) || !Files.exists(path)) {
                  Files.createDirectories(path.getParent());
                  BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

                  try {
                     bufferedwriter.write(s);
                  } catch (Throwable throwable1) {
                     if (bufferedwriter != null) {
                        try {
                           bufferedwriter.close();
                        } catch (Throwable throwable) {
                           throwable1.addSuppressed(throwable);
                        }
                     }

                     throw throwable1;
                  }

                  if (bufferedwriter != null) {
                     bufferedwriter.close();
                  }
               }

               pCache.putNew(path, s1);
            } catch (IOException ioexception) {
               LOGGER.error("Couldn't save tags to {}", path, ioexception);
            }

         }
      });
   }

   private boolean missing(Tag.BuilderEntry reference) {
      Tag.Entry entry = reference.entry();
      // We only care about non-optional tag entries, this is the only type that can reference a resource and needs validation
      // Optional tags should not be validated

      if (entry instanceof Tag.TagEntry nonOptionalEntry) {
         return existingFileHelper == null || !existingFileHelper.exists(nonOptionalEntry.getId(), resourceType);
      }
      return false;
   }

   /**
    * Resolves a Path for the location to save the given tag.
    */
   protected Path getPath(ResourceLocation pId) {
      ResourceKey<? extends Registry<T>> resourcekey = this.registry.key();
      return this.generator.getOutputFolder().resolve("data/" + pId.getNamespace() + "/" + TagManager.getTagDir(resourcekey) + "/" + pId.getPath() + ".json");
   }

   protected TagsProvider.TagAppender<T> tag(TagKey<T> pTag) {
      Tag.Builder tag$builder = this.getOrCreateRawBuilder(pTag);
      return new TagsProvider.TagAppender<>(tag$builder, this.registry, modId);
   }

   protected Tag.Builder getOrCreateRawBuilder(TagKey<T> pTag) {
      return this.builders.computeIfAbsent(pTag.location(), (p_176838_) -> {
         existingFileHelper.trackGenerated(p_176838_, resourceType);
         return new Tag.Builder();
      });
   }

   public static class TagAppender<T> implements net.minecraftforge.common.extensions.IForgeTagAppender<T> {
      private final Tag.Builder builder;
      public final Registry<T> registry;
      private final String source;

      TagAppender(Tag.Builder pBuilder, Registry<T> pRegistry, String pSource) {
         this.builder = pBuilder;
         this.registry = pRegistry;
         this.source = pSource;
      }

      public TagsProvider.TagAppender<T> add(T pItem) {
         this.builder.addElement(this.registry.getKey(pItem), this.source);
         return this;
      }

      @SafeVarargs
      public final TagsProvider.TagAppender<T> add(ResourceKey<T>... pToAdd) {
         for(ResourceKey<T> resourcekey : pToAdd) {
            this.builder.addElement(resourcekey.location(), this.source);
         }

         return this;
      }

      public TagsProvider.TagAppender<T> addOptional(ResourceLocation pLocation) {
         this.builder.addOptionalElement(pLocation, this.source);
         return this;
      }

      public TagsProvider.TagAppender<T> addTag(TagKey<T> pTag) {
         this.builder.addTag(pTag.location(), this.source);
         return this;
      }

      public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation pLocation) {
         this.builder.addOptionalTag(pLocation, this.source);
         return this;
      }

      @SafeVarargs
      public final TagsProvider.TagAppender<T> add(T... pToAdd) {
         Stream.<T>of(pToAdd).map(this.registry::getKey).forEach((p_126587_) -> {
            this.builder.addElement(p_126587_, this.source);
         });
         return this;
      }

      public TagsProvider.TagAppender<T> add(Tag.Entry tag) {
          builder.add(tag, source);
          return this;
      }

      public Tag.Builder getInternalBuilder() {
          return builder;
      }

      public String getModID() {
          return source;
      }
   }
}
