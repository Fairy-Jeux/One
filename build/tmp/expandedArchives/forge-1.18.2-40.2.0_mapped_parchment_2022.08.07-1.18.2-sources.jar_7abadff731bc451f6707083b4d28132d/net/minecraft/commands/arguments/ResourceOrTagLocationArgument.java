package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public class ResourceOrTagLocationArgument<T> implements ArgumentType<ResourceOrTagLocationArgument.Result<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
   private static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType((p_210974_) -> {
      return new TranslatableComponent("commands.locatebiome.invalid", p_210974_);
   });
   private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType((p_210967_) -> {
      return new TranslatableComponent("commands.locate.invalid", p_210967_);
   });
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceOrTagLocationArgument(ResourceKey<? extends Registry<T>> pRegistryKey) {
      this.registryKey = pRegistryKey;
   }

   public static <T> ResourceOrTagLocationArgument<T> resourceOrTag(ResourceKey<? extends Registry<T>> pRegistryKey) {
      return new ResourceOrTagLocationArgument<>(pRegistryKey);
   }

   private static <T> ResourceOrTagLocationArgument.Result<T> getRegistryType(CommandContext<CommandSourceStack> pContext, String pName, ResourceKey<Registry<T>> pRegistryKey, DynamicCommandExceptionType p_210959_) throws CommandSyntaxException {
      ResourceOrTagLocationArgument.Result<?> result = pContext.getArgument(pName, ResourceOrTagLocationArgument.Result.class);
      Optional<ResourceOrTagLocationArgument.Result<T>> optional = result.cast(pRegistryKey);
      return optional.orElseThrow(() -> {
         return p_210959_.create(result);
      });
   }

   public static ResourceOrTagLocationArgument.Result<Biome> getBiome(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
      return getRegistryType(pContext, pName, Registry.BIOME_REGISTRY, ERROR_INVALID_BIOME);
   }

   public static ResourceOrTagLocationArgument.Result<ConfiguredStructureFeature<?, ?>> getStructureFeature(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
      return getRegistryType(pContext, pName, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ERROR_INVALID_STRUCTURE);
   }

   public ResourceOrTagLocationArgument.Result<T> parse(StringReader p_210951_) throws CommandSyntaxException {
      if (p_210951_.canRead() && p_210951_.peek() == '#') {
         int i = p_210951_.getCursor();

         try {
            p_210951_.skip();
            ResourceLocation resourcelocation1 = ResourceLocation.read(p_210951_);
            return new ResourceOrTagLocationArgument.TagResult<>(TagKey.create(this.registryKey, resourcelocation1));
         } catch (CommandSyntaxException commandsyntaxexception) {
            p_210951_.setCursor(i);
            throw commandsyntaxexception;
         }
      } else {
         ResourceLocation resourcelocation = ResourceLocation.read(p_210951_);
         return new ResourceOrTagLocationArgument.ResourceResult<>(ResourceKey.create(this.registryKey, resourcelocation));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder p_210978_) {
      Object object = pContext.getSource();
      if (object instanceof SharedSuggestionProvider) {
         SharedSuggestionProvider sharedsuggestionprovider = (SharedSuggestionProvider)object;
         return sharedsuggestionprovider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL, p_210978_, pContext);
      } else {
         return p_210978_.buildFuture();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static record ResourceResult<T>(ResourceKey<T> key) implements ResourceOrTagLocationArgument.Result<T> {
      public Either<ResourceKey<T>, TagKey<T>> unwrap() {
         return Either.left(this.key);
      }

      public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> p_210988_) {
         return this.key.cast(p_210988_).map(ResourceOrTagLocationArgument.ResourceResult::new);
      }

      public boolean test(Holder<T> p_210986_) {
         return p_210986_.is(this.key);
      }

      public String asPrintable() {
         return this.key.location().toString();
      }
   }

   public interface Result<T> extends Predicate<Holder<T>> {
      Either<ResourceKey<T>, TagKey<T>> unwrap();

      <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> pRegistryKey);

      String asPrintable();
   }

   public static class Serializer implements ArgumentSerializer<ResourceOrTagLocationArgument<?>> {
      public void serializeToNetwork(ResourceOrTagLocationArgument<?> p_211009_, FriendlyByteBuf p_211010_) {
         p_211010_.writeResourceLocation(p_211009_.registryKey.location());
      }

      public ResourceOrTagLocationArgument<?> deserializeFromNetwork(FriendlyByteBuf p_211012_) {
         ResourceLocation resourcelocation = p_211012_.readResourceLocation();
         return new ResourceOrTagLocationArgument(ResourceKey.createRegistryKey(resourcelocation));
      }

      public void serializeToJson(ResourceOrTagLocationArgument<?> p_211006_, JsonObject p_211007_) {
         p_211007_.addProperty("registry", p_211006_.registryKey.location().toString());
      }
   }

   static record TagResult<T>(TagKey<T> key) implements ResourceOrTagLocationArgument.Result<T> {
      public Either<ResourceKey<T>, TagKey<T>> unwrap() {
         return Either.right(this.key);
      }

      public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> p_211022_) {
         return this.key.cast(p_211022_).map(ResourceOrTagLocationArgument.TagResult::new);
      }

      public boolean test(Holder<T> p_211020_) {
         return p_211020_.is(this.key);
      }

      public String asPrintable() {
         return "#" + this.key.location();
      }
   }
}