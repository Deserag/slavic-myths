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
 private void sit(float a){p("pelvis").y+=10*a;p("chest").xRot-=.12F*a;p("bl").xRot-=.75F*a;p("br").xRot-=.75F*a;p("bl").y+=a;p("br").y+=a;p("blLower").xRot+=2.15F*a;p("brLower").xRot+=2.15F*a;p("blFoot").xRot-=1.4F*a;p("brFoot").xRot-=1.4F*a;p("tail1").yRot+=.65F*a;p("tail1").xRot-=.35F*a;p("tail2").xRot+=.35F*a;}
 @Override public void setupAnim(GreyWolf e,float walk,float amount,float age,float yaw,float pitch){
  parts.forEach((n,p)->{var r=rest.get(n);p.setPos(r[0],r[1],r[2]);p.xRot=p.yRot=p.zRot=0;});
  float partial=age-e.tickCount,t=e.animationTicks(partial);boolean run=e.getDeltaMovement().horizontalDistanceSqr()>.035;float move=Math.min(1,amount*2),cycle=age*(run?.48F:.24F);
  String[] legs={"fl","fr","bl","br"};
  for(int i=0;i<4;i++){float a=run?Mth.sin(cycle+(i<2?0:2.3F)+i%2*.35F)*.85F*move:Mth.cos(cycle+(i==0||i==3?0:Mth.PI))*.45F*move;p(legs[i]).xRot=a;p(legs[i]+"Lower").xRot=(i<2?-1:1)*Math.max(0,-a)*.8F;p(legs[i]+"Foot").xRot=-a*.3F;}
  p("chest").y+=Mth.sin(age*.045F)*.4F;p("chest").zRot=Mth.sin(cycle)*move*.025F;p("loin").xRot=Mth.sin(cycle)*move*.03F;
  p("head").yRot=Mth.clamp(yaw,-25,25)*Mth.DEG_TO_RAD*.7F;p("neck").yRot=Mth.clamp(yaw,-25,25)*Mth.DEG_TO_RAD*.3F;p("head").xRot=pitch*Mth.DEG_TO_RAD*.4F;
  p("earL").zRot=-.12F+Mth.sin(age*.035F)*.04F;p("earR").zRot=.12F-Mth.sin(age*.032F)*.04F;
  p("earLTip").xRot=Mth.sin(age*.03F)*.04F;p("earRTip").xRot=Mth.sin(age*.035F)*.04F;
  if(e.hurtTime>0&&e.move()==WolfTimeline.Move.NONE&&e.state()!=WolfTimeline.State.TRIAL_COMPLETE_CINEMATIC){float hurt=e.hurtTime/10F;p("head").xRot+=.18F*hurt;p("chest").zRot+=.04F*hurt;p("earL").xRot=p("earR").xRot=.2F*hurt;}
  p("tail1").xRot=-.15F;p("tail1").yRot=Mth.sin(age*.04F)*.12F;p("tail2").xRot=-.12F;p("tail2").yRot=Mth.sin((age-5)*.04F)*.16F;p("tail3").yRot=Mth.sin((age-10)*.04F)*.2F;
  if(e.state()==WolfTimeline.State.POST_TRIAL_NPC)sit(ease(t/26)*(1-move));
  if(e.state()==WolfTimeline.State.TRIAL_PREPARE){float a=ease(t/8)*ease((30-t)/8);p("head").xRot+=.2F*a;p("jaw").xRot=.45F*a;p("maneRear").xRot=-.1F*a;}
  if(e.state()==WolfTimeline.State.TRIAL_COMPLETE_CINEMATIC){float a=ease(t/12);p("head").xRot=.25F*a*(1-ease((t-29)/21));p("jaw").xRot=.12F*a*(1-ease((t-13)/15));sit(ease((t-50)/20));}
  switch(e.move()){
   case HOWL_SUMMON -> {float raise=ease((t-10)/20)*(1-ease((t-110)/22));p("neck").xRot=-.5F*raise;p("head").xRot=-.44F*raise;p("jaw").xRot=.52F*raise+Mth.sin(age*.2F)*.035F*raise;p("head").yRot=p("neck").yRot=0;p("chest").y-=raise*.45F;p("fl").zRot=-.07F*raise;p("fr").zRot=.07F*raise;p("maneFront").y-=raise*.5F;p("tail1").xRot=-.25F;for(String n:legs){p(n).xRot=0;p(n+"Lower").xRot=0;p(n+"Foot").xRot=0;}}
   case BITE -> {float a=t<12?ease(t/12):1-ease((t-12)/11);p("neck").xRot=.2F*a;p("head").z-=3*a;p("jaw").xRot=.65F*ease((t-7)/5)*(1-ease((t-14)/4));}
   case DOUBLE_CLAW -> {float a=ease((t-9)/5)*(1-ease((t-14)/10)),b=ease((t-25)/8)*(1-ease((t-33)/14));p("fr").xRot=-1.2F*a;p("frLower").xRot=.7F*a;p("fl").xRot=-1.2F*b;p("flLower").xRot=.7F*b;p("chest").zRot=.1F*(a-b);p("jaw").xRot=.3F*(a+b);}
   case POUNCE -> {float prep=ease(t/12)*(1-ease((t-13)/4));p("pelvis").y+=4*prep;p("blLower").xRot+=.8F*prep;p("brLower").xRot+=.8F*prep;float air=ease((t-13)/4)*(1-ease((t-27)/13));for(String n:legs){p(n).xRot=Mth.lerp(air,p(n).xRot,-.65F);p(n+"Lower").xRot=Mth.lerp(air,p(n+"Lower").xRot,.6F);}p("tail1").xRot*=1-air;}
   case FLANK_DASH -> {float a=ease(t/9)*(1-ease((t-27)/14));p("chest").zRot=.1F*a;p("head").xRot=.15F*a;p("jaw").xRot=.3F*a;}
   default -> { }
  }
  blend(e,age);
 }
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){p("root").render(pose,out,light,overlay,color);}
}
