package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.StratimBirdEntity;
import org.slavicmyths.marine.StratimBirdContent;

public final class StratimBirdRenderer extends MobRenderer<StratimBirdEntity,StratimBirdModel>{
    private static final ResourceLocation TEXTURE=
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    public StratimBirdRenderer(EntityRendererProvider.Context ctx){
        super(ctx,new StratimBirdModel(),2.3F);
    }
    @Override public ResourceLocation getTextureLocation(StratimBirdEntity e){return TEXTURE;}
    @Override public boolean shouldRender(StratimBirdEntity e,Frustum f,double x,double y,double z){
        return f.isVisible(e.getBoundingBox().inflate(19,10,14));
    }
    public static void register(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(StratimBirdContent.BOSS.get(),StratimBirdRenderer::new);
    }
}
