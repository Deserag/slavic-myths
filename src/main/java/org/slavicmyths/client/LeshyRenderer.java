package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.LeshyEntity;

public final class LeshyRenderer extends MobRenderer<LeshyEntity, LeshyModel> {
 private final LeshyModel normal=new LeshyModel(),blighted=new LeshyModel(true);
 @Override public void render(LeshyEntity e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack p,net.minecraft.client.renderer.MultiBufferSource out,int light){model=MorVisual.shown(e)?blighted:normal;super.render(e,yaw,partial,p,out,light);}

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/entity/leshy.png");
    public LeshyRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager) { super(manager, new LeshyModel(), 0.5F); }
    @Override public ResourceLocation getTextureLocation(LeshyEntity entity) {if(MorVisual.shown(entity))return MorVisual.texture(entity,"leshy"); return TEXTURE; }
}
