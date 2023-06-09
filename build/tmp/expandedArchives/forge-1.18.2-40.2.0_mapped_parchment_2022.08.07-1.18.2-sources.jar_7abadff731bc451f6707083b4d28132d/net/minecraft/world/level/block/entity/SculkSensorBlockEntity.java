package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;

public class SculkSensorBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
   private final VibrationListener listener;
   private int lastVibrationFrequency;

   public SculkSensorBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.SCULK_SENSOR, pPos, pBlockState);
      this.listener = new VibrationListener(new BlockPositionSource(this.worldPosition), ((SculkSensorBlock)pBlockState.getBlock()).getListenerRange(), this);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.lastVibrationFrequency = pTag.getInt("last_vibration_frequency");
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.putInt("last_vibration_frequency", this.lastVibrationFrequency);
   }

   public VibrationListener getListener() {
      return this.listener;
   }

   public int getLastVibrationFrequency() {
      return this.lastVibrationFrequency;
   }

   public boolean shouldListen(Level pLevel, GameEventListener pListener, BlockPos pPos, GameEvent pGameEvent, @Nullable Entity pEntity) {
      boolean flag = pGameEvent == GameEvent.BLOCK_DESTROY && pPos.equals(this.getBlockPos());
      boolean flag1 = pGameEvent == GameEvent.BLOCK_PLACE && pPos.equals(this.getBlockPos());
      return !flag && !flag1 && SculkSensorBlock.canActivate(this.getBlockState());
   }

   public void onSignalReceive(Level pLevel, GameEventListener pListener, GameEvent pGameEvent, int pDistance) {
      BlockState blockstate = this.getBlockState();
      if (!pLevel.isClientSide() && SculkSensorBlock.canActivate(blockstate)) {
         this.lastVibrationFrequency = SculkSensorBlock.VIBRATION_STRENGTH_FOR_EVENT.getInt(pGameEvent);
         SculkSensorBlock.activate(pLevel, this.worldPosition, blockstate, getRedstoneStrengthForDistance(pDistance, pListener.getListenerRadius()));
      }

   }

   public static int getRedstoneStrengthForDistance(int pDistance, int pRadius) {
      double d0 = (double)pDistance / (double)pRadius;
      return Math.max(1, 15 - Mth.floor(d0 * 15.0D));
   }
}