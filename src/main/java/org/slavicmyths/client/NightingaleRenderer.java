package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.bandit.NightingaleEntity;
public final class NightingaleRenderer extends MobRenderer<NightingaleEntity,NightingaleModel> {
 private final NightingaleModel first,second;
 public NightingaleRenderer(EntityRendererProvider.Context manager){super(manager,new NightingaleModel(false),.6F);first=model;second=new NightingaleModel(true);}
 private boolean second(NightingaleEntity e,float partial){return e.phase()==NightingaleEntity.Phase.PHASE_TWO||e.phase()==NightingaleEntity.Phase.TRANSFORMING&&e.elapsed(partial)>=20;}
 @Override public void render(NightingaleEntity e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffer,int light){model=second(e,partial)?second:first;super.render(e,yaw,partial,pose,buffer,light);}
 @Override protected void scale(NightingaleEntity e,com.mojang.blaze3d.vertex.PoseStack pose,float partial){pose.scale(1,1,1);}
 public ResourceLocation getTextureLocation(NightingaleEntity e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(model==second?"nightingale_phase2":"nightingale_phase1")+".png");}
 protected float getFlipDegrees(NightingaleEntity e){return 0;}
}
