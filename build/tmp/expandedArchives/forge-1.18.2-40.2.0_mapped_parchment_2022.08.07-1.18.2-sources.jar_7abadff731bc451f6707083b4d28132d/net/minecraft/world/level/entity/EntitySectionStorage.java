package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.Spliterators;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.Consumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public class EntitySectionStorage<T extends EntityAccess> {
   private final Class<T> entityClass;
   private final Long2ObjectFunction<Visibility> intialSectionVisibility;
   private final Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap<>();
   private final LongSortedSet sectionIds = new LongAVLTreeSet();

   public EntitySectionStorage(Class<T> pEntityClass, Long2ObjectFunction<Visibility> pIntialSectionVisibility) {
      this.entityClass = pEntityClass;
      this.intialSectionVisibility = pIntialSectionVisibility;
   }

   public void forEachAccessibleNonEmptySection(AABB pBoundingBox, Consumer<EntitySection<T>> pConsumer) {
      int i = SectionPos.posToSectionCoord(pBoundingBox.minX - 2.0D);
      int j = SectionPos.posToSectionCoord(pBoundingBox.minY - 2.0D);
      int k = SectionPos.posToSectionCoord(pBoundingBox.minZ - 2.0D);
      int l = SectionPos.posToSectionCoord(pBoundingBox.maxX + 2.0D);
      int i1 = SectionPos.posToSectionCoord(pBoundingBox.maxY + 2.0D);
      int j1 = SectionPos.posToSectionCoord(pBoundingBox.maxZ + 2.0D);

      for(int k1 = i; k1 <= l; ++k1) {
         long l1 = SectionPos.asLong(k1, 0, 0);
         long i2 = SectionPos.asLong(k1, -1, -1);
         LongIterator longiterator = this.sectionIds.subSet(l1, i2 + 1L).iterator();

         while(longiterator.hasNext()) {
            long j2 = longiterator.nextLong();
            int k2 = SectionPos.y(j2);
            int l2 = SectionPos.z(j2);
            if (k2 >= j && k2 <= i1 && l2 >= k && l2 <= j1) {
               EntitySection<T> entitysection = this.sections.get(j2);
               if (entitysection != null && !entitysection.isEmpty() && entitysection.getStatus().isAccessible()) {
                  pConsumer.accept(entitysection);
               }
            }
         }
      }

   }

   public LongStream getExistingSectionPositionsInChunk(long pPos) {
      int i = ChunkPos.getX(pPos);
      int j = ChunkPos.getZ(pPos);
      LongSortedSet longsortedset = this.getChunkSections(i, j);
      if (longsortedset.isEmpty()) {
         return LongStream.empty();
      } else {
         OfLong oflong = longsortedset.iterator();
         return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(oflong, 1301), false);
      }
   }

   private LongSortedSet getChunkSections(int pX, int pZ) {
      long i = SectionPos.asLong(pX, 0, pZ);
      long j = SectionPos.asLong(pX, -1, pZ);
      return this.sectionIds.subSet(i, j + 1L);
   }

   public Stream<EntitySection<T>> getExistingSectionsInChunk(long pPos) {
      return this.getExistingSectionPositionsInChunk(pPos).mapToObj(this.sections::get).filter(Objects::nonNull);
   }

   private static long getChunkKeyFromSectionKey(long pPos) {
      return ChunkPos.asLong(SectionPos.x(pPos), SectionPos.z(pPos));
   }

   public EntitySection<T> getOrCreateSection(long pSectionPos) {
      return this.sections.computeIfAbsent(pSectionPos, this::createSection);
   }

   @Nullable
   public EntitySection<T> getSection(long pSectionPos) {
      return this.sections.get(pSectionPos);
   }

   private EntitySection<T> createSection(long p_156902_) {
      long i = getChunkKeyFromSectionKey(p_156902_);
      Visibility visibility = this.intialSectionVisibility.get(i);
      this.sectionIds.add(p_156902_);
      return new EntitySection<>(this.entityClass, visibility);
   }

   public LongSet getAllChunksWithExistingSections() {
      LongSet longset = new LongOpenHashSet();
      this.sections.keySet().forEach((java.util.function.LongConsumer)(p_156886_) -> {
         longset.add(getChunkKeyFromSectionKey(p_156886_));
      });
      return longset;
   }

   public void getEntities(AABB pBounds, Consumer<T> pConsumer) {
      this.forEachAccessibleNonEmptySection(pBounds, (p_188368_) -> {
         p_188368_.getEntities(pBounds, pConsumer);
      });
   }

   public <U extends T> void getEntities(EntityTypeTest<T, U> pTest, AABB pBounds, Consumer<U> pConsumer) {
      this.forEachAccessibleNonEmptySection(pBounds, (p_188361_) -> {
         p_188361_.getEntities(pTest, pBounds, pConsumer);
      });
   }

   public void remove(long pSectionId) {
      this.sections.remove(pSectionId);
      this.sectionIds.remove(pSectionId);
   }

   @VisibleForDebug
   public int count() {
      return this.sectionIds.size();
   }
}