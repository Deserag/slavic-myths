package org.slavicmyths.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.DomovoyEntity;

public final class DomovoyRenderer extends MobRenderer<DomovoyEntity, DomovoyModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("slavicmyths", "textures/entity/domovoy.png");
    public DomovoyRenderer(EntityRendererManager manager) { super(manager, new DomovoyModel(), 0.3F); }
    @Override public ResourceLocation getTextureLocation(DomovoyEntity entity) { return TEXTURE; }
    @Override protected void scale(DomovoyEntity entity, MatrixStack pose, float partialTick) { pose.scale(0.8F,0.8F,0.8F); }
}
