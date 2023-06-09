package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class GameTestServer extends MinecraftServer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int PROGRESS_REPORT_INTERVAL = 20;
   private final List<GameTestBatch> testBatches;
   private final BlockPos spawnPos;
   private static final GameRules TEST_GAME_RULES = Util.make(new GameRules(), (p_177615_) -> {
      p_177615_.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, (MinecraftServer)null);
      p_177615_.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, (MinecraftServer)null);
   });
   private static final LevelSettings TEST_SETTINGS = new LevelSettings("Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, TEST_GAME_RULES, DataPackConfig.DEFAULT);
   @Nullable
   private MultipleTestTracker testTracker;

   public static GameTestServer create(Thread pServerThread, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, Collection<GameTestBatch> pTestBatches, BlockPos pSpawnPos) {
      if (pTestBatches.isEmpty()) {
         throw new IllegalArgumentException("No test batches were given!");
      } else {
         WorldStem.InitConfig worldstem$initconfig = new WorldStem.InitConfig(pPackRepository, Commands.CommandSelection.DEDICATED, 4, false);

         try {
            WorldStem worldstem = WorldStem.load(worldstem$initconfig, () -> {
               return DataPackConfig.DEFAULT;
            }, (p_206604_, p_206605_) -> {
               RegistryAccess.Frozen registryaccess$frozen = RegistryAccess.BUILTIN.get();
               Registry<Biome> registry = registryaccess$frozen.registryOrThrow(Registry.BIOME_REGISTRY);
               Registry<StructureSet> registry1 = registryaccess$frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
               Registry<DimensionType> registry2 = registryaccess$frozen.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
               WorldData worlddata = new PrimaryLevelData(TEST_SETTINGS, new WorldGenSettings(0L, false, false, WorldGenSettings.withOverworld(registry2, DimensionType.defaultDimensions(registryaccess$frozen, 0L), new FlatLevelSource(registry1, FlatLevelGeneratorSettings.getDefault(registry, registry1)))), Lifecycle.stable());
               return Pair.of(worlddata, registryaccess$frozen);
            }, Util.backgroundExecutor(), Runnable::run).get();
            worldstem.updateGlobals();
            return new GameTestServer(pServerThread, pStorageSource, pPackRepository, worldstem, pTestBatches, pSpawnPos);
         } catch (Exception exception) {
            LOGGER.warn("Failed to load vanilla datapack, bit oops", (Throwable)exception);
            System.exit(-1);
            throw new IllegalStateException();
         }
      }
   }

   public GameTestServer(Thread pServerThreaed, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, WorldStem pWorldStem, Collection<GameTestBatch> pTestBatchs, BlockPos pSpawnPos) {
      super(pServerThreaed, pStorageSource, pPackRepository, pWorldStem, Proxy.NO_PROXY, DataFixers.getDataFixer(), (MinecraftSessionService)null, (GameProfileRepository)null, (GameProfileCache)null, LoggerChunkProgressListener::new);
      this.testBatches = Lists.newArrayList(pTestBatchs);
      this.spawnPos = pSpawnPos;
   }

   /**
    * Initialises the server and starts it.
    */
   public boolean initServer() {
      this.setPlayerList(new PlayerList(this, this.registryAccess(), this.playerDataStorage, 1) {
      });
      if (!net.minecraftforge.server.ServerLifecycleHooks.handleServerAboutToStart(this)) return false;
      this.loadLevel();
      ServerLevel serverlevel = this.overworld();
      serverlevel.setDefaultSpawnPos(this.spawnPos, 0.0F);
      int i = 20000000;
      serverlevel.setWeatherParameters(20000000, 20000000, false, false);
      return net.minecraftforge.server.ServerLifecycleHooks.handleServerStarting(this);
   }

   /**
    * Main function called by run() every loop.
    */
   public void tickServer(BooleanSupplier pHasTimeLeft) {
      super.tickServer(pHasTimeLeft);
      ServerLevel serverlevel = this.overworld();
      if (!this.haveTestsStarted()) {
         this.startTests(serverlevel);
      }

      if (serverlevel.getGameTime() % 20L == 0L) {
         LOGGER.info(this.testTracker.getProgressBar());
      }

      if (this.testTracker.isDone()) {
         this.halt(false);
         LOGGER.info(this.testTracker.getProgressBar());
         GlobalTestReporter.finish();
         LOGGER.info("========= {} GAME TESTS COMPLETE ======================", (int)this.testTracker.getTotalCount());
         if (this.testTracker.hasFailedRequired()) {
            LOGGER.info("{} required tests failed :(", (int)this.testTracker.getFailedRequiredCount());
            this.testTracker.getFailedRequired().forEach((p_206615_) -> {
               LOGGER.info("   - {}", (Object)p_206615_.getTestName());
            });
         } else {
            LOGGER.info("All {} required tests passed :)", (int)this.testTracker.getTotalCount());
         }

         if (this.testTracker.hasFailedOptional()) {
            LOGGER.info("{} optional tests failed", (int)this.testTracker.getFailedOptionalCount());
            this.testTracker.getFailedOptional().forEach((p_206613_) -> {
               LOGGER.info("   - {}", (Object)p_206613_.getTestName());
            });
         }

         LOGGER.info("====================================================");
      }

   }

   public SystemReport fillServerSystemReport(SystemReport pReport) {
      pReport.setDetail("Type", "Game test server");
      return pReport;
   }

   /**
    * Directly calls System.exit(0), instantly killing the program.
    */
   public void onServerExit() {
      super.onServerExit();
      System.exit(this.testTracker.getFailedRequiredCount());
   }

   /**
    * Called on exit from the main run() loop.
    */
   public void onServerCrash(CrashReport pReport) {
      System.exit(1);
   }

   private void startTests(ServerLevel pServerLevel) {
      Collection<GameTestInfo> collection = GameTestRunner.runTestBatches(this.testBatches, new BlockPos(0, -60, 0), Rotation.NONE, pServerLevel, GameTestTicker.SINGLETON, 8);
      this.testTracker = new MultipleTestTracker(collection);
      LOGGER.info("{} tests are now running!", (int)this.testTracker.getTotalCount());
   }

   private boolean haveTestsStarted() {
      return this.testTracker != null;
   }

   /**
    * Defaults to false.
    */
   public boolean isHardcore() {
      return false;
   }

   public int getOperatorUserPermissionLevel() {
      return 0;
   }

   public int getFunctionCompilationLevel() {
      return 4;
   }

   public boolean shouldRconBroadcast() {
      return false;
   }

   public boolean isDedicatedServer() {
      return false;
   }

   public int getRateLimitPacketsPerSecond() {
      return 0;
   }

   /**
    * Get if native transport should be used. Native transport means linux server performance improvements and optimized
    * packet sending/receiving on linux
    */
   public boolean isEpollEnabled() {
      return false;
   }

   /**
    * Return whether command blocks are enabled.
    */
   public boolean isCommandBlockEnabled() {
      return true;
   }

   /**
    * Returns true if this integrated server is open to LAN
    */
   public boolean isPublished() {
      return false;
   }

   public boolean shouldInformAdmins() {
      return false;
   }

   public boolean isSingleplayerOwner(GameProfile pProfile) {
      return false;
   }
}
