package org.slavicmyths.losthalls.client;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.losthalls.ShadowDecoy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
/** Uses owner skin as a read-only client reference; never copies equipment stacks. */
public final class ShadowDecoyRenderer extends EntityRenderer<ShadowDecoy> {
 private final PlayerModel<ShadowDecoy> model;
 public ShadowDecoyRenderer(EntityRendererProvider.Context c){super(c);model=new PlayerModel<>(c.bakeLayer(ModelLayers.PLAYER),false);shadowRadius=.25F;}
 @Override public ResourceLocation getTextureLocation(ShadowDecoy e){var p=e.owner()==null?null:e.level().getPlayerByUUID(e.owner());return p instanceof net.minecraft.client.player.AbstractClientPlayer player?player.getSkin().texture():DefaultPlayerSkin.get(e.owner()==null?new java.util.UUID(0,0):e.owner()).texture();}
 @Override public void render(ShadowDecoy e,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-yaw));pose.scale(-1,-1,1);pose.translate(0,-1.501,0);model.attackTime=e.getAttackAnim(partial);model.prepareMobModel(e,e.walkAnimation.position(partial),e.walkAnimation.speed(partial),partial);model.setupAnim(e,e.walkAnimation.position(partial),e.walkAnimation.speed(partial),e.tickCount+partial,0,e.getXRot());model.renderToBuffer(pose,out.getBuffer(RenderType.entityTranslucent(getTextureLocation(e))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0x887db4d9);pose.popPose();super.render(e,yaw,partial,pose,out,light);}
}
