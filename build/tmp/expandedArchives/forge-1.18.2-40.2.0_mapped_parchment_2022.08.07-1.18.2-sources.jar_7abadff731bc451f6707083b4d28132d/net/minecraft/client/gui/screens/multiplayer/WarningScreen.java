package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class WarningScreen extends Screen {
   private final Component titleComponent;
   private final Component content;
   private final Component check;
   private final Component narration;
   protected final Screen previous;
   @Nullable
   protected Checkbox stopShowing;
   private MultiLineLabel message = MultiLineLabel.EMPTY;

   protected WarningScreen(Component pTitleComponent, Component pContent, Component pCheck, Component pNarration, Screen pPrevious) {
      super(NarratorChatListener.NO_TITLE);
      this.titleComponent = pTitleComponent;
      this.content = pContent;
      this.check = pCheck;
      this.narration = pNarration;
      this.previous = pPrevious;
   }

   protected abstract void initButtons(int pYOffset);

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.content, this.width - 50);
      int i = (this.message.getLineCount() + 1) * 9 * 2;
      this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, this.check, false);
      this.addRenderableWidget(this.stopShowing);
      this.initButtons(i);
   }

   public Component getNarrationMessage() {
      return this.narration;
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderDirtBackground(0);
      drawString(pPoseStack, this.font, this.titleComponent, 25, 30, 16777215);
      this.message.renderLeftAligned(pPoseStack, 25, 70, 9 * 2, 16777215);
      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
   }
}