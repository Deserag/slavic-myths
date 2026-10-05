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
/** Small native body models, using the world-boss material atlas and existing Curios attachment API. */
public final class BossAccessoryRenderer{
 private static final HumanoidModel<LivingEntity> EYE=build(false),BELT=build(true);
 private static void box(PartDefinition body,int[] index,int material,float x,float y,float z,float w,float h,float d){body.addOrReplaceChild("piece_"+index[0]++,CubeListBuilder.create().texOffs(material%8*64,material/8*64).addBox(x,y,z,w,h,d),PartPose.ZERO);}
 private static HumanoidModel<LivingEntity> build(boolean belt){var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);PartDefinition body=mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);int[] index={0};if(belt){box(body,index,10,-4.5F,9,-2.7F,9,2.8F,5.4F);box(body,index,11,-1.5F,9.2F,-3.3F,3,2.2F,.6F);for(int i=0;i<18;i++){double a=i*Math.PI*3.5/17,r=1.1*(1-i/24.0);box(body,index,12,(float)(Math.cos(a)*r)-.15F,(float)(10.3+Math.sin(a)*r),-3.6F,.3F,.35F,.25F);}box(body,index,10,-2.8F,11.8F,-2.9F,.6F,3,.4F);box(body,index,10,2.2F,11.8F,-2.9F,.6F,3.7F,.4F);for(int x:new int[]{-4,-2,2,3})box(body,index,11,x,9.5F,-3,.8F,1.4F,.3F);}else{box(body,index,2,-1.8F,0,-2.5F,.3F,4,.3F);box(body,index,2,1.5F,0,-2.5F,.3F,4,.3F);box(body,index,19,-1.5F,3.8F,-2.7F,3,3.5F,.5F);box(body,index,7,-1.1F,4.7F,-3.3F,2.2F,1.6F,.3F);box(body,index,8,-.5F,4.8F,-3.65F,1,1.4F,.3F);box(body,index,6,-.15F,5,-4,.3F,.9F,.2F);box(body,index,5,-1.6F,3.7F,-2.9F,3.2F,.45F,.4F);box(body,index,5,-1.6F,7,-2.9F,3.2F,.45F,.4F);box(body,index,4,-.3F,7.4F,-2.9F,.6F,2,.3F);}return new HumanoidModel<>(LayerDefinition.create(mesh,512,512).bakeRoot());}
 public static void render(ItemStack stack,LivingEntity e,PoseStack pose,MultiBufferSource buffer,int light){boolean belt=stack.getItem()==org.slavicmyths.registry.ModItems.POYAS_TUGARINA.get();HumanoidModel<LivingEntity> m=belt?BELT:EYE;top.theillusivec4.curios.api.client.ICurioRenderer.followBodyRotations(e,m);m.body.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(belt?"tugarin_zmey":"likho_one_eyed")+".png"))),light,OverlayTexture.NO_OVERLAY);}
}
