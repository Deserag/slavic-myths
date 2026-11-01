package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.morovoy.MorovoyRaven;
import org.slavicmyths.morovoy.MorovoyRavenContent;

/**
 * Base albedo and an alpha-masked UV emissive PNG render the SAME ModelPart rig.
 * No random glowing shapes, ModelPart green cubes, procedural letters or shader pixels.
 */
public final class MorovoyRavenRenderer extends MobRenderer<MorovoyRaven, MorovoyRavenModel> {
    private static final ResourceLocation ALBEDO = MorovoyRavenContent.id("textures/entity/morovoy_raven.png");
    private static final ResourceLocation EMISSIVE = MorovoyRavenContent.id("textures/entity/morovoy_raven_emissive.png");

    public MorovoyRavenRenderer(EntityRendererProvider.Context context) {
        super(context, new MorovoyRavenModel(), 1.6F);
        addLayer(new RenderLayer<MorovoyRaven, MorovoyRavenModel>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, MorovoyRaven raven, float limbSwing, float limbSwingAmount, float partialTick, float age, float netHeadYaw, float headPitch) {
                if (raven.isInvisible()) return;
                // Transparent PNG texels discard; only manually painted UV veins glow.
                getParentModel().renderToBuffer(poseStack, buffers.getBuffer(RenderType.eyes(EMISSIVE)), 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(MorovoyRaven raven) { return ALBEDO; }

    @Override
    protected void scale(MorovoyRaven raven, PoseStack matrices, float partialTick) {
        if (raven.minion()) matrices.scale(.28F, .28F, .28F);
    }
}
