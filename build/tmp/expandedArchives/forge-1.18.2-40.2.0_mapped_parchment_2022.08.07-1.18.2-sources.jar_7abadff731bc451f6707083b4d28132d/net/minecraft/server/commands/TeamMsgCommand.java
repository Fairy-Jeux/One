package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
   private static final Style SUGGEST_STYLE = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.type.team.hover"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
   private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(new TranslatableComponent("commands.teammsg.failed.noteam"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = pDispatcher.register(Commands.literal("teammsg").then(Commands.argument("message", MessageArgument.message()).executes((p_139002_) -> {
         return sendMessage(p_139002_.getSource(), MessageArgument.getMessage(p_139002_, "message"));
      })));
      pDispatcher.register(Commands.literal("tm").redirect(literalcommandnode));
   }

   private static int sendMessage(CommandSourceStack pSource, Component pMessage) throws CommandSyntaxException {
      Entity entity = pSource.getEntityOrException();
      PlayerTeam playerteam = (PlayerTeam)entity.getTeam();
      if (playerteam == null) {
         throw ERROR_NOT_ON_TEAM.create();
      } else {
         Component component = playerteam.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
         List<ServerPlayer> list = pSource.getServer().getPlayerList().getPlayers();

         for(ServerPlayer serverplayer : list) {
            if (serverplayer == entity) {
               serverplayer.sendMessage(new TranslatableComponent("chat.type.team.sent", component, pSource.getDisplayName(), pMessage), entity.getUUID());
            } else if (serverplayer.getTeam() == playerteam) {
               serverplayer.sendMessage(new TranslatableComponent("chat.type.team.text", component, pSource.getDisplayName(), pMessage), entity.getUUID());
            }
         }

         return list.size();
      }
   }
}