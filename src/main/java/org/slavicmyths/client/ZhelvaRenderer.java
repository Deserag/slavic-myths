package org.slavicmyths.client;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.ZhelvaContent;
import org.slavicmyths.marine.ZhelvaEntity;

/** White albedo is intentionally tinted by each real baked, colored cuboid. */
public final class ZhelvaRenderer extends MobRenderer<ZhelvaEntity, ZhelvaModel> {
    private static final ResourceLocation WHITE =
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    public ZhelvaRenderer(EntityRendererProvider.Context context) {
        super(context, new ZhelvaModel(), 2.5F);
    }
    @Override public ResourceLocation getTextureLocation(ZhelvaEntity entity) { return WHITE; }
    @Override public boolean shouldRender(ZhelvaEntity entity, Frustum frustum,
                                           double cameraX, double cameraY, double cameraZ) {
        return frustum.isVisible(entity.getBoundingBox().inflate(7, 3.5, 7));
    }
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ZhelvaContent.BOSS.get(), ZhelvaRenderer::new);
    }
}
