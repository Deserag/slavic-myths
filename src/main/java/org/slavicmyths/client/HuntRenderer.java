package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.hunt.HuntMob;
public class HuntRenderer<T extends HuntMob> extends MobRenderer<T,HuntModel<T>>{
 private final ResourceLocation texture;private final HuntModel<T> ordinary, higher;private final boolean oven;
 public HuntRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager,boolean oven){super(manager,new HuntModel<T>(oven),oven?.7F:.6F);this.oven=oven;ordinary=model;higher=oven?new HuntModel<T>(true,true):null;texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(oven?"ovinnik":"volkolak")+".png");addLayer(new RenderLayer<T,HuntModel<T>>(this){@Override public void render(PoseStack p,MultiBufferSource out,int light,T e,float stride,float amount,float partial,float age,float yaw,float pitch){model.glow(p,out.getBuffer(RenderType.eyes(getTextureLocation(e))),e);}});}
 @Override public void render(T e,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){model=oven&&e.morState()!=0?higher:ordinary;super.render(e,yaw,partial,p,out,light);}
 @Override protected void scale(T e,PoseStack p,float partial){if(oven&&e.morState()!=0)p.scale(1.25F,1.15F,1.25F);}
 @Override public ResourceLocation getTextureLocation(T e){return oven&&e.morState()!=0?MorVisual.texture(e,"ovinnik"):texture;}
}
