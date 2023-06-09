package net.minecraft.world.entity.npc;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.AABB;

public class CatSpawner implements CustomSpawner {
   private static final int TICK_DELAY = 1200;
   private int nextTick;

   public int tick(ServerLevel pLevel, boolean pSpawnHostiles, boolean pSpawnPassives) {
      if (pSpawnPassives && pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
         --this.nextTick;
         if (this.nextTick > 0) {
            return 0;
         } else {
            this.nextTick = 1200;
            Player player = pLevel.getRandomPlayer();
            if (player == null) {
               return 0;
            } else {
               Random random = pLevel.random;
               int i = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
               int j = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
               BlockPos blockpos = player.blockPosition().offset(i, 0, j);
               int k = 10;
               if (!pLevel.hasChunksAt(blockpos.getX() - 10, blockpos.getZ() - 10, blockpos.getX() + 10, blockpos.getZ() + 10)) {
                  return 0;
               } else {
                  if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, pLevel, blockpos, EntityType.CAT)) {
                     if (pLevel.isCloseToVillage(blockpos, 2)) {
                        return this.spawnInVillage(pLevel, blockpos);
                     }

                     Registry<ConfiguredStructureFeature<?, ?>> registry = pLevel.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
                     if (ChunkGenerator.allConfigurations(registry, StructureFeature.SWAMP_HUT).anyMatch((p_207764_) -> {
                        return pLevel.structureFeatureManager().getStructureWithPieceAt(blockpos, p_207764_).isValid();
                     })) {
                        return this.spawnInHut(pLevel, blockpos);
                     }
                  }

                  return 0;
               }
            }
         }
      } else {
         return 0;
      }
   }

   private int spawnInVillage(ServerLevel pServerLevel, BlockPos pPos) {
      int i = 48;
      if (pServerLevel.getPoiManager().getCountInRange(PoiType.HOME.getPredicate(), pPos, 48, PoiManager.Occupancy.IS_OCCUPIED) > 4L) {
         List<Cat> list = pServerLevel.getEntitiesOfClass(Cat.class, (new AABB(pPos)).inflate(48.0D, 8.0D, 48.0D));
         if (list.size() < 5) {
            return this.spawnCat(pPos, pServerLevel);
         }
      }

      return 0;
   }

   private int spawnInHut(ServerLevel pServerLevel, BlockPos pPos) {
      int i = 16;
      List<Cat> list = pServerLevel.getEntitiesOfClass(Cat.class, (new AABB(pPos)).inflate(16.0D, 8.0D, 16.0D));
      return list.size() < 1 ? this.spawnCat(pPos, pServerLevel) : 0;
   }

   private int spawnCat(BlockPos pPos, ServerLevel pServerLevel) {
      Cat cat = EntityType.CAT.create(pServerLevel);
      if (cat == null) {
         return 0;
      } else {
         cat.moveTo(pPos, 0.0F, 0.0F); // Fix MC-147659: Some witch huts spawn the incorrect cat
         if(net.minecraftforge.common.ForgeHooks.canEntitySpawn(cat, pServerLevel, pPos.getX(), pPos.getY(), pPos.getZ(), null, MobSpawnType.NATURAL) == -1) return 0;
         cat.finalizeSpawn(pServerLevel, pServerLevel.getCurrentDifficultyAt(pPos), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
         pServerLevel.addFreshEntityWithPassengers(cat);
         return 1;
      }
   }
}
