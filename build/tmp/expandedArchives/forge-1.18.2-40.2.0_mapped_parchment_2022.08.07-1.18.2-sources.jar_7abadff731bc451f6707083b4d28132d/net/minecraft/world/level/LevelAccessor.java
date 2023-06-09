package net.minecraft.world.level;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
   default long dayTime() {
      return this.getLevelData().getDayTime();
   }

   long nextSubTickCount();

   LevelTickAccess<Block> getBlockTicks();

   private <T> ScheduledTick<T> createTick(BlockPos pPos, T pType, int pDelay, TickPriority pPriority) {
      return new ScheduledTick<>(pType, pPos, this.getLevelData().getGameTime() + (long)pDelay, pPriority, this.nextSubTickCount());
   }

   private <T> ScheduledTick<T> createTick(BlockPos pPos, T pType, int pDelay) {
      return new ScheduledTick<>(pType, pPos, this.getLevelData().getGameTime() + (long)pDelay, this.nextSubTickCount());
   }

   default void scheduleTick(BlockPos pPos, Block pBlock, int pDelay, TickPriority pPriority) {
      this.getBlockTicks().schedule(this.createTick(pPos, pBlock, pDelay, pPriority));
   }

   default void scheduleTick(BlockPos pPos, Block pBlock, int pDelay) {
      this.getBlockTicks().schedule(this.createTick(pPos, pBlock, pDelay));
   }

   LevelTickAccess<Fluid> getFluidTicks();

   default void scheduleTick(BlockPos pPos, Fluid pFluid, int pDelay, TickPriority pPriority) {
      this.getFluidTicks().schedule(this.createTick(pPos, pFluid, pDelay, pPriority));
   }

   default void scheduleTick(BlockPos pPos, Fluid pFluid, int pDelay) {
      this.getFluidTicks().schedule(this.createTick(pPos, pFluid, pDelay));
   }

   /**
    * Returns the world's WorldInfo object
    */
   LevelData getLevelData();

   DifficultyInstance getCurrentDifficultyAt(BlockPos pPos);

   @Nullable
   MinecraftServer getServer();

   default Difficulty getDifficulty() {
      return this.getLevelData().getDifficulty();
   }

   /**
    * Gets the world's chunk provider
    */
   ChunkSource getChunkSource();

   default boolean hasChunk(int pChunkX, int pChunkZ) {
      return this.getChunkSource().hasChunk(pChunkX, pChunkZ);
   }

   Random getRandom();

   default void blockUpdated(BlockPos pPos, Block pBlock) {
   }

   /**
    * Plays a sound. On the server, the sound is broadcast to all nearby <em>except</em> the given player. On the
    * client, the sound only plays if the given player is the client player. Thus, this method is intended to be called
    * from code running on both sides. The client plays it locally and the server plays it for everyone else.
    */
   void playSound(@Nullable Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch);

   void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed);

   void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData);

   default void levelEvent(int pType, BlockPos pPos, int pData) {
      this.levelEvent((Player)null, pType, pPos, pData);
   }

   void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, BlockPos pPos);

   default void gameEvent(GameEvent pGameEvent, BlockPos pPos) {
      this.gameEvent((Entity)null, pGameEvent, pPos);
   }

   default void gameEvent(GameEvent pGameEvent, Entity pEntity) {
      this.gameEvent((Entity)null, pGameEvent, pEntity.blockPosition());
   }

   default void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, Entity pTarget) {
      this.gameEvent(pEntity, pEvent, pTarget.blockPosition());
   }
}