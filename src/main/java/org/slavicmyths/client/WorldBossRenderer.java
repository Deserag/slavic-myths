package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.hunt.WorldBoss;
public final class WorldBossRenderer extends MobRenderer<WorldBoss,WorldBossModel>{
 private final ResourceLocation texture;
 private final boolean likho;private final WorldBossModel earth,serpent;private WorldBossModel higher;
 public WorldBossRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager,boolean likho){super(manager,new WorldBossModel(likho),likho?.45F:.85F);this.likho=likho;earth=model;if(likho)higher=new WorldBossModel(true,false,true);serpent=likho?null:new WorldBossModel(false,true);texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(likho?"likho_one_eyed":"tugarin_zmey")+".png");addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<WorldBoss,WorldBossModel>(this){@Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffer,int light,WorldBoss e,float stride,float amount,float partial,float age,float yaw,float pitch){model.glow(pose,buffer.getBuffer(net.minecraft.client.renderer.RenderType.eyes(getTextureLocation(e))),e);}});}
 @Override public void render(WorldBoss e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffer,int light){if(likho)model=e.morState()!=0?higher:earth;if(!likho)model=e.tugarinState()==WorldBoss.TugarinState.PHASE_TWO||e.tugarinState()==WorldBoss.TugarinState.TRANSFORMING&&e.animationTime(partial)>=30?serpent:earth;super.render(e,yaw,partial,pose,buffer,light);}
 @Override protected void scale(WorldBoss e,com.mojang.blaze3d.vertex.PoseStack pose,float partial){if(likho&&e.morState()!=0)pose.scale(1.35F,1.35F,1.35F);}
 @Override protected float getFlipDegrees(WorldBoss e){return 0;}
 @Override public ResourceLocation getTextureLocation(WorldBoss e){return likho?(e.morState()!=0?MorVisual.texture(e,"likho"):texture):ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(model==serpent?"tugarin_phase2":"tugarin_phase1")+".png");}
}
