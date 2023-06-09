package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HolderSet<T> extends Iterable<Holder<T>> {
   Stream<Holder<T>> stream();

   int size();

   Either<TagKey<T>, List<Holder<T>>> unwrap();

   Optional<Holder<T>> getRandomElement(Random pRandom);

   Holder<T> get(int pIndex);

   boolean contains(Holder<T> pHolder);

   boolean isValidInRegistry(Registry<T> pRegistry);

   @SafeVarargs
   static <T> HolderSet.Direct<T> direct(Holder<T>... pContents) {
      return new HolderSet.Direct<>(List.of(pContents));
   }

   static <T> HolderSet.Direct<T> direct(List<? extends Holder<T>> pContents) {
      return new HolderSet.Direct<>(List.copyOf(pContents));
   }

   @SafeVarargs
   static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> p_205807_, E... p_205808_) {
      return direct(Stream.of(p_205808_).map(p_205807_).toList());
   }

   static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> p_205804_, List<E> p_205805_) {
      return direct(p_205805_.stream().map(p_205804_).toList());
   }

   public static class Direct<T> extends HolderSet.ListBacked<T> {
      private final List<Holder<T>> contents;
      @Nullable
      private Set<Holder<T>> contentsSet;

      Direct(List<Holder<T>> pContents) {
         this.contents = pContents;
      }

      protected List<Holder<T>> contents() {
         return this.contents;
      }

      public Either<TagKey<T>, List<Holder<T>>> unwrap() {
         return Either.right(this.contents);
      }

      public boolean contains(Holder<T> pHolder) {
         if (this.contentsSet == null) {
            this.contentsSet = Set.copyOf(this.contents);
         }

         return this.contentsSet.contains(pHolder);
      }

      public String toString() {
         return "DirectSet[" + this.contents + "]";
      }
   }

   public abstract static class ListBacked<T> implements HolderSet<T> {
      protected abstract List<Holder<T>> contents();

      public int size() {
         return this.contents().size();
      }

      public Spliterator<Holder<T>> spliterator() {
         return this.contents().spliterator();
      }

      @NotNull
      public Iterator<Holder<T>> iterator() {
         return this.contents().iterator();
      }

      public Stream<Holder<T>> stream() {
         return this.contents().stream();
      }

      public Optional<Holder<T>> getRandomElement(Random p_205825_) {
         return Util.getRandomSafe(this.contents(), p_205825_);
      }

      public Holder<T> get(int p_205823_) {
         return this.contents().get(p_205823_);
      }

      public boolean isValidInRegistry(Registry<T> p_211043_) {
         return true;
      }
   }

   public static class Named<T> extends HolderSet.ListBacked<T> {
      private final Registry<T> registry;
      private final TagKey<T> key;
      private List<Holder<T>> contents = List.of();

      public Named(Registry<T> pRegistry, TagKey<T> pKey) {
         this.registry = pRegistry;
         this.key = pKey;
      }

      public void bind(List<Holder<T>> pContents) {
         this.contents = List.copyOf(pContents);
      }

      public TagKey<T> key() {
         return this.key;
      }

      protected List<Holder<T>> contents() {
         return this.contents;
      }

      public Either<TagKey<T>, List<Holder<T>>> unwrap() {
         return Either.left(this.key);
      }

      public boolean contains(Holder<T> pHolder) {
         return pHolder.is(this.key);
      }

      public String toString() {
         return "NamedSet(" + this.key + ")[" + this.contents + "]";
      }

      public boolean isValidInRegistry(Registry<T> pRegistry) {
         return this.registry == pRegistry;
      }
   }
}