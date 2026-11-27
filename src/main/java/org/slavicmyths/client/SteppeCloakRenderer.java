package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
public final class SteppeCloakRenderer implements ICurioRenderer {
 private static final ResourceLocation TEX=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/armor/steppe/steppe_atlas.png");
 private final HumanoidModel<LivingEntity> model=create();
 private static void piece(PartDefinition root,String name,int mat,float x,float y,float z,float w,float h,float d){root.addOrReplaceChild(name,CubeListBuilder.create().texOffs(mat%4*64,mat/4*64).addBox(x,y,z,w,h,d),PartPose.ZERO);}
 private static HumanoidModel<LivingEntity> create(){
  var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var body=mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);
  var panel=body.addOrReplaceChild("panel",CubeListBuilder.create(),PartPose.offset(0,1,3.8F));
  piece(panel,"cloth",5,-5.1F,0,0,10.2F,19,.65F);
  for(int s:new int[]{-1,1}){piece(panel,"border"+s,2,s<0?-5.7F:4.7F,0,.2F,1.1F,19.5F,.65F);piece(panel,"trim"+s,6,s<0?-4.6F:4.1F,3,.75F,.5F,15,.3F);}
  piece(panel,"bottom",6,-4.7F,18.3F,.7F,9.4F,.9F,.3F);
  for(int y=0;y<7;y++)for(int x=0;x<7;x++)if(Math.abs(x-3)+Math.abs(y-3)==3||x==3&&y==3)piece(panel,"diamond"+x+"_"+y,6,-3.15F+x*.9F,7+y*.9F,.7F,.9F,.9F,.3F);
  for(int x=0;x<7;x++)for(int row=0;row<3;row++)piece(body,"fur"+x+"_"+row,(x+row)%3==0?4:0,-5.8F+x*1.65F,-1.7F+row*1.2F,2.1F+row*.45F,1.8F,2.3F,2.3F);
  for(int s:new int[]{-1,1}){piece(body,"collar"+s,0,s<0?-5.5F:2.4F,-1.7F,-3.1F,3.1F,2.7F,5.7F);piece(body,"clasp"+s,6,s<0?-2.6F:1.6F,.3F,-3.6F,1,1.3F,.6F);piece(body,"fang"+s,7,s<0?-2.5F:1.8F,1.4F,-3.5F,.6F,2.1F,.5F);}
  return new HumanoidModel<>(LayerDefinition.create(mesh,256,128).bakeRoot());
 }
 @Override public <T extends LivingEntity,M extends EntityModel<T>> void render(ItemStack s,SlotContext c,PoseStack pose,RenderLayerParent<T,M> parent,MultiBufferSource buffer,int light,float walk,float amount,float partial,float age,float yaw,float pitch){
  if(c.entity().isInvisible())return;ICurioRenderer.followBodyRotations(c.entity(),model);
  model.body.getChild("panel").xRot=-(.13F+.12F*amount+(float)Math.sin(walk*.65F)*.05F*amount);
  model.body.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(TEX)),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
 }
}
