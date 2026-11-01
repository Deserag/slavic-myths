package org.slavicmyths.morovoy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Poses are driven by synchronized server action ages, not client-authoritative combat. */
public final class MorovoyFireZmeyModel extends EntityModel<MorovoyFireZmey> {
 private final ModelPart root=MorovoyFireZmeyMesh.create();
 private final ModelPart body=root.getChild("body");
 @Override public void setupAnim(MorovoyFireZmey boss,float limb,float amount,float time,float yaw,float pitch){
  root.getAllParts().forEach(ModelPart::resetPose);
  boolean asleep=boss.state()==MorovoyFireZmey.SLEEPING,waking=boss.state()==MorovoyFireZmey.WAKING,dead=boss.state()==MorovoyFireZmey.DYING;
  float wake=Mth.clamp(boss.stateAge()/65f,0,1),flight=boss.flying()?1:0;
  body.y+=asleep?5:dead?Math.min(9,boss.stateAge()*.12f):(float)Math.sin(time*.05)*.5f;
  body.xRot=asleep?.25f:dead?.35f:flight*-.12f;
  for(String side:new String[]{"left","right"}){
   float flip=side.equals("left")?-1:1;
   ModelPart n0=body.getChild("neck_"+side+"_0"),n1=n0.getChild("neck_"+side+"_1"),head=n1.getChild("head_"+side);
   n0.xRot=asleep?.83f:waking?.83f*(1-wake):dead?.8f:-.08f+(float)Math.sin(time*.08+(flip<0?0:2))*.09f;
   n0.yRot=asleep?flip*.45f:flip*.14f+(float)Math.sin(time*.045+(flip<0?0:2))*.1f;
   n1.xRot=asleep?.55f:waking?.55f*(1-wake):dead?.5f:-.05f+(float)Math.sin(time*.06+(flip<0?1:0))*.12f;
   head.yRot=(float)Math.sin(time*.07+(flip<0?0:1.5))*.08f;
   int a=boss.action(),age=boss.actionAge();boolean attacking=side.equals("left")?(a==MorovoyFireZmey.MOR_BOLT||a==MorovoyFireZmey.MOR_BREATH||a==MorovoyFireZmey.DOUBLE_BOLT||a==MorovoyFireZmey.BITE_MOR):(a==MorovoyFireZmey.FIRE_BOLT||a==MorovoyFireZmey.FIRE_BREATH||a==MorovoyFireZmey.DOUBLE_BOLT||a==MorovoyFireZmey.BITE_FIRE);
   head.getChild("jaw_"+side).xRot=attacking&&age>=8&&age<58?.5f+(float)Math.sin(age*.2)*.12f:.05f;
   ModelPart wing=body.getChild("wing_"+side),tip=wing.getChild("wing_tip_"+side);
   float flap=(float)Math.sin(time*(boss.state()==MorovoyFireZmey.PHASE_II?.24:.18)+(flip<0?0:.32));
   wing.zRot=flip*(asleep?1.35f:waking?1.35f*(1-wake)+.2f:dead?1.6f:(boss.flying()?flap*.52f:.15f));
   wing.yRot=flip*(asleep?.7f:boss.flying()?(float)Math.sin(time*.1)*.17f:.15f);
   tip.zRot=flip*(asleep?.55f:boss.flying()?flap*.28f:0);
   for(int i=0;i<2;i++){ModelPart leg=body.getChild("leg_"+side+"_"+i);leg.xRot=asleep?.5f:flight*.38f+(float)Math.cos(limb*.6f+(flip<0?0:Math.PI)+(i==0?0:Math.PI))*amount*.33f;if(boss.action()==MorovoyFireZmey.CLAW&&age>=8&&age<=26&&i==0)leg.xRot-=.9f;}
   ModelPart tail=body.getChild("tail_"+side+"_0");for(int i=0;i<6;i++){tail.yRot=flip*(asleep?.3f:0)+(float)Math.sin(time*.07-i*.5+(flip<0?0:1.8))*.1f;boolean hit=side.equals("left")?a==MorovoyFireZmey.MOR_TAIL:a==MorovoyFireZmey.FIRE_TAIL;if(hit&&age>=12&&age<=29)tail.yRot+=flip*(float)Math.sin((age-12)/17f*Math.PI)*(.3f+i*.08f);if(i<5)tail=tail.getChild("tail_"+side+"_"+(i+1));}
  }
 }
 @Override public void renderToBuffer(PoseStack matrices,VertexConsumer out,int light,int overlay,int color){root.render(matrices,out,light,overlay,color);}
}
