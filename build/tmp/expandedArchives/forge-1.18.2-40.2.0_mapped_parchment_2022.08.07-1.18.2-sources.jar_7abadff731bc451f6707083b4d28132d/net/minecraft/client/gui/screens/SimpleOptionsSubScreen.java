package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SimpleOptionsSubScreen extends OptionsSubScreen {
   private final Option[] smallOptions;
   @Nullable
   private AbstractWidget narratorButton;
   private OptionsList list;

   public SimpleOptionsSubScreen(Screen pLastScreen, Options pOptions, Component pTitle, Option[] pSmallOptions) {
      super(pLastScreen, pOptions, pTitle);
      this.smallOptions = pSmallOptions;
   }

   protected void init() {
      this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
      this.list.addSmall(this.smallOptions);
      this.addWidget(this.list);
      this.createFooter();
      this.narratorButton = this.list.findOption(Option.NARRATOR);
      if (this.narratorButton != null) {
         this.narratorButton.active = NarratorChatListener.INSTANCE.isActive();
      }

   }

   protected void createFooter() {
      this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (p_96680_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pPoseStack);
      this.list.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
      drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 20, 16777215);
      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
      List<FormattedCharSequence> list = tooltipAt(this.list, pMouseX, pMouseY);
      this.renderTooltip(pPoseStack, list, pMouseX, pMouseY);
   }

   public void updateNarratorButton() {
      if (this.narratorButton instanceof CycleButton) {
         ((CycleButton)this.narratorButton).setValue(this.options.narratorStatus);
      }

   }
}