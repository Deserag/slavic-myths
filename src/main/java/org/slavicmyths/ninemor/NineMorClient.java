package org.slavicmyths.ninemor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Uses existing dark-stone texture atlas; accent crowns are tinted per attack type. */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class NineMorClient {
    private static final ResourceLocation SKIN =
        NineMorContent.id("textures/entity/morovoy_fire_zmey_albedo.png");

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NineMorContent.BOSS.get(), BossRenderer::new);
        event.registerEntityRenderer(NineMorContent.SHOT.get(), ThrownItemRenderer::new);
    }

    private static final class BossRenderer extends MobRenderer<NineMorSerpent, NineMorModel> {
        private BossRenderer(EntityRendererProvider.Context context) {
            super(context, new NineMorModel(), 2.7F);
            addLayer(new RenderLayer<NineMorSerpent, NineMorModel>(this) {
                @Override
                public void render(PoseStack pose, MultiBufferSource buffers, int packedLight,
                                   NineMorSerpent boss, float limbSwing, float limbSwingAmount,
                                   float partialTick, float age, float netHeadYaw, float headPitch) {
                    getParentModel().renderAccents(pose,
                        buffers.getBuffer(RenderType.entityCutoutNoCull(SKIN)),
                        packedLight, OverlayTexture.NO_OVERLAY);
                }
            });
        }

        @Override public ResourceLocation getTextureLocation(NineMorSerpent boss) { return SKIN; }

        @Override
        public boolean shouldRender(NineMorSerpent boss,
                                    net.minecraft.client.renderer.culling.Frustum frustum,
                                    double x, double y, double z) {
            return frustum.isVisible(boss.getBoundingBox().inflate(11D, 9D, 11D));
        }
    }

    private NineMorClient() {}
}
