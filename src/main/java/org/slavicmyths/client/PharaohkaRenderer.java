package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.PharaohkaContent;
import org.slavicmyths.marine.PharaohkaEntity;

public final class PharaohkaRenderer extends MobRenderer<PharaohkaEntity,PharaohkaModel> {
    private static final ResourceLocation WHITE =
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    public PharaohkaRenderer(EntityRendererProvider.Context context,boolean leader) {
        super(context,new PharaohkaModel(leader),leader?.65F:.35F);
    }
    @Override protected void scale(PharaohkaEntity entity,com.mojang.blaze3d.vertex.PoseStack pose,float partialTick) {
        float scale=entity.leader()?.70F:.52F;
        pose.scale(scale,scale,scale);
    }
    @Override public ResourceLocation getTextureLocation(PharaohkaEntity entity) {return WHITE;}
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PharaohkaContent.PHARAOHKA.get(),ctx->new PharaohkaRenderer(ctx,false));
        event.registerEntityRenderer(PharaohkaContent.LEADER.get(),ctx->new PharaohkaRenderer(ctx,true));
    }
}