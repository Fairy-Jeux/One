package net.minecraft.world.level.gameevent.vibrations;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VibrationListener implements GameEventListener {
   protected final PositionSource listenerSource;
   protected final int listenerRange;
   protected final VibrationListener.VibrationListenerConfig config;
   protected Optional<GameEvent> receivingEvent = Optional.empty();
   protected int receivingDistance;
   protected int travelTimeInTicks = 0;

   public VibrationListener(PositionSource pListenerSource, int pListenerRange, VibrationListener.VibrationListenerConfig pConfig) {
      this.listenerSource = pListenerSource;
      this.listenerRange = pListenerRange;
      this.config = pConfig;
   }

   public void tick(Level pLevel) {
      if (this.receivingEvent.isPresent()) {
         --this.travelTimeInTicks;
         if (this.travelTimeInTicks <= 0) {
            this.travelTimeInTicks = 0;
            this.config.onSignalReceive(pLevel, this, this.receivingEvent.get(), this.receivingDistance);
            this.receivingEvent = Optional.empty();
         }
      }

   }

   /**
    * Gets the position of the listener itself.
    */
   public PositionSource getListenerSource() {
      return this.listenerSource;
   }

   /**
    * Gets the listening radius of the listener. Events within this radius will notify the listener when broadcasted.
    */
   public int getListenerRadius() {
      return this.listenerRange;
   }

   /**
    * Called when a game event within range of the listener has been broadcasted.
    * @param pLevel The level where the event was broadcasted.
    * @param pEvent The event being detected.
    * @param pEntity The entity that caused the event to happen.
    * @param pPos The originating position of the event.
    */
   public boolean handleGameEvent(Level pLevel, GameEvent pEvent, @Nullable Entity pEntity, BlockPos pPos) {
      if (!this.isValidVibration(pEvent, pEntity)) {
         return false;
      } else {
         Optional<BlockPos> optional = this.listenerSource.getPosition(pLevel);
         if (!optional.isPresent()) {
            return false;
         } else {
            BlockPos blockpos = optional.get();
            if (!this.config.shouldListen(pLevel, this, pPos, pEvent, pEntity)) {
               return false;
            } else if (this.isOccluded(pLevel, pPos, blockpos)) {
               return false;
            } else {
               this.sendSignal(pLevel, pEvent, pPos, blockpos);
               return true;
            }
         }
      }
   }

   private boolean isValidVibration(GameEvent pEvent, @Nullable Entity pEntity) {
      if (this.receivingEvent.isPresent()) {
         return false;
      } else if (!pEvent.is(GameEventTags.VIBRATIONS)) {
         return false;
      } else {
         if (pEntity != null) {
            if (pEvent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING) && pEntity.isSteppingCarefully()) {
               return false;
            }

            if (pEntity.occludesVibrations()) {
               return false;
            }
         }

         return pEntity == null || !pEntity.isSpectator();
      }
   }

   private void sendSignal(Level pLevel, GameEvent pEvent, BlockPos pOrigin, BlockPos pDestination) {
      this.receivingEvent = Optional.of(pEvent);
      if (pLevel instanceof ServerLevel) {
         this.receivingDistance = Mth.floor(Math.sqrt(pOrigin.distSqr(pDestination)));
         this.travelTimeInTicks = this.receivingDistance;
         ((ServerLevel)pLevel).sendVibrationParticle(new VibrationPath(pOrigin, this.listenerSource, this.travelTimeInTicks));
      }

   }

   private boolean isOccluded(Level pLevel, BlockPos pFrom, BlockPos pTo) {
      return pLevel.isBlockInLine(new ClipBlockStateContext(Vec3.atCenterOf(pFrom), Vec3.atCenterOf(pTo), (p_157915_) -> {
         return p_157915_.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS);
      })).getType() == HitResult.Type.BLOCK;
   }

   public interface VibrationListenerConfig {
      boolean shouldListen(Level pLevel, GameEventListener pListener, BlockPos pPos, GameEvent pGameEvent, @Nullable Entity pEntity);

      void onSignalReceive(Level pLevel, GameEventListener pListener, GameEvent pGameEvent, int pDistance);
   }
}