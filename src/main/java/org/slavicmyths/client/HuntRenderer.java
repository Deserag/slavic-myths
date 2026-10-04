package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.hunt.HuntMob;
public class HuntRenderer<T extends HuntMob> extends MobRenderer<T,HuntModel<T>>{
 private final ResourceLocation texture;
 public HuntRenderer(EntityRendererManager manager,boolean oven){super(manager,new HuntModel<T>(oven),oven?.7F:.6F);texture=new ResourceLocation("slavicmyths","textures/entity/"+(oven?"ovinnik":"volkolak")+".png");addLayer(new LayerRenderer<T,HuntModel<T>>(this){@Override public void render(MatrixStack p,IRenderTypeBuffer out,int light,T e,float stride,float amount,float partial,float age,float yaw,float pitch){model.glow(p,out.getBuffer(RenderType.eyes(texture)),e);}});}
 @Override public ResourceLocation getTextureLocation(T e){return texture;}
}
