package org.slavicmyths.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.morozko.*;
public final class MorozkoRenderer extends MobRenderer<Morozko,MorozkoModel> {
 public MorozkoRenderer(EntityRendererProvider.Context c){super(c,new MorozkoModel(),.7f);addLayer(new Glow(this));}
 @Override public ResourceLocation getTextureLocation(Morozko e){return FrostContent.id("textures/entity/"+(e instanceof IceDouble?"morozko_ice_double":"morozko")+".png");}
 @Override protected RenderType getRenderType(Morozko e,boolean visible,boolean translucent,boolean glowing){return e instanceof IceDouble?RenderType.entityTranslucent(getTextureLocation(e)):super.getRenderType(e,visible,translucent,glowing);}
 @Override protected float getFlipDegrees(Morozko e){return 0;}
 private static final class Glow extends net.minecraft.client.renderer.entity.layers.RenderLayer<Morozko,MorozkoModel> {
  Glow(MorozkoRenderer r){super(r);}
  @Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource out,int light,Morozko e,float a,float b,float partial,float c,float d,float f){int color=e instanceof IceDouble?0x50FFFFFF:e.level().isDay()?0x80FFFFFF:0xDFFFFFFF;getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.entityTranslucentEmissive(FrostContent.id("textures/entity/morozko_glow.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,color);}
 }
}
