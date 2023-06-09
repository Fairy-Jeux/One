package net.minecraft.world.entity.ai.village.poi;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class PoiType extends net.minecraftforge.registries.ForgeRegistryEntry<PoiType> {
   private static final Supplier<Set<PoiType>> ALL_JOB_POI_TYPES = Suppliers.memoize(() -> {
      return Registry.VILLAGER_PROFESSION.stream().map(VillagerProfession::getJobPoiType).collect(Collectors.toSet());
   });
   public static final Predicate<PoiType> ALL_JOBS = (p_27399_) -> {
      return ALL_JOB_POI_TYPES.get().contains(p_27399_);
   };
   public static final Predicate<PoiType> ALL = (p_27394_) -> {
      return true;
   };
   private static final Set<BlockState> BEDS = ImmutableList.of(Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED).stream().flatMap((p_27389_) -> {
      return p_27389_.getStateDefinition().getPossibleStates().stream();
   }).filter((p_27396_) -> {
      return p_27396_.getValue(BedBlock.PART) == BedPart.HEAD;
   }).collect(ImmutableSet.toImmutableSet());
   private static final Set<BlockState> CAULDRONS = ImmutableList.of(Blocks.CAULDRON, Blocks.LAVA_CAULDRON, Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON).stream().flatMap((p_148697_) -> {
      return p_148697_.getStateDefinition().getPossibleStates().stream();
   }).collect(ImmutableSet.toImmutableSet());
   private static final Map<BlockState, PoiType> TYPE_BY_STATE = net.minecraftforge.registries.GameData.getBlockStatePointOfInterestTypeMap();
   public static final PoiType UNEMPLOYED = register("unemployed", ImmutableSet.of(), 1, ALL_JOBS, 1);
   public static final PoiType ARMORER = register("armorer", getBlockStates(Blocks.BLAST_FURNACE), 1, 1);
   public static final PoiType BUTCHER = register("butcher", getBlockStates(Blocks.SMOKER), 1, 1);
   public static final PoiType CARTOGRAPHER = register("cartographer", getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, 1);
   public static final PoiType CLERIC = register("cleric", getBlockStates(Blocks.BREWING_STAND), 1, 1);
   public static final PoiType FARMER = register("farmer", getBlockStates(Blocks.COMPOSTER), 1, 1);
   public static final PoiType FISHERMAN = register("fisherman", getBlockStates(Blocks.BARREL), 1, 1);
   public static final PoiType FLETCHER = register("fletcher", getBlockStates(Blocks.FLETCHING_TABLE), 1, 1);
   public static final PoiType LEATHERWORKER = register("leatherworker", CAULDRONS, 1, 1);
   public static final PoiType LIBRARIAN = register("librarian", getBlockStates(Blocks.LECTERN), 1, 1);
   public static final PoiType MASON = register("mason", getBlockStates(Blocks.STONECUTTER), 1, 1);
   public static final PoiType NITWIT = register("nitwit", ImmutableSet.of(), 1, 1);
   public static final PoiType SHEPHERD = register("shepherd", getBlockStates(Blocks.LOOM), 1, 1);
   public static final PoiType TOOLSMITH = register("toolsmith", getBlockStates(Blocks.SMITHING_TABLE), 1, 1);
   public static final PoiType WEAPONSMITH = register("weaponsmith", getBlockStates(Blocks.GRINDSTONE), 1, 1);
   public static final PoiType HOME = register("home", BEDS, 1, 1);
   public static final PoiType MEETING = register("meeting", getBlockStates(Blocks.BELL), 32, 6);
   public static final PoiType BEEHIVE = register("beehive", getBlockStates(Blocks.BEEHIVE), 0, 1);
   public static final PoiType BEE_NEST = register("bee_nest", getBlockStates(Blocks.BEE_NEST), 0, 1);
   public static final PoiType NETHER_PORTAL = register("nether_portal", getBlockStates(Blocks.NETHER_PORTAL), 0, 1);
   public static final PoiType LODESTONE = register("lodestone", getBlockStates(Blocks.LODESTONE), 0, 1);
   public static final PoiType LIGHTNING_ROD = register("lightning_rod", getBlockStates(Blocks.LIGHTNING_ROD), 0, 1);
   protected static final Set<BlockState> ALL_STATES = new ObjectOpenHashSet<>(TYPE_BY_STATE.keySet());
   private final String name;
   private final Set<BlockState> matchingStates;
   private final int maxTickets;
   private final Predicate<PoiType> predicate;
   private final int validRange;

   public static Set<BlockState> getBlockStates(Block pBlock) {
      return ImmutableSet.copyOf(pBlock.getStateDefinition().getPossibleStates());
   }

   public PoiType(String pName, Set<BlockState> pMatchingStates, int pMaxTickets, Predicate<PoiType> pPredicate, int pValidRange) {
      this.name = pName;
      this.matchingStates = ImmutableSet.copyOf(pMatchingStates);
      this.maxTickets = pMaxTickets;
      this.predicate = pPredicate;
      this.validRange = pValidRange;
   }

   public PoiType(String pName, Set<BlockState> pMatchingStates, int pMaxTickets, int pValidRange) {
      this.name = pName;
      this.matchingStates = ImmutableSet.copyOf(pMatchingStates);
      this.maxTickets = pMaxTickets;
      this.predicate = (p_148695_) -> {
         return p_148695_ == this;
      };
      this.validRange = pValidRange;
   }

   public String getName() {
      return this.name;
   }

   public int getMaxTickets() {
      return this.maxTickets;
   }

   public Predicate<PoiType> getPredicate() {
      return this.predicate;
   }

   public boolean is(BlockState pState) {
      return this.matchingStates.contains(pState);
   }

   public int getValidRange() {
      return this.validRange;
   }

   public String toString() {
      return this.name;
   }

   private static PoiType register(String pKey, Set<BlockState> pBlockStates, int pMaxFreeTickets, int pValidRange) {
      return registerBlockStates(Registry.register(Registry.POINT_OF_INTEREST_TYPE, new ResourceLocation(pKey), new PoiType(pKey, pBlockStates, pMaxFreeTickets, pValidRange)));
   }

   private static PoiType register(String pKey, Set<BlockState> pBlockStates, int pMaxFreeTickets, Predicate<PoiType> pPredicate, int pValidRange) {
      return registerBlockStates(Registry.register(Registry.POINT_OF_INTEREST_TYPE, new ResourceLocation(pKey), new PoiType(pKey, pBlockStates, pMaxFreeTickets, pPredicate, pValidRange)));
   }

   private static PoiType registerBlockStates(PoiType pPoiType) {
      return pPoiType;
   }

   public static Optional<PoiType> forState(BlockState pState) {
      return Optional.ofNullable(TYPE_BY_STATE.get(pState));
   }
   
   public ImmutableSet<BlockState> getBlockStates() {
      return ImmutableSet.copyOf(this.matchingStates);
   }
}
