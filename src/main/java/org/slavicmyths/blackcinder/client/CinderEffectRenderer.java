package org.slavicmyths.blackcinder.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
/** Geometric projectile and rising ash spikes, not a flat particle billboard. */
public final class CinderEffectRenderer<T extends Entity> extends EntityRenderer<T> {
 private final ModelPart mesh;private final boolean spike;
 public CinderEffectRenderer(EntityRendererProvider.Context c,boolean spike){super(c);this.spike=spike;var m=new MeshDefinition();var root=m.getRoot();if(spike){root.addOrReplaceChild("base",CubeListBuilder.create().texOffs(0,0).addBox(-6,-2,-6,12,2,12),PartPose.ZERO);root.addOrReplaceChild("shaft",CubeListBuilder.create().texOffs(0,0).addBox(-3,-24,-3,6,22,6),PartPose.ZERO);root.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(0,0).addBox(-1,-32,-1,2,8,2),PartPose.ZERO);}else root.addOrReplaceChild("coal",CubeListBuilder.create().texOffs(0,0).addBox(-2,-2,-2,4,4,4),PartPose.ZERO);mesh=LayerDefinition.create(m,64,64).bakeRoot();}
 @Override public void render(T e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource out,int light){pose.pushPose();float scale=spike?Math.min(1,(e.tickCount+partial)/5F):1;pose.scale(1,-scale,1);mesh.render(pose,out.getBuffer(RenderType.entityCutout(getTextureLocation(e))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);pose.popPose();super.render(e,yaw,partial,pose,out,light);}
 @Override public ResourceLocation getTextureLocation(T e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/cinder_effect.png");}
}
