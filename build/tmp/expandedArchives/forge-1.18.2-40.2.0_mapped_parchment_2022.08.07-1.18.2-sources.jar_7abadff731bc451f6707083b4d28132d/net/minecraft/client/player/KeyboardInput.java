package net.minecraft.client.player;

import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyboardInput extends Input {
   private final Options options;
   private static final float MOVING_SLOW_FACTOR = 0.3F;

   public KeyboardInput(Options pOptions) {
      this.options = pOptions;
   }

   private static float calculateImpulse(boolean pInput, boolean pOtherInput) {
      if (pInput == pOtherInput) {
         return 0.0F;
      } else {
         return pInput ? 1.0F : -1.0F;
      }
   }

   public void tick(boolean pIsMovingSlowly) {
      this.up = this.options.keyUp.isDown();
      this.down = this.options.keyDown.isDown();
      this.left = this.options.keyLeft.isDown();
      this.right = this.options.keyRight.isDown();
      this.forwardImpulse = calculateImpulse(this.up, this.down);
      this.leftImpulse = calculateImpulse(this.left, this.right);
      this.jumping = this.options.keyJump.isDown();
      this.shiftKeyDown = this.options.keyShift.isDown();
      if (pIsMovingSlowly) {
         this.leftImpulse *= 0.3F;
         this.forwardImpulse *= 0.3F;
      }

   }
}