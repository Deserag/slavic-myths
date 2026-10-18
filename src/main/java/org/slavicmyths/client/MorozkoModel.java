package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.morozko.*;
public final class MorozkoModel extends EntityModel<Morozko> {
 public final ModelPart root=MorozkoMesh.create(),pelvis=root.getChild("pelvis"),torso=pelvis.getChild("torso"),head=torso.getChild("head"),right=torso.getChild("upper_arm_right"),left=torso.getChild("upper_arm_left");
 @Override public void setupAnim(Morozko e,float walk,float speed,float time,float yaw,float pitch){sample(e.action(),e.age(),e.phase(),e.tickCount,e.hurtTime,e.deathTime,e instanceof IceDouble,walk,speed,time,yaw,pitch);}
 public void sample(int action,float age,int phase,int tickCount,int hurtTicks,int deathTicks,boolean iceDouble,float walk,float speed,float time,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);float breathe=(float)Math.sin(time*Math.PI/40)*.009f;torso.xRot=breathe;head.yRot=yaw*.0174533f;head.xRot=pitch*.0174533f;head.getChild("beard_main").xRot=breathe*2;head.getChild("beard_main").getChild("beard_tips").xRot=breathe*3;
 var crystal=right.getChild("forearm_right").getChild("hand_right").getChild("staff_root").getChild("staff_root_head").getChild("staff_root_crystals");crystal.yRot=breathe*.6f;
 for(String side:new String[]{"left","right"}){float sign=side.equals("left")?1:-1;pelvis.getChild("leg_"+side).xRot=(float)Math.cos(walk*.55)*speed*.55f*sign;pelvis.getChild("coat_front_"+side).xRot=(float)Math.cos(walk*.55)*speed*.1f*sign;pelvis.getChild("coat_tail_"+side).xRot=-breathe*2;torso.getChild("shoulder_fur_"+side).zRot=breathe*sign;}
 right.getChild("forearm_right").xRot=-.12f;left.xRot=(float)Math.cos(walk*.55)*speed*.12f;float t=age/20f;int a=action;
 if(a==1){float wind=Math.min(1,t/.52f);right.zRot=-.45f;right.xRot=-.7f;right.yRot=-1.2f+wind*2.4f;torso.yRot=-.3f+wind*.6f;right.getChild("forearm_right").xRot=-.4f;}
 if(a==2){left.xRot=-1.25f;left.getChild("forearm_left").xRot=-.25f;right.xRot=-.25f;}
 if(a==3||a==7||a==8){float lift=Math.min(1,t/(a==7?1.6f:2.4f));right.xRot=-lift*2.6f;left.xRot=-lift*2.1f;left.zRot=lift*.3f;right.zRot=-lift*.25f;head.xRot=-lift*.2f;}
 if(a==4||a==6){float stroke=(float)Math.sin(Math.min(1,t/(a==4?.9f:1.2f))*Math.PI);right.xRot=-stroke*.9f;left.xRot=-stroke*.6f;torso.xRot=stroke*.18f;}
 if(a==5){head.xRot=t<.8f?-.35f:.25f;torso.xRot=t<.8f?-.06f:.12f;left.xRot=-.45f;}
 if(hurtTicks>0){torso.zRot=(float)Math.sin(hurtTicks*.5)*.03f;head.xRot-=.05f;}
 var heart=left.getChild("forearm_left").getChild("hand_left").getChild("yield_heart");heart.visible=phase==4||phase==5;
 if(phase>=4){head.xRot=.3f;torso.xRot=.15f;left.xRot=-.8f;right.xRot=-.2f;left.getChild("forearm_left").xRot=-.8f;}
 if(phase==6){float dissolve=Math.max(.05f,1-age/(iceDouble?12f:26f));root.yScale=dissolve;root.xScale=dissolve;root.zScale=dissolve;}
 if(phase==0&&iceDouble){root.yScale=Math.max(.05f,Math.min(1,age/33f));}
 if(iceDouble&&deathTicks>0){root.xScale=root.zScale=Math.max(.01f,1-deathTicks/11f);}
 WeaponGrip58.morozko(torso,right,action,age,phase,speed);
 }
 @Override public void renderToBuffer(PoseStack p,VertexConsumer b,int light,int overlay,int color){root.render(p,b,light,overlay,color);}
}
