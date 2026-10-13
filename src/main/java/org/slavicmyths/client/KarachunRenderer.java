package org.slavicmyths.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.slavicmyths.karachun.*;
public final class KarachunRenderer extends MobRenderer<Karachun,KarachunModel> {
 public KarachunRenderer(EntityRendererProvider.Context c){super(c,new KarachunModel(),.8f);addLayer(new RenderLayer<Karachun,KarachunModel>(this){@Override public void render(com.mojang.blaze3d.vertex.PoseStack p,MultiBufferSource out,int light,Karachun e,float a,float b,float partial,float time,float y,float x){getParentModel().renderToBuffer(p,out.getBuffer(RenderType.eyes(NightContent.id("textures/entity/karachun_glow.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xFFFFFFFF);}});}
 @Override public net.minecraft.resources.ResourceLocation getTextureLocation(Karachun e){return NightContent.id("textures/entity/karachun.png");}
}
