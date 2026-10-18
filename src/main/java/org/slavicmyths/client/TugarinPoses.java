package org.slavicmyths.client;

import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
 private static void spin(Map<String,ModelPart> p,float t,float recovery){
  t=Mth.clamp(t,0,52);recovery=Mth.clamp(recovery,0,24);
  float prep=ease(t/8),active=Mth.clamp((t-16)/36,0,1),clock=Math.max(0,t-16);
  float fade=1-ease(recovery/24),finish=ease((t-40)/12);
  float turns=t<16?-.4F*ease((t-8)/7)*(1-ease(t-15)):
   t<40?(t-16)/12*Mth.TWO_PI:(2+(t-40)/12+sin(Mth.PI*(t-40)/12)/Mth.PI)*Mth.TWO_PI;
  p.get("pelvis").yRot=turns;
  p.get("body").yRot=(-.55F*prep*(1-ease((t-15)/2))+.24F*sin(clock*Mth.TWO_PI/12))*(1-finish)*fade;
  rx(p,"body",(.1F+.08F*active)*prep*fade);p.get("body").zRot=.08F*sin(clock*Mth.TWO_PI/12)*fade*(1-finish);
  float step=clock*Mth.TWO_PI/6+recovery*.3F;
  legs(p,.22F*prep*fade,step,.85F*ease((t-15)/2)*(1-finish)*fade+.22F*sin(recovery*Mth.PI/24),1.3F*prep*fade);
  for(String side:new String[]{"L","R"}){
   float sign=side.equals("R")?1:-1,phase=clock*Mth.TWO_PI/12+(sign>0?0:Mth.PI);
   float arc=sin(phase),drive=(1-finish)*fade;
   rx(p,"arm"+side,(-.65F*prep+(sign>0?-.35F-.25F*active:.12F+.2F*active)*prep+.28F*arc*ease((t-15)/2))*fade);
   rz(p,"arm"+side,sign*(.32F+.16F*arc)*prep*drive);
   rx(p,"fore"+side,-(.7F+(sign>0?.2F:0)+.18F*arc+.2F*active)*prep*fade);
   rx(p,"wrist"+side,-.12F*prep*fade);p.get("wrist"+side).yRot=sign*.3F*arc*drive;
   rz(p,"moustache"+side,sign*.13F*prep*fade);rx(p,"moustache"+side,.07F*sin(recovery*.35F)*fade);
  }
  p.get("head").yRot-=p.get("body").yRot*.35F;
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
    case RECOVERY -> {if(previous==Move.SPIN)spin(p,52,t);else{float a=1-ease(t/24);legs(p,.2F*a,t*.4F,.25F*a,1.2F*a);rx(p,"body",.18F*a);rx(p,"foreR",-.5F*a);}}
    case BLADE_COMBO -> {int stage=Math.min(2,(int)(t/12));for(int stroke=0;stroke<3;stroke++)if(t>=stroke*12)slash(p,stroke==1?"L":"R",t-stroke*12,10);legs(p,.15F,stage*Mth.PI,.25F,1);}
    case SWEEP -> {slash(p,"R",t,14);legs(p,.18F,0,0,1.2F);}
    case PUNCH -> {float prep=ease(t/7),hit=ease((t-7)/3),back=1-ease((t-10)/10);legs(p,.2F*prep*back,0,0,1);p.get("body").yRot=(-.45F*prep+.8F*hit)*back;rx(p,"armR",(-1.25F*prep+.3F*hit)*back);rx(p,"foreR",(-1.2F*prep+1.05F*hit)*back);rx(p,"armL",-.3F*prep*back);}
    case CHARGE -> {float a=ease(t/10);legs(p,.18F*a,walk*1.1F,Math.min(1,amount*2)*a,0);rx(p,"body",.32F*a);rx(p,"head",-.1F*a);rx(p,"armL",-.65F*a);rx(p,"foreL",-.65F*a);rx(p,"foreR",-.8F*a);}
    case STONE -> {float prep=ease(t/14),throwing=ease((t-14)/4),back=1-ease((t-18)/10);legs(p,.2F*prep*back,0,0,.7F);p.get("body").yRot=(-.4F*prep+.7F*throwing)*back;rx(p,"armR",(-2.5F*prep+2.6F*throwing)*back);rx(p,"foreR",-.8F*prep*(1-throwing));}
    case SLAM -> {float prep=ease(t/10),drop=ease((t-11)/3),back=1-ease((t-14)/10);legs(p,(.12F+.18F*drop)*back,0,0,1);rx(p,"body",(-.1F*prep+.45F*drop)*back);for(String side:new String[]{"L","R"}){rx(p,"arm"+side,(-2.3F*prep+2.1F*drop)*back);rx(p,"fore"+side,-.6F*prep*back);}}
    case TRANSITION -> {float brace=ease(t/20),change=ease((t-20)/10);legs(p,.28F*brace,0,0,1.5F*brace);rx(p,"body",.16F*brace-.3F*change);p.get("belly").xScale=1+.035F*brace;p.get("body").xScale=1-.1F*change;rx(p,"head",-.25F*change);rx(p,"armL",-.65F*brace);rx(p,"foreR",-.8F*brace);}
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
  if(m==Move.TRANSITION){float growth=ease((t-30)/30);p.get("body").yScale=.72F+.28F*growth;p.get("neck").yScale=.7F+.3F*ease((t-30)/15);open=.08F+.85F*ease((t-30)/10);rx(p,"head",-.2F*(1-growth));}
  if(m==Move.WING_DIVE){float takeoff=ease(t/8);open=t<8?.12F+.83F*takeoff:t<20?.95F:.72F-.12F*ease((t-20)/16);flap=t<20?sin((t-8)*.65F)*.18F:0;rx(p,"body",t<8?-.2F*takeoff:t<20?-.12F:.5F*ease((t-20)/8));rx(p,"legL",-.4F*takeoff);rx(p,"legR",-.4F*takeoff);}
  if(m==Move.RECOVERY&&previous==Move.WING_DIVE){float a=1-ease(t/25);open=.12F+.55F*a;rx(p,"body",.25F*a);rx(p,"legL",-.3F*a);rx(p,"legR",-.3F*a);}
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
