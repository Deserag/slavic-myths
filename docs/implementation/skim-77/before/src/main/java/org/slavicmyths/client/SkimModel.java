package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.skim.*;
public final class SkimModel<T extends SkimActor> extends EntityModel<T> {
 public final ModelPart root;public final Map<String,ModelPart> nodes=new HashMap<>();private final boolean beast;
 public SkimModel(ModelPart root,boolean beast){this.root=root;this.beast=beast;List<String> names=new ArrayList<>(List.of("hips","body","neck","head","jaw","quiver","saber","bow","string","draw_arrow","back_bow","flask"));for(var side:List.of("right","left"))for(var part:List.of("thigh","shin","foot","shoulder","hand","flap","front","front_shin","front_paw","back","back_shin","back_paw"))names.add(side+"_"+part);for(int i=0;i<24;i++){names.add("mane_"+i);names.add("tail_"+i);names.add("cloak_"+i);names.add("scarf_"+i);}index("root",root,names);}
 private void index(String key,ModelPart p,List<String> names){nodes.put(key,p);for(var n:names)if(p.hasChild(n))index(n,p.getChild(n),names);}
 private void x(String k,float d){var p=nodes.get(k);if(p!=null)p.xRot=d*Mth.DEG_TO_RAD;}private void y(String k,float d){var p=nodes.get(k);if(p!=null)p.yRot=d*Mth.DEG_TO_RAD;}private void z(String k,float d){var p=nodes.get(k);if(p!=null)p.zRot=d*Mth.DEG_TO_RAD;}
 private static float ramp(float t,float a,float b){float f=Math.clamp((t-a)/(b-a),0,1);return f*f*(3-2*f);}private static float key(float t,float[] ts,float... vs){if(t<=ts[0])return vs[0];for(int i=1;i<ts.length;i++)if(t<=ts[i])return Mth.lerp(ramp(t,ts[i-1],ts[i]),vs[i-1],vs[i]);return vs[vs.length-1];}
 @Override public void setupAnim(T e,float swing,float amount,float age,float yaw,float pitch){pose(e.action(),e.clipAge(age-e.tickCount),swing,amount,age,yaw,pitch);if(e instanceof SkimBeastEntity b&&b.action()==SkimAction.ROAR&&!b.canRear()){nodes.get("body").resetPose();x("right_front",0);x("left_front",0);}if(e.hurtTime>0){x("head",Mth.sin(e.hurtTime*.4F)*5);z("body",Mth.sin(e.hurtTime*.4F)*4);}}
 public void pose(SkimAction a,float t,float swing,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);float gait=Mth.cos(swing*(beast?.45F:.8F))*Math.clamp(amount,0,1);y("head",Math.clamp(yaw,-35,35));x("head",Math.clamp(pitch,-20,20));
  if(beast){for(int sign:new int[]{-1,1})for(int i=0;i<2;i++){var k=(sign<0?"right":"left")+(i==0?"_front":"_back");x(k,gait*sign*(i==0?1:-1)*25);x(k+"_shin",Math.max(0,-gait*sign)*20);}for(int i=0;i<5;i++)y("tail_"+i,Mth.sin(age*.075F-i*.3F)*8);for(int i=0;i<24;i++)x("mane_"+i,Mth.sin(age*.05F-i*.2F)*3);x("jaw",6+Mth.sin(age*.05F)*2);x("body",Mth.sin(age*.07F)*1);
   switch(a){
    case AWAKEN->{x("neck",key(t,new float[]{0,20,40,60},20,0,-12,0));x("jaw",key(t,new float[]{0,20,40,60},0,15,30,6));}
    case CLAWS->{x("right_front",key(t,new float[]{0,8,11,16,24,30,39},0,-35,40,0,-45,55,0));x("left_front",key(t,new float[]{0,11,16,19,24,30,39},0,0,-35,45,-45,55,0));y("body",key(t,new float[]{0,11,19,30,39},0,12,-12,15,0));}
    case LEAP->{float compress=ramp(t,0,12)-ramp(t,12,16);nodes.get("body").y+=5*compress;x("right_front",key(t,new float[]{0,12,18,29,44},0,-35,-65,25,0));x("left_front",key(t,new float[]{0,12,18,29,44},0,-35,-65,25,0));x("hips",key(t,new float[]{0,12,18,29,44},0,20,-12,15,0));}
    case WHISTLE->{x("neck",-18*(ramp(t,0,14)-ramp(t,31,48)));x("jaw",6+22*(ramp(t,0,14)-ramp(t,31,48)));}
    case ROAR->{float rear=ramp(t,0,18)-ramp(t,31,58);x("body",-20*rear);nodes.get("body").y-=6*rear;x("right_front",-65*rear);x("left_front",-65*rear);x("neck",-18*rear);x("jaw",6+37*(ramp(t,14,26)-ramp(t,31,48)));}
    case FLANK->{y("body",35*(ramp(t,0,10)-ramp(t,26,38)));x("right_front",-35*(ramp(t,10,22)-ramp(t,26,38)));x("jaw",25*(ramp(t,10,22)-ramp(t,26,38)));}
    case GRIP->{x("jaw",key(t,new float[]{0,14,18,30,44,45,65},6,43,8,13,12,40,6));x("neck",key(t,new float[]{0,14,18,30,44,45,65},0,20,-10,-10,-10,20,0));}
    case RAGE->{y("head",Mth.sin(t*.35F)*10*(1-ramp(t,50,64)));x("jaw",6+35*(ramp(t,0,13)-ramp(t,50,64)));}
    case EXHAUSTED->{float k=ramp(t,0,10)-ramp(t,55,70);nodes.get("body").y+=6*k;x("neck",18*k);x("right_front_shin",25*k);x("left_front_shin",25*k);x("jaw",15+Mth.sin(t*.3F)*6);}
    case STAGGER->{y("head",Mth.sin(t*.4F)*12*(1-ramp(t,30,40)));x("neck",15*(1-ramp(t,30,40)));}
    case DEATH->{nodes.get("body").y+=15*ramp(t,0,52);z("body",85*ramp(t,26,75));x("jaw",22*ramp(t,0,40));}
    default->{}
   }
  }else{for(int sign:new int[]{-1,1}){var k=sign<0?"right":"left";x(k+"_thigh",gait*sign*28);x(k+"_shin",Math.max(0,-gait*sign)*25);x(k+"_shoulder",-gait*sign*12);}x("body",Mth.sin(age*.07F)*1.5F);for(int i=0;i<3;i++){x("cloak_"+i,5+Math.abs(gait)*9+Mth.sin(age*.07F-i*.4F)*3);z("scarf_"+i,Mth.sin(age*.1F-i*.4F)*6);}
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
