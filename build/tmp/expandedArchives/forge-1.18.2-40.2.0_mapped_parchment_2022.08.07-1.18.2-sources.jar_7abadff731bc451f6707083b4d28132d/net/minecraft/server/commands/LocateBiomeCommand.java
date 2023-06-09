package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
   private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType((p_137850_) -> {
      return new TranslatableComponent("commands.locatebiome.notFound", p_137850_);
   });
   private static final int MAX_SEARCH_RADIUS = 6400;
   private static final int SEARCH_STEP = 8;

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("locatebiome").requires((p_201816_) -> {
         return p_201816_.hasPermission(2);
      }).then(Commands.argument("biome", ResourceOrTagLocationArgument.resourceOrTag(Registry.BIOME_REGISTRY)).executes((p_201814_) -> {
         return locateBiome(p_201814_.getSource(), ResourceOrTagLocationArgument.getBiome(p_201814_, "biome"));
      })));
   }

   private static int locateBiome(CommandSourceStack pSource, ResourceOrTagLocationArgument.Result<Biome> pBiome) throws CommandSyntaxException {
      BlockPos blockpos = new BlockPos(pSource.getPosition());
      Pair<BlockPos, Holder<Biome>> pair = pSource.getLevel().findNearestBiome(pBiome, blockpos, 6400, 8);
      if (pair == null) {
         throw ERROR_BIOME_NOT_FOUND.create(pBiome.asPrintable());
      } else {
         return LocateCommand.showLocateResult(pSource, pBiome, blockpos, pair, "commands.locatebiome.success");
      }
   }
}