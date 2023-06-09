package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface ProfilerFiller {
   String ROOT = "root";

   void startTick();

   void endTick();

   /**
    * Start section
    */
   void push(String pName);

   void push(Supplier<String> pNameSupplier);

   /**
    * End section
    */
   void pop();

   void popPush(String pName);

   void popPush(Supplier<String> pNameSupplier);

   void markForCharting(MetricCategory pCategory);

   default void incrementCounter(String pEntryId) {
      this.incrementCounter(pEntryId, 1);
   }

   void incrementCounter(String p_185258_, int p_185259_);

   default void incrementCounter(Supplier<String> pEntryIdSupplier) {
      this.incrementCounter(pEntryIdSupplier, 1);
   }

   void incrementCounter(Supplier<String> p_185260_, int p_185261_);

   static ProfilerFiller tee(final ProfilerFiller p_18579_, final ProfilerFiller p_18580_) {
      if (p_18579_ == InactiveProfiler.INSTANCE) {
         return p_18580_;
      } else {
         return p_18580_ == InactiveProfiler.INSTANCE ? p_18579_ : new ProfilerFiller() {
            public void startTick() {
               p_18579_.startTick();
               p_18580_.startTick();
            }

            public void endTick() {
               p_18579_.endTick();
               p_18580_.endTick();
            }

            /**
             * Start section
             */
            public void push(String p_18594_) {
               p_18579_.push(p_18594_);
               p_18580_.push(p_18594_);
            }

            public void push(Supplier<String> p_18596_) {
               p_18579_.push(p_18596_);
               p_18580_.push(p_18596_);
            }

            public void markForCharting(MetricCategory p_145961_) {
               p_18579_.markForCharting(p_145961_);
               p_18580_.markForCharting(p_145961_);
            }

            /**
             * End section
             */
            public void pop() {
               p_18579_.pop();
               p_18580_.pop();
            }

            public void popPush(String p_18599_) {
               p_18579_.popPush(p_18599_);
               p_18580_.popPush(p_18599_);
            }

            public void popPush(Supplier<String> p_18601_) {
               p_18579_.popPush(p_18601_);
               p_18580_.popPush(p_18601_);
            }

            public void incrementCounter(String p_185263_, int p_185264_) {
               p_18579_.incrementCounter(p_185263_, p_185264_);
               p_18580_.incrementCounter(p_185263_, p_185264_);
            }

            public void incrementCounter(Supplier<String> p_185266_, int p_185267_) {
               p_18579_.incrementCounter(p_185266_, p_185267_);
               p_18580_.incrementCounter(p_185266_, p_185267_);
            }
         };
      }
   }
}