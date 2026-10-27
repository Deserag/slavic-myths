package org.slavicmyths.blackcinder.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.blackcinder.*;
public final class CinderRenderer extends MobRenderer<CinderMob,CinderModel> {
 public CinderRenderer(EntityRendererProvider.Context context,CinderContent.Kind kind){super(context,new CinderModel(kind),kind==CinderContent.Kind.SPARKLING?.12F:kind==CinderContent.Kind.ZHARENITSA?.9F:.55F);
  addLayer(new RenderLayer<CinderMob,CinderModel>(this){@Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource out,int light,CinderMob e,float a,float b,float partial,float age,float yaw,float pitch){getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.eyes(ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+CinderContent.id(e.kind)+"_glow.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,e.second()?0xffffffff:0xbfffffff);}});
 }
 @Override public ResourceLocation getTextureLocation(CinderMob e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+CinderContent.id(e.kind)+".png");}
 @Override protected float getFlipDegrees(CinderMob e){return 0;}
}
