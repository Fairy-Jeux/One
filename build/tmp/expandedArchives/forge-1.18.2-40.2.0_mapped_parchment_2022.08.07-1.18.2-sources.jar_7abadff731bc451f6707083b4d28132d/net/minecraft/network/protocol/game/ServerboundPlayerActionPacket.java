package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
   private final BlockPos pos;
   private final Direction direction;
   /** Status of the digging (started, ongoing, broken). */
   private final ServerboundPlayerActionPacket.Action action;

   public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action pAction, BlockPos pPos, Direction pDirection) {
      this.action = pAction;
      this.pos = pPos.immutable();
      this.direction = pDirection;
   }

   public ServerboundPlayerActionPacket(FriendlyByteBuf pBuffer) {
      this.action = pBuffer.readEnum(ServerboundPlayerActionPacket.Action.class);
      this.pos = pBuffer.readBlockPos();
      this.direction = Direction.from3DDataValue(pBuffer.readUnsignedByte());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.action);
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeByte(this.direction.get3DDataValue());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handlePlayerAction(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public ServerboundPlayerActionPacket.Action getAction() {
      return this.action;
   }

   public static enum Action {
      START_DESTROY_BLOCK,
      ABORT_DESTROY_BLOCK,
      STOP_DESTROY_BLOCK,
      DROP_ALL_ITEMS,
      DROP_ITEM,
      RELEASE_USE_ITEM,
      SWAP_ITEM_WITH_OFFHAND;
   }
}