package org.slavicmyths.client;
import java.util.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.*;
import org.slavicmyths.volkolak.CursedVolkolak;

/** One articulated rig, two physical stances; rotations never alter the approved old Volkolak. */
public final class CursedVolkolakModel extends EntityModel<CursedVolkolak> {
 public ModelPart trophyHead(){return parts.get("head");}
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 private final Map<String,ModelPart> parts=new LinkedHashMap<>();private final Map<String,float[]> rest=new LinkedHashMap<>();
 private static final class Blend{int key;float started;Map<String,float[]> previous,from;}
 private final Map<Object,Blend> blends=new WeakHashMap<>();
 private final Map<String,String> parents=new LinkedHashMap<>();
 private final Map<String,List<float[]>> cuboids=new LinkedHashMap<>();
 private float stanceFactor,walkPhase,movement,airborne;private boolean sampledHeld;
 private CursedVolkolak.Move sampledAction;
 private float ground(){return rest.get("root")[1];}
 private float sole(String name){return (float)cuboids.get(name).stream().mapToDouble(b->b[1]+b[4]).max().orElse(0);}
 private PoseStack through(String name){var chain=new ArrayList<String>();for(String at=name;at!=null;at=parents.get(at))chain.add(at);Collections.reverse(chain);var pose=new PoseStack();for(String at:chain)p(at).translateAndRotate(pose);return pose;}

 public CursedVolkolakModel(){CursedVolkolakGeometry.build(this);}
 public void part(String name,String parent,float[] pos,float[] rotation){var p=geometry.part(512,512);p.setPos(pos[0],pos[1],pos[2]);p.xRot=rotation[0];p.yRot=rotation[1];p.zRot=rotation[2];parts.put(name,p);parents.put(name,parent);cuboids.put(name,new ArrayList<>());rest.put(name,new float[]{pos[0],pos[1],pos[2],rotation[0],rotation[1],rotation[2]});if(parent!=null)geometry.attach(parts.get(parent),p);}
 public void box(String name,int u,int v,float[] b){cuboids.get(name).add(b.clone());geometry.box(parts.get(name),u,v,b[0],b[1],b[2],b[3],b[4],b[5]);}
 private ModelPart p(String name){return parts.get(name);}private static float ease(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
 private Map<String,float[]> capture(){var map=new LinkedHashMap<String,float[]>();parts.forEach((n,p)->map.put(n,new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot}));return map;}
 /** Production blend, also callable by the native no-world regression harness. */
 public void blendPose(Object identity,int key,float age){
  var b=blends.computeIfAbsent(identity,k->new Blend());
  if(key!=b.key){b.key=key;b.started=age;b.from=b.previous;}
  if(b.from!=null){float f=ease((age-b.started)/5);parts.forEach((n,p)->{var a=b.from.get(n);p.setPos(Mth.lerp(f,a[0],p.x),Mth.lerp(f,a[1],p.y),Mth.lerp(f,a[2],p.z));p.xRot=Mth.lerp(f,a[3],p.xRot);p.yRot=Mth.lerp(f,a[4],p.yRot);p.zRot=Mth.lerp(f,a[5],p.zRot);});if(f>=1)b.from=null;}
  // Interpolation of angles alone cannot preserve a foot contact: solve after blending too.
  stabilize();b.previous=capture();
 }
 private void stance(float f){
  float thigh=Mth.lerp(f,1.25F,.18F),shin=Mth.lerp(f,-2.15F,-.50F);
  float femur=rest.get("shinL")[1],tibia=rest.get("footL")[1];
  p("pelvis").y=-(rest.get("thighL")[1]+femur*Mth.cos(thigh)+tibia*Mth.cos(thigh+shin)+sole("footL"));
  p("spine").xRot-=1.13F*f;p("spine").y-=2F*(1-f);p("spine").y+=2*f;
  p("chest").xRot-=.1F*f;p("neck").xRot+=.34F*f;p("neck").y-=1.0F*(1-f);p("head").xRot+=.71F*f;
  for(String side:List.of("L","R")){
   p("upper"+side).xRot=Mth.lerp(f,.65F,1.05F);p("fore"+side).xRot=Mth.lerp(f,-1.3F,-1.35F);p("hand"+side).xRot=Mth.lerp(f,.65F,1.10F);
   p("thigh"+side).xRot=thigh;p("shin"+side).xRot=shin;p("foot"+side).xRot=-(thigh+shin);
  }
 }
 /** Two-bone IK using the authored joint distances, with a world-flat paw pad. */
 private void reach(String upper,String lower,String tip,float x,float y,float z,float weight){
  if(weight<=0)return;
  var parent=through(parents.get(upper));var local=new org.joml.Matrix4f(parent.last().pose()).invert().transformPosition(new org.joml.Vector3f(x/16,y/16,z/16)).mul(16);
  var a=p(upper);float dx=local.x-a.x,dy=local.y-a.y,dz=local.z-a.z;
  float l1=rest.get(lower)[1],l2=rest.get(tip)[1],radius=Mth.clamp(Mth.sqrt(dx*dx+dy*dy+dz*dz),Math.abs(l1-l2)+.001F,l1+l2-.001F);
  float roll=(float)Math.atan2(-dx,dy),base=(float)Math.atan2(dz,Mth.sqrt(dx*dx+dy*dy));
  float bend=-(float)Math.acos(Mth.clamp((radius*radius-l1*l1-l2*l2)/(2*l1*l2),-1,1));
  float shoulder=base+(float)Math.acos(Mth.clamp((l1*l1+radius*radius-l2*l2)/(2*l1*radius),-1,1));
  a.xRot=Mth.lerp(weight,a.xRot,shoulder);a.yRot=Mth.lerp(weight,a.yRot,0);a.zRot=Mth.lerp(weight,a.zRot,roll);
  var elbow=p(lower);elbow.xRot=Mth.lerp(weight,elbow.xRot,bend);elbow.yRot=elbow.zRot=0;
  var rotation=through(lower).last().pose().getUnnormalizedRotation(new org.joml.Quaternionf()).conjugate().getEulerAnglesZYX(new org.joml.Vector3f());
  var paw=p(tip);paw.xRot=Mth.lerp(weight,paw.xRot,rotation.x);paw.yRot=Mth.lerp(weight,paw.yRot,rotation.y);paw.zRot=Mth.lerp(weight,paw.zRot,rotation.z);
 }
 private void plantHand(String side,float target,float weight){reach("upper"+side,"fore"+side,"hand"+side,side.equals("L")?-6:6,target-sole("hand"+side),-10.5F,weight);}
 private void plantFoot(String side,float target,float z){reach("thigh"+side,"shin"+side,"foot"+side,rest.get("thigh"+side)[0],target-sole("foot"+side),z,1);}
 private void floorFall(){
  float maxY=-Float.MAX_VALUE;
  for(var entry:cuboids.entrySet()){
   var matrix=through(entry.getKey()).last().pose();
   for(var box:entry.getValue())for(int corner=0;corner<8;corner++){
    float x=box[0]+((corner&1)==0?0:box[3]),y=box[1]+((corner&2)==0?0:box[4]),z=box[2]+((corner&4)==0?0:box[5]);
    maxY=Math.max(maxY,matrix.transformPosition(new org.joml.Vector3f(x/16,y/16,z/16)).y*16);
   }
  }
  if(maxY>-Float.MAX_VALUE)p("root").y+=ground()-maxY;
 }
 private void stabilize(){
  if(sampledAction==CursedVolkolak.Move.ESCAPE){floorFall();return;}
  for(String side:List.of("L","R")){
   float a=Mth.sin(walkPhase+(side.equals("L")?0:Mth.PI))*movement;
   plantFoot(side,ground()-Math.max(0,-a)*2-2*airborne,3.2F-a*2);
   boolean free=sampledAction==CursedVolkolak.Move.CLAW_L&&side.equals("L")||sampledAction==CursedVolkolak.Move.CLAW_R&&side.equals("R")||sampledAction==CursedVolkolak.Move.DOUBLE||sampledAction==CursedVolkolak.Move.TRIPLE;
   if(!free&&stanceFactor<1)plantHand(side,ground()-Math.max(0,a)*2.5F-4*airborne,1-stanceFactor);
   if(sampledHeld&&sampledAction==CursedVolkolak.Move.GRAB){
    reach("upper"+side,"fore"+side,"hand"+side,side.equals("L")?-8:8,8,-.7F*16/CursedVolkolakRenderer.DEPTH_SCALE,1);
   }
  }
 }
 private void claw(String side,float t,float hit){float prep=ease(t/(hit-3)),swing=ease((t-hit+3)/4),back=ease((t-hit-2)/9);float a=prep*(1-back);p("upper"+side).xRot-=1.35F*a;p("upper"+side).zRot+=(side.equals("L")?-1:1)*(.65F*a-1.0F*swing*(1-back));p("fore"+side).xRot-=.35F*a;p("chest").zRot+=(side.equals("L")?-1:1)*.16F*a;p("jaw").xRot=.2F*a;}
 @Override public void setupAnim(CursedVolkolak e,float walk,float amount,float age,float yaw,float pitch){
  sample(e.move(),e.animationTicks(age-e.tickCount),e.upright(),e.heldEntity()>=0,e.move()==CursedVolkolak.Move.ALERT||e.move()==CursedVolkolak.Move.SNARL,e.hurtTime,walk,amount,age,yaw,pitch);blendPose(e,e.move().ordinal()*10+(e.upright()?1:0),age);
 }
 public void sample(CursedVolkolak.Move action,float t,boolean upright,boolean held,boolean target,float hurtTicks,float walk,float amount,float age,float yaw,float pitch){
  parts.forEach((n,p)->{var a=rest.get(n);p.setPos(a[0],a[1],a[2]);p.xRot=a[3];p.yRot=a[4];p.zRot=a[5];p.xScale=p.yScale=p.zScale=1;p.visible=true;p.skipDraw=false;});float f=upright||held?1:0;
  if(action==CursedVolkolak.Move.RISE)f=ease((t-7)/8);if(action==CursedVolkolak.Move.DROP)f=1-ease(t/6);stance(f);stanceFactor=f;sampledAction=action;sampledHeld=held;airborne=0;
  // Limb phase follows actual travelled distance; idle time cannot advance planted feet.
  float cycle=walk*(f>.5?.75F:1.05F),move=Math.min(1,amount*2);walkPhase=cycle;movement=move;
  for(String side:List.of("L","R")){
   float a=Mth.sin(cycle+(side.equals("L")?0:Mth.PI))*move;
   p("upper"+side).xRot+=a*(f>.5?.22F:.45F);p("fore"+side).xRot-=Math.max(0,a)*.25F*(1-f);
   plantFoot(side,ground()-Math.max(0,-a)*2,3.2F-a*2);
  }
  p("chest").y+=Mth.sin(age*Mth.TWO_PI/(f>.5?80:100))*.25F;p("head").yRot=Mth.clamp(yaw,-25,25)*Mth.DEG_TO_RAD;p("head").xRot+=pitch*Mth.DEG_TO_RAD*.4F;p("earL").zRot+=Mth.sin(age*Mth.TWO_PI/100)*.045F;p("earR").xRot+=Mth.sin(age*Mth.TWO_PI/105)*.055F;
  p("tail1").yRot=Mth.sin(age*Mth.TWO_PI/80)*.08F;p("tail2").yRot=Mth.sin((age-5)*Mth.TWO_PI/80)*.1F;p("tail3").yRot=Mth.sin((age-10)*Mth.TWO_PI/80)*.15F;
  if(target&&move<.5&&action==CursedVolkolak.Move.IDLE){p("head").xRot+=.17F;p("chest").y+=2*(1-f);p("neck").yRot+=Mth.sin(age*Mth.TWO_PI/38)*.035F;}
  if(hurtTicks>0){float hurt=hurtTicks/10F;p("chest").zRot+=.08F*hurt;p("head").xRot+=.12F*hurt;p("earL").xRot+=.2F*hurt;}
  for(String side:List.of("L","R")){float a=Mth.sin(cycle+(side.equals("L")?0:Mth.PI))*move;plantHand(side,ground()-Math.max(0,a)*2.5F,1-f);}
  switch(action){
   case CLAW_L -> claw("L",t,10);
   case CLAW_R -> claw("R",t,12);
   case DOUBLE,TRIPLE -> {claw("L",t,10);if(t>10)claw("R",t-10,10);if(action==CursedVolkolak.Move.TRIPLE&&t>20){claw("L",t-20,10);claw("R",t-20,10);p("chest").xRot+=.15F*ease((t-20)/10);}}
   case BITE -> {float a=ease(t/10)*(1-ease((t-14)/10));p("neck").z-=3*a;p("head").xRot+=.15F*a;p("jaw").xRot=.65F*ease((t-7)/6)*(1-ease((t-14)/6));}
   case POUNCE -> {
    float prep=ease(t/13)*(1-ease((t-14)/3));p("pelvis").y+=3*prep;
    airborne=ease((t-14)/3)*(1-ease((t-28)/10));
    for(String side:List.of("L","R"))p("upper"+side).xRot-=.5F*airborne;
    p("jaw").xRot=.35F*airborne;
   }
   case FLANK -> {p("chest").zRot+=.12F*ease(t/7)*(1-ease((t-24)/8));p("jaw").xRot=.5F*ease((t-18)/5)*(1-ease((t-24)/7));}
   case GRAB -> {float hold=ease((t-21)/4)*(1-ease((t-45)/10));if(held){for(String side:List.of("L","R")){p("upper"+side).xRot-=1.0F*hold;p("fore"+side).xRot-=.9F*hold;p("upper"+side).zRot=(side.equals("L")?1:-1)*.25F*hold;}p("head").xRot+=.22F*hold;p("jaw").xRot=.55F*ease((t-24)/4)*(1-ease((t-30)/6));}else if(t<22){p("chest").xRot+=.12F*ease(t/13);p("jaw").xRot=.3F;}}
   case SNIFF -> {float a=Mth.sin(t*Mth.PI/18);p("head").xRot+=.22F*a;p("neck").z-=a;p("nose").y+=Mth.sin(t*1.1F)*.08F;}
   case LISTEN -> {p("earL").zRot-=.18F;p("earR").zRot+=.18F;p("head").yRot+=Mth.sin(t*Mth.PI/24)*.25F;}
   case SNARL -> {p("jaw").xRot=.35F*ease(t/6)*(1-ease((t-16)/9));p("head").xRot+=.1F;}
   case HOWL_SHORT -> {float a=ease(t/12)*(1-ease((t-31)/11));p("neck").xRot-=.3F*a;p("head").xRot-=.4F*a;p("jaw").xRot=.5F*a;}
   case ALERT -> {p("head").xRot-=.1F*ease(t/8);p("earL").zRot-=.1F;p("earR").zRot+=.1F;}
   case ROAR -> {float a=ease((t-5)/7)*(1-ease((t-19)/7));p("neck").xRot-=.4F*a;p("head").xRot-=.35F*a;p("jaw").xRot=.65F*a;}
   case STAGGER -> {p("chest").xRot+=.12F*(1-ease(t/20));p("head").xRot-=.15F;}
   case ESCAPE -> {
    float fall=ease(t/19)*(1-ease((t-60)/27));
    p("root").zRot=-1.3F*fall;p("root").y-=12*Mth.sin(1.3F*fall)-.7F*ease(fall)*ease(fall);
    p("head").xRot+=.22F*fall;p("earL").xRot+=.5F*fall;p("earR").xRot+=.5F*fall;
    if(t>=19&&t<49){p("eyeL").visible=false;p("eyeR").visible=false;}
    if(t>=49&&t<87)p("chest").y+=Mth.sin((t-49)*.4F)*.18F*fall;
    if(t>=87){float a=1-ease((t-102)/10);p("head").xRot-=.4F*a;p("jaw").xRot=.55F*a;}
   }
   default -> {}
  }
  stabilize();
 }
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){p("root").render(pose,out,light,overlay,color);}
}
