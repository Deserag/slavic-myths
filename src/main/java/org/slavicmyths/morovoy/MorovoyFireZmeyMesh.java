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
  PartDefinition body=root.addOrReplaceChild("body",box(0,0,-16,-10,-47,32,17,88).texOffs(2,63).addBox(-10,3,-38,20,5,66).texOffs(4,25).addBox(-10,-13,-31,20,3,52),PartPose.offset(0,12,0));
  // Anatomical transitions modelled as named child bones, following Gorynych's Java voxel-mesh approach.
  // They bridge the long torso into hip roots and make the twin tail bases part of the body.
  body.addOrReplaceChild("ribcage",box(0,0,-14,-10,-36,28,15,43),PartPose.ZERO);
  body.addOrReplaceChild("abdomen",box(2,63,-11,-9,0,22,14,38),PartPose.ZERO);
  body.addOrReplaceChild("pelvis",box(4,25,-10,-8,29,20,13,19),PartPose.ZERO);
  for(int i=0;i<9;i++){int z=-34+i*8;body.addOrReplaceChild("spine_"+i,box(3,10,-2,-22-(i%3)*2,z,4,11+(i%3)*2,6),PartPose.ZERO);for(int side:new int[]{-1,1})body.addOrReplaceChild("rib_"+i+"_"+side,box(13,41,side<0?-15:11,-7,z,4,9,3),PartPose.ZERO);}
  for(int side:new int[]{-1,1}){
   String n=side<0?"left":"right";int hx=side<0?100:178;
   PartDefinition neck0=body.addOrReplaceChild("neck_"+n+"_base",box(hx,6,-5,-5,-22,10,10,26).texOffs(hx,36).addBox(-4,-8,-16,8,4,12),PartPose.offset(side*11,-10,-37));
   PartDefinition neck1=neck0.addOrReplaceChild("neck_"+n+"_mid",box(hx+12,8,-4,-5,-18,8,9,22).texOffs(hx,55).addBox(-3,-7,-13,6,3,10),PartPose.offset(side*2,-7,-20));
   // Both heads share the same compact bone plan but have different crests, brows and jaws.
   PartDefinition neck2=neck1.addOrReplaceChild("neck_"+n+"_tip",box(hx+18,35,-3,-4,-13,6,7,17),PartPose.offset(side*2,-5,-17));
   PartDefinition head=neck2.addOrReplaceChild("head_"+n,box(hx,64,-6,-7,-17,12,12,19)
    .texOffs(hx+25,64).addBox(-4,-3,-25,8,6,12),PartPose.offset(side*3,-5,-19));
   head.addOrReplaceChild("brow_"+n,box(hx+12,70,-6,-9,-15,12,3,10),PartPose.ZERO);
   PartDefinition jaw=head.addOrReplaceChild("jaw_"+n,box(hx+4,90,-5,0,-23,10,3,21),PartPose.offset(0,4,0));
   for(int fangSide:new int[]{-1,1}){
    jaw.addOrReplaceChild("tooth_"+fangSide,box(hx+19,94,fangSide*3-1,0,-20,2,5,3),PartPose.ZERO);
    head.addOrReplaceChild("horn_"+n+"_"+(fangSide<0?0:1),box(2,165,-2,-12,-6,4,10,4),PartPose.offset(fangSide*5,0,0));
   }
   // Eyes are near the forward cheekbones rather than floating over the muzzle.
   head.addOrReplaceChild("eye_"+n+"_a",box(side<0?123:204,60,-7,-4,-15,2,3,4),PartPose.ZERO);
   head.addOrReplaceChild("eye_"+n+"_b",box(side<0?123:204,60,5,-4,-15,2,3,4),PartPose.ZERO);
   for(int crest=0;crest<4;crest++){
    int lift=side<0?5+crest:7+crest*2;
    head.addOrReplaceChild("crest_"+n+"_"+crest,box(side<0?13:202,side<0?40:162,-2,-lift,-8+crest*5,4,lift-2,4),PartPose.offset(side*(crest%2==0?3:-3),0,0));
   }
   for(int front=0;front<2;front++){
    PartDefinition leg=body.addOrReplaceChild("leg_"+n+"_"+front,box(19,75,-6,-3,-6,12,17,12).texOffs(44,80).addBox(-5,9,-7,10,11,10),PartPose.offset(side*15,3,front==0?-26:28));
    for(int c=-1;c<=1;c++)leg.addOrReplaceChild("claw_"+c,box(7,96,c*4-2,18,-14,3,4,10),PartPose.ZERO);
   }
   // Single articulated pair. The leading bone stays thick, but the membranes are thin
   // tapered strips instead of the old full-width rectangular glowing slabs.
   PartDefinition wing=body.addOrReplaceChild("wing_"+n,box(8,140,side<0?-49:0,-3,-5,49,5,10),PartPose.offset(side*15,-11,-8));
   for(int j=0;j<5;j++){
    float x=side<0?-48+j*9:j*9;
    wing.addOrReplaceChild("membrane_"+j,box(5+j*13,177,x,1,5+j*2,9,1,21-j*3),PartPose.ZERO);
    wing.addOrReplaceChild("spar_"+j,box(70+j*12,151,x,0,3+j*2,3,3,20-j*3),PartPose.ZERO);
   }
   PartDefinition tip=wing.addOrReplaceChild("wing_tip_"+n,box(5,145,side<0?-39:0,-3,-4,39,4,8),PartPose.offset(side*49,0,-3));
   for(int j=0;j<4;j++)tip.addOrReplaceChild("tip_rib_"+j,box(120+j*9,174,side<0?-37+j*9:j*9,0,4+j*2,3,2,15-j*3),PartPose.ZERO);
   PartDefinition previous=body;
   for(int t=0;t<6;t++){int width=Math.max(3,10-t),len=12-t/2;String key="tail_"+n+"_"+t;PartDefinition section=previous.addOrReplaceChild(key,box(side<0?140:193,140+t*14,-width/2f,-3,-1,width,7-t/2f,len).texOffs(side<0?158:217,140+t*14).addBox(-1,-7,-1,2,5,5),t==0?PartPose.offset(side*8,2,39):PartPose.offset(0,1,10));previous=section;}
  }
  return LayerDefinition.create(mesh,256,256).bakeRoot();
 }
 private MorovoyFireZmeyMesh(){}
}
