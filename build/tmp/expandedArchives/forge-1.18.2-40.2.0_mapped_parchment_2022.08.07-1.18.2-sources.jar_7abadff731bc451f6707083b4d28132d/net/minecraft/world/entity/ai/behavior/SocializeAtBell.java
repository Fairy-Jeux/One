package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell extends Behavior<LivingEntity> {
   private static final float SPEED_MODIFIER = 0.3F;

   public SocializeAtBell() {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT, MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_ABSENT));
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, LivingEntity pOwner) {
      Brain<?> brain = pOwner.getBrain();
      Optional<GlobalPos> optional = brain.getMemory(MemoryModuleType.MEETING_POINT);
      return pLevel.getRandom().nextInt(100) == 0 && optional.isPresent() && pLevel.dimension() == optional.get().dimension() && optional.get().pos().closerToCenterThan(pOwner.position(), 4.0D) && brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains((p_24189_) -> {
         return EntityType.VILLAGER.equals(p_24189_.getType());
      });
   }

   protected void start(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap((p_186067_) -> {
         return p_186067_.findClosest((p_186064_) -> {
            return EntityType.VILLAGER.equals(p_186064_.getType()) && p_186064_.distanceToSqr(pEntity) <= 32.0D;
         });
      }).ifPresent((p_147977_) -> {
         brain.setMemory(MemoryModuleType.INTERACTION_TARGET, p_147977_);
         brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(p_147977_, true));
         brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(p_147977_, false), 0.3F, 1));
      });
   }
}