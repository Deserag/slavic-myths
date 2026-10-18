package org.slavicmyths.client;
import java.util.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.*;
import org.slavicmyths.wolf.GreyWolf;
import org.slavicmyths.wolf.WolfTimeline;
/** Authored anatomy in 1/16 blocks; root y=24 is ground, -y up, -z nose. No render scale. */
public final class GreyWolfModel extends EntityModel<GreyWolf> {
 private final FolkModelGeometry geometry=new FolkModelGeometry();private final Map<String,ModelPart> parts=new LinkedHashMap<>();private final Map<String,float[]> rest=new LinkedHashMap<>();
 private static final class Blend {int key;float started;Map<String,float[]> previous,from;}
 private final Map<GreyWolf,Blend> blends=new WeakHashMap<>();
 private Map<String,float[]> capture(){var result=new LinkedHashMap<String,float[]>();parts.forEach((n,p)->result.put(n,new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot}));return result;}
 private void blend(GreyWolf e,float age){int key=e.state().ordinal()*10+e.move().ordinal();var b=blends.get(e);if(b==null){b=new Blend();b.key=key;blends.put(e,b);}else if(b.key!=key){b.key=key;b.started=age;b.from=b.previous;}
  if(b.from!=null){float weight=ease((age-b.started)/6);for(var entry:parts.entrySet()){var p=entry.getValue();var from=b.from.get(entry.getKey());p.setPos(Mth.lerp(weight,from[0],p.x),Mth.lerp(weight,from[1],p.y),Mth.lerp(weight,from[2],p.z));p.xRot=Mth.lerp(weight,from[3],p.xRot);p.yRot=Mth.lerp(weight,from[4],p.yRot);p.zRot=Mth.lerp(weight,from[5],p.zRot);}if(weight>=1)b.from=null;}b.previous=capture();
 }
 public GreyWolfModel(){GreyWolfGeometry.build(this);}
 public void part(String name,String parent,float[] pos){var p=geometry.part(512,512);p.setPos(pos[0],pos[1],pos[2]);parts.put(name,p);rest.put(name,pos);if(parent!=null)geometry.attach(parts.get(parent),p);}
 public void box(String name,int u,int v,float[] b){geometry.box(parts.get(name),u,v,b[0],b[1],b[2],b[3],b[4],b[5]);}
 private ModelPart p(String name){return parts.get(name);}
 private static float ease(float value){float x=Mth.clamp(value,0,1);return x*x*(3-2*x);}
 private void sit(float a){
  float thigh=-.25F-.8F*a,knee=.55F+1.45F*a;
  p("pelvis").y=-(12*Mth.cos(thigh)+8*Mth.cos(thigh+knee)+3);
  p("chest").xRot-=.12F*a;p("loin").xRot-=.25F*a;p("loin").y+=3*a;
  for(String name:List.of("bl","br")){p(name).y=0;p(name).xRot=thigh;p(name+"Lower").xRot=knee;p(name+"Foot").xRot=-(thigh+knee);}
  p("tail1").yRot+=.5F*a;p("tail1").xRot-=.2F*a;p("tail2").xRot+=.2F*a;
 }
 private void footPlant(String leg,float upper,float lower,float lift){
  boolean front=leg.charAt(0)=='f';float first=front?14:12,second=front?10:8;
  p(leg).xRot=upper;p(leg+"Lower").xRot=lower;p(leg+"Foot").xRot=-(upper+lower);
  p(leg).y=-(first*Mth.cos(upper)+second*Mth.cos(upper+lower)+3)-lift-(front?0:p("pelvis").y);
 }
 @Override public void setupAnim(GreyWolf e,float walk,float amount,float age,float yaw,float pitch){
  sample(e.state(),e.move(),e.animationTicks(age-e.tickCount),e.getDeltaMovement().horizontalDistanceSqr()>.035,e.hurtTime,walk,amount,age,yaw,pitch);
  blend(e,age);
 }
 /** Exact production pose inputs, exported by native offline fixtures without an entity/world. */
 public void sample(WolfTimeline.State state,WolfTimeline.Move action,float t,boolean run,float hurtTicks,float walk,float amount,float age,float yaw,float pitch){
  parts.forEach((n,p)->{var r=rest.get(n);p.setPos(r[0],r[1],r[2]);p.xRot=p.yRot=p.zRot=0;p.xScale=p.yScale=p.zScale=1;p.visible=true;p.skipDraw=false;});
  float move=Math.min(1,amount*1.5F),cycle=walk*(run?.75F:.5F);
  if(action==WolfTimeline.Move.HOWL_SUMMON||action==WolfTimeline.Move.POUNCE)move=0;
  if(action==WolfTimeline.Move.FLANK_DASH){move=Math.max(move,.8F*ease(t/9)*(1-ease((t-27)/14)));cycle=t*.7F;}
  String[] legs={"fl","fr","bl","br"};
  for(int i=0;i<4;i++){
   boolean front=i<2;float phase=cycle+(i==0||i==3?0:Mth.PI);
   float stride=Mth.cos(phase)*(run?.62F:.42F)*move,swing=Math.max(0,Mth.sin(phase));
   float upper=(front?0:-.25F)+stride,lower=(front?-.08F:.55F)+(front?-.6F:.75F)*swing*move;
   footPlant(legs[i],upper,lower,swing*(run?4:2.5F)*move);
  }
  p("chest").y+=Mth.sin(age*.045F)*.22F;p("chest").zRot=Mth.sin(cycle)*move*.025F;p("loin").xRot=Mth.sin(cycle)*move*.03F;
  p("head").yRot=Mth.clamp(yaw,-25,25)*Mth.DEG_TO_RAD*.7F;p("neck").yRot=Mth.clamp(yaw,-25,25)*Mth.DEG_TO_RAD*.3F;p("head").xRot=pitch*Mth.DEG_TO_RAD*.4F;
  p("earL").zRot=-.12F+Mth.sin(age*.035F)*.04F;p("earR").zRot=.12F-Mth.sin(age*.032F)*.04F;
  p("earLTip").xRot=Mth.sin(age*.03F)*.04F;p("earRTip").xRot=Mth.sin(age*.035F)*.04F;
  if(hurtTicks>0&&action==WolfTimeline.Move.NONE&&state!=WolfTimeline.State.TRIAL_COMPLETE_CINEMATIC){float hurt=hurtTicks/10F;p("head").xRot+=.18F*hurt;p("chest").zRot+=.04F*hurt;p("earL").xRot=p("earR").xRot=.2F*hurt;}
  p("tail1").xRot=-.18F;p("tail1").yRot=Mth.sin(age*.04F)*.12F;p("tail2").xRot=-.14F;p("tail2").yRot=Mth.sin((age-5)*.04F)*.16F;p("tail3").yRot=Mth.sin((age-10)*.04F)*.2F;
  if(state==WolfTimeline.State.GREETING||state==WolfTimeline.State.WAITING_DECISION){p("head").zRot=.07F;p("head").xRot+=.05F+Mth.sin(age*.08F)*.025F;p("earR").xRot=.08F;}
  if(state==WolfTimeline.State.POST_TRIAL_NPC)sit(ease(t/26)*(1-move));
  if(state==WolfTimeline.State.TRIAL_PREPARE){float a=ease(t/8)*ease((30-t)/8);p("head").xRot+=.2F*a;p("jaw").xRot=.45F*a;p("maneRear").xRot=-.1F*a;}
  if(state==WolfTimeline.State.TRIAL_COMPLETE_CINEMATIC){float a=ease(t/12);p("head").xRot=.25F*a*(1-ease((t-29)/21));p("jaw").xRot=.12F*a*(1-ease((t-13)/15));sit(ease((t-50)/20));}
  switch(action){
   case HOWL_SUMMON -> {float raise=ease((t-10)/20)*(1-ease((t-110)/22));p("neck").xRot=-.5F*raise;p("head").xRot=-.44F*raise;p("jaw").xRot=.52F*raise+Mth.sin(age*.2F)*.035F*raise;p("head").yRot=p("neck").yRot=0;p("chest").y-=raise*.45F;p("fl").zRot=-.07F*raise;p("fr").zRot=.07F*raise;p("maneFront").y-=raise*.5F;p("tail1").xRot=-.25F;for(String n:legs)footPlant(n,n.charAt(0)=='f'?0:-.25F,n.charAt(0)=='f'?0:.55F,0);}
   case BITE -> {float a=t<12?ease(t/12):1-ease((t-12)/11);p("neck").xRot=.2F*a;p("head").z-=3*a;p("jaw").xRot=.65F*ease((t-7)/5)*(1-ease((t-14)/4));}
   case DOUBLE_CLAW -> {float a=ease((t-9)/5)*(1-ease((t-14)/10)),b=ease((t-25)/8)*(1-ease((t-33)/14));p("fr").xRot=-1.2F*a;p("frLower").xRot=.7F*a;p("fl").xRot=-1.2F*b;p("flLower").xRot=.7F*b;p("chest").zRot=.1F*(a-b);p("jaw").xRot=.3F*(a+b);}
   case POUNCE -> {float prep=ease(t/12)*(1-ease((t-13)/4));float thigh=-.25F-.35F*prep,knee=.55F+.7F*prep;p("pelvis").y=-(12*Mth.cos(thigh)+8*Mth.cos(thigh+knee)+3);p("chest").y+=1.1F*prep;for(String n:List.of("bl","br")){p(n).y=0;p(n).xRot=thigh;p(n+"Lower").xRot=knee;p(n+"Foot").xRot=-(thigh+knee);}float air=ease((t-13)/4)*(1-ease((t-27)/13));for(String n:legs){boolean front=n.charAt(0)=='f';p(n).xRot=Mth.lerp(air,p(n).xRot,front?-1.15F:.45F);p(n+"Lower").xRot=Mth.lerp(air,p(n+"Lower").xRot,front?.22F:1.1F);p(n+"Foot").xRot=-(p(n).xRot+p(n+"Lower").xRot);}p("root").xRot=.08F*air;p("neck").z-=1.2F*air;p("tail1").xRot*=1-air;}
   case FLANK_DASH -> {float a=ease(t/9)*(1-ease((t-27)/14));p("chest").zRot=.12F*a;p("pelvis").zRot=.06F*a;p("neck").yRot=-.1F*a;p("head").xRot=.15F*a;p("jaw").xRot=.3F*a;}
   default -> { }
  }
 }
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){p("root").render(pose,out,light,overlay,color);}
}
