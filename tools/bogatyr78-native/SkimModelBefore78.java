package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.skim.*;
public final class SkimModelBefore78<T extends SkimActor> extends EntityModel<T> {
 public final ModelPart root;public final Map<String,ModelPart> nodes=new HashMap<>();private final boolean beast;private final SkimBeastPose beastPose;
 public SkimModelBefore78(ModelPart root,boolean beast){this.root=root;this.beast=beast;List<String> names=new ArrayList<>(List.of("hips","body","neck","head","jaw","quiver","saber","bow","string","draw_arrow","back_bow","flask"));for(var side:List.of("right","left"))for(var part:List.of("thigh","shin","foot","shoulder","hand","flap","front","front_shin","front_paw","back","back_shin","back_paw"))names.add(side+"_"+part);for(int i=0;i<24;i++){names.add("mane_"+i);names.add("tail_"+i);names.add("cloak_"+i);names.add("scarf_"+i);}if(beast)names.addAll(SkimMesh.names());index("root",root,new ArrayList<>(new LinkedHashSet<>(names)));beastPose=beast?new SkimBeastPose(nodes):null;}
 private void index(String key,ModelPart p,List<String> names){nodes.put(key,p);for(var n:names)if(p.hasChild(n))index(n,p.getChild(n),names);}
 private void x(String k,float d){var p=nodes.get(k);if(p!=null)p.xRot=d*Mth.DEG_TO_RAD;}private void y(String k,float d){var p=nodes.get(k);if(p!=null)p.yRot=d*Mth.DEG_TO_RAD;}private void z(String k,float d){var p=nodes.get(k);if(p!=null)p.zRot=d*Mth.DEG_TO_RAD;}
 /** Visual-only glow envelopes from the approved1.7.3 passport; no server state changes. */
 public static float rageGlow(SkimAction action,float t){return action==SkimAction.RAGE?ramp(t,13,20)*(1-ramp(t,50,64)):0;}
 public static float eyeGlow(SkimAction action,float t){return action==SkimAction.DEATH?.30F*(1-ramp(t,26,75)):.30F+.39F*rageGlow(action,t);}
 private static float ramp(float t,float a,float b){float f=Math.clamp((t-a)/(b-a),0,1);return f*f*(3-2*f);}private static float key(float t,float[] ts,float... vs){if(t<=ts[0])return vs[0];for(int i=1;i<ts.length;i++)if(t<=ts[i])return Mth.lerp(ramp(t,ts[i-1],ts[i]),vs[i-1],vs[i]);return vs[vs.length-1];}
 @Override public void setupAnim(T e,float swing,float amount,float age,float yaw,float pitch){if(beast&&e instanceof SkimBeastEntity b){poseBeast(e.action(),e.clipAge(age-e.tickCount),swing,amount,age,yaw,pitch,b.canRear(),e.hurtTime);return;}pose(e.action(),e.clipAge(age-e.tickCount),swing,amount,age,yaw,pitch);if(e.hurtTime>0){x("head",Mth.sin(e.hurtTime*.4F)*5);z("body",Mth.sin(e.hurtTime*.4F)*4);}}
 public void poseBeast(SkimAction a,float t,float swing,float amount,float age,float yaw,float pitch,boolean rearAllowed,float hurt){if(!beast)throw new IllegalStateException("Beast pose on Alyosha");root.getAllParts().forEach(ModelPart::resetPose);beastPose.apply(a,t,swing,amount,age,yaw,pitch,rearAllowed,hurt);}
 public void pose(SkimAction a,float t,float swing,float amount,float age,float yaw,float pitch){if(beast){poseBeast(a,t,swing,amount,age,yaw,pitch,true,0);return;}root.getAllParts().forEach(ModelPart::resetPose);float gait=Mth.cos(swing*(beast?.45F:.8F))*Math.clamp(amount,0,1);y("head",Math.clamp(yaw,-35,35));x("head",Math.clamp(pitch,-20,20));
  {for(int sign:new int[]{-1,1}){var k=sign<0?"right":"left";x(k+"_thigh",gait*sign*28);x(k+"_shin",Math.max(0,-gait*sign)*25);x(k+"_shoulder",-gait*sign*12);}x("body",Mth.sin(age*.07F)*1.5F);for(int i=0;i<3;i++){x("cloak_"+i,5+Math.abs(gait)*9+Mth.sin(age*.07F-i*.4F)*3);z("scarf_"+i,Mth.sin(age*.1F-i*.4F)*6);}
   x("right_hand",a==SkimAction.BOW||a==SkimAction.DRINK?0:-90);
   nodes.get("back_bow").visible=a!=SkimAction.BOW;nodes.get("saber").visible=a!=SkimAction.BOW&&a!=SkimAction.DRINK;nodes.get("bow").visible=a==SkimAction.BOW;nodes.get("draw_arrow").visible=a==SkimAction.BOW&&t>=6&&t<29;nodes.get("flask").visible=a==SkimAction.DRINK;
   switch(a){
    case TAUNT->{x("left_shoulder",key(t,new float[]{0,4,8,11,16,22,26},0,-35,-70,-70,-35,-15,0));z("right_shoulder",20*(ramp(t,0,8)-ramp(t,16,26)));x("head",-8*(ramp(t,0,4)-ramp(t,16,26)));}
    case DODGE->{z("body",key(t,new float[]{0,2,4,7,8,12},0,12,25,25,12,0));x("right_thigh",-40);x("left_thigh",25);x("right_shin",65);}
    case SABER->{x("right_shoulder",key(t,new float[]{0,4,6,8,11,13,16,18,19,22},0,45,-70,35,-65,40,-80,35,-60,0));y("body",key(t,new float[]{0,6,11,16,19,22},0,15,-15,20,-20,0));}
    case FEINT->{x("right_shoulder",key(t,new float[]{0,6,7,12,14,18},0,-100,-70,-40,-80,0));z("body",12*(ramp(t,7,10)-ramp(t,14,18)));}
    case BOW->{x("left_shoulder",-90*(ramp(t,0,6)-ramp(t,29,35)));x("right_shoulder",-90*(ramp(t,0,6)-ramp(t,29,35)));y("right_shoulder",-45);float draw=(ramp(t,6,11)-ramp(t,11,13))+(ramp(t,13,17)-ramp(t,17,19))+(ramp(t,19,23)-ramp(t,23,29));nodes.get("string").z-=3*draw;nodes.get("draw_arrow").z-=3*draw;}
    case COUNTER->{y("body",-18*(ramp(t,0,3)-ramp(t,10,16)));x("right_shoulder",key(t,new float[]{0,3,8,10,16},0,40,-75,-25,0));}
    case RETREAT->{x("body",14);x("right_shoulder",-50);x("left_shoulder",-25);}
    case DRINK,REST->{nodes.get("hips").y+=5;x("right_thigh",-60);x("right_shin",100);x("left_thigh",-25);x("left_shin",80);if(a==SkimAction.DRINK){x("right_shoulder",-120*(ramp(t,5,16)-ramp(t,30,46)));x("flask",45*(ramp(t,16,18)-ramp(t,30,46)));}}
    case HURT->{x("body",Mth.sin(t*.4F)*-8);}
    case ARRIVE->{root.yScale=ramp(t,0,20);}
    case RECALL->{root.yScale=1-ramp(t,0,18);}
    default->{}
   }
  }
 }
 @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int l,int o,int c){root.render(p,v,l,o,c);}
}
