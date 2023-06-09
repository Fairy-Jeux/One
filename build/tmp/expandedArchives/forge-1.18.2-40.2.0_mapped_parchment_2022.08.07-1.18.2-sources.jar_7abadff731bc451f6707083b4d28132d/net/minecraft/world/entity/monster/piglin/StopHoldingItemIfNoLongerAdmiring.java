package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Items;

public class StopHoldingItemIfNoLongerAdmiring<E extends Piglin> extends Behavior<E> {
   public StopHoldingItemIfNoLongerAdmiring() {
      super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT));
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
      return !pOwner.getOffhandItem().isEmpty() && !pOwner.getOffhandItem().canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK);
   }

   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
      PiglinAi.stopHoldingOffHandItem(pEntity, true);
   }
}
