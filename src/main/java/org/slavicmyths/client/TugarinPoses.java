package org.slavicmyths.client;

import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.hunt.BossRules.Move;

/** Client poses only. All clocks and damage events remain owned by TugarinCombat. */
final class TugarinPoses {
 private static float ease(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
 private static float sin(float x){return Mth.sin(x);}
 private static void rx(Map<String,ModelPart> p,String n,float a){if(p.containsKey(n))p.get(n).xRot+=a;}
 private static void rz(Map<String,ModelPart> p,String n,float a){if(p.containsKey(n))p.get(n).zRot+=a;}
 private static void legs(Map<String,ModelPart> p,float bend,float gait,float strength,float spread){
  float hipHeight=13*Mth.cos(-bend)+7*Mth.cos(bend)+4;
  p.get("pelvis").y=-hipHeight;
  for(String side:new String[]{"L","R"}){
   float sign=side.equals("L")?1:-1,a=sin(gait+(sign>0?0:Mth.PI))*strength;
   var leg=p.get("leg"+side);var knee=p.get("knee"+side);var ankle=p.get("ankle"+side);
   leg.x=sign*(10+spread);leg.xRot=-bend+a*.4F;knee.xRot=2*bend+Math.max(0,a)*.3F;
   ankle.xRot=-(leg.xRot+knee.xRot);
   leg.y=hipHeight-13*Mth.cos(leg.xRot)-7*Mth.cos(leg.xRot+knee.xRot)-4-Math.max(0,a)*2.4F;
   leg.yRot=sin(gait+(sign>0?0:Mth.PI))*.15F*strength;
  }
 }
 private static void slash(Map<String,ModelPart> p,String side,float t,float hit){
  float prep=ease(t/(hit-3)),cut=ease((t-hit+3)/3),settle=ease((t-hit)/8),a=1-settle;
  float sign=side.equals("R")?1:-1;
  p.get("body").yRot+=sign*(-.55F*prep+1.05F*cut)*a;
  rx(p,"body",.11F*cut*a);rx(p,"arm"+side,(-1.35F*prep+.8F*cut)*a);
  rz(p,"arm"+side,sign*(.22F*prep+.38F*cut)*a);
  rx(p,"fore"+side,(-1.15F*prep+.65F*cut)*a);
  p.get("wrist"+side).yRot+=sign*(-.3F*prep+.7F*cut)*a;
  rx(p,"wrist"+side,-.15F*prep*a);rx(p,"head",.08F*cut*a);
 }
 // Continuous angular velocity through hit1/2/3; final revolution brakes to rest.
 private static float hermite(float u,float a,float b,float va,float vb,float length){
  u=Mth.clamp(u,0,1);float u2=u*u,u3=u2*u;
  return (2*u3-3*u2+1)*a+(u3-2*u2+u)*va*length+(-2*u3+3*u2)*b+(u3-u2)*vb*length;
 }
 private static float spinYaw(float t){
  if(t<=8)return hermite(t/8,0,-.3F,0,-.07F,8);
  if(t<=12)return hermite((t-8)/4,-.3F,-.62F,-.07F,0,4);
  if(t<=16)return hermite((t-12)/4,-.62F,0,0,.35F,4);
  if(t<=28)return hermite((t-16)/12,0,Mth.TWO_PI,.35F,.58F,12);
  if(t<=40)return hermite((t-28)/12,Mth.TWO_PI,2*Mth.TWO_PI,.58F,.58F,12);
  return hermite((t-40)/12,2*Mth.TWO_PI,3*Mth.TWO_PI,.58F,0,12);
 }
 // Two-link legs solve actual floor-space boot targets. The stance boot stays
 // planted while the pelvis turns; only the other boot travels around the axis.
 private static void plant(Map<String,ModelPart> p,String side,float wx,float wz,float lift,float yaw){
  var pelvis=p.get("pelvis");var leg=p.get("leg"+side);var knee=p.get("knee"+side);var ankle=p.get("ankle"+side);
  float c=Mth.cos(pelvis.yRot),s=Mth.sin(pelvis.yRot);
  float dx=c*(wx-pelvis.x)-s*wz-leg.x,dz=s*(wx-pelvis.x)+c*wz;
  float dy=-pelvis.y-4-lift,r=(float)Math.sqrt(dx*dx+dy*dy);
  float d=Mth.clamp((float)Math.sqrt(r*r+dz*dz),6.01F,19.999F);
  float alpha=(float)Math.acos(Mth.clamp((13*13+d*d-7*7)/(26*d),-1,1));
  leg.y=0;leg.yRot=0;leg.zRot=-(float)Math.atan2(dx,dy);
  leg.xRot=(float)Math.atan2(dz,r)-alpha;
  knee.xRot=(float)Math.acos(Mth.clamp((d*d-13*13-7*7)/(2*13F*7),-1,1));
  var flat=new Quaternionf().rotationZYX(leg.zRot,0,leg.xRot+knee.xRot).invert().rotateY(yaw-pelvis.yRot).normalize();
  // Explicit Rz*Ry*Rx decomposition, matching ModelPart.translateAndRotate.
  ankle.xRot=(float)Math.atan2(2*(flat.w*flat.x+flat.y*flat.z),1-2*(flat.x*flat.x+flat.y*flat.y));
  ankle.yRot=(float)Math.asin(Mth.clamp(2*(flat.w*flat.y-flat.z*flat.x),-1,1));
  ankle.zRot=(float)Math.atan2(2*(flat.w*flat.z+flat.x*flat.y),1-2*(flat.y*flat.y+flat.z*flat.z));
 }
 private static void attackFeet(Map<String,ModelPart> p,float t,float hit,int strokes){
  float left=0,right=0,liftL=0,liftR=0;
  for(int i=0;i<strokes;i++){
   float clock=t-i*12;if(clock<0)continue;
   float weight=ease(clock/(hit-2))*(1-ease((clock-hit)/8));
   float lift=2.1F*sin(Mth.PI*ease(clock/(hit-3)));
   if(i==1){left+=weight;liftL=Math.max(liftL,lift);}else{right+=weight;liftR=Math.max(liftR,lift);}
  }
  left=Math.min(1,left);right=Math.min(1,right);
  p.get("pelvis").y=-(24-2.6F*Math.max(left,right));
  p.get("pelvis").x=1.1F*(left-right);
  plant(p,"L",10+ease(t/8),-4*left+1.5F*right,liftL,0);
  plant(p,"R",-10-ease(t/8),-4*right+1.5F*left,liftR,0);
 }
 private static void spin(Map<String,ModelPart> p,float t,float recovery){
  t=Mth.clamp(t,0,52);recovery=Mth.clamp(recovery,0,24);
  float prep=ease(t/12),fade=1-ease(recovery/24),clock=Math.max(0,t-16);
  float phase=clock*Mth.TWO_PI/12,brake=ease((t-40)/12),active=ease((t-12)/4);
  var pelvis=p.get("pelvis");pelvis.yRot=spinYaw(t);
  pelvis.x=.65F*sin(phase)*active*(1-brake)*fade;
  pelvis.y=-(24-5.5F*prep*fade);
  for(String side:new String[]{"L","R"}){
   float sign=side.equals("L")?1:-1,offset=sign>0?0:3;
   float wx,wz,lift=0,footYaw;
   if(t<16){
    // Wide brace first, then cross-step into the central pivot one foot at a time.
    float step=side.equals("L")?(t<8?ease(t/8):ease((t-12)/4)):ease((t-8)/4);
    wx=side.equals("L")?(t<8?Mth.lerp(step,10,12):Mth.lerp(step,12,3)):-Mth.lerp(step,10,3);
    lift=2.1F*sin(Mth.PI*step);wz=0;footYaw=sign*.12F*sin(Mth.PI*ease(t/16));
   }
   else if(recovery>0){
    float step=ease((recovery-(sign>0?0:12))/12);
    float endAngle=spinYaw(sign>0?52:49);
    wx=Mth.lerp(step,sign*3*Mth.cos(endAngle),sign*10);wz=Mth.lerp(step,-sign*3*Mth.sin(endAngle),0);footYaw=Mth.lerp(step,endAngle,3*Mth.TWO_PI);
    lift=2.2F*sin(Mth.PI*step);
   }else{
    float cycle=(clock-offset)/6,whole=(float)Math.floor(cycle),within=(cycle-whole)*6;
    float plantTime=16+offset+whole*6,nextTime=plantTime+6;
    float oldAngle=spinYaw(Math.max(16,plantTime)),nextAngle=spinYaw(Math.min(52,nextTime));
    float travel=ease((within-3)/3),angle=Mth.lerp(travel,oldAngle,nextAngle);
    float radius=3+5*sin(Mth.PI*travel);
    wx=sign*radius*Mth.cos(angle);wz=-sign*radius*Mth.sin(angle);
    lift=2.3F*sin(Mth.PI*travel);footYaw=angle;
   }
   plant(p,side,wx,wz,lift,footYaw);
   float hand=side.equals("R")?1:-1,arc=sin(phase+(hand>0?0:Mth.PI));
   float drive=active*(1-.45F*brake)*fade;
   p.get("arm"+side).yRot=hand*(-.45F*prep+.7F*arc*active)*fade;
   rx(p,"arm"+side,(-.75F*prep+(hand>0?-.35F:.18F)*prep-.4F*arc*active)*fade);
   rz(p,"arm"+side,hand*(.28F*prep+.35F*(.5F+.5F*arc)*active)*fade);
   rx(p,"fore"+side,-(1.05F*prep-.42F*arc*active+.2F*brake)*fade);
   p.get("fore"+side).yRot=hand*.12F*arc*drive;
   rx(p,"wrist"+side,(-.18F*prep+.24F*arc*active)*fade);
   p.get("wrist"+side).yRot=hand*(.2F*arc*drive-.12F*brake*fade);
   rz(p,"moustache"+side,hand*.1F*prep*fade);
   rx(p,"moustache"+side,(.09F*sin(phase-.8F)*active+.07F*sin(recovery*.35F))*fade);
  }
  p.get("body").yRot=(-.55F*prep*(1-active)+.32F*sin(phase-.7F)*active*(1-brake))*fade;
  rx(p,"body",(.13F*prep+.12F*active+.1F*brake)*fade);
  p.get("body").zRot=.12F*sin(phase-.5F)*active*(1-brake)*fade;
  p.get("head").yRot-=p.get("body").yRot*.6F;
  rx(p,"head",-.09F*prep*fade);
 }
 static void sample(Map<String,ModelPart> p,boolean serpent,Move m,float t,Move previous,int hurt,int death,float walk,float amount,float age){
  float breathing=sin(age*.07F);
  if(serpent){serpent(p,m,t,previous,breathing,age);}
  else{
   legs(p,.03F,walk*.65F,Math.min(1,amount*2),0);
   p.get("belly").zScale=1+breathing*.012F;rx(p,"body",breathing*.012F);
   for(String side:new String[]{"L","R"}){rx(p,"fore"+side,-.2F);rz(p,"wrist"+side,(side.equals("L")?1:-1)*.12F);rx(p,"moustache"+side,breathing*.025F);}
   switch(m){
    case SPIN -> spin(p,t,0);
    case RECOVERY -> {if(previous==Move.SPIN)spin(p,52,t);else if(previous==Move.CHARGE){float a=1-ease(t/24);legs(p,.18F*a,t*.4F,.3F*a,0);rx(p,"body",.32F*a);rx(p,"head",-.1F*a);rx(p,"armL",-.65F*a);rx(p,"foreL",-.65F*a);rx(p,"foreR",-.8F*a);}}
    case BLADE_COMBO -> {int stage=Math.min(2,(int)(t/12));for(int stroke=0;stroke<3;stroke++)if(t>=stroke*12)slash(p,stroke==1?"L":"R",t-stroke*12,10);attackFeet(p,t,10,3);}
    case SWEEP -> {slash(p,"R",t,14);attackFeet(p,t,14,1);}
    case PUNCH -> {float prep=ease(t/7),hit=ease((t-7)/3),back=1-ease((t-10)/10);attackFeet(p,t,10,1);p.get("body").yRot=(-.45F*prep+.8F*hit)*back;rx(p,"armR",(-1.25F*prep+.3F*hit)*back);rx(p,"foreR",(-1.2F*prep+1.05F*hit)*back);rx(p,"armL",-.3F*prep*back);}
    case CHARGE -> {float a=ease(t/10);legs(p,.18F*a,t<10?t*.16F:1.6F+(t-10)*1.1F,Mth.lerp(ease((t-10)/3),.25F,Math.max(.6F,Math.min(1,amount*2)))*a,0);rx(p,"body",.32F*a);rx(p,"head",-.1F*a);rx(p,"armL",-.65F*a);rx(p,"foreL",-.65F*a);rx(p,"foreR",-.8F*a);}
    case STONE -> {float prep=ease(t/14),throwing=ease((t-14)/4),back=1-ease((t-18)/10);legs(p,.2F*prep*back,0,0,.7F);p.get("body").yRot=(-.4F*prep+.7F*throwing)*back;rx(p,"armR",(-2.5F*prep+2.6F*throwing)*back);rx(p,"foreR",-.8F*prep*(1-throwing));}
    case SLAM -> {float prep=ease(t/10),drop=ease((t-11)/3),back=1-ease((t-14)/10);legs(p,(.12F+.18F*drop)*back,0,0,1);rx(p,"body",(-.1F*prep+.45F*drop)*back);for(String side:new String[]{"L","R"}){rx(p,"arm"+side,(-2.3F*prep+2.1F*drop)*back);rx(p,"fore"+side,-.6F*prep*back);}}
    case TRANSITION -> {float brace=ease(t/20),change=ease((t-20)/10);legs(p,.28F*brace,0,0,1.5F*brace);rx(p,"body",.16F*brace-.3F*change);p.get("belly").xScale=1+.035F*brace;p.get("body").xScale=1-.1F*change;rx(p,"head",-.25F*change);rx(p,"armL",-.65F*brace);rx(p,"foreL",-.85F*brace);rx(p,"armR",-.45F*brace);rx(p,"foreR",-.8F*brace);}
    default -> {}
   }
  }
  if(hurt>0){rx(p,"body",-.12F*hurt/10);rx(p,"head",-.15F*hurt/10);}
  if(serpent&&death==0)alignMouth(p,breathWeight(m,t,previous));
  if(death>0){float a=ease(death/20F);p.get("root").zRot=.9F*a;p.get("root").y-=5*a;rx(p,"body",.3F*a);}
 }
 private static void serpent(Map<String,ModelPart> p,Move m,float t,Move previous,float breathing,float age){
  p.get("root").yScale=.94F;
  rx(p,"neck",breathing*.04F);rx(p,"neckUpper",sin(age*.045F-.5F)*.035F);
  for(int i=0;i<4;i++)p.get("tail"+i).yRot+=sin(age*.06F-i*.6F)*.09F;
  float open=.12F+breathing*.025F,flap=0;
  if(m==Move.TRANSITION){float growth=ease((t-30)/30);p.get("body").yScale=.72F+.28F*growth;p.get("neck").yScale=.7F+.3F*ease((t-30)/15);open=.08F+.97F*ease((t-30)/10)-.13F*ease((t-40)/20);rx(p,"head",-.2F*(1-growth));}
  if(m==Move.WING_DIVE){float takeoff=ease(t/8);open=t<8?.12F+.83F*takeoff:t<20?.95F:.72F-.12F*ease((t-20)/16);flap=t<20?sin((t-8)*.65F)*.18F:0;rx(p,"body",t<8?-.2F*takeoff:t<20?-.12F:.5F*ease((t-20)/8));rx(p,"legL",-.4F*takeoff);rx(p,"legR",-.4F*takeoff);}
  if(m==Move.RECOVERY&&previous==Move.WING_DIVE){float a=1-ease(t/25);open=.12F+.55F*a;rx(p,"body",.5F*a);rx(p,"legL",-.4F*a);rx(p,"legR",-.4F*a);rx(p,"neck",-.08F*sin(Mth.PI*ease(t/25)));}
  for(String side:new String[]{"L","R"}){float sign=side.equals("L")?1:-1;rz(p,"wing"+side,-sign*(open+flap));p.get("wing"+side).yRot=-sign*1.35F*(1-Math.min(1,open));rz(p,"wingLowerJoint"+side,sign*(.2F*(1-open)-flap*.5F));}
  if(m==Move.BREATH||m==Move.RECOVERY&&previous==Move.BREATH){
   float weight=breathWeight(m,t,previous);
   rx(p,"neck",-.16F*weight);p.get("head").xRot=Mth.lerp(weight,p.get("head").xRot,-.08F);p.get("head").yRot*=1-weight;
   rx(p,"jaw",.5F*weight);p.get("body").zScale=1+.025F*weight;
  }
  if(m==Move.SERPENT_LUNGE){float a=ease(t/8)*(1-ease((t-12)/6));rx(p,"body",.4F*a);rx(p,"armR",-1.2F*a);rx(p,"head",.15F*a);rx(p,"jaw",.35F*a);}
  if(m==Move.SMOKE_BURST){float a=ease(t/14)*(1-ease((t-14)/10));rx(p,"armL",-.8F*a);rx(p,"armR",-1.1F*a);rz(p,"wingL",-.4F*a);rz(p,"wingR",.4F*a);rx(p,"jaw",.25F*a);}
 }
 private static float breathWeight(Move move,float t,Move previous){
  if(move!=Move.BREATH&&(move!=Move.RECOVERY||previous!=Move.BREATH))return 0;
  float clock=move==Move.BREATH?t:42+t;return ease(clock/18)*(1-ease((clock-42)/18));
 }
 // WorldBoss eye height 4.15, TugarinCombat flame origin eyeY-0.2 / heading*1.
 // Bounded client transform chain; the server origin/ray and damage are unchanged.
 private static void alignMouth(Map<String,ModelPart> p,float weight){
  if(weight<=0)return;
  var pose=new PoseStack();p.get("root").translateAndRotate(pose);p.get("body").translateAndRotate(pose);
  var inverse=new Matrix4f(pose.last().pose()).invert();
  for(String name:new String[]{"neck","neckUpper","head","mouthGlow"})p.get(name).translateAndRotate(pose);
  var mouth=pose.last().pose().transformPosition(new Vector3f(0,4.5F/16,-23.75F/16)).mul(16);
  var delta=inverse.transformDirection(new Vector3f(0,24-3.95F*16,-16).sub(mouth));
  p.get("neck").x+=delta.x*weight;p.get("neck").y+=delta.y*weight;p.get("neck").z+=delta.z*weight;
 }
 private TugarinPoses(){}
}
