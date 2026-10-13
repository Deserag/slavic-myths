package org.slavicmyths.client;
import java.util.*;import com.mojang.blaze3d.vertex.*;import net.minecraft.client.model.EntityModel;import net.minecraft.client.model.geom.ModelPart;import net.minecraft.util.Mth;import org.slavicmyths.finist.*;
public final class FinistModel<T extends FalconActor> extends EntityModel<T>{
 public final ModelPart root;public final Map<String,ModelPart> nodes=new HashMap<>();private final boolean boss;
 public FinistModel(ModelPart root,boolean boss){this.root=root;this.boss=boss;index("root",root);}
 private void index(String n,ModelPart p){nodes.put(n,p);for(var k:List.of("hips","body","head","crown","pendant","front_panel","right_leg","left_leg","right_boot","left_boot","right_arm","left_arm","right_hand","left_hand","right_skirt","left_skirt","cape_0","cape_1","cape_2","weapon","flask","cloth"))if(p.hasChild(k))index(k,p.getChild(k));}
 private void x(String n,float v){if(nodes.containsKey(n))nodes.get(n).xRot=v*Mth.DEG_TO_RAD;}private void y(String n,float v){if(nodes.containsKey(n))nodes.get(n).yRot=v*Mth.DEG_TO_RAD;}private void z(String n,float v){if(nodes.containsKey(n))nodes.get(n).zRot=v*Mth.DEG_TO_RAD;}
 private static float ramp(float t,float a,float b){float f=Math.clamp((t-a)/(b-a),0,1);return f*f*(3-2*f);}private static float key(float t,float[] times,float... values){if(t<=times[0])return values[0];for(int i=1;i<times.length;i++)if(t<=times[i])return Mth.lerp(ramp(t,times[i-1],times[i]),values[i-1],values[i]);return values[values.length-1];}
 @Override public void setupAnim(T e,float swing,float amount,float age,float yaw,float pitch){pose(e.action(),e.age(age-e.tickCount),swing,amount,age,yaw,pitch);root.visible=!(e instanceof FinistEntity f&&f.morphed());if(e.hurtTime>0)x("head",Mth.sin(e.hurtTime*.35F)*5);}
 public void pose(FalconAction a,float t,float swing,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(p->{p.resetPose();p.visible=true;});var flask=nodes.get("flask");if(flask!=null)flask.visible=a==FalconAction.DRINK;float gait=Mth.cos(swing*(boss?.5F:.8F))*Math.clamp(amount,0,1);x("right_leg",gait*(boss?18:30));x("left_leg",-gait*(boss?18:30));x("right_arm",-gait*12);x("left_arm",gait*12);x("right_skirt",gait*4);x("left_skirt",-gait*4);y("head",Math.clamp(yaw,-35,35));x("head",Math.clamp(pitch,-20,20));for(int i=0;i<3;i++)x("cape_"+i,Mth.sin(age*.075F-i*.5F)*3-amount*(boss?4:9));
 if(!boss){z("weapon",58);switch(a){
 case SWORD->{x("right_arm",key(t,new float[]{0,4,7,10,16},-55,-75,10,35,0));y("body",key(t,new float[]{0,4,7,10,16},0,-22,45,18,0));z("right_hand",-30*(ramp(t,0,4)-ramp(t,10,16)));}
 case MARK->{x("left_arm",-80*(ramp(t,0,6)-ramp(t,16,24)));y("head",10*(ramp(t,6,11)-ramp(t,16,24)));x("right_arm",15);}
 case INSPIRE->{x("left_arm",-105*(ramp(t,0,8)-ramp(t,22,30)));z("left_arm",-25*(ramp(t,0,8)-ramp(t,22,30)));}
 case DASH->{x("body",key(t,new float[]{0,4,8,17,21,26},18,-10,-15,35,10,0));x("right_leg",-35*(ramp(t,0,8)-ramp(t,21,26)));x("left_leg",20*(ramp(t,0,8)-ramp(t,21,26)));x("right_arm",-55);for(int i=0;i<3;i++)x("cape_"+i,-28*(ramp(t,0,8)-ramp(t,21,26)));}
 case MORPH->{float f=ramp(t,0,12);x("body",25*f);x("right_arm",-70*f);x("left_arm",-70*f);for(int i=0;i<3;i++)z("cape_"+i,(i%2==0?1:-1)*15*f);}
 case RETREAT->{x("body",18*ramp(t,0,10));x("right_arm",20);y("head",-25*ramp(t,10,26));}
 case DRINK->{x("body",18);x("right_leg",-50);x("left_leg",-50);x("left_arm",key(t,new float[]{0,14,28,38,46,60},0,-65,-100,-105,-50,0));x("left_hand",-25);x("head",key(t,new float[]{0,28,38,60},0,19,19,0));if(nodes.containsKey("weapon"))nodes.get("weapon").visible=false;}
 case VICTORY->{x("right_arm",-130*(ramp(t,0,12)-ramp(t,36,44)));}
 case ARRIVE,RECALL->{x("left_arm",-25*(ramp(t,0,8)-ramp(t,20,28)));}
 case HURT,STAGGER->{x("body",-19*(ramp(t,0,3)-ramp(t,8,12)));z("left_arm",-35);}
 default->{} }
 }else{switch(a){
 case SWORD->{x("right_arm",key(t,new float[]{0,6,10,13,19,24},0,-37,-55,50,30,0));y("body",key(t,new float[]{0,10,13,24},0,27,-25,0));z("weapon",key(t,new float[]{0,10,13,24},0,-25,70,0));}
 case BANNER->{x("right_arm",key(t,new float[]{0,8,16,24,40,56},0,-70,-165,-20,-25,0));x("left_arm",-100*(ramp(t,0,8)-ramp(t,24,48)));z("weapon",20*(ramp(t,8,16)-ramp(t,24,40)));}
 case GUARDS->{x("right_arm",-75*(ramp(t,0,9)-ramp(t,33,50)));x("left_arm",-80*(ramp(t,0,18)-ramp(t,33,50)));z("left_arm",-30);}
 case WAVE->{x("right_arm",key(t,new float[]{0,10,18,26,49},-30,-115,20,35,0));y("body",key(t,new float[]{0,10,18,49},0,-25,25,0));z("weapon",35*(ramp(t,0,10)-ramp(t,26,49)));}
 case CHAINS->{x("left_arm",-95*(ramp(t,0,8)-ramp(t,28,44)));x("left_hand",-45*(ramp(t,8,14)-ramp(t,28,44)));x("right_arm",-35);}
 case EYE->{x("body",-8*(ramp(t,0,12)-ramp(t,42,67)));x("right_arm",-165*(ramp(t,0,12)-ramp(t,42,67)));z("weapon",-10);}
 case CURSE->{x("right_arm",-90*(ramp(t,0,10)-ramp(t,45,57)));y("head",15*(ramp(t,0,10)-ramp(t,45,57)));x("left_arm",-50*(ramp(t,10,20)-ramp(t,45,57)));}
 case STAGGER->{x("body",22*(ramp(t,0,5)-ramp(t,40,60)));x("right_arm",25);x("right_leg",-20);x("left_leg",-15);}
 case RAGE->{x("body",15*(ramp(t,0,12)-ramp(t,50,60)));x("right_arm",-30*(ramp(t,0,12)-ramp(t,50,60)));z("left_arm",-55*(ramp(t,12,35)-ramp(t,50,60)));}
 case INTRO->{x("left_arm",-65*(ramp(t,0,15)-ramp(t,30,40)));}
 case DEATH->{x("body",key(t,new float[]{0,14,27,45,70},0,12,30,65,85));x("right_leg",-65*ramp(t,14,45));x("left_leg",-65*ramp(t,14,45));z("weapon",75*ramp(t,0,14));x("head",35*ramp(t,27,45));}
 default->{} }
 }}
 @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
}
