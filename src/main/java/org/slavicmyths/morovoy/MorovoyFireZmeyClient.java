package org.slavicmyths.morovoy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class MorovoyFireZmeyClient {
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MorovoyFireZmeyContent.BOSS.get(), BossRenderer::new);
        event.registerEntityRenderer(MorovoyFireZmeyContent.BOLT.get(), EffectsRenderer::new);
        event.registerEntityRenderer(MorovoyFireZmeyContent.FIELD.get(), EffectsRenderer::new);
    }

    public static final class BossRenderer extends MobRenderer<MorovoyFireZmey, MorovoyFireZmeyModel> {
        private static final ResourceLocation ALBEDO = MorovoyFireZmeyContent.id("textures/entity/morovoy_fire_zmey_albedo.png");
        private static final ResourceLocation MOR_EMISSIVE = MorovoyFireZmeyContent.id("textures/entity/morovoy_fire_zmey_mor_emissive.png");
        private static final ResourceLocation FIRE_EMISSIVE = MorovoyFireZmeyContent.id("textures/entity/morovoy_fire_zmey_fire_emissive.png");

        public BossRenderer(EntityRendererProvider.Context context) {
            super(context, new MorovoyFireZmeyGeoModel(), 1.6F);
            addLayer(new RenderLayer<MorovoyFireZmey, MorovoyFireZmeyModel>(this) {
                @Override
                public void render(PoseStack pose, MultiBufferSource buffers, int packedLight, MorovoyFireZmey boss, float limbSwing, float limbSwingAmount, float partialTick, float age, float netHeadYaw, float headPitch) {
                    MorovoyFireZmeyModel model = getParentModel();
                    // Isolated left-side atlas: plague-green pixels never participate in the right-side pass.
                    model.renderElementalSide(pose, buffers.getBuffer(RenderType.eyes(MOR_EMISSIVE)), 15728880, OverlayTexture.NO_OVERLAY, false);
                    // Isolated right-side atlas: deep purple -> violet -> white, without the green atlas.
                    model.renderElementalSide(pose, buffers.getBuffer(RenderType.eyes(FIRE_EMISSIVE)), 15728880, OverlayTexture.NO_OVERLAY, true);
                    // Head-local three-tone fire geometry, following the right neck's actual three-bone pose.
                    model.renderFireFlame(pose, buffers.getBuffer(RenderType.lightning()));
                }
            });
        }

        @Override
        public ResourceLocation getTextureLocation(MorovoyFireZmey boss) {
            return ALBEDO;
        }

        @Override
        public boolean shouldRender(MorovoyFireZmey boss, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
            return frustum.isVisible(boss.getBoundingBox().inflate(10.0D, 8.0D, 10.0D));
        }
    }

    public static final class EffectsRenderer<T extends Entity> extends EntityRenderer<T> {
        public EffectsRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(T entity) {
            return BossRenderer.ALBEDO;
        }

        @Override
        public void render(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
            if (entity instanceof MorovoyZmeyBolt bolt) {
                var consumer = buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
                int color = bolt.element() == 0 ? 0xFF32EA47 : 0xFFB43BFA;
                org.slavicmyths.client.GuardianClient.cube(pose, consumer, -0.24, 0.0, -0.24, 0.48, 0.48, 0.48, color);
            }
            super.render(entity, yaw, partialTick, pose, buffers, light);
        }
    }

    private MorovoyFireZmeyClient() {}
}
