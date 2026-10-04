package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
public final class ElementRenderer<T extends MobEntity> extends MobRenderer<T,ElementModel<T>> {
 private final ResourceLocation texture;
 public ElementRenderer(EntityRendererManager manager,boolean fire){super(manager,new ElementModel<T>(fire),fire?.6F:.5F);texture=new ResourceLocation("slavicmyths","textures/entity/"+(fire?"fire_serpent":"podvey")+".png");addLayer(new LayerRenderer<T,ElementModel<T>>(this){@Override public void render(MatrixStack p,IRenderTypeBuffer out,int light,T e,float stride,float amount,float partial,float age,float yaw,float pitch){model.glow(p,out.getBuffer(RenderType.eyes(texture)),e);}});}
 @Override public ResourceLocation getTextureLocation(T e){return texture;}
}
