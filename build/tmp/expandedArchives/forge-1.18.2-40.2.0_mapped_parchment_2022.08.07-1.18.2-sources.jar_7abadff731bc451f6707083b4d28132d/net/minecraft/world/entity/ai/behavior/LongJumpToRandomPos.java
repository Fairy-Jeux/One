package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob> extends Behavior<E> {
   private static final int FIND_JUMP_TRIES = 20;
   private static final int PREPARE_JUMP_DURATION = 40;
   private static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
   public static final int TIME_OUT_DURATION = 200;
   private final UniformInt timeBetweenLongJumps;
   private final int maxLongJumpHeight;
   private final int maxLongJumpWidth;
   private final float maxJumpVelocity;
   private final List<LongJumpToRandomPos.PossibleJump> jumpCandidates = new ArrayList<>();
   private Optional<Vec3> initialPosition = Optional.empty();
   private Optional<LongJumpToRandomPos.PossibleJump> chosenJump = Optional.empty();
   private int findJumpTries;
   private long prepareJumpStart;
   private Function<E, SoundEvent> getJumpSound;

   public LongJumpToRandomPos(UniformInt pTimeBetweenLongJumps, int pMaxLongJumpHeight, int pMaxLongJumpWidth, float pMaxJumpVelocity, Function<E, SoundEvent> pGetJumpSound) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), 200);
      this.timeBetweenLongJumps = pTimeBetweenLongJumps;
      this.maxLongJumpHeight = pMaxLongJumpHeight;
      this.maxLongJumpWidth = pMaxLongJumpWidth;
      this.maxJumpVelocity = pMaxJumpVelocity;
      this.getJumpSound = pGetJumpSound;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Mob pOwner) {
      return pOwner.isOnGround() && !pLevel.getBlockState(pOwner.blockPosition()).is(Blocks.HONEY_BLOCK);
   }

   protected boolean canStillUse(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      boolean flag = this.initialPosition.isPresent() && this.initialPosition.get().equals(pEntity.position()) && this.findJumpTries > 0 && (this.chosenJump.isPresent() || !this.jumpCandidates.isEmpty());
      if (!flag && !pEntity.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isPresent()) {
         pEntity.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(pLevel.random) / 2);
      }

      return flag;
   }

   protected void start(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      this.chosenJump = Optional.empty();
      this.findJumpTries = 20;
      this.jumpCandidates.clear();
      this.initialPosition = Optional.of(pEntity.position());
      BlockPos blockpos = pEntity.blockPosition();
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      Iterable<BlockPos> iterable = BlockPos.betweenClosed(i - this.maxLongJumpWidth, j - this.maxLongJumpHeight, k - this.maxLongJumpWidth, i + this.maxLongJumpWidth, j + this.maxLongJumpHeight, k + this.maxLongJumpWidth);
      PathNavigation pathnavigation = pEntity.getNavigation();

      for(BlockPos blockpos1 : iterable) {
         double d0 = blockpos1.distSqr(blockpos);
         if ((i != blockpos1.getX() || k != blockpos1.getZ()) && pathnavigation.isStableDestination(blockpos1) && pEntity.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(pEntity.level, blockpos1.mutable())) == 0.0F) {
            Optional<Vec3> optional = this.calculateOptimalJumpVector(pEntity, Vec3.atCenterOf(blockpos1));
            optional.ifPresent((p_147670_) -> {
               this.jumpCandidates.add(new LongJumpToRandomPos.PossibleJump(new BlockPos(blockpos1), p_147670_, Mth.ceil(d0)));
            });
         }
      }

   }

   protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
      if (this.chosenJump.isPresent()) {
         if (pGameTime - this.prepareJumpStart >= 40L) {
            pOwner.setYRot(pOwner.yBodyRot);
            pOwner.setDiscardFriction(true);
            Vec3 vec3 = this.chosenJump.get().getJumpVector();
            double d0 = vec3.length();
            double d1 = d0 + pOwner.getJumpBoostPower();
            pOwner.setDeltaMovement(vec3.scale(d1 / d0));
            pOwner.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
            pLevel.playSound((Player)null, pOwner, this.getJumpSound.apply(pOwner), SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
      } else {
         --this.findJumpTries;
         Optional<LongJumpToRandomPos.PossibleJump> optional = WeightedRandom.getRandomItem(pLevel.random, this.jumpCandidates);
         if (optional.isPresent()) {
            this.jumpCandidates.remove(optional.get());
            pOwner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(optional.get().getJumpTarget()));
            PathNavigation pathnavigation = pOwner.getNavigation();
            Path path = pathnavigation.createPath(optional.get().getJumpTarget(), 0, 8);
            if (path == null || !path.canReach()) {
               this.chosenJump = optional;
               this.prepareJumpStart = pGameTime;
            }
         }
      }

   }

   private Optional<Vec3> calculateOptimalJumpVector(Mob p_147657_, Vec3 p_147658_) {
      Optional<Vec3> optional = Optional.empty();

      for(int i = 65; i < 85; i += 5) {
         Optional<Vec3> optional1 = this.calculateJumpVectorForAngle(p_147657_, p_147658_, i);
         if (!optional.isPresent() || optional1.isPresent() && optional1.get().lengthSqr() < optional.get().lengthSqr()) {
            optional = optional1;
         }
      }

      return optional;
   }

   private Optional<Vec3> calculateJumpVectorForAngle(Mob p_147660_, Vec3 p_147661_, int p_147662_) {
      Vec3 vec3 = p_147660_.position();
      Vec3 vec31 = (new Vec3(p_147661_.x - vec3.x, 0.0D, p_147661_.z - vec3.z)).normalize().scale(0.5D);
      p_147661_ = p_147661_.subtract(vec31);
      Vec3 vec32 = p_147661_.subtract(vec3);
      float f = (float)p_147662_ * (float)Math.PI / 180.0F;
      double d0 = Math.atan2(vec32.z, vec32.x);
      double d1 = vec32.subtract(0.0D, vec32.y, 0.0D).lengthSqr();
      double d2 = Math.sqrt(d1);
      double d3 = vec32.y;
      double d4 = Math.sin((double)(2.0F * f));
      double d5 = 0.08D;
      double d6 = Math.pow(Math.cos((double)f), 2.0D);
      double d7 = Math.sin((double)f);
      double d8 = Math.cos((double)f);
      double d9 = Math.sin(d0);
      double d10 = Math.cos(d0);
      double d11 = d1 * 0.08D / (d2 * d4 - 2.0D * d3 * d6);
      if (d11 < 0.0D) {
         return Optional.empty();
      } else {
         double d12 = Math.sqrt(d11);
         if (d12 > (double)this.maxJumpVelocity) {
            return Optional.empty();
         } else {
            double d13 = d12 * d8;
            double d14 = d12 * d7;
            int i = Mth.ceil(d2 / d13) * 2;
            double d15 = 0.0D;
            Vec3 vec33 = null;

            for(int j = 0; j < i - 1; ++j) {
               d15 += d2 / (double)i;
               double d16 = d7 / d8 * d15 - Math.pow(d15, 2.0D) * 0.08D / (2.0D * d11 * Math.pow(d8, 2.0D));
               double d17 = d15 * d10;
               double d18 = d15 * d9;
               Vec3 vec34 = new Vec3(vec3.x + d17, vec3.y + d16, vec3.z + d18);
               if (vec33 != null && !this.isClearTransition(p_147660_, vec33, vec34)) {
                  return Optional.empty();
               }

               vec33 = vec34;
            }

            return Optional.of((new Vec3(d13 * d10, d14, d13 * d9)).scale((double)0.95F));
         }
      }
   }

   private boolean isClearTransition(Mob p_147664_, Vec3 p_147665_, Vec3 p_147666_) {
      EntityDimensions entitydimensions = p_147664_.getDimensions(Pose.LONG_JUMPING);
      Vec3 vec3 = p_147666_.subtract(p_147665_);
      double d0 = (double)Math.min(entitydimensions.width, entitydimensions.height);
      int i = Mth.ceil(vec3.length() / d0);
      Vec3 vec31 = vec3.normalize();
      Vec3 vec32 = p_147665_;

      for(int j = 0; j < i; ++j) {
         vec32 = j == i - 1 ? p_147666_ : vec32.add(vec31.scale(d0 * (double)0.9F));
         AABB aabb = entitydimensions.makeBoundingBox(vec32);
         if (!p_147664_.level.noCollision(p_147664_, aabb)) {
            return false;
         }
      }

      return true;
   }

   public static class PossibleJump extends WeightedEntry.IntrusiveBase {
      private final BlockPos jumpTarget;
      private final Vec3 jumpVector;

      public PossibleJump(BlockPos pJumpTarget, Vec3 pJumpVector, int pWeight) {
         super(pWeight);
         this.jumpTarget = pJumpTarget;
         this.jumpVector = pJumpVector;
      }

      public BlockPos getJumpTarget() {
         return this.jumpTarget;
      }

      public Vec3 getJumpVector() {
         return this.jumpVector;
      }
   }
}