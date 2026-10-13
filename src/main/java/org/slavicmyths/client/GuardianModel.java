package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.guardians.*;
/** Independent fire/water meshes with native segment animation, no humanoid fallback. */
public final class GuardianModel<T extends GuardianEntity> extends EntityModel<T> {
 public final ModelPart root,pelvis,torso,head,right,left;private final boolean water;
 public GuardianModel(boolean water){this.water=water;root=water?SpringKeeperMesh.create():FireGuardianMesh.create();pelvis=root.getChild("pelvis");torso=pelvis.getChild("torso");head=torso.getChild("head");right=torso.getChild("arm_right");left=torso.getChild("arm_left");}
 @Override public void setupAnim(T e,float walk,float speed,float time,float yaw,float pitch){
  root.getAllParts().forEach(ModelPart::resetPose);float a=e.age()+(time-(int)time);int s=e.state();float breath=(float)Math.sin(time*.06);head.yRot=Math.clamp(yaw,-55,55)*.017453f;head.xRot=pitch*.017453f*.4f;torso.xRot=breath*.015f;
  for(String tag:new String[]{"left","right"}){float sign=tag.equals("left")?1:-1;pelvis.getChild("leg_"+tag).xRot=(float)Math.cos(walk*.65f)*speed*(water?.3f:.55f)*sign;var arm=torso.getChild("arm_"+tag);arm.xRot=-.04f+(float)Math.cos(walk*.65f)*speed*.18f*-sign;arm.getChild("forearm_"+tag).xRot=-.04f;}
  if(water){for(String side:new String[]{"left","right","back"})head.getChild("hair_"+side).xRot=breath*.025f;for(String side:new String[]{"left","right"}){pelvis.getChild("stream_"+side).y+=breath*.35f;for(int i=0;i<4;i++)pelvis.getChild("skirt_"+side+"_"+i).xRot=breath*.02f;var whip=torso.getChild("arm_"+side).getChild("forearm_"+side).getChild("hand_"+side).getChild("whip_"+side+"_0");whip.visible=s==GuardianRules.WHIP;for(int i=0;i<5;i++){whip.yRot=(float)Math.sin((a-(side.equals("left")?0:10))*.16+i*.4)*.08f;whip.xRot=-.025f;if(i<4)whip=whip.getChild("whip_"+side+"_"+(i+1));}}}
  else {var core=torso.getChild("core");core.xScale=core.yScale=1+breath*.06f;core.zScale=1+breath*.09f;pelvis.getChild("apron_left").xRot=breath*.015f;pelvis.getChild("apron_right").xRot=-breath*.015f;}
  if(s==GuardianRules.IDLE){head.xRot=.3f;if(water)root.y+=2;}if(s==GuardianRules.AWAKEN){float t=Math.clamp(a/60,0,1);head.xRot=.4f*(1-t);right.zRot=-.15f*t;left.zRot=.15f*t;if(water)root.y+=12*(1-t);else pelvis.y+=(float)Math.sin(t*Math.PI)*1.5f;}
  if(s==GuardianRules.MELEE){float t=Math.clamp(a/GuardianRules.MELEE_WINDUP,0,1);var arm=e.pattern()==0?right:left;arm.xRot=a<11?-1.6f*t:.5f*(1-Math.clamp((a-11)/9,0,1));arm.getChild(e.pattern()==0?"forearm_right":"forearm_left").xRot=-.25f;torso.yRot=(e.pattern()==0?1:-1)*.18f;}
  if(s==GuardianRules.PULSE){float t=Math.clamp(a/30,0,1);right.zRot=-t*1.2f;left.zRot=t*1.2f;torso.xRot=.2f*t;if(a>=30){right.zRot=-1.4f;left.zRot=1.4f;torso.xRot=-.1f;}}
  if(s==GuardianRules.SLAM){float t=Math.clamp(a/20,0,1);if(a<20){right.xRot=left.xRot=-2.6f*t;torso.xRot=-.12f*t;}else ground();}
  if(s==GuardianRules.LOCKDOWN)ground();
  if(s==GuardianRules.EXHAUSTED||s==GuardianRules.INTERRUPTED){head.xRot=.45f;torso.xRot=.18f;right.xRot=left.xRot=.08f;}
  if(s==GuardianRules.WHIP){float l=strike(a,16),r=strike(a,26);left.xRot=right.xRot=-.75f;left.yRot=-1.05f+l*2.1f;right.yRot=1.05f-r*2.1f;torso.yRot=(l-r)*.3f;head.getChild("hair_left").yRot=left.yRot*.12f;head.getChild("hair_right").yRot=right.yRot*.12f;}
  if(s==GuardianRules.WHIRL){left.xRot=-1.2f;right.xRot=-.9f;left.zRot=.3f;torso.yRot=(float)Math.sin(a*.08)*.16f;}
  if(s==GuardianRules.PRISON){right.xRot=-1.5f;left.xRot=-1.5f;left.yRot=-.3f;right.yRot=.3f;}
  if(s==GuardianRules.FLOOD){left.xRot=right.xRot=-.8f;left.zRot=.65f;right.zRot=-.65f;torso.yRot=(float)Math.sin(a*.05)*.35f;}
  if(s==GuardianRules.SEEK){head.xRot=.15f;}if(s==GuardianRules.HEAL){right.xRot=left.xRot=-1.4f;right.yRot=.3f;left.yRot=-.3f;head.xRot=.25f;}
  if(s==GuardianRules.PURIFY){head.xRot=.5f;torso.xRot=.18f;if(!water){pelvis.y+=7;pelvis.getChild("leg_right").xRot=-.85f;}else root.y+=2;if(e.scene()){float t=Math.clamp(e.sceneAge()/60f,0,1);head.xRot=.5f*(1-t);}}
  if(s==GuardianRules.CLEANSED){head.xRot=.08f;torso.xRot=breath*.01f;left.xRot=right.xRot=-.04f;}
  if(e.hurtTime>0&&s<GuardianRules.PURIFY)torso.zRot=(float)Math.sin(e.hurtTime*.5)*.06f;
 }
 private float strike(float a,float hit){return a<hit?Math.clamp(a/hit,0,1):Math.max(0,1-(a-hit)/8);}
 private void ground(){pelvis.y+=9;torso.xRot=0;right.xRot=left.xRot=-.25f;right.getChild("forearm_right").xRot=left.getChild("forearm_left").xRot=0;head.xRot=.2f;}
 @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){root.render(p,out,light,overlay,color);}
}
