package org.slavicmyths.morovoy;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Independent three-joint necks, one wing pair and two six-joint tails. Pixel units: 16 = 1 block. */
public final class MorovoyFireZmeyMesh {
 private static CubeListBuilder box(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
 public static ModelPart create(){
  MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot();
  // Chest 36 px = 2.25 blocks wide; the entire trunk is 114 px long (7.125 blocks).
  PartDefinition body=root.addOrReplaceChild("body",box(0,0,-18,-13,-48,36,22,46).texOffs(0,70).addBox(-14,2,-44,28,6,43),PartPose.offset(0,13,0));
  PartDefinition abdomen=body.addOrReplaceChild("abdomen",box(0,122,-14,-11,-4,28,20,42).texOffs(2,185).addBox(-10,4,0,20,5,32),PartPose.offset(0,0,-2));
  PartDefinition pelvis=abdomen.addOrReplaceChild("pelvis",box(94,118,-11,-9,-2,22,17,33),PartPose.offset(0,0,35));
  for(int i=0;i<5;i++){body.addOrReplaceChild("chest_spine_"+i,box(151,2,-2,-20,-43+i*9,4,9,6),PartPose.ZERO);}
  for(int i=0;i<4;i++){abdomen.addOrReplaceChild("abdomen_spine_"+i,box(151,2,-2,-18,2+i*9,4,8,5),PartPose.ZERO);}
  for(int side:new int[]{-1,1}){
   String n=side<0?"left":"right";int base=side<0?100:177;
   // Every neck joint owns an independent pivot; head yaw/pitch are applied to the terminal head bone.
   PartDefinition neck0=body.addOrReplaceChild("neck_"+n+"_0",box(base,0,-6,-6,-23,12,12,26),PartPose.offset(side*10,-5,-39));
   PartDefinition neck1=neck0.addOrReplaceChild("neck_"+n+"_1",box(base+4,45,-5,-5,-22,10,10,24),PartPose.offset(0,-2,-21));
   PartDefinition neck2=neck1.addOrReplaceChild("neck_"+n+"_2",box(base+10,78,-4,-4,-20,8,9,22),PartPose.offset(0,-2,-20));
   PartDefinition head=neck2.addOrReplaceChild("head_"+n,box(base,110,-7,-7,-18,14,12,20).texOffs(base+27,105).addBox(-5,-3,-24,10,7,12),PartPose.offset(0,-2,-19));
   head.addOrReplaceChild("jaw_"+n,box(base,143,-6,0,-24,12,4,23),PartPose.offset(0,4,0));
   for(int horn=0;horn<2;horn++){head.addOrReplaceChild("horn_"+n+"_"+horn,box(145,32,-2,-13,-3,4,12-horn*3,5),PartPose.offset(horn==0?-5:5,0,horn*8));}
   for(int eye=0;eye<2;eye++){head.addOrReplaceChild("eye_"+n+"_"+eye,box(base+42,163,eye==0?-8:6,-3,-15,2,3,4),PartPose.ZERO);}
   if(side<0){head.addOrReplaceChild("mor_vein_left",box(120,180,-6,-8,-15,12,2,15),PartPose.ZERO);}
   else{head.addOrReplaceChild("flame_right",box(195,180,-3,-13,-16,6,7,10).texOffs(219,180).addBox(-1,-17,-12,2,7,5),PartPose.ZERO);head.addOrReplaceChild("flame_core_right",box(230,202,-1,-18,-11,2,7,4),PartPose.ZERO);}
   // Four claws-bearing limbs (forelegs attached to chest; hindlegs to pelvis).
   for(int legIndex=0;legIndex<2;legIndex++){
    PartDefinition parent=legIndex==0?body:pelvis;
    PartDefinition leg=parent.addOrReplaceChild("leg_"+n+"_"+legIndex,box(11,203,-6,-3,-7,12,18,13).texOffs(45,203).addBox(-5,11,-10,10,10,12),PartPose.offset(side*(legIndex==0?15:9),3,legIndex==0?-23:16));
    for(int claw=-1;claw<=1;claw++){leg.addOrReplaceChild("claw_"+claw,box(75,206,claw*4-2,18,-16,3,4,9),PartPose.ZERO);}
   }
   // A single left/right articulated pair, with spars and varying-width membrane ribbons.
   PartDefinition wing=body.addOrReplaceChild("wing_"+n,box(4,86,side<0?-53:0,-4,-10,53,7,16),PartPose.offset(side*17,-9,-11));
   for(int rib=0;rib<5;rib++){wing.addOrReplaceChild("spar_"+rib,box(150,23,side<0?-52+rib*10:rib*10,-3,-9+rib*3,4,4,22-rib*2),PartPose.ZERO);}
   for(int strip=0;strip<5;strip++){wing.addOrReplaceChild("membrane_"+strip,box(2+strip*11,225,side<0?-51+strip*10:strip*10,0,2+strip*3,10,1,20-strip*3),PartPose.ZERO);}
   PartDefinition wingTip=wing.addOrReplaceChild("wing_tip_"+n,box(1,94,side<0?-46:0,-3,-6,46,6,14),PartPose.offset(side*52,0,-6));
   for(int rib=0;rib<4;rib++){wingTip.addOrReplaceChild("tip_rib_"+rib,box(160,40,side<0?-45+rib*10:rib*10,0,7+rib*3,3,3,21-rib*3),PartPose.ZERO);}
   // Separate tail chains originate from the pelvis, not from a common tail bone.
   PartDefinition parent=pelvis;
   for(int section=0;section<6;section++){
    int width=Math.max(3,11-section);int length=19-section;String key="tail_"+n+"_"+section;
    PartDefinition tail=parent.addOrReplaceChild(key,box(side<0?92:176,178+section*9,-width/2f,-width/2f,-1,width,width,length),section==0?PartPose.offset(side*6,1,25):PartPose.offset(0,0,17-(section-1)));
    if(side<0){tail.addOrReplaceChild("mor_spike_"+section,box(129,205,-2,-width/2f-7,2,4,8,7),PartPose.ZERO);}
    else if(section>=3){tail.addOrReplaceChild("fire_flare_"+section,box(213,205,-3,-width/2f-7,1,6,8,8),PartPose.ZERO);}
    if(side>0&&section==5){tail.addOrReplaceChild("flame_tip_right",box(200,220,-5,-6,7,10,10,14),PartPose.ZERO);tail.addOrReplaceChild("flame_tip_core_right",box(215,235,-1,-3,11,2,5,9),PartPose.ZERO);}
    parent=tail;
   }
  }
  return LayerDefinition.create(mesh,256,256).bakeRoot();
 }
 private MorovoyFireZmeyMesh(){}
}
