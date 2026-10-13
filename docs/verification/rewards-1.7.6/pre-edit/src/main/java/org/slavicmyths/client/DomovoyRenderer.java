package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.DomovoyEntity;

public final class DomovoyRenderer extends MobRenderer<DomovoyEntity, DomovoyModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/entity/domovoy.png");
    public DomovoyRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager) { super(manager, new DomovoyModel(), 0.3F); }
    @Override public ResourceLocation getTextureLocation(DomovoyEntity entity) { return TEXTURE; }
    @Override protected void scale(DomovoyEntity entity, PoseStack pose, float partialTick) { pose.scale(0.8F,0.8F,0.8F); }
}
