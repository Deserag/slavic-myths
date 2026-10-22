package org.slavicmyths.client;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
/** Small native back quiver attached to the same humanoid body rotation as existing accessories. */
public final class FinistQuiverRenderer implements top.theillusivec4.curios.api.client.ICurioRenderer {
 private static final net.minecraft.resources.ResourceLocation TEX=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/armor/finist_quiver_worn.png");
 public static HumanoidModel<LivingEntity> model(){var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var body=mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);body.addOrReplaceChild("pouch",CubeListBuilder.create().texOffs(0,0).addBox(.5F,1,3.4F,5.1F,8.3F,3.2F),PartPose.ZERO);body.addOrReplaceChild("rim",CubeListBuilder.create().texOffs(0,16).addBox(.25F,.2F,3.15F,5.6F,1.4F,3.7F),PartPose.ZERO);body.addOrReplaceChild("emblem",CubeListBuilder.create().texOffs(32,0).addBox(1.8F,4.2F,6.65F,2.5F,1.1F,.3F),PartPose.ZERO);
  for(int i=0;i<6;i++)body.addOrReplaceChild("strap"+i,CubeListBuilder.create().texOffs(32,8).addBox(-4+i*1.5F,i*1.7F,3.3F,1.8F,2.2F,.4F),PartPose.ZERO);
  for(int i=0;i<4;i++){body.addOrReplaceChild("arrow"+i,CubeListBuilder.create().texOffs(32,16).addBox(1+i*1.1F,-2.5F,4.2F,.4F,5,.4F),PartPose.ZERO);body.addOrReplaceChild("feather"+i,CubeListBuilder.create().texOffs(40,16).addBox(.75F+i*1.1F,-2.8F,4.1F,1,1.3F,.6F),PartPose.ZERO);}return new HumanoidModel<>(LayerDefinition.create(mesh,64,64).bakeRoot());}
 private static final HumanoidModel<LivingEntity> MODEL=model();
 @Override public <T extends LivingEntity,M extends EntityModel<T>> void render(ItemStack s,top.theillusivec4.curios.api.SlotContext c,PoseStack pose,RenderLayerParent<T,M> parent,MultiBufferSource buffer,int light,float walk,float amount,float partial,float age,float yaw,float pitch){if(c.entity().isInvisible())return;top.theillusivec4.curios.api.client.ICurioRenderer.followBodyRotations(c.entity(),MODEL);MODEL.body.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(TEX)),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);}
}
