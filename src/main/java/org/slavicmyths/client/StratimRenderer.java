package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.*;
public final class StratimRenderer extends MobRenderer<StratimEntity,StratimModel>{
    private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    public StratimRenderer(EntityRendererProvider.Context ctx){super(ctx,new StratimModel(),2.2F);}
    @Override public ResourceLocation getTextureLocation(StratimEntity bird){return WHITE;}
    @Override public boolean shouldRender(StratimEntity bird,Frustum frustum,double cx,double cy,double cz){
        return frustum.isVisible(bird.getBoundingBox().inflate(20,10,20));
    }
    public static void register(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(StratimContent.BOSS.get(),StratimRenderer::new);
    }
}
