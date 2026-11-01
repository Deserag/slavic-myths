package org.slavicmyths.morovoy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Dedicated skinned rig. Head aim, neck bends and tail attacks never share a pivot. */
public final class MorovoyFireZmeyModel extends EntityModel<MorovoyFireZmey> {
 private final ModelPart root=MorovoyFireZmeyMesh.create();
 private final ModelPart body=root.getChild("body");
 private final ModelPart abdomen=body.getChild("abdomen");
 private final ModelPart pelvis=abdomen.getChild("pelvis");
 private final ModelPart[] neckRoot={body.getChild("neck_left_0"),body.getChild("neck_right_0")};
 private final ModelPart[] tails={pelvis.getChild("tail_left_0"),pelvis.getChild("tail_right_0")};
 private final ModelPart[] wings={body.getChild("wing_left"),body.getChild("wing_right")};

 @Override public void setupAnim(MorovoyFireZmey boss,float limb,float stride,float time,float yaw,float pitch){
  root.getAllParts().forEach(ModelPart::resetPose);
  boolean sleeping=boss.state()==MorovoyFireZmey.SLEEPING,waking=boss.state()==MorovoyFireZmey.WAKING,dead=boss.state()==MorovoyFireZmey.DYING;
  float wake=waking?Mth.clamp(boss.stateAge()/65f,0,1):sleeping?0:1;
  float active=dead?0:wake;
  float air=boss.flying()?1:0;
  float flap=(float)Math.sin(time*(boss.state()==MorovoyFireZmey.PHASE_II?.32:.24));
  body.y+=dead?Math.min(10,boss.stateAge()*.12f):sleeping?4.8f:(float)Math.sin(time*.065)*.45f;
  body.xRot=dead?.45f:(1-wake)*.20f-air*.11f;
  abdomen.yRot=(1-wake)*1.07f+active*(float)Math.sin(time*.04)*.045f;
  abdomen.xRot=(1-wake)*.18f+active*(float)Math.sin(time*.07)*.034f;
  pelvis.yRot=(1-wake)*1.00f+active*(float)Math.sin(time*.052+.6f)*.075f;
  pelvis.xRot=(1-wake)*.12f+active*(float)Math.sin(time*.06)*.03f;
  for(int side=0;side<2;side++){
   float sign=side==0?-1:1;
   String name=side==0?"left":"right";
   float headYaw=(float)Math.toRadians(side==0?boss.morHeadYaw():boss.fireHeadYaw());
   float headPitch=(float)Math.toRadians(side==0?boss.morHeadPitch():boss.fireHeadPitch());
   ModelPart n0=neckRoot[side],n1=n0.getChild("neck_"+name+"_1"),n2=n1.getChild("neck_"+name+"_2"),head=n2.getChild("head_"+name);
   float drift=(float)Math.sin(time*.055+side*1.83f);
   n0.yRot=sign*(1-wake)*.48f+active*(headYaw*.26f+sign*.17f+drift*.065f);
   n1.yRot=sign*(1-wake)*.21f+active*(headYaw*.27f+(float)Math.sin(time*.074+side*2f)*.09f);
   n2.yRot=sign*(1-wake)*.13f+active*(headYaw*.25f+(float)Math.sin(time*.087+side)*.10f);
   head.yRot=active*headYaw*.22f;
   n0.xRot=(1-wake)*1.02f+active*(headPitch*.26f-.12f+drift*.06f);
   n1.xRot=(1-wake)*.73f+active*(headPitch*.27f-.15f+(float)Math.sin(time*.071+side)*.09f);
   n2.xRot=(1-wake)*.52f+active*(headPitch*.28f-.12f+(float)Math.sin(time*.061+side)*.09f);
   head.xRot=active*headPitch*.19f;
   int action=boss.action(),age=boss.actionAge();
   boolean mouth=side==0?(action==MorovoyFireZmey.MOR_BOLT||action==MorovoyFireZmey.MOR_BREATH||action==MorovoyFireZmey.DOUBLE_BOLT||action==MorovoyFireZmey.BITE_MOR):(action==MorovoyFireZmey.FIRE_BOLT||action==MorovoyFireZmey.FIRE_BREATH||action==MorovoyFireZmey.DOUBLE_BOLT||action==MorovoyFireZmey.BITE_FIRE);
   float open=mouth&&age>=8&&age<=58?(float)Math.sin(Mth.clamp((age-8)/12f,0,1)*Math.PI*.5f)*.62f:.05f;
   head.getChild("jaw_"+name).xRot=open*active;
   ModelPart wing=wings[side];
   wing.zRot=sign*((1-wake)*1.48f+active*(air>0.5f?flap*.59f:.18f));
   wing.yRot=sign*((1-wake)*.60f+active*(air>0.5f?.12f*(float)Math.sin(time*.11f):.15f));
   wing.getChild("wing_tip_"+name).zRot=sign*((1-wake)*.62f+active*(air>0.5f?flap*.29f:.08f));
   wing.getChild("wing_tip_"+name).xRot=active*air*(float)Math.sin(time*.15f+side)*.08f;
   for(int legIndex=0;legIndex<2;legIndex++){
    ModelPart leg=(legIndex==0?body:pelvis).getChild("leg_"+name+"_"+legIndex);
    leg.xRot=(1-wake)*.65f+active*(air*.42f+(float)Math.cos(limb*.64f+side*Math.PI+legIndex*Math.PI)*stride*.30f);
    if(action==MorovoyFireZmey.CLAW&&age>=10&&age<=25&&legIndex==0)leg.xRot-=active*.85f;
   }
   ModelPart tail=tails[side];
   for(int segment=0;segment<6;segment++){
    float tailWave=(float)Math.sin(time*.095f-segment*.51f+side*1.9f)*.14f;
    tail.yRot=sign*(1-wake)*(.50f+segment*.11f)+active*(tailWave+sign*.08f);
    tail.xRot=(1-wake)*(.09f+segment*.05f)+active*(float)Math.sin(time*.07f-segment*.4f+side)*.055f;
    boolean attack=side==0?action==MorovoyFireZmey.MOR_TAIL:action==MorovoyFireZmey.FIRE_TAIL;
    int hitStart=side==0?20:23;
    if(attack&&age>=10&&age<=34){float windup=Mth.clamp((age-10)/10f,0,1);float sweep=Mth.clamp((age-hitStart)/8f,0,1);tail.yRot+=sign*active*(-.50f*windup+1.05f*sweep)*(1f+segment*.08f);}
    if(segment<5)tail=tail.getChild("tail_"+name+"_"+(segment+1));
   }
  }
 }

 @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,int color){
  neckRoot[1].visible=false;tails[1].visible=false;
  try{root.render(pose,buffer,light,overlay,color);}finally{neckRoot[1].visible=true;tails[1].visible=true;}
 }

 /** Fully separate render pass: right neck/head/tail never sample the green Mor atlas. */
 public void renderFireSide(PoseStack pose,VertexConsumer buffer,int light,int overlay,int color){
  pose.pushPose();root.translateAndRotate(pose);body.translateAndRotate(pose);neckRoot[1].render(pose,buffer,light,overlay,color);pose.popPose();
  pose.pushPose();root.translateAndRotate(pose);body.translateAndRotate(pose);abdomen.translateAndRotate(pose);pelvis.translateAndRotate(pose);tails[1].render(pose,buffer,light,overlay,color);pose.popPose();
 }

 public void renderMorAccents(PoseStack pose,VertexConsumer buffer,int light,int overlay,int color){
  pose.pushPose();root.translateAndRotate(pose);body.translateAndRotate(pose);
  ModelPart n0=neckRoot[0],n1=n0.getChild("neck_left_1"),n2=n1.getChild("neck_left_2"),head=n2.getChild("head_left");
  n0.translateAndRotate(pose);n1.translateAndRotate(pose);n2.translateAndRotate(pose);head.translateAndRotate(pose);
  head.getChild("mor_vein_left").render(pose,buffer,light,overlay,color);
  head.getChild("eye_left_0").render(pose,buffer,light,overlay,color);head.getChild("eye_left_1").render(pose,buffer,light,overlay,color);pose.popPose();
  pose.pushPose();root.translateAndRotate(pose);body.translateAndRotate(pose);abdomen.translateAndRotate(pose);pelvis.translateAndRotate(pose);
  ModelPart tail=tails[0];for(int i=0;i<6;i++){pose.pushPose();tail.translateAndRotate(pose);tail.getChild("mor_spike_"+i).render(pose,buffer,light,overlay,color);pose.popPose();tail.translateAndRotate(pose);if(i<5)tail=tail.getChild("tail_left_"+(i+1));}
  pose.popPose();
 }

 public void renderFireAccents(PoseStack pose,VertexConsumer buffer,int light,int overlay,int color,boolean whiteCore){
  pose.pushPose();root.translateAndRotate(pose);body.translateAndRotate(pose);
  ModelPart n0=neckRoot[1],n1=n0.getChild("neck_right_1"),n2=n1.getChild("neck_right_2"),head=n2.getChild("head_right");
  n0.translateAndRotate(pose);n1.translateAndRotate(pose);n2.translateAndRotate(pose);head.translateAndRotate(pose);
  head.getChild(whiteCore?"flame_core_right":"flame_right").render(pose,buffer,light,overlay,color);
  if(whiteCore){head.getChild("eye_right_0").render(pose,buffer,light,overlay,color);head.getChild("eye_right_1").render(pose,buffer,light,overlay,color);}
  pose.popPose();
  pose.pushPose();root.translateAndRotate(pose);body.translateAndRotate(pose);abdomen.translateAndRotate(pose);pelvis.translateAndRotate(pose);
  ModelPart tail=tails[1];for(int i=0;i<6;i++){pose.pushPose();tail.translateAndRotate(pose);if(i>=3&&!whiteCore)tail.getChild("fire_flare_"+i).render(pose,buffer,light,overlay,color);if(i==5)tail.getChild(whiteCore?"flame_tip_core_right":"flame_tip_right").render(pose,buffer,light,overlay,color);pose.popPose();tail.translateAndRotate(pose);if(i<5)tail=tail.getChild("tail_right_"+(i+1));}
  pose.popPose();
 }
}
