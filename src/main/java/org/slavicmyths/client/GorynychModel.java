package org.slavicmyths.client;
import org.slavicmyths.gorynych.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
public final class GorynychModel extends EntityModel<Gorynych> {
 public final ModelPart root=GorynychMesh.create();private final ModelPart body=root.getChild("body");
 @Override public void setupAnim(Gorynych e,float limb,float amount,float time,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);boolean exhausted=e.state()>=GorynychRules.EXHAUSTED&&e.state()<GorynychRules.PURIFIED;body.y+=(float)Math.sin(time*.04)*.6f;body.xRot=e.flying()?-.12f:0;
  for(String side:new String[]{"left","right"})for(String place:new String[]{"front","back"}){var leg=body.getChild("leg_"+place+"_"+side);leg.xRot=e.flying()?.6f:(float)Math.cos(limb*.55+(side.equals("left")^place.equals("back")?Math.PI:0))*amount*.4f;}
  var heads=e.heads();for(int h=0;h<3;h++){var neck=body.getChild("neck_"+h+"_0");neck.yRot=exhausted?0:(float)Math.toRadians(Math.clamp(heads.getFloat("Yaw"+h),-55,55));neck.xRot=exhausted?1.3f:e.ducking()?.8f:(float)Math.sin(time*.04+h)*.035f;for(int i=1;i<5;i++){neck=neck.getChild("neck_"+h+"_"+i);neck.xRot=exhausted||e.ducking()?0:(float)Math.toRadians(heads.getFloat("Pitch"+h)*.18);neck.yRot=0;}var head=neck.getChild("head_"+h);var jaw=head.getChild("jaw_"+h);int action=heads.getInt("Action"+h),age=heads.getInt("Age"+h);jaw.xRot=exhausted?.08f:action>0?Math.min(.65f,age*.022f):.05f+(float)Math.sin(time*.05+h)*.02f;if(h==2&&action>0)head.z-=Math.sin(Math.min(1,age/20.)*Math.PI)*7;}
  var tail=body.getChild("tail_0");for(int i=0;i<8;i++){tail.yRot=(e.ducking()?.35f:0)+(float)Math.sin(time*.065-i*.5)*.07f+(e.bodyAction()==1?(float)Math.sin(time*.18)*.16f:0);if(i<7)tail=tail.getChild("tail_"+(i+1));}
  for(String name:new String[]{"wing_left","wing_right"}){var wing=body.getChild(name);wing.visible=e.wings();float sign=name.endsWith("left")?-1:1;wing.yRot=e.ducking()?-sign*1.2f:0;float unfold=e.state()==GorynychRules.TRANSFORM?Math.clamp((e.age()-40)/80f,0,1):1;wing.zRot=sign*(1-unfold)*1.4f+sign*(e.flying()?(float)Math.sin(time*.15)*.4f:exhausted?.5f:.16f);if(e.bodyAction()==5)wing.zRot+=sign*(float)Math.sin(time*.28)*.7f;}
 }
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color){root.render(pose,vertices,light,overlay,color);}
}
