package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.hunt.WorldBoss;
public final class WorldBossRenderer extends MobRenderer<WorldBoss,WorldBossModel>{
 private final ResourceLocation texture;
 public WorldBossRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager,boolean likho){super(manager,new WorldBossModel(likho),likho?.45F:.85F);texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(likho?"likho_one_eyed":"tugarin_zmey")+".png");addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<WorldBoss,WorldBossModel>(this){@Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffer,int light,WorldBoss e,float stride,float amount,float partial,float age,float yaw,float pitch){model.glow(pose,buffer.getBuffer(net.minecraft.client.renderer.RenderType.eyes(texture)),e);}});}
 @Override protected float getFlipDegrees(WorldBoss e){return 0;}
 @Override public ResourceLocation getTextureLocation(WorldBoss e){return texture;}
}
