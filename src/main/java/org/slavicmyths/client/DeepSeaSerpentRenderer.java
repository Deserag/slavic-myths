package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.DeepSeaSerpentContent;
import org.slavicmyths.marine.DeepSeaSerpentEntity;

/** Colored voxel cuboids; the entire 27-block body is actually linked and bends. */
public final class DeepSeaSerpentRenderer extends MobRenderer<DeepSeaSerpentEntity,DeepSeaSerpentModel> {
    private static final ResourceLocation WHITE=
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    public DeepSeaSerpentRenderer(EntityRendererProvider.Context ctx){
        super(ctx,new DeepSeaSerpentModel(),1.55F);
    }
    @Override public ResourceLocation getTextureLocation(DeepSeaSerpentEntity mob){return WHITE;}
    @Override public boolean shouldRender(DeepSeaSerpentEntity mob,Frustum frustum,
        double cameraX,double cameraY,double cameraZ) {
        // 27-block body extends far beyond the compact collision root.
        return frustum.isVisible(mob.getBoundingBox().inflate(29,5,29));
    }
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DeepSeaSerpentContent.BOSS.get(),DeepSeaSerpentRenderer::new);
    }
}
