package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.marinka.*;
/** Native rig keyframes. No particles stand in for body animation; server clips own hit frames. */
public final class WitchModel<T extends LivingEntity> extends EntityModel<T> {
 public final ModelPart root;public final Map<String,ModelPart> nodes=new HashMap<>();public final int form;
 private static final List<String> NAMES=List.of("body","bottle","braid_0","braid_1","braid_2","braid_3","braid_4","braid_5","cloak_0","cloak_1","cloak_2","ember","hair_0","hair_1","hair_2","hair_3","hair_4","hair_5","hair_6","head","hem_0","hem_1","hem_2","hem_3","hem_4","hem_5","hem_6","hem_7","hips","hump","left_arm","left_back","left_back_hoof","left_foot","left_front","left_front_hoof","left_hand","left_horn","left_shin","left_shoulder","left_thigh","left_tip","left_wing","mantle","neck","right_arm","right_back","right_back_hoof","right_foot","right_front","right_front_hoof","right_hand","right_horn","right_shin","right_shoulder","right_thigh","right_tip","right_wing","ring_0","ring_1","ring_2","shield","spear","tail");
 public WitchModel(ModelPart root,int form){this.root=root;this.form=form;index("root",root);}
 private void index(String key,ModelPart p){nodes.put(key,p);for(String n:NAMES)if(p.hasChild(n))index(n,p.getChild(n));}
 private ModelPart n(String name){return nodes.get(name);}private void rx(String n,float deg){if(n(n)!=null)n(n).xRot=rad(deg);}private void ry(String n,float deg){if(n(n)!=null)n(n).yRot=rad(deg);}private void rz(String n,float deg){if(n(n)!=null)n(n).zRot=rad(deg);}
 public static float rad(float d){return d*Mth.DEG_TO_RAD;}private static float ramp(float t,float a,float b){float u=Math.clamp((t-a)/(b-a),0,1);return u*u*(3-2*u);}private static float key(float t,float[] ts,float[] values){if(t<=ts[0])return values[0];for(int i=1;i<ts.length;i++)if(t<=ts[i])return Mth.lerp(ramp(t,ts[i-1],ts[i]),values[i-1],values[i]);return values[values.length-1];}
 @Override public void setupAnim(T e,float swing,float movement,float age,float yaw,float pitch){WitchAction a=e instanceof WitchActor w?w.action():WitchAction.IDLE;float t=e instanceof WitchActor w?w.age(age-e.tickCount)*(w.length()>0?w.action().ticks/(float)w.length():1):age;pose(a,t,swing,movement,age,yaw,pitch);if(e.hurtTime>0){float force=e instanceof WitchActor w?w.flinch():1;rx("body",(float)Math.sin(e.hurtTime*.4)*-7*force);rx("head",5*force);}}
 public void pose(WitchAction a,float t,float swing,float movement,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);float gait=Mth.cos(swing*.7F)*Math.clamp(movement,0,1);ry("head",Math.clamp(yaw,-45,45));rx("head",Math.clamp(pitch,-20,20));
  if(form==0||form==1){rx("body",Mth.sin(age*.07F)*1.2F);for(int sign:new int[]{-1,1}){String tag=sign<0?"right":"left";rx(tag+"_thigh",gait*sign*21);rx(tag+"_shin",Math.max(0,-gait*sign)*16);rx(tag+"_shoulder",gait*sign*-5);}
   for(int i=0;i<7;i++){rx("hair_"+i,Mth.sin(age*.09F+i*.6F)*4);rx("braid_"+i,Mth.sin(age*.06F-i*.5F)*5);}for(int i=0;i<8;i++)rx("hem_"+i,Mth.sin(age*.08F+i)*3+gait*2);for(int i=0;i<3;i++)rx("cloak_"+i,5+Math.abs(gait)*8+Mth.sin(age*.07F+i)*2);
  }
  if(form==0)hero(a,t,age);else if(form==1)witch(a,t,age);else if(form==2){for(int sign:new int[]{-1,1})for(int i=0;i<2;i++)rx((sign<0?"right":"left")+(i==0?"_front":"_back"),gait*sign*(i==0?1:-1)*23);rx("tail",Mth.sin(age*.08F)*8);if(a==WitchAction.BULL_CHARGE){rx("head",20*(ramp(t,0,7)-ramp(t,21,28)));rx("body",4);}}else if(form==3){rx("right_wing",Mth.sin(age*.55F)*40);rx("left_wing",-Mth.sin(age*.55F)*40);rz("right_tip",Mth.sin(age*.55F-.3F)*18);rz("left_tip",-Mth.sin(age*.55F-.3F)*18);rx("tail",7+Mth.sin(age*.13F)*4);}else{for(int i=0;i<3;i++)ry("ring_"+i,age*(i%2==0?1:-1)*2);}
 }
 private void hero(WitchAction a,float t,float age){n("bottle").visible=a==WitchAction.DRINK;n("spear").visible=a!=WitchAction.DRINK&&a!=WitchAction.INCAPACITATED;rx("spear",-12);rx("shield",0);rx("left_shoulder",-12);
 switch(a){
 case SIDESTEP->{rz("body",12*(ramp(t,0,4)-ramp(t,4,8)));rx("right_shin",20);rx("left_shin",20);}
 case PARRY->{rx("left_shoulder",key(t,new float[]{0,2,4,6,10,13},new float[]{-12,-55,-55,-40,-20,-12}));ry("body",-14*(ramp(t,0,4)-ramp(t,6,13)));rx("right_shoulder",key(t,new float[]{0,4,6,10,13},new float[]{0,-35,-20,-68,0}));rx("spear",key(t,new float[]{0,4,6,10,13},new float[]{-12,-50,-35,30,-12}));}
 case COMBO->{rx("right_shoulder",key(t,new float[]{0,3,6,10,13,18,23,30},new float[]{0,32,-62,25,-65,35,-78,0}));ry("body",key(t,new float[]{0,6,13,23,30},new float[]{0,18,-18,28,0}));rx("spear",key(t,new float[]{0,3,6,10,13,18,23,30},new float[]{-12,-50,70,-50,70,-60,75,-12}));}
 case THRUST->{rx("right_shoulder",key(t,new float[]{0,3,8,10,18},new float[]{0,-25,-90,-90,0}));rx("spear",key(t,new float[]{0,3,8,10,18},new float[]{-12,-55,4,4,-12}));if(n("spear")!=null)n("spear").z-=16*(ramp(t,3,8)-ramp(t,10,18));rx("body",12*(ramp(t,0,8)-ramp(t,10,18)));}
 case SWEEP->{ry("body",key(t,new float[]{0,5,12,19,25},new float[]{0,-75,115,200,0}));rz("right_shoulder",-70*(ramp(t,0,5)-ramp(t,19,25)));rz("spear",80*(ramp(t,0,5)-ramp(t,19,25)));rx("left_shoulder",-35);}
 case WARD->{rx("left_shoulder",-65*(ramp(t,0,4)-ramp(t,16,21)));rx("shield",60*(ramp(t,0,4)-ramp(t,16,21)));rx("body",8);}
 case UNRAVEL->{rx("right_shoulder",key(t,new float[]{0,5,12,13,22},new float[]{0,-120,-120,-80,0}));rx("spear",key(t,new float[]{0,5,12,13,22},new float[]{-12,-35,-35,15,-12}));rx("left_shoulder",-55);}
 case TRANSFORM->{n("hips").y+=8*ramp(t,0,20);rx("right_shin",60*ramp(t,0,20));rx("left_shin",60*ramp(t,0,20));rx("body",35*ramp(t,0,20));rx("spear",-30);}
 case REVERT->{n("hips").y+=8*(1-ramp(t,0,24));rx("right_shin",60*(1-ramp(t,0,24)));rx("left_shin",60*(1-ramp(t,0,24)));}
 case RETREAT->{rx("left_shoulder",-50);rx("body",12);rx("spear",-25);}
 case DRINK,RECOVER,INCAPACITATED->{n("hips").y+=5;rx("right_thigh",-60);rx("left_thigh",-25);rx("right_shin",100);rx("left_shin",80);if(a==WitchAction.DRINK){rx("right_shoulder",key(t,new float[]{0,11,19,32,43,50,63},new float[]{0,-100,-125,-125,-80,-20,0}));rx("bottle",45*(ramp(t,11,19)-ramp(t,43,50)));}}
 case RALLY->{n("hips").y+=5*(1-ramp(t,0,12));rx("right_shin",80*(1-ramp(t,0,12)));rx("right_shoulder",-25*ramp(t,6,12));}
 case SALUTE->{rx("right_shoulder",-65*(ramp(t,0,14)-ramp(t,22,34)));rx("head",8*(ramp(t,14,22)-ramp(t,22,34)));}
 default->{}
 }}
 private void witch(WitchAction a,float t,float age){float k;switch(a){
 case INTRO->{rx("left_shoulder",-35*ramp(t,10,32));ry("head",Mth.sin(t*.04F)*15);}
 case TRACE->{rx("body",22*(ramp(t,0,11)-ramp(t,20,29)));rx("right_shoulder",key(t,new float[]{0,5,11,20,29},new float[]{0,25,-30,-90,0}));}
 case FIRE->{rx("left_shoulder",key(t,new float[]{0,5,10,11,16,22},new float[]{0,-45,-80,-90,-40,0}));ry("body",20*(ramp(t,0,10)-ramp(t,11,22)));}
 case LURE->{rx("head",-15*(ramp(t,0,9)-ramp(t,30,40)));rx("right_shoulder",-80*(ramp(t,0,20)-ramp(t,30,40)));rx("left_shoulder",-80*(ramp(t,0,20)-ramp(t,30,40)));}
 case SHADOW->{rx("right_shoulder",-120*(ramp(t,0,4)-ramp(t,12,18)));rx("body",12*(ramp(t,0,8)-ramp(t,12,18)));for(int i=0;i<8;i++)rz("hem_"+i,(i%2==0?1:-1)*12*ramp(t,0,12));}
 case VEIL->{rx("right_shoulder",-65*(ramp(t,0,8)-ramp(t,12,17)));rx("left_shoulder",-65*(ramp(t,0,8)-ramp(t,12,17)));rz("right_arm",25);rz("left_arm",-25);}
 case CRACK->{rx("body",-13*(ramp(t,0,4)-ramp(t,8,12)));rx("head",8);}
 case BLINK->{ry("head",-30*ramp(t,0,3));rx("right_shoulder",-75*(ramp(t,0,7)-ramp(t,11,16)));}
 case BIRD_CHANGE->{rx("right_shoulder",-65*ramp(t,0,9));rx("left_shoulder",-65*ramp(t,0,9));root.yScale=1-.35F*ramp(t,0,9);}
 case BIRD_RETURN->{root.yScale=.65F+.35F*ramp(t,0,9);}
 case CURSE->{rx("body",25*(ramp(t,0,9)-ramp(t,33,47)));rx("right_shoulder",key(t,new float[]{0,9,17,25,33,47},new float[]{0,35,-35,-100,-100,0}));rx("left_shoulder",key(t,new float[]{0,9,17,25,33,47},new float[]{0,20,-35,-100,-100,0}));}
 case HERD->{rx("right_shoulder",-65*(ramp(t,0,9)-ramp(t,19,24)));rx("head",10);}
 case RAGE->{rx("body",Mth.sin(t*.2F)*6*(1-ramp(t,32,42)));rx("right_shoulder",-80*(ramp(t,0,20)-ramp(t,32,42)));rx("left_shoulder",-80*(ramp(t,0,20)-ramp(t,32,42)));}
 case EXHAUSTED->{rx("body",25);rx("head",12);rx("left_shoulder",-22);}
 case DEFEAT->{n("hips").y+=8*ramp(t,0,22);rx("right_shin",95*ramp(t,0,22));rx("left_shin",95*ramp(t,0,22));rx("body",40*ramp(t,10,35));rz("head",20*ramp(t,20,45));n("ember").visible=t<35;}
 case CLONE_BREAK->{root.yScale=1-.85F*ramp(t,0,9);root.xScale=root.zScale=1+.25F*ramp(t,0,9);}
 default->{}
 }}
 @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
}
