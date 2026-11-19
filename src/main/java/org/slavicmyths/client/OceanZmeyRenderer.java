package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.OceanZmeyContent;
import org.slavicmyths.marine.OceanZmeyEntity;

public final class OceanZmeyRenderer extends MobRenderer<OceanZmeyEntity,OceanZmeyModel> {
    private static final ResourceLocation WHITE=
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    public OceanZmeyRenderer(EntityRendererProvider.Context ctx){
        super(ctx,new OceanZmeyModel(),2.1F);
    }
    @Override public ResourceLocation getTextureLocation(OceanZmeyEntity mob){return WHITE;}
    @Override public boolean shouldRender(OceanZmeyEntity mob,Frustum frustum,
        double cameraX,double cameraY,double cameraZ){
        return frustum.isVisible(mob.getBoundingBox().inflate(34,9,34));
    }
    public static void register(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(OceanZmeyContent.BOSS.get(),OceanZmeyRenderer::new);
    }
}
