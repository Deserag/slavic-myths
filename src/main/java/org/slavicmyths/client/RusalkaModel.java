package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import org.slavicmyths.water.*;

/** Dedicated tailless linen/hair rig; Vodyanoy keeps the original WaterSpiritModel. */
public final class RusalkaModel extends EntityModel<RusalkaEntity> {
 public static final int WIDTH=128,HEIGHT=64;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 public final Map<String,ModelPart> parts=new LinkedHashMap<>();
 private static final class Blend {int action=-1;float changedAt;final float[] last=new float[31*6],from=new float[31*6];}
 private final Map<UUID,Blend> blends=new LinkedHashMap<>(64,.75F,true);
 private ModelPart add(String name,String parent,int u,int v,float px,float py,float pz,float x,float y,float z,float w,float h,float d){
  ModelPart part=geometry.part(WIDTH,HEIGHT,u,v);geometry.box(part,x,y,z,w,h,d);part.setPos(px,py,pz);part.setInitialPose(PartPose.offset(px,py,pz));
  parts.put(name,part);if(parent!=null)geometry.attach(parts.get(parent),part);return part;
 }
 public RusalkaModel(){
  add("body",null,32,0,0,0,0,-3.5F,0,-2,7,11,4);
  add("head","body",0,0,0,0,0,-3.5F,-8,-3.5F,7,8,7);
  add("cap","head",0,18,0,-8,0,-4,-1,-4,8,2,8);
  add("hairBack","head",0,30,0,-7,2,-4,0,0,8,9,2);
  add("hairMid","hairBack",22,30,0,8,0,-3.5F,0,.25F,7,6,2);
  add("hairTip","hairMid",42,30,0,5,0,-3.5F,0,.25F,7,5,2);
  add("hairL","head",62,18,3.1F,-7,-2.6F,-.2F,0,0,2,9,2);
  add("hairR","head",62,18,-3.1F,-7,-2.6F,-1.8F,0,0,2,10,2);
  add("hairLTip","hairL",72,18,0,8,0,-.2F,0,0,2,7,2);
  add("hairRTip","hairR",72,18,0,9,0,-1.8F,0,0,2,6,2);
  add("crown","head",84,18,0,-8,-3.6F,-3.5F,0,0,7,1,1);
  add("flowerL","crown",104,18,-2,-.5F,-.1F,-.5F,0,0,1,1,1);
  add("flowerR","crown",104,18,2,0,-.1F,-.5F,0,0,1,1,1);
  add("armL","body",56,0,3.5F,.3F,0,0,0,-1.5F,2.5F,7,3);
  add("armR","body",56,0,-3.5F,.3F,0,-2.5F,0,-1.5F,2.5F,7,3);
  add("foreL","armL",68,0,0,7,0,0,0,-1.5F,2.5F,5,3);
  add("foreR","armR",68,0,0,7,0,-2.5F,0,-1.5F,2.5F,5,3);
  add("handL","foreL",80,0,0,5,0,0,0,-1.5F,2.5F,2,3);
  add("handR","foreR",80,0,0,5,0,-2.5F,0,-1.5F,2.5F,2,3);
  add("legL","body",92,0,1.8F,11,0,-1.5F,0,-1.5F,3,11,3);
  add("legR","body",92,0,-1.8F,11,0,-1.5F,0,-1.5F,3,11,3);
  add("footL","legL",106,0,0,10,0,-1.5F,0,-3,3,3,5);
  add("footR","legR",106,0,0,10,0,-1.5F,0,-3,3,3,5);
  add("skirtFrontL","body",0,45,1.9F,10,-2.4F,-2.2F,0,-.4F,4.4F,10,1);
  add("skirtFrontR","body",12,45,-1.9F,10,-2.4F,-2.2F,0,-.4F,4.4F,9,1);
  add("skirtBackL","body",24,45,1.9F,10,2.4F,-2.2F,0,-.6F,4.4F,9,1);
  add("skirtBackR","body",36,45,-1.9F,10,2.4F,-2.2F,0,-.6F,4.4F,10,1);
  add("skirtSideL","body",58,45,3.6F,10,0,-.5F,0,-2.4F,1,9,4.8F);
  add("skirtSideR","body",72,45,-3.6F,10,0,-.5F,0,-2.4F,1,10,4.8F);
  add("hemL","skirtFrontL",48,45,0,9,0,-1.9F,0,-.4F,2.8F,1,1);
  add("hemR","skirtBackR",48,45,0,9,0,-.8F,0,-.6F,2.7F,1,1);
 }
 private ModelPart p(String name){return parts.get(name);}
 private static float sin(float v){return (float)Math.sin(v);}
 private static float ease(float v){v=Math.clamp(v,0,1);return v*v*(3-2*v);}
 /** Pure pose input, also exercised/exported by the offline native ModelPart fixture. */
 public void sample(int action,float time,float walk,float amount,float age,float yaw,float pitch,boolean water,float hurt,float death){
  parts.values().forEach(p->{p.resetPose();p.visible=true;p.skipDraw=false;});
  float wave=sin(age*.065F),stride=sin(walk*.5F)*Math.min(1,amount);
  p("body").y=wave*.08F;p("head").yRot=Math.clamp(yaw,-65,65)*.0174533F;p("head").xRot=Math.clamp(pitch,-45,45)*.0174533F;
  p("legL").xRot=stride*.38F;p("legR").xRot=-stride*.38F;
  p("armL").xRot=-stride*.12F;p("armR").xRot=stride*.12F;p("armL").zRot=.06F;p("armR").zRot=-.06F;
  if(water){p("body").xRot=1.18F;p("body").y=5+wave*.15F;p("legL").xRot=stride*.22F;p("legR").xRot=-stride*.22F;p("armL").xRot=-.5F+stride*.22F;p("armR").xRot=-.5F-stride*.22F;}
  switch(action){
   case RusalkaActions.OBSERVE -> {p("head").zRot=.14F+wave*.015F;p("armR").zRot=-.12F;}
   case RusalkaActions.SONG_START,RusalkaActions.LURE,RusalkaActions.INTENSE,RusalkaActions.WARNING -> {float w=action==RusalkaActions.SONG_START?ease(time/24):1;p("head").xRot-=w*.18F;p("head").zRot=.07F*w;p("armR").xRot=-.95F*w;p("armR").yRot=-.18F*w;p("foreR").xRot=-.25F*w;p("armL").xRot=-.15F*w;p("handR").zRot=-.1F+wave*.03F;}
   case RusalkaActions.BREAK -> {float w=ease(time/12);p("head").xRot=.28F*w;p("head").zRot=-.12F*w;p("armR").xRot=-.8F*(1-w);p("body").zRot=.025F;}
   case RusalkaActions.WINDUP -> {float w=ease(time/12);p("body").yRot=-.22F*w;p("armL").xRot=.55F*w;p("armR").xRot=.7F*w;p("foreL").xRot=-.8F*w;p("foreR").xRot=-.65F*w;p("head").xRot=.1F;}
   case RusalkaActions.GRAB -> {p("body").xRot-=.22F;p("body").yRot=.12F;p("armL").xRot=p("armR").xRot=-1.45F;p("armL").yRot=.12F;p("armR").yRot=-.12F;p("foreL").xRot=p("foreR").xRot=-.12F;}
   case RusalkaActions.RECOVERY -> {float w=1-ease(time/7);p("armL").xRot=-1.45F*w;p("armR").xRot=-1.45F*w;p("body").xRot-=.22F*w;}
   case RusalkaActions.DIVE -> {float w=ease(time/8);p("body").xRot=1.18F+.22F*w;p("armL").xRot=.35F;p("armR").xRot=.35F;p("legL").xRot=.12F;p("legR").xRot=-.12F;}
   default -> { }
  }
  if(action==RusalkaActions.INTENSE||action==RusalkaActions.WARNING){p("head").zRot=-.11F;p("armL").xRot=-.55F;p("foreL").xRot=-.35F;p("head").xRot+=sin(age*.11F)*.025F;}
  if(action==RusalkaActions.WARNING){p("head").xRot=.2F;p("armR").xRot=-1.25F;p("handR").xRot=.35F;}
  // Separate hinges and delayed phases give heavy hair and linen rather than rigid sleeves.
  p("hairBack").xRot=.04F+sin(age*.055F)*.025F-p("body").xRot*.22F;
  p("hairMid").xRot=.035F+sin(age*.055F-.6F)*.04F;p("hairTip").xRot=sin(age*.055F-1.2F)*.055F;
  p("hairL").zRot=.04F+wave*.02F;p("hairR").zRot=-.04F-wave*.02F;
  p("hairLTip").xRot=sin(age*.06F-.5F)*.04F;p("hairRTip").xRot=sin(age*.06F-1)*.04F;
  for(String n:List.of("skirtFrontL","skirtFrontR","skirtBackL","skirtBackR")){p(n).xRot=p("legL").xRot*.12F+sin(age*.06F+(n.endsWith("L")?0:1))*.025F;}
  p("skirtFrontL").zRot=-.025F;p("skirtFrontR").zRot=.025F;p("skirtBackL").zRot=-.025F;p("skirtBackR").zRot=.025F;
  p("skirtSideL").zRot=-.025F;p("skirtSideR").zRot=.025F;
  if(water||action==RusalkaActions.DIVE)p("head").xRot-=p("body").xRot*.8F;
  if(hurt>0){p("body").zRot+=sin(hurt*.6F)*.14F;p("head").xRot+=.15F;}
  if(death>0){float w=ease(death/20);p("body").zRot=.5F*w;p("body").y+=5*w;p("head").xRot=.4F*w;p("armL").xRot=.3F*w;p("armR").xRot=.3F*w;}
 }
 /** Per-entity, bounded transition memory: a shared renderer must never reuse another singer's pose. */
 public void animate(UUID id,int action,float time,float walk,float amount,float age,float yaw,float pitch,boolean water,float hurt,float death){
  Blend state=blends.get(id);
  if(state==null){if(blends.size()>=64)blends.remove(blends.keySet().iterator().next());state=new Blend();blends.put(id,state);}
  boolean first=state.action==-1;
  if(state.action!=action){System.arraycopy(state.last,0,state.from,0,state.last.length);state.changedAt=first?age-5:age;}
  sample(action,time,walk,amount,age,yaw,pitch,water,hurt,death);
  float blend=first||action==RusalkaActions.GRAB||action==RusalkaActions.BREAK?1:ease((age-state.changedAt)/5);
  int i=0;
  for(var p:parts.values()){
   p.x=state.from[i]+(p.x-state.from[i])*blend;state.last[i++]=p.x;
   p.y=state.from[i]+(p.y-state.from[i])*blend;state.last[i++]=p.y;
   p.z=state.from[i]+(p.z-state.from[i])*blend;state.last[i++]=p.z;
   p.xRot=state.from[i]+(p.xRot-state.from[i])*blend;state.last[i++]=p.xRot;
   p.yRot=state.from[i]+(p.yRot-state.from[i])*blend;state.last[i++]=p.yRot;
   p.zRot=state.from[i]+(p.zRot-state.from[i])*blend;state.last[i++]=p.zRot;
  }
  state.action=action;
 }
 @Override public void setupAnim(RusalkaEntity e,float walk,float amount,float age,float yaw,float pitch){
  animate(e.getUUID(),e.visualAction(),e.actionTime(age),walk,amount,age,yaw,pitch,e.isInWater(),e.hurtTime,e.deathTime);
 }
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertex,int light,int overlay,int color){p("body").render(pose,vertex,light,overlay,color);}
}
