package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GoToClosestVillage extends Behavior<Villager> {
   private final float speedModifier;
   private final int closeEnoughDistance;

   public GoToClosestVillage(float pSpeedModifier, int pCloseEnoughDistance) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
      this.speedModifier = pSpeedModifier;
      this.closeEnoughDistance = pCloseEnoughDistance;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      return !pLevel.isVillage(pOwner.blockPosition());
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      PoiManager poimanager = pLevel.getPoiManager();
      int i = poimanager.sectionsToVillage(SectionPos.of(pEntity.blockPosition()));
      Vec3 vec3 = null;

      for(int j = 0; j < 5; ++j) {
         Vec3 vec31 = LandRandomPos.getPos(pEntity, 15, 7, (p_147554_) -> {
            return (double)(-poimanager.sectionsToVillage(SectionPos.of(p_147554_)));
         });
         if (vec31 != null) {
            int k = poimanager.sectionsToVillage(SectionPos.of(new BlockPos(vec31)));
            if (k < i) {
               vec3 = vec31;
               break;
            }

            if (k == i) {
               vec3 = vec31;
            }
         }
      }

      if (vec3 != null) {
         pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedModifier, this.closeEnoughDistance));
      }

   }
}