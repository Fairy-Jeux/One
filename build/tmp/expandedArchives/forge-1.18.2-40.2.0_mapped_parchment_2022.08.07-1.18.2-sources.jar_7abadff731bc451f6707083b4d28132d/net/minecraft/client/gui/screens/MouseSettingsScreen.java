package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MouseSettingsScreen extends OptionsSubScreen {
   private OptionsList list;
   private static final Option[] OPTIONS = new Option[]{Option.SENSITIVITY, Option.INVERT_MOUSE, Option.MOUSE_WHEEL_SENSITIVITY, Option.DISCRETE_MOUSE_SCROLL, Option.TOUCHSCREEN};

   public MouseSettingsScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, new TranslatableComponent("options.mouse_settings.title"));
   }

   protected void init() {
      this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
      if (InputConstants.isRawMouseInputSupported()) {
         this.list.addSmall(Stream.concat(Arrays.stream(OPTIONS), Stream.of(Option.RAW_MOUSE_INPUT)).toArray((p_96225_) -> {
            return new Option[p_96225_];
         }));
      } else {
         this.list.addSmall(OPTIONS);
      }

      this.addWidget(this.list);
      this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (p_96232_) -> {
         this.options.save();
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pPoseStack);
      this.list.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
      drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 5, 16777215);
      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
   }
}