package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.savanna.Div;
public final class DivRenderer extends MobRenderer<Div,DivModel>{
    static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/savanna/div_palette.png");
    private static final ResourceLocation EYES=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/savanna/div_eyes.png");
    public DivRenderer(EntityRendererProvider.Context c){super(c,new DivModel(),2.2F);addLayer(new RenderLayer<Div,DivModel>(this){@Override public void render(PoseStack pose,MultiBufferSource out,int light,Div entity,float walk,float amount,float partial,float age,float yaw,float pitch){int intensity=entity.phase()==3||entity.fateCharge()>0?255:190;int tint=0xFF000000|(intensity<<16)|(intensity<<8)|intensity;getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.eyes(EYES)),15728880,OverlayTexture.NO_OVERLAY,tint);}});}
    @Override protected void scale(Div e,PoseStack p,float partial){p.scale(3.2F,3.2F,3.2F);}
    @Override public ResourceLocation getTextureLocation(Div e){return TEXTURE;}
}
