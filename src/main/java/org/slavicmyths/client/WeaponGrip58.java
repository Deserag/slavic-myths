package org.slavicmyths.client;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;
/** Client pose chains only. No world access, physics, hit timing or renderer rotation. */
final class WeaponGrip58 {
 private static float ease(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
 private static Quaternionf rotation(ModelPart p){return new Quaternionf().rotationZYX(p.zRot,p.yRot,p.xRot);}
 private static void set(ModelPart p,Quaternionf q){q.normalize();
  // Exact Rz*Ry*Rx decomposition used by ModelPart. Keep the full yaw term in X denominator.
  p.xRot=(float)Math.atan2(2*(q.w*q.x+q.y*q.z),1-2*(q.x*q.x+q.y*q.y));
  p.yRot=(float)Math.asin(Mth.clamp(2*(q.w*q.y-q.z*q.x),-1,1));
  p.zRot=(float)Math.atan2(2*(q.w*q.z+q.x*q.y),1-2*(q.y*q.y+q.z*q.z));}
 private static Matrix4f matrix(ModelPart... parts){var m=new Matrix4f();for(var p:parts)m.translate(p.x,p.y,p.z).rotate(rotation(p));return m;}
 private static void upright(ModelPart torso,ModelPart arm,ModelPart fore,ModelPart hand,ModelPart weapon,Quaternionf desired){
  var q=rotation(torso).mul(rotation(arm)).mul(rotation(fore)).mul(rotation(hand));set(weapon,q.invert().mul(desired));
 }
 static void karachun(ModelPart torso,ModelPart right,ModelPart left,int action,float tick,int phase,float motion,float age){
  var fore=right.getChild("forearm_right");var hand=fore.getChild("hand_right");var weapon=hand.getChild("scythe");
  boolean two=phase>=1&&phase<=3&&action>=1&&action<=3;
  float hit=action==3?14:10,prep=ease(tick/(hit-2)),stroke=ease((tick-hit+2)/2),out=1-ease((tick-hit)/10);
  float swing=two?(2*stroke-prep)*out:0;
  if(two){
   torso.yRot=(action==2?-1:1)*.28F*swing;torso.xRot=.13F+(action==3?.12F*stroke*out:0);
   right.xRot=-.45F-.12F*prep*out;right.yRot=-.9F+(action==2?-.12F:.12F)*swing;fore.xRot=-.55F+.12F*stroke*out;
  }else if(action==0||phase>=4){right.xRot=-.12F;right.yRot=0;fore.xRot=-.3F;}
  var desired=new Quaternionf().rotationZYX(two?-.12F*swing:.035F,(action==2?-.8F:.8F)*swing,two?.2F+(.4F*prep-.25F*stroke)*out:-.025F*Math.min(motion,1));
  upright(torso,right,fore,hand,weapon,desired);
  if(two)leftContact(right,left,weapon,-6F);
 }
 private static void leftContact(ModelPart right,ModelPart left,ModelPart weapon,float shaftY){
  var foreR=right.getChild("forearm_right");var handR=foreR.getChild("hand_right");
  var m=matrix(right,foreR,handR,weapon);var target=m.transformPosition(new Vector3f(0,shaftY,0));
  var axis=m.transformDirection(new Vector3f(0,1,0)).normalize();
  // The palm centre is 2.5 below its wrist. Solve the actual 14+10 arm to that wrist.
  var wrist=new Vector3f(target).fma(-2.5F,axis);var shoulder=new Vector3f(left.x,left.y,left.z);var d=new Vector3f(wrist).sub(shoulder);float distance=d.length();
  d.normalize();distance=Math.max(4.01F,Math.min(23.99F,distance));
  float a=(196-100+distance*distance)/(2*distance),h=(float)Math.sqrt(Math.max(0,196-a*a));
  var hint=new Vector3f(1,0,-.35F);hint.fma(-hint.dot(d),d).normalize();var elbow=new Vector3f(shoulder).fma(a,d).fma(h,hint);
  var upper=new Quaternionf().rotationTo(new Vector3f(0,1,0),new Vector3f(elbow).sub(shoulder).normalize());set(left,upper);
  var fore=left.getChild("forearm_left");var lower=new Quaternionf().rotationTo(new Vector3f(0,1,0),new Vector3f(wrist).sub(elbow).normalize());set(fore,new Quaternionf(upper).invert().mul(lower));
  var hand=fore.getChild("hand_left");var weaponRotation=m.getUnnormalizedRotation(new Quaternionf()).normalize();set(hand,new Quaternionf(lower).invert().mul(weaponRotation));
 }
 static void morozko(ModelPart torso,ModelPart right,int action,float tick,int phase,float motion){
  var fore=right.getChild("forearm_right");var hand=fore.getChild("hand_right");var weapon=hand.getChild("staff_root");
  float x=0,z=0;
  if(action==1&&phase<4){float prep=ease(tick/8),strike=ease((tick-8)/2),out=1-ease((tick-10)/8);float a=(strike*1.4F-prep*.7F)*out;
   right.xRot=-.25F-.35F*prep*out;right.yRot=0;right.zRot=0;fore.xRot=-.4F;torso.yRot=-.25F*prep*out+.5F*strike*out;x=(.15F*prep+.65F*strike)*out;z=-.035F*strike*out;
  }else if(action==2&&phase<4){float a=ease(tick/10)*(1-ease((tick-13)/8));right.xRot=-.25F-.25F*a;fore.xRot=-.3F;x=-.45F*a;}
  else if(action>=3&&phase<4){float release=switch(action){case 3->50;case 4->5;case 5->16;case 6->24;case 7->7;default->48;};float a=ease(tick/release);x=-.15F*a;z=-.1F*a;}
  else{right.xRot=-.015F;right.yRot=0;right.zRot=0;fore.xRot=-.12F-.35F*Math.min(1,motion);}
  upright(torso,right,fore,hand,weapon,new Quaternionf().rotationZYX(z,0,x));
 }
 private WeaponGrip58(){}
}
