package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.morovoy.MorovoyRaven;
import org.slavicmyths.morovoy.MorovoyRavenContent;

/** Unique four-wing boss rig; the smaller plague ravens use the identical rig at .28. */
public final class MorovoyRavenRenderer extends MobRenderer<MorovoyRaven,MorovoyRavenModel> {
    private static final ResourceLocation TEXTURE = MorovoyRavenContent.id("textures/entity/morovoy_raven.png");
    public MorovoyRavenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,new MorovoyRavenModel(),1.6F);
        addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<MorovoyRaven,MorovoyRavenModel>(this) {
            @Override public void render(PoseStack pose,net.minecraft.client.renderer.MultiBufferSource out,int light,MorovoyRaven raven,float limb,float limbAmount,float partial,float age,float yaw,float pitch) {
                getParentModel().renderToBuffer(pose,out.getBuffer(net.minecraft.client.renderer.RenderType.eyes(MorovoyRavenContent.id("textures/entity/morovoy_raven_glow.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
            }
        });
    }
    @Override public ResourceLocation getTextureLocation(MorovoyRaven raven) { return TEXTURE; }
    @Override protected void scale(MorovoyRaven raven,PoseStack matrices,float tick) {
        if (raven.minion()) matrices.scale(.28F,.28F,.28F);
    }
}
