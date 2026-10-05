package org.slavicmyths.client;

import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
/** Curios' existing body attachment API, no second accessory renderer framework. */
public final class HuntAccessoryRenderer {
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/hunt_accessories.png");
 private static final HumanoidModel<LivingEntity> BELT=model(true),CHARM=model(false),STAR=star();
 private static void box(PartDefinition body,int[] index,int mat,float x,float y,float z,float w,float h,float d){body.addOrReplaceChild("piece_"+index[0]++,CubeListBuilder.create().texOffs(mat%4*64,mat/4*64).addBox(x,y,z,w,h,d),PartPose.ZERO);}
 private static HumanoidModel<LivingEntity> star(){var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);PartDefinition body=mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);int[] index={0};box(body,index,5,-2,0,-2.4F,.4F,4,.4F);box(body,index,5,1.6F,0,-2.4F,.4F,4,.4F);body.addOrReplaceChild("diamond",CubeListBuilder.create().texOffs(128,0).addBox(-1.8F,-1.8F,-.2F,3.6F,3.6F,.6F),PartPose.offsetAndRotation(0,5.5F,-2.6F,0,0,.785F));box(body,index,7,-.25F,4,-3.3F,.5F,3,.3F);box(body,index,7,-1.5F,5.25F,-3.3F,3,.5F,.3F);box(body,index,3,-.35F,5,-3.4F,.7F,.7F,.2F);box(body,index,5,-2.4F,6,-2.8F,.6F,4,.4F);return new HumanoidModel<>(LayerDefinition.create(mesh,256,256).bakeRoot());}
 private static HumanoidModel<LivingEntity> model(boolean belt){var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);PartDefinition body=mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);int[] index={0};if(belt){box(body,index,5,-4.5F,9,-2.7F,9,2.5F,5.4F);box(body,index,8,-1.2F,9.1F,-3.3F,2.4F,2.2F,.7F);box(body,index,1,-4.6F,8.7F,-2.8F,9.2F,.7F,5.6F);box(body,index,3,-3,11,-3,1,3,.7F);box(body,index,3,-1.6F,11,-3,1,3.4F,.7F);box(body,index,7,2,11,-3,1.3F,3,.5F);}else{box(body,index,5,-2.2F,0,-2.3F,.4F,4,.4F);box(body,index,5,1.8F,0,-2.3F,.4F,4,.4F);box(body,index,2,-1.7F,3.5F,-2.5F,3.4F,4.2F,.7F);box(body,index,7,-.3F,4,-3.3F,.6F,2.8F,.2F);box(body,index,7,-1.1F,5,-3.3F,2.2F,.5F,.2F);box(body,index,3,-.4F,5.5F,-3.2F,.8F,.8F,.2F);}return new HumanoidModel<>(LayerDefinition.create(mesh,256,256).bakeRoot());}
 public static void render(ItemStack stack,LivingEntity wearer,PoseStack pose,MultiBufferSource buffer,int light){boolean star=stack.getItem()==org.slavicmyths.registry.ModItems.OBEREG_PADAYUSCHEY_ZVEZDY.get();HumanoidModel<LivingEntity> m=star?STAR:stack.getItem()==org.slavicmyths.registry.ModItems.VOLCHIY_POYAS.get()?BELT:CHARM;top.theillusivec4.curios.api.client.ICurioRenderer.followBodyRotations(wearer,m);m.body.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(star?ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/hunt_star_accessory.png"):TEXTURE)),light,OverlayTexture.NO_OVERLAY);}
}
