package org.slavicmyths.client;
import java.util.*;import com.mojang.blaze3d.vertex.*;import net.minecraft.client.model.EntityModel;import net.minecraft.client.model.geom.ModelPart;import net.minecraft.util.Mth;import net.minecraft.world.entity.LivingEntity;import org.slavicmyths.kalin.*;import org.slavicmyths.finist.*;
public final class KalinModel<T extends LivingEntity> extends EntityModel<T>{
 public final ModelPart root;public final Map<String,ModelPart> nodes=new LinkedHashMap<>();private final boolean horse;
 public KalinModel(ModelPart p,boolean horse){root=p;this.horse=horse;index("root",p);}
 private void index(String n,ModelPart p){nodes.put(n,p);for(var k:List.of("hips","body","head","neck","right_arm","left_arm","right_hand","left_hand","right_leg","left_leg","right_tasset","left_tasset","cape_0","cape_1","cape_2","weapon","shield","left_front","right_front","left_back","right_back","left_front_hoof","right_front_hoof","left_back_hoof","right_back_hoof","left_barding","right_barding","tail"))if(p.hasChild(k))index(k,p.getChild(k));}
 private void x(String n,float v){if(nodes.containsKey(n))nodes.get(n).xRot=v*Mth.DEG_TO_RAD;}private void y(String n,float v){if(nodes.containsKey(n))nodes.get(n).yRot=v*Mth.DEG_TO_RAD;}private void z(String n,float v){if(nodes.containsKey(n))nodes.get(n).zRot=v*Mth.DEG_TO_RAD;}
 private static float ramp(float t,float a,float b){return Math.clamp((t-a)/(b-a),0,1);}
 @Override public void setupAnim(T e,float swing,float amount,float age,float yaw,float pitch){var a=e instanceof FalconActor f?f.action():FalconAction.IDLE;float t=e instanceof FalconActor f?f.clipAge(age-e.tickCount):0;boolean mounted=e instanceof KalinEntity k&&k.mounted();pose(a,t,swing,amount,age,yaw,pitch,mounted);if(e instanceof KalinSoldier s&&s.swinging)x("right_arm",-80*net.minecraft.util.Mth.sin(e.getAttackAnim(age-e.tickCount)*(float)Math.PI));if(e instanceof WarHorse&&a==FalconAction.RECALL)x("body",-8);}
 public void pose(FalconAction a,float t,float swing,float amount,float age,float yaw,float pitch,boolean mounted){root.getAllParts().forEach(ModelPart::resetPose);float gait=Mth.cos(swing*.65F)*Math.clamp(amount,0,1);y("head",Math.clamp(yaw,-35,35));x("head",Math.clamp(pitch,-18,18));if(horse){for(var n:List.of("left_front","right_back"))x(n,gait*27);for(var n:List.of("right_front","left_back"))x(n,-gait*27);x("tail",Mth.sin(age*.08F)*7);if(a==FalconAction.BANNER||a==FalconAction.MORPH){float f=ramp(t,0,10)-ramp(t,22,36);x("body",-12*f);x("left_front",-35*f);x("right_front",-35*f);}return;}
 x("right_leg",mounted?-65:gait*22);x("left_leg",mounted?-65:-gait*22);z("right_leg",mounted?18:0);z("left_leg",mounted?-18:0);for(int i=0;i<3;i++)x("cape_"+i,Mth.sin(age*.09F-i*.5F)*3-amount*8);z("weapon",52);x("left_arm",-gait*8);
 switch(a){case SWORD->{float f=ramp(t,0,8)-ramp(t,13,16);x("right_arm",-145*f);y("body",-20*f+35*ramp(t,10,12)*(1-ramp(t,13,16)));}
 case WAVE->{float f=ramp(t,0,10)-ramp(t,39,49);z("right_arm",-75*f);y("body",(t<12?0:Math.min(3,(t-12)/12))*360*f);x("right_arm",-35*f);}
 case DASH->{x("body",28*(ramp(t,0,8)-ramp(t,21,26)));x("right_arm",-75);for(int i=0;i<3;i++)x("cape_"+i,-28);}
 case GUARDS->{float f=ramp(t,0,10)-ramp(t,28,42);x("right_arm",-145*f);x("left_arm",-75*f);z("weapon",0);}
 case INTERCEPT->{x("right_arm",-65*(ramp(t,0,3)-ramp(t,14,25)));z("right_arm",-30);y("weapon",75);x("left_arm",-35);}
 case MORPH->{x("body",25*(ramp(t,0,12)-ramp(t,25,36)));x("left_arm",-65);}
 case BANNER->{x("right_arm",-105*(ramp(t,0,10)-ramp(t,30,56)));}
 case STAGGER->{x("body",25);x("head",18);}
 case DEATH->{float f=ramp(t,0,30);nodes.get("hips").y+=9*f;x("left_leg",-85*f);x("right_leg",30*f);x("body",18*f);x("head",25*f);z("weapon",-15*f);}
 default->{} }
 }
 @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
}
