package com.mojang.realmsclient.gui.task;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RepeatableTask implements Runnable {
   private final BooleanSupplier isActive;
   private final RestartDelayCalculator restartDelayCalculator;
   private final Duration interval;
   private final Runnable runnable;

   private RepeatableTask(Runnable pRunnable, Duration pInterval, BooleanSupplier pIsActive, RestartDelayCalculator pRestartDelayCalculator) {
      this.runnable = pRunnable;
      this.interval = pInterval;
      this.isActive = pIsActive;
      this.restartDelayCalculator = pRestartDelayCalculator;
   }

   public void run() {
      if (this.isActive.getAsBoolean()) {
         this.restartDelayCalculator.markExecutionStart();
         this.runnable.run();
      }

   }

   public ScheduledFuture<?> schedule(ScheduledExecutorService p_167586_) {
      return p_167586_.scheduleAtFixedRate(this, this.restartDelayCalculator.getNextDelayMs(), this.interval.toMillis(), TimeUnit.MILLISECONDS);
   }

   public static RepeatableTask withRestartDelayAccountingForInterval(Runnable pRunnable, Duration pInterval, BooleanSupplier pIsActive) {
      return new RepeatableTask(pRunnable, pInterval, pIsActive, new IntervalBasedStartupDelay(pInterval));
   }

   public static RepeatableTask withImmediateRestart(Runnable pRunnable, Duration pInterval, BooleanSupplier pIsActive) {
      return new RepeatableTask(pRunnable, pInterval, pIsActive, new NoStartupDelay());
   }
}