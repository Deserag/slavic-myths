package org.slavicmyths.morovoy;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Dedicated native voxel anatomy: 2 articulated necks/heads, one wing pair, 2 segmented tails. */
public final class MorovoyFireZmeyMesh {
 private static CubeListBuilder box(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
 public static ModelPart create(){MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot();
  PartDefinition body=root.addOrReplaceChild("body",box(0,0,-11,-13,-28,22,22,61).texOffs(2,63).addBox(-9,2,-26,18,8,45).texOffs(4,25).addBox(-9,-16,-18,18,4,34),PartPose.offset(0,12,0));
  for(int i=0;i<9;i++){int z=-22+i*6;body.addOrReplaceChild("spine_"+i,box(3,10,-2,-22-(i%3)*2,z,4,11+(i%3)*2,6),PartPose.ZERO);for(int side:new int[]{-1,1})body.addOrReplaceChild("rib_"+i+"_"+side,box(13,41,side<0?-12:8,-7,z,4,9,3),PartPose.ZERO);}
  for(int side:new int[]{-1,1}){
   String n=side<0?"left":"right";int hx=side<0?100:178;
   PartDefinition neck0=body.addOrReplaceChild("neck_"+n+"_0",box(hx,6,-5,-6,-19,10,11,23).texOffs(hx,36).addBox(-4,-8,-16,8,4,12),PartPose.offset(side*13,-6,-23));
   PartDefinition neck1=neck0.addOrReplaceChild("neck_"+n+"_1",box(hx+12,8,-4,-5,-18,8,9,22).texOffs(hx,55).addBox(-3,-7,-13,6,3,10),PartPose.offset(0,-2,-17));
   PartDefinition head=neck1.addOrReplaceChild("head_"+n,box(hx,64,-7,-8,-18,14,13,20).texOffs(hx+25,64).addBox(-5,-3,-25,10,7,12).texOffs(hx+40,70).addBox(-6,-9,-9,12,4,12),PartPose.offset(0,-3,-16));
   head.addOrReplaceChild("jaw_"+n,box(hx+4,90,-6,0,-23,12,4,22).texOffs(hx+19,94).addBox(-4,3,-21,2,7,3).texOffs(hx+23,94).addBox(3,3,-21,2,7,3),PartPose.offset(0,4,0));
   for(int i=0;i<2;i++){head.addOrReplaceChild("horn_"+n+"_"+i,box(2,165,-2,-16,-9+i*10,4,10-i*2,4),PartPose.offset(side*(i==0?5:-5),0,0));}
   head.addOrReplaceChild("eye_"+n+"_a",box(side<0?123:204,60,-7,-5,-16,2,3,4),PartPose.ZERO);
   head.addOrReplaceChild("eye_"+n+"_b",box(side<0?123:204,60,5,-5,-16,2,3,4),PartPose.ZERO);
   for(int front=0;front<2;front++){
    PartDefinition leg=body.addOrReplaceChild("leg_"+n+"_"+front,box(19,75,-6,-3,-6,12,17,12).texOffs(44,80).addBox(-5,9,-7,10,11,10),PartPose.offset(side*10,3,front==0?-17:21));
    for(int c=-1;c<=1;c++)leg.addOrReplaceChild("claw_"+c,box(7,96,c*4-2,18,-14,3,4,10),PartPose.ZERO);
   }
   int wingRoot=side<0?-47:0;
   PartDefinition wing=body.addOrReplaceChild("wing_"+n,box(1,142,wingRoot,-5,-12,47,8,18).texOffs(4,179).addBox(wingRoot,-1,2,47,3,19),PartPose.offset(side*11,-10,-3));
   for(int j=0;j<5;j++)wing.addOrReplaceChild("membrane_"+j,box(2+(j%3)*14,180,side<0?-46+j*8:j*8,1,3+j*2,8,1,20-j*2),PartPose.ZERO);
   for(int j=0;j<4;j++)wing.addOrReplaceChild("structural_spar_"+j,box(57,165,side<0?-43+j*11:j*11,-3,-8+j*3,4,5,23-j*2),PartPose.ZERO);
   PartDefinition tip=wing.addOrReplaceChild("wing_tip_"+n,box(15,160,side<0?-38:0,-4,-7,38,6,16).texOffs(40,184).addBox(side<0?-38:0,0,8,38,2,16),PartPose.offset(side*45,0,-4));
   for(int j=0;j<4;j++)tip.addOrReplaceChild("tip_rib_"+j,box(57,165,side<0?-36+j*9:j*9,0,16+j*2,4,1,12-j),PartPose.ZERO);
   PartDefinition previous=body;
   for(int t=0;t<6;t++){int width=Math.max(3,10-t),len=12-t/2;String key="tail_"+n+"_"+t;PartDefinition section=previous.addOrReplaceChild(key,box(side<0?140:193,140+t*14,-width/2f,-3,-1,width,7-t/2f,len).texOffs(side<0?158:217,140+t*14).addBox(-1,-7,-1,2,5,5),t==0?PartPose.offset(side*7,2,29):PartPose.offset(0,1,10));previous=section;}
  }
  return LayerDefinition.create(mesh,256,256).bakeRoot();
 }
 private MorovoyFireZmeyMesh(){}
}
