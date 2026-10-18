package org.slavicmyths.client;

import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import static org.slavicmyths.bandit.NightingaleEntity.*;

/** Pose-only client choreography. NightingaleEntity owns physics, clocks and all hits. */
final class NightingalePoses {
 private static float ease(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
 private static void rx(Map<String,ModelPart> p,String n,float a){if(p.containsKey(n))p.get(n).xRot+=a;}
 private static void rz(Map<String,ModelPart> p,String n,float a){if(p.containsKey(n))p.get(n).zRot+=a;}
 private static void legs(Map<String,ModelPart> p,boolean second,float bend,float cycle,float motion,boolean air,float tuck){
  float upper=second?7.2F:6.5F,lower=3,sole=second?1.6F:1.5F;
  float height=upper*Mth.cos(-bend)+lower*Mth.cos(bend)+sole;p.get("pelvis").y=-height;
  for(String side:new String[]{"L","R"}){
   float wave=Mth.sin(cycle+(side.equals("L")?0:Mth.PI))*motion;
   var leg=p.get("leg"+side);var knee=p.get("knee"+side);var ankle=p.get("ankle"+side);
   leg.xRot=-bend+wave*.42F-(air?.45F*tuck:0);knee.xRot=2*bend+Math.max(0,wave)*.45F+(air?.9F*tuck:0);
   ankle.xRot=-(leg.xRot+knee.xRot);
   leg.y=air?0:height-upper*Mth.cos(leg.xRot)-lower*Mth.cos(leg.xRot+knee.xRot)-sole-Math.max(0,wave)*1.4F;
  }
 }
 private static void slash(Map<String,ModelPart> p,String side,float t,float hit){
  float prep=ease(t/(hit-2)),stroke=ease((t-hit+2)/2),back=1-ease((t-hit)/7),sign=side.equals("R")?1:-1;
  p.get("pelvis").yRot+=sign*(-.1F*prep+.2F*stroke)*back;
  p.get("body").yRot+=sign*(-.32F*prep+.65F*stroke)*back;
  rx(p,"arm"+side,(-1.25F*prep+.65F*stroke)*back);rz(p,"arm"+side,sign*(.25F*prep+.1F*stroke)*back);
  rx(p,"fore"+side,(-.85F*prep+.55F*stroke)*back);
  p.get("wrist"+side).yRot+=sign*(-.2F*prep+.5F*stroke)*back;
  rz(p,"wrist"+side,sign*.08F*stroke*back);p.get("leg"+side).z-=.6F*stroke*back;
 }
 private static float whistle(Map<String,ModelPart> p,float t,float release){
  float prep=ease(t/release),out=ease((t-release)/3),back=1-ease((t-release)/10),a=prep*back;
  p.get("ribcage").xScale=1+.075F*prep*(1-out)*back;p.get("ribcage").zScale=1+.09F*prep*(1-out)*back;
  rx(p,"body",(-.12F*prep+.2F*out)*back);rx(p,"head",(-.18F*prep+.3F*out)*back);
  p.get("jaw").xRot=(.24F*prep*(1-out)+.04F*out)*back;
  for(String side:new String[]{"L","R"}){rx(p,"arm"+side,-.25F*a);rx(p,"fore"+side,-.55F*a);}
  p.get("lipRim").visible=p.get("whistleLips").visible=a>.1F;return a;
 }
 static void sample(Map<String,ModelPart> p,boolean second,int action,float t,boolean phaseTwo,boolean grounded,float velocityY,int hurt,int death,float stride,float amount,float age){
  boolean air=!grounded;float movement=Math.min(1,amount*2),cycle=stride*(second?.9F:.75F),breath=Mth.sin(age*.065F);
  p.get("lipRim").visible=p.get("whistleLips").visible=false;
  legs(p,second,.035F,cycle,movement,air,air?.6F:0);
  p.get("body").y+=breath*.045F;p.get("ribcage").zScale=1+breath*.012F;
  for(String side:new String[]{"L","R"}){
   float sign=side.equals("L")?1:-1;
   rx(p,"arm"+side,-Mth.sin(cycle+(sign>0?0:Mth.PI))*.23F*movement);
   rx(p,"fore"+side,-.18F);p.get("wrist"+side).yRot=sign*.35F;
   rx(p,"moustache"+side,breath*.02F);
  }
  switch(action){
   case QUICK -> slash(p,"R",t,10);
   case COMBO -> {for(int stage=0;stage<3;stage++)if(t>=stage*12)slash(p,stage==1?"L":"R",t-stage*12,8);}
   case INHALE,WAVE,DEEP_INHALE,DEEP_EXHALE,EXHALE,RADIAL -> whistle(p,t,16);
   case WHIRLWIND -> {
    float a=whistle(p,t,16),turn=ease((t-16)/14);p.get("pelvis").yRot=turn*Mth.TWO_PI;
    legs(p,second,.16F*a,(t-16)*Mth.TWO_PI/7,t>=16?.65F*(1-ease((t-25)/5)):0,air,.6F);
    p.get("body").yRot+=.18F*Mth.sin((t-16)*.45F)*a;rx(p,"armR",-.35F*a);rx(p,"foreL",-.25F*a);
   }
   case DASH -> dash(p,second,t,stride,movement,air);
   case ONSLAUGHT -> {
    if(t<22)dash(p,second,t,stride,movement,air);
    if(t>=14)slash(p,"R",t-14,8);if(t>=26)slash(p,"L",t-26,8);
    if(t>=38)whistle(p,t-38,8);
   }
   case LEAP_ATTACK -> {
    float prep=ease(t/10),land=grounded&&t>=12?1-ease((t-12)/10):0;
    legs(p,second,.3F*(t<10?prep:land),cycle,movement*.25F,air,.8F);
    rx(p,"body",t<10?.18F*prep:air?-.1F:.22F*land);
    for(String side:new String[]{"L","R"}){rx(p,"arm"+side,-.6F*prep);rx(p,"fore"+side,-.35F*prep);}
   }
   case AIR_DIVE -> {
    boolean diving=t>=10&&t<36&&velocityY<-.1F;
    float a=diving?ease((t-8)/4):1-ease((t-36)/12);a=Mth.clamp(a,0,1);
    legs(p,second,.08F,cycle,movement*.2F,air,.8F);rx(p,"body",diving?.55F*a:-.08F*a);
    rx(p,"armL",diving?-1.55F*a:-.45F*a);rx(p,"armR",diving?-1.75F*a:-.55F*a);rx(p,"foreL",-.55F*a);rx(p,"foreR",-.65F*a);
   }
   case BREAK_AWAY -> {float a=whistle(p,t,8);legs(p,second,.18F*a,stride*.9F,movement,air,.6F);rx(p,"body",-.18F*a);rx(p,"armL",-.3F*a);}
   case TRANSFORM -> {
    float wind=ease(t/20),unfold=ease((t-20)/20);
    legs(p,second,second?.2F*(1-unfold):.18F*wind,0,0,false,0);
    rx(p,"body",second?-.14F*(1-unfold):-.12F*wind);p.get("ribcage").zScale=1+.08F*(second?1-unfold:wind);
    rx(p,"head",-.15F*(second?1-unfold:wind));rx(p,"foreL",-.6F);rx(p,"foreR",-.6F);
   }
   default -> {}
  }
  for(int i=0;i<3;i++)rx(p,"cloakStrip"+i,Mth.sin(age*.06F-i*.55F)*.025F+movement*.09F+(!grounded?.12F:0));
  for(String side:new String[]{"L","R"}){rx(p,"coatTail"+side,Mth.sin(age*.075F+(side.equals("L")?0:1))*.035F+movement*.12F+(!grounded?.15F:0));rx(p,"cloakSide"+side,movement*.06F);}
  rx(p,"topknot",Mth.sin(age*.065F)*.035F+movement*.06F);
  if(hurt>0){rx(p,"body",-.12F*hurt/10);rx(p,"head",.15F*hurt/10);}
  if(death>0){
   float brace=ease(death/12F),fall=ease((death-12)/23F);legs(p,second,.32F*brace,0,0,false,0);
   for(String side:new String[]{"L","R"}){rx(p,"arm"+side,-.65F*brace);rx(p,"fore"+side,-.9F*brace);}
   p.get("root").zRot=1.45F*fall;p.get("root").y-=(second?4.9F:7.3F)*Mth.sin(1.45F*fall);rx(p,"head",.2F*brace);
   p.get("lipRim").visible=p.get("whistleLips").visible=false;
  }
 }
 private static void dash(Map<String,ModelPart> p,boolean second,float t,float stride,float movement,boolean air){
  float a=ease(t/8)*(1-ease((t-18)/5));legs(p,second,.18F*a,stride*1.05F,movement,air,.6F);
  rx(p,"body",.32F*a);rx(p,"head",-.12F*a);rx(p,"armL",-.35F*a);rx(p,"foreL",-.45F*a);rx(p,"foreR",-.65F*a);
 }
 private NightingalePoses(){}
}
