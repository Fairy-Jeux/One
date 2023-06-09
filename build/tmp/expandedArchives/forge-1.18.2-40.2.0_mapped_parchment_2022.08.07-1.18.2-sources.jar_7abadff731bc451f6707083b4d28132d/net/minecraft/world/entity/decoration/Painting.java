package net.minecraft.world.entity.decoration;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class Painting extends HangingEntity {
   public Motive motive = Motive.KEBAB;

   public Painting(EntityType<? extends Painting> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public Painting(Level pLevel, BlockPos pPos, Direction pFacingDirection) {
      super(EntityType.PAINTING, pLevel, pPos);
      List<Motive> list = Lists.newArrayList();
      int i = 0;

      for(Motive motive : Registry.MOTIVE) {
         this.motive = motive;
         this.setDirection(pFacingDirection);
         if (this.survives()) {
            list.add(motive);
            int j = motive.getWidth() * motive.getHeight();
            if (j > i) {
               i = j;
            }
         }
      }

      if (!list.isEmpty()) {
         Iterator<Motive> iterator = list.iterator();

         while(iterator.hasNext()) {
            Motive motive1 = iterator.next();
            if (motive1.getWidth() * motive1.getHeight() < i) {
               iterator.remove();
            }
         }

         this.motive = list.get(this.random.nextInt(list.size()));
      }

      this.setDirection(pFacingDirection);
   }

   public Painting(Level pLevel, BlockPos pPos, Direction pFacingDirection, Motive pMotive) {
      this(pLevel, pPos, pFacingDirection);
      this.motive = pMotive;
      this.setDirection(pFacingDirection);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      pCompound.putString("Motive", Registry.MOTIVE.getKey(this.motive).toString());
      pCompound.putByte("Facing", (byte)this.direction.get2DDataValue());
      super.addAdditionalSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      this.motive = Registry.MOTIVE.get(ResourceLocation.tryParse(pCompound.getString("Motive")));
      this.direction = Direction.from2DDataValue(pCompound.getByte("Facing"));
      super.readAdditionalSaveData(pCompound);
      this.setDirection(this.direction);
   }

   public int getWidth() {
      return this.motive.getWidth();
   }

   public int getHeight() {
      return this.motive.getHeight();
   }

   /**
    * Called when this entity is broken. Entity parameter may be null.
    */
   public void dropItem(@Nullable Entity pBrokenEntity) {
      if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
         if (pBrokenEntity instanceof Player) {
            Player player = (Player)pBrokenEntity;
            if (player.getAbilities().instabuild) {
               return;
            }
         }

         this.spawnAtLocation(Items.PAINTING);
      }
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
   }

   /**
    * Sets the location and rotation of the entity in the world.
    */
   public void moveTo(double pX, double pY, double pZ, float pYaw, float pPitch) {
      this.setPos(pX, pY, pZ);
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
      BlockPos blockpos = this.pos.offset(pX - this.getX(), pY - this.getY(), pZ - this.getZ());
      this.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
   }

   public Packet<?> getAddEntityPacket() {
      return new ClientboundAddPaintingPacket(this);
   }

   public ItemStack getPickResult() {
      return new ItemStack(Items.PAINTING);
   }
}