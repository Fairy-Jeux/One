package net.minecraft.client.renderer.entity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface EntityRendererProvider<T extends Entity> {
   EntityRenderer<T> create(EntityRendererProvider.Context p_174010_);

   @OnlyIn(Dist.CLIENT)
   public static class Context {
      private final EntityRenderDispatcher entityRenderDispatcher;
      private final ItemRenderer itemRenderer;
      private final ResourceManager resourceManager;
      private final EntityModelSet modelSet;
      private final Font font;

      public Context(EntityRenderDispatcher pEntityRendererDispatcher, ItemRenderer pItemRenderer, ResourceManager pResourceManager, EntityModelSet pModelSet, Font pFont) {
         this.entityRenderDispatcher = pEntityRendererDispatcher;
         this.itemRenderer = pItemRenderer;
         this.resourceManager = pResourceManager;
         this.modelSet = pModelSet;
         this.font = pFont;
      }

      public EntityRenderDispatcher getEntityRenderDispatcher() {
         return this.entityRenderDispatcher;
      }

      public ItemRenderer getItemRenderer() {
         return this.itemRenderer;
      }

      public ResourceManager getResourceManager() {
         return this.resourceManager;
      }

      public EntityModelSet getModelSet() {
         return this.modelSet;
      }

      public ModelPart bakeLayer(ModelLayerLocation pLayer) {
         return this.modelSet.bakeLayer(pLayer);
      }

      public Font getFont() {
         return this.font;
      }
   }
}