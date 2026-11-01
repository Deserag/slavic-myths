package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.morovoy.RavenBlightBolt;
import org.slavicmyths.morovoy.MorovoyRavenContent;

/** Pixel quad and a server-side moving projectile; particles are decorative only. */
public final class RavenBlightRenderer extends EntityRenderer<RavenBlightBolt> {
    private static final ResourceLocation TEXTURE = MorovoyRavenContent.id("textures/entity/morovoy_blight.png");
    public RavenBlightRenderer(EntityRendererProvider.Context ctx) { super(ctx); }
    @Override public ResourceLocation getTextureLocation(RavenBlightBolt entity) { return TEXTURE; }
    @Override public void render(RavenBlightBolt entity,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light) {
        pose.pushPose();pose.mulPose(entityRenderDispatcher.cameraOrientation());pose.scale(.44F,.44F,.44F);
        VertexConsumer buffer = out.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        var layer = pose.last();
        vertex(buffer,layer,-1,-1,0,1,light);vertex(buffer,layer,1,-1,1,1,light);
        vertex(buffer,layer,1,1,1,0,light);vertex(buffer,layer,-1,1,0,0,light);
        pose.popPose();super.render(entity,yaw,partial,pose,out,light);
    }
    private static void vertex(VertexConsumer out,PoseStack.Pose pose,float x,float y,int u,int v,int light) {
        out.addVertex(pose,x,y,0).setColor(255,255,255,255).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);
    }
}
