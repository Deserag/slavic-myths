package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.resources.ResourceLocation;
public final class ElementRenderer<T extends Mob> extends MobRenderer<T,ElementModel<T>> {
 private final ResourceLocation texture;
 public ElementRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager,boolean fire){super(manager,new ElementModel<T>(fire),fire?.6F:.5F);texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(fire?"fire_serpent":"podvey")+".png");addLayer(new RenderLayer<T,ElementModel<T>>(this){@Override public void render(PoseStack p,MultiBufferSource out,int light,T e,float stride,float amount,float partial,float age,float yaw,float pitch){model.glow(p,out.getBuffer(RenderType.eyes(texture)),e);}});}
 @Override public ResourceLocation getTextureLocation(T e){return texture;}
}
