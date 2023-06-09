package net.minecraft.network.chat;

public enum ChatType {
   CHAT((byte)0, false),
   SYSTEM((byte)1, true),
   GAME_INFO((byte)2, true);

   private final byte index;
   private final boolean interrupt;

   private ChatType(byte pIndex, boolean pInterrupt) {
      this.index = pIndex;
      this.interrupt = pInterrupt;
   }

   public byte getIndex() {
      return this.index;
   }

   public static ChatType getForIndex(byte pId) {
      for(ChatType chattype : values()) {
         if (pId == chattype.index) {
            return chattype;
         }
      }

      return CHAT;
   }

   public boolean shouldInterrupt() {
      return this.interrupt;
   }
}