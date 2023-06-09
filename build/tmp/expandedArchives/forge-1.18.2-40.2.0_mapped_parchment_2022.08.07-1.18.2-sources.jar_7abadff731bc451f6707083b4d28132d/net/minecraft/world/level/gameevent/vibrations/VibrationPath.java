package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;

public class VibrationPath {
   public static final Codec<VibrationPath> CODEC = RecordCodecBuilder.create((p_157940_) -> {
      return p_157940_.group(BlockPos.CODEC.fieldOf("origin").forGetter((p_157953_) -> {
         return p_157953_.origin;
      }), PositionSource.CODEC.fieldOf("destination").forGetter((p_157950_) -> {
         return p_157950_.destination;
      }), Codec.INT.fieldOf("arrival_in_ticks").forGetter((p_157942_) -> {
         return p_157942_.arrivalInTicks;
      })).apply(p_157940_, VibrationPath::new);
   });
   private final BlockPos origin;
   private final PositionSource destination;
   private final int arrivalInTicks;

   public VibrationPath(BlockPos p_157935_, PositionSource p_157936_, int p_157937_) {
      this.origin = p_157935_;
      this.destination = p_157936_;
      this.arrivalInTicks = p_157937_;
   }

   public int getArrivalInTicks() {
      return this.arrivalInTicks;
   }

   public BlockPos getOrigin() {
      return this.origin;
   }

   public PositionSource getDestination() {
      return this.destination;
   }

   public static VibrationPath read(FriendlyByteBuf pBuffer) {
      BlockPos blockpos = pBuffer.readBlockPos();
      PositionSource positionsource = PositionSourceType.fromNetwork(pBuffer);
      int i = pBuffer.readVarInt();
      return new VibrationPath(blockpos, positionsource, i);
   }

   public static void write(FriendlyByteBuf pBuffer, VibrationPath pPath) {
      pBuffer.writeBlockPos(pPath.origin);
      PositionSourceType.toNetwork(pPath.destination, pBuffer);
      pBuffer.writeVarInt(pPath.arrivalInTicks);
   }
}