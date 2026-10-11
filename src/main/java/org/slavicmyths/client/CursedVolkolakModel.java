package org.slavicmyths.client;
import java.util.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.*;
import org.slavicmyths.volkolak.CursedVolkolak;

/** One articulated rig, two physical stances; rotations never alter the approved old Volkolak. */
public final class CursedVolkolakModel extends EntityModel<CursedVolkolak> {
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 private final Map<String,ModelPart> parts=new LinkedHashMap<>();private final Map<String,float[]> rest=new LinkedHashMap<>();
 private static final class Blend{int key;float started;Map<String,float[]> previous,from;}
 private final Map<CursedVolkolak,Blend> blends=new WeakHashMap<>();
 public CursedVolkolakModel(){CursedVolkolakGeometry.build(this);}
 public void part(String name,String parent,float[] pos,float[] rotation){var p=geometry.part(512,512);p.setPos(pos[0],pos[1],pos[2]);p.xRot=rotation[0];p.yRot=rotation[1];p.zRot=rotation[2];parts.put(name,p);rest.put(name,new float[]{pos[0],pos[1],pos[2],rotation[0],rotation[1],rotation[2]});if(parent!=null)geometry.attach(parts.get(parent),p);}
 public void box(String name,int u,int v,float[] b){geometry.box(parts.get(name),u,v,b[0],b[1],b[2],b[3],b[4],b[5]);}
 private ModelPart p(String name){return parts.get(name);}private static float ease(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
 private Map<String,float[]> capture(){var map=new LinkedHashMap<String,float[]>();parts.forEach((n,p)->map.put(n,new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot}));return map;}
 private void blend(CursedVolkolak e,float age){int key=e.move().ordinal()*10+(e.upright()?1:0);var b=blends.computeIfAbsent(e,k->new Blend());if(key!=b.key){b.key=key;b.started=age;b.from=b.previous;}if(b.from!=null){float f=ease((age-b.started)/5);parts.forEach((n,p)->{var a=b.from.get(n);p.setPos(Mth.lerp(f,a[0],p.x),Mth.lerp(f,a[1],p.y),Mth.lerp(f,a[2],p.z));p.xRot=Mth.lerp(f,a[3],p.xRot);p.yRot=Mth.lerp(f,a[4],p.yRot);p.zRot=Mth.lerp(f,a[5],p.zRot);});if(f>=1)b.from=null;}b.previous=capture();}
 private void stance(float f){p("pelvis").y-=4*f;p("spine").xRot-=1.13F*f;p("spine").y+=2*f;p("chest").xRot-=.1F*f;p("neck").xRot+=.34F*f;p("head").xRot+=.71F*f;for(String side:List.of("L","R")){p("upper"+side).xRot=Mth.lerp(f,.82F,1.15F);p("fore"+side).xRot=Mth.lerp(f,-1.65F,-.39F);p("hand"+side).xRot=Mth.lerp(f,.83F,.26F);p("thigh"+side).xRot=Mth.lerp(f,1.35F,1.15F);p("shin"+side).xRot=Mth.lerp(f,-2.4F,-2.08F);p("foot"+side).xRot=Mth.lerp(f,1.05F,.93F);}}
 private void claw(String side,float t,float hit){float prep=ease(t/(hit-3)),swing=ease((t-hit+3)/4),back=ease((t-hit-2)/9);float a=prep*(1-back);p("upper"+side).xRot-=1.35F*a;p("upper"+side).zRot+=(side.equals("L")?-1:1)*(.65F*a-1.0F*swing*(1-back));p("fore"+side).xRot-=.35F*a;p("chest").zRot+=(side.equals("L")?-1:1)*.16F*a;p("jaw").xRot=.2F*a;}
 @Override public void setupAnim(CursedVolkolak e,float walk,float amount,float age,float yaw,float pitch){parts.forEach((n,p)->{var a=rest.get(n);p.setPos(a[0],a[1],a[2]);p.xRot=a[3];p.yRot=a[4];p.zRot=a[5];});float t=e.animationTicks(age-e.tickCount),f=e.upright()||e.heldEntity()>=0?1:0;
  if(e.move()==CursedVolkolak.Move.RISE)f=ease((t-3)/12);if(e.move()==CursedVolkolak.Move.DROP)f=1-ease((t-2)/9);stance(f);
  // Limb phase follows actual travelled distance; idle time cannot advance planted feet.
  float cycle=walk*(f>.5?.75F:1.05F),move=Math.min(1,amount*2);for(String side:List.of("L","R")){float a=Mth.sin(cycle+(side.equals("L")?0:Mth.PI))*move;p("upper"+side).xRot+=a*(f>.5?.22F:.45F);p("fore"+side).xRot-=Math.max(0,a)*.25F*(1-f);p("thigh"+side).xRot-=a*.3F;p("shin"+side).xRot+=Math.max(0,a)*.3F;}
  p("chest").y+=Mth.sin(age*Mth.TWO_PI/(f>.5?80:100))*.25F;p("head").yRot=Mth.clamp(yaw,-25,25)*Mth.DEG_TO_RAD;p("head").xRot+=pitch*Mth.DEG_TO_RAD*.4F;p("earL").zRot+=Mth.sin(age*Mth.TWO_PI/100)*.045F;p("earR").xRot+=Mth.sin(age*Mth.TWO_PI/105)*.055F;
  p("tail1").yRot=Mth.sin(age*Mth.TWO_PI/80)*.08F;p("tail2").yRot=Mth.sin((age-5)*Mth.TWO_PI/80)*.1F;p("tail3").yRot=Mth.sin((age-10)*Mth.TWO_PI/80)*.15F;
  if(e.getTarget()!=null&&move<.5&&e.move()==CursedVolkolak.Move.IDLE){p("head").xRot+=.17F;p("chest").y+=2*(1-f);p("neck").yRot+=Mth.sin(age*Mth.TWO_PI/38)*.035F;}
  if(e.hurtTime>0){float hurt=e.hurtTime/10F;p("chest").zRot+=.08F*hurt;p("head").xRot+=.12F*hurt;p("earL").xRot+=.2F*hurt;}
  switch(e.move()){
   case CLAW_L -> claw("L",t,10);
   case CLAW_R -> claw("R",t,12);
   case DOUBLE,TRIPLE -> {claw("L",t,10);if(t>10)claw("R",t-10,10);if(e.move()==CursedVolkolak.Move.TRIPLE&&t>20){claw("L",t-20,10);claw("R",t-20,10);p("chest").xRot+=.15F*ease((t-20)/10);}}
   case BITE -> {float a=ease(t/10)*(1-ease((t-14)/10));p("neck").z-=3*a;p("head").xRot+=.15F*a;p("jaw").xRot=.65F*ease((t-7)/6)*(1-ease((t-14)/6));}
   case POUNCE -> {float prep=ease(t/13)*(1-ease((t-14)/3));p("pelvis").y+=3*prep;float airborne=ease((t-14)/3)*(1-ease((t-28)/10));p("upperL").xRot-=.5F*airborne;p("upperR").xRot-=.5F*airborne;p("thighL").xRot+=.3F*airborne;p("thighR").xRot+=.3F*airborne;p("jaw").xRot=.35F*airborne;}
   case FLANK -> {p("chest").zRot+=.12F*ease(t/7)*(1-ease((t-24)/8));p("jaw").xRot=.5F*ease((t-18)/5)*(1-ease((t-24)/7));}
   case GRAB -> {float hold=ease((t-21)/4)*(1-ease((t-45)/10));if(e.heldEntity()>=0){for(String side:List.of("L","R")){p("upper"+side).xRot-=1.0F*hold;p("fore"+side).xRot-=.9F*hold;p("upper"+side).zRot=(side.equals("L")?1:-1)*.25F*hold;}p("head").xRot+=.22F*hold;p("jaw").xRot=.55F*ease((t-24)/4)*(1-ease((t-30)/6));}else if(t<22){p("pelvis").y+=2*ease(t/13);p("jaw").xRot=.3F;}}
   case SNIFF -> {float a=Mth.sin(t*Mth.PI/18);p("head").xRot+=.22F*a;p("neck").z-=a;p("nose").y+=Mth.sin(t*1.1F)*.08F;}
   case LISTEN -> {p("earL").zRot-=.18F;p("earR").zRot+=.18F;p("head").yRot+=Mth.sin(t*Mth.PI/24)*.25F;}
   case SNARL -> {p("jaw").xRot=.35F*ease(t/6)*(1-ease((t-16)/9));p("head").xRot+=.1F;}
   case HOWL_SHORT -> {float a=ease(t/12)*(1-ease((t-31)/11));p("neck").xRot-=.3F*a;p("head").xRot-=.4F*a;p("jaw").xRot=.5F*a;}
   case ALERT -> {p("head").xRot-=.1F*ease(t/8);p("earL").zRot-=.1F;p("earR").zRot+=.1F;}
   case ROAR -> {float a=ease((t-5)/7)*(1-ease((t-19)/7));p("neck").xRot-=.4F*a;p("head").xRot-=.35F*a;p("jaw").xRot=.65F*a;}
   case STAGGER -> {p("chest").xRot+=.12F*(1-ease(t/20));p("head").xRot-=.15F;}
   case ESCAPE -> {float fall=ease(t/19)*(1-ease((t-49)/38));p("root").zRot=-1.15F*fall;p("pelvis").y+=8*fall;p("head").xRot+=.22F*fall;p("earL").xRot+=.5F*fall;p("earR").xRot+=.5F*fall;if(t>=87){float a=1-ease((t-102)/10);p("head").xRot-=.4F*a;p("jaw").xRot=.55F*a;}}
   default -> {}
  }blend(e,age);
 }
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){p("root").render(pose,out,light,overlay,color);}
}
