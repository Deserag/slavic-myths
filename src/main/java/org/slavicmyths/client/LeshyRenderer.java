package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.LeshyEntity;

public final class LeshyRenderer extends MobRenderer<LeshyEntity, LeshyModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/entity/leshy.png");
    public LeshyRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager) { super(manager, new LeshyModel(), 0.5F); }
    @Override public ResourceLocation getTextureLocation(LeshyEntity entity) { return TEXTURE; }
}
