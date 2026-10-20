package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slavicmyths.skim.SkimAction;
/** Model-only #77 animation adapter. Frozen server action clocks and collision never change. */
final class SkimBeastPose {
 private final Map<String,ModelPart> n;private final Map<String,String> parent=new HashMap<>();private final Set<String> neckBones=new HashSet<>();
 SkimBeastPose(Map<String,ModelPart> nodes){n=nodes;parent.putAll(SkimMesh.parents());for(String name:parent.keySet())for(String at=name;at!=null;at=parent.get(at))if(at.equals("neck")){neckBones.add(name);break;}}

 private ModelPart p(String name){return n.get(name);}private static float ramp(float t,float a,float b){float f=Mth.clamp((t-a)/(b-a),0,1);return f*f*(3-2*f);}
 private static float key(float t,float[] ts,float... vs){if(t<=ts[0])return vs[0];for(int i=1;i<ts.length;i++)if(t<=ts[i])return Mth.lerp(ramp(t,ts[i-1],ts[i]),vs[i-1],vs[i]);return vs[vs.length-1];}
 private void x(String name,float degrees){p(name).xRot+=degrees*Mth.DEG_TO_RAD;}private void y(String name,float degrees){p(name).yRot+=degrees*Mth.DEG_TO_RAD;}
 private PoseStack through(String name){var chain=new ArrayList<String>();for(String at=name;at!=null;at=parent.get(at))chain.add(at);Collections.reverse(chain);var pose=new PoseStack();for(String at:chain)p(at).translateAndRotate(pose);return pose;}
 private Vector3f point(String name,float x,float y,float z){return through(name).last().pose().transformPosition(new Vector3f(x/16,y/16,z/16)).mul(16);}
 /** Uses actual authored joint lengths, arbitrary parent yaw/roll, and a world-flat terminal pad. */
 private void support(String leg,float targetY,float targetZ,float weight){
  float targetX=leg.startsWith("right")?-(leg.endsWith("front")?13:10):(leg.endsWith("front")?13:10);reach(leg,targetX,targetY,targetZ,weight);
 }
 private void reach(String leg,float targetX,float targetY,float targetZ,float weight){
  if(weight<=0)return;var a=p(leg);var b=p(leg+"_shin");var tip=p(leg+"_paw");
  var local=new Matrix4f(through(parent.get(leg)).last().pose()).invert().transformPosition(new Vector3f(targetX/16,(targetY-SkimMesh.sole(leg))/16,targetZ/16)).mul(16);
  float dx=local.x-a.x,dy=local.y-a.y,dz=local.z-a.z,l1=b.getInitialPose().y,l2=tip.getInitialPose().y;
  float r=Mth.clamp(Mth.sqrt(dx*dx+dy*dy+dz*dz),Math.abs(l1-l2)+.001F,l1+l2-.001F);
  float roll=(float)Math.atan2(-dx,dy);
  // Preserve the anatomical bend plane as a paw rises above its shoulder; do not roll a limb180degrees.
  if(roll>Mth.PI*.5F)roll-=Mth.PI;else if(roll<-Mth.PI*.5F)roll+=Mth.PI;
  float planarY=-Mth.sin(roll)*dx+Mth.cos(roll)*dy,base=(float)Math.atan2(dz,planarY);
  float bend=-(float)Math.acos(Mth.clamp((r*r-l1*l1-l2*l2)/(2*l1*l2),-1,1));
  float shoulder=base+(float)Math.acos(Mth.clamp((l1*l1+r*r-l2*l2)/(2*l1*r),-1,1));
  a.xRot=Mth.lerp(weight,a.xRot,shoulder);a.yRot=Mth.lerp(weight,a.yRot,0);a.zRot=Mth.lerp(weight,a.zRot,roll);b.xRot=Mth.lerp(weight,b.xRot,bend);b.yRot=b.zRot=0;
  // ModelPart uses Rz*Ry*Rx. JOML1.10.5 Quaternion.getEulerAnglesZYX fails a combined-axis roundtrip;
  // extract the inverse matrix directly, otherwise a yawed paw penetrates the floor.
  var inverse=new Matrix4f(through(leg+"_shin").last().pose()).invert();
  float rx=(float)Math.atan2(inverse.m12(),inverse.m22()),ry=(float)Math.asin(Mth.clamp(-inverse.m02(),-1,1)),rz=(float)Math.atan2(inverse.m01(),inverse.m00());
  tip.xRot=Mth.lerp(weight,tip.xRot,rx);tip.yRot=Mth.lerp(weight,tip.yRot,ry);tip.zRot=Mth.lerp(weight,tip.zRot,rz);
 }
 /** Preserve the exact server tether at1.7forward/.55feet up; mouth grips the rear upper-leg surface, leaving the standing victim head clear of the skull. */
 private void alignGrip(float weight){
  var contact=point("jaw",0,-.3F,-11).add(point("head",0,5.4F,-13)).mul(.5F);var target=new Vector3f(0,24-.55F*16-7*.9375F,-1.7F*16+2*.9375F);var inverse=new Matrix4f(through("body").last().pose()).invert();
  var from=inverse.transformPosition(contact.div(16),new Vector3f()).mul(16);var to=inverse.transformPosition(target.div(16),new Vector3f()).mul(16);var delta=to.sub(from).mul(weight);
  var neck=p("neck");neck.x+=delta.x;neck.y+=delta.y;neck.z+=delta.z;
 }
 public void apply(SkimAction action,float t,float swing,float amount,float age,float yaw,float pitch,boolean rearAllowed,float hurt){
  float gait=Math.min(1,amount),cycle=swing*.45F;float roar=action==SkimAction.ROAR?ramp(t,0,18)-ramp(t,31,58):0,rear=rearAllowed?roar:0;
  float held=action==SkimAction.GRIP?ramp(t,14,18)*(1-ramp(t,44,49)):0;
  float locked=Math.max(held,roar);
  y("head",Mth.clamp(yaw,-35,35)*(1-locked));x("head",Mth.clamp(pitch,-20,20)*(1-locked));x("body",Mth.sin(age*.07F)*.65F);
  for(int i=0;i<5;i++)y("tail_"+i,Mth.sin(age*.075F-i*.3F)*7);for(int i=0;i<24;i++)x("mane_"+i,Mth.sin(age*.05F-i*.2F)*2.2F);x("jaw",10+Mth.sin(age*.05F)*2);
  float air=0;
  switch(action){
   case AWAKEN->{x("neck",key(t,new float[]{0,20,40,60},20,0,-12,0));x("jaw",key(t,new float[]{0,20,40,60},0,15,30,6));}
   case CLAWS->{x("right_front",key(t,new float[]{0,8,11,16,24,30,39},0,-35,40,0,-45,55,0));x("left_front",key(t,new float[]{0,11,16,19,24,30,39},0,0,-35,45,-45,55,0));y("body",key(t,new float[]{0,11,19,30,39},0,12,-12,15,0));}
   case LEAP->{float compress=ramp(t,0,12)-ramp(t,12,16);p("body").y+=4*compress;air=ramp(t,12,16)-ramp(t,27,32);x("hips",key(t,new float[]{0,12,18,29,44},0,20,-12,15,0));x("right_front",-45*air);x("left_front",-45*air);}
   case WHISTLE->{x("neck",-18*(ramp(t,0,14)-ramp(t,31,48)));x("jaw",22*(ramp(t,0,14)-ramp(t,31,48)));}
   case ROAR->{if(!rearAllowed)p("body").y+=2*(ramp(t,0,18)-ramp(t,31,58));x("body",-24*rear);p("body").y-=6.5F*rear;x("right_front",-65*rear);x("left_front",-65*rear);x("neck",-18*(ramp(t,0,18)-ramp(t,31,58)));x("jaw",37*(ramp(t,14,26)-ramp(t,31,48)));}
   case FLANK->{y("body",35*(ramp(t,0,10)-ramp(t,26,38)));x("jaw",25*(ramp(t,10,22)-ramp(t,26,38)));}
   case GRIP->{p("jaw").xRot=key(t,new float[]{0,14,18,30,44,45,65},6,43,30,34,32,43,6)*Mth.DEG_TO_RAD;x("neck",key(t,new float[]{0,14,18,30,44,45,65},0,20,-10,-10,-10,20,0));}
   case RAGE->{y("head",Mth.sin(t*.35F)*10*(1-ramp(t,50,64)));x("jaw",35*(ramp(t,0,13)-ramp(t,50,64)));}
   case EXHAUSTED->{float k=ramp(t,0,10)-ramp(t,55,70);p("body").y+=6*k;x("neck",18*k);p("jaw").xRot=(15+Mth.sin(t*.3F)*6)*Mth.DEG_TO_RAD;}
   case STAGGER->{y("head",Mth.sin(t*.4F)*12*(1-ramp(t,30,40)));x("neck",15*(1-ramp(t,30,40)));}
   case DEATH->{p("body").y+=15*ramp(t,0,52);p("body").zRot=85*ramp(t,26,75)*Mth.DEG_TO_RAD;p("jaw").xRot=22*ramp(t,0,40)*Mth.DEG_TO_RAD;}
   default->{}
  }
  if(hurt>0){x("head",Mth.sin(hurt*.4F)*5*(1-locked));p("body").zRot+=Mth.sin(hurt*.4F)*4*Mth.DEG_TO_RAD*(1-locked);}
  if(rear>0)rearNeckFit(rear);
  // Keep hind targets reachable after torso yaw/rearing; lower the same body, never stretch a leg.
  if(action!=SkimAction.DEATH){
   float drop=0;
   for(String side:List.of("right","left")){
    String leg=side+"_back";float phase=cycle+(side.equals("right")?0:Mth.PI),s=Mth.sin(phase)*gait;
    var at=point(leg,0,0,0);float tx=side.equals("right")?-10:10,tz=20-s*4,ty=24-Math.max(0,-s)*3-air*4-SkimMesh.sole(leg);
    float reach=p(leg+"_shin").getInitialPose().y+p(leg+"_paw").getInitialPose().y-.02F;
    float vertical=Mth.sqrt(Math.max(0,reach*reach-(tx-at.x)*(tx-at.x)-(tz-at.z)*(tz-at.z)));
    drop=Math.max(drop,ty-at.y-vertical);
   }
   p("body").y+=drop;
  }
  for(String side:List.of("right","left")){
   float phase=cycle+(side.equals("right")?0:Mth.PI);float s=Mth.sin(phase)*gait;
   support(side+"_back",24-Math.max(0,-s)*3-air*4,20-s*4,1);
   float weight=1-rear;if(action==SkimAction.CLAWS)weight=side.equals("right")?1-(ramp(t,0,8)-ramp(t,33,39)):1-(ramp(t,11,16)-ramp(t,33,39));
   if(action==SkimAction.DEATH)weight=1;
   if(weight<1){var shin=p(side+"_front_shin");shin.xRot-=.7F*(1-weight);}
   support(side+"_front",24-Math.max(0,s)*3.5F-air*6,-20.5F+s*4,weight);
   if(weight<1){String leg=side+"_front";var at=point(leg+"_paw",0,0,0);float z=at.z;
    if(rear>0)z=Mth.lerp(ramp(rear,0,.5F),z,Math.max(z,-2.6F*16+.2F+SkimMesh.frontPawReach()));
    reach(leg,at.x,Math.min(23.98F,at.y+SkimMesh.sole(leg)),z,1);
   }
  }
  if(action==SkimAction.GRIP){
   float hold=ramp(t,14,18)*(1-ramp(t,44,49));alignGrip(hold);
   // The bulky collar compacts against the shoulders when the head crouches to grip the leg.
   for(int i=0;i<24;i++)p("mane_"+i).y-=2.5F*hold;
  }
  if(action==SkimAction.DEATH){
   float collapse=ramp(t,26,75);
   for(String side:List.of("right","left"))for(String end:List.of("front","back")){
    String leg=side+"_"+end;float upper=end.equals("front")?.65F:1.0F,lower=end.equals("front")?-1.3F:-1.8F;
    var a=p(leg);var b=p(leg+"_shin");var paw=p(leg+"_paw");a.xRot=Mth.lerp(collapse,a.xRot,upper);a.yRot*=1-collapse;a.zRot*=1-collapse;b.xRot=Mth.lerp(collapse,b.xRot,lower);paw.xRot=Mth.lerp(collapse,paw.xRot,-upper-lower);paw.yRot*=1-collapse;paw.zRot*=1-collapse;
   }
   floor();
  }
 }
 /** Keep the reared head/fangs inside the EXISTING server ceiling-check footprint, without changing AABB.
  * Fit becomes complete before the tusks can rise above the2.95block standing clearance. */
 private void rearNeckFit(float rear){
  float front=Float.MAX_VALUE;var matrices=new HashMap<String,Matrix4f>();
  for(var shape:SkimMesh.bounds())if(neckBones.contains(shape.name())){
   var matrix=matrices.computeIfAbsent(shape.name(),name->new Matrix4f(through(name).last().pose()));var b=shape.box();
   for(int corner=0;corner<8;corner++)front=Math.min(front,matrix.transformPosition(new Vector3f((b[0]+((corner&1)==0?0:b[3]))/16,(b[1]+((corner&2)==0?0:b[4]))/16,(b[2]+((corner&4)==0?0:b[5]))/16)).z*16);
  }
  float shift=Math.max(0,-2.6F*16+.2F-front)*ramp(rear,0,.35F);
  var delta=new Matrix4f(through("body").last().pose()).invert().transformDirection(new Vector3f(0,0,shift));
  p("neck").x+=delta.x;p("neck").y+=delta.y;p("neck").z+=delta.z;
 }
 /** Actual ModelPart cube bounds, only the bounded DEATH clip; no world reads. */
 private void floor(){
  float bottom=-Float.MAX_VALUE;
  // Generated shape corners are exposed as a compact array, avoiding client reflection.
  var matrices=new HashMap<String,Matrix4f>();
  for(var shape:SkimMesh.bounds()){
   var matrix=matrices.computeIfAbsent(shape.name(),name->new Matrix4f(through(name).last().pose()));var b=shape.box();
   for(int corner=0;corner<8;corner++){var v=matrix.transformPosition(new Vector3f((b[0]+((corner&1)==0?0:b[3]))/16,(b[1]+((corner&2)==0?0:b[4]))/16,(b[2]+((corner&4)==0?0:b[5]))/16));bottom=Math.max(bottom,v.y*16);}
  }
  p("root").y+=24-bottom;
 }
}
