package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public enum GossipType {
   MAJOR_NEGATIVE("major_negative", -5, 100, 10, 10),
   MINOR_NEGATIVE("minor_negative", -1, 200, 20, 20),
   MINOR_POSITIVE("minor_positive", 1, 200, 1, 5),
   MAJOR_POSITIVE("major_positive", 5, 100, 0, 100),
   TRADING("trading", 1, 25, 2, 20);

   public static final int REPUTATION_CHANGE_PER_EVENT = 25;
   public static final int REPUTATION_CHANGE_PER_EVERLASTING_MEMORY = 20;
   public static final int REPUTATION_CHANGE_PER_TRADE = 2;
   public final String id;
   public final int weight;
   public final int max;
   public final int decayPerDay;
   public final int decayPerTransfer;
   private static final Map<String, GossipType> BY_ID = Stream.of(values()).collect(ImmutableMap.toImmutableMap((p_26290_) -> {
      return p_26290_.id;
   }, Function.identity()));

   private GossipType(String pId, int pWeight, int pMax, int pDecayPerDay, int pDecayPerTransfer) {
      this.id = pId;
      this.weight = pWeight;
      this.max = pMax;
      this.decayPerDay = pDecayPerDay;
      this.decayPerTransfer = pDecayPerTransfer;
   }

   @Nullable
   public static GossipType byId(String pId) {
      return BY_ID.get(pId);
   }
}