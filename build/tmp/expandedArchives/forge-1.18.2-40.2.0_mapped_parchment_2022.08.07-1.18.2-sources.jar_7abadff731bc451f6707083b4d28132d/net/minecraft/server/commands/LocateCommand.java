package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public class LocateCommand {
   private static final DynamicCommandExceptionType ERROR_FAILED = new DynamicCommandExceptionType((p_201831_) -> {
      return new TranslatableComponent("commands.locate.failed", p_201831_);
   });
   private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType((p_207534_) -> {
      return new TranslatableComponent("commands.locate.invalid", p_207534_);
   });

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("locate").requires((p_207513_) -> {
         return p_207513_.hasPermission(2);
      }).then(Commands.argument("structure", ResourceOrTagLocationArgument.resourceOrTag(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY)).executes((p_207508_) -> {
         return locate(p_207508_.getSource(), ResourceOrTagLocationArgument.getStructureFeature(p_207508_, "structure"));
      })));
   }

   private static int locate(CommandSourceStack pSource, ResourceOrTagLocationArgument.Result<ConfiguredStructureFeature<?, ?>> pStructure) throws CommandSyntaxException {
      Registry<ConfiguredStructureFeature<?, ?>> registry = pSource.getLevel().registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
      HolderSet<ConfiguredStructureFeature<?, ?>> holderset = pStructure.unwrap().map((p_207532_) -> {
         return registry.getHolder(p_207532_).map((p_207529_) -> {
            return HolderSet.direct(p_207529_);
         });
      }, registry::getTag).orElseThrow(() -> {
         return ERROR_INVALID.create(pStructure.asPrintable());
      });
      BlockPos blockpos = new BlockPos(pSource.getPosition());
      ServerLevel serverlevel = pSource.getLevel();
      Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair = serverlevel.getChunkSource().getGenerator().findNearestMapFeature(serverlevel, holderset, blockpos, 100, false);
      if (pair == null) {
         throw ERROR_FAILED.create(pStructure.asPrintable());
      } else {
         return showLocateResult(pSource, pStructure, blockpos, pair, "commands.locate.success");
      }
   }

   public static int showLocateResult(CommandSourceStack pSource, ResourceOrTagLocationArgument.Result<?> pStructure, BlockPos pPos, Pair<BlockPos, ? extends Holder<?>> p_207521_, String p_207522_) {
      BlockPos blockpos = p_207521_.getFirst();
      String s = pStructure.unwrap().map((p_207538_) -> {
         return p_207538_.location().toString();
      }, (p_207511_) -> {
         return "#" + p_207511_.location() + " (" + (String)p_207521_.getSecond().unwrapKey().map((p_207536_) -> {
            return p_207536_.location().toString();
         }).orElse("[unregistered]") + ")";
      });
      int i = Mth.floor(dist(pPos.getX(), pPos.getZ(), blockpos.getX(), blockpos.getZ()));
      Component component = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", blockpos.getX(), "~", blockpos.getZ())).withStyle((p_207527_) -> {
         return p_207527_.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockpos.getX() + " ~ " + blockpos.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip")));
      });
      pSource.sendSuccess(new TranslatableComponent(p_207522_, s, component, i), false);
      return i;
   }

   private static float dist(int pX1, int pZ1, int pX2, int pZ2) {
      int i = pX2 - pX1;
      int j = pZ2 - pZ1;
      return Mth.sqrt((float)(i * i + j * j));
   }
}