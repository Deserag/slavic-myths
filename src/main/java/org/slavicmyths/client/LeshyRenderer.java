package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.LeshyEntity;

public final class LeshyRenderer extends MobRenderer<LeshyEntity, LeshyModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("slavicmyths", "textures/entity/leshy.png");
    public LeshyRenderer(EntityRendererManager manager) { super(manager, new LeshyModel(), 0.5F); }
    @Override public ResourceLocation getTextureLocation(LeshyEntity entity) { return TEXTURE; }
}
