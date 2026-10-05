package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.hunt.HuntMob;
public class HuntRenderer<T extends HuntMob> extends MobRenderer<T,HuntModel<T>>{
 private final ResourceLocation texture;
 public HuntRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager,boolean oven){super(manager,new HuntModel<T>(oven),oven?.7F:.6F);texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(oven?"ovinnik":"volkolak")+".png");addLayer(new RenderLayer<T,HuntModel<T>>(this){@Override public void render(PoseStack p,MultiBufferSource out,int light,T e,float stride,float amount,float partial,float age,float yaw,float pitch){model.glow(p,out.getBuffer(RenderType.eyes(texture)),e);}});}
 @Override public ResourceLocation getTextureLocation(T e){return texture;}
}
