package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.client.MobMotion;
import org.slavicmyths.navya.*;
import org.slavicmyths.navya.client.*;
import org.slavicmyths.losthalls.client.HallsMesh;
/** Mathematical and actual production rig tests, no game/client/world. */
public final class MobMotionOffline {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  for(int fps:new int[]{24,30,60}){float previous=0;for(int i=0;i<=fps*3;i++){float phase=(float)(i*2*Math.PI/fps);var s=MobMotion.step(phase,.6F,7,3.5F);check(s.lift()>=-.0001,"negative lift");if(!s.swing())check(s.lift()==0,"stance sole lift");if(i>0)check(Math.abs(s.forward()-previous)<1.5,"discontinuous foot target at "+fps);previous=s.forward();}}
  var rig=NavyaMesh.create(NavyaContent.Kind.CRAWLER);
  for(String side:List.of("left","right"))for(int leg=0;leg<4;leg++){
   Set<Integer> knee=new HashSet<>(),foot=new HashSet<>();
   for(int i=0;i<64;i++){NavyaPose.pose(rig,NavyaContent.Kind.CRAWLER,NavyaMob.Action.IDLE,0,i,i*.2F,.8F,0,0,0,0);knee.add(Math.round(rig.nodes().get("shin_"+side+leg).zRot*1000));foot.add(Math.round(rig.nodes().get("toe_"+side+leg).zRot*1000));}
   check(knee.size()>10&&foot.size()>10,"static bone leg "+side+leg);
  }
  NavyaPose.pose(rig,NavyaContent.Kind.CRAWLER,NavyaMob.Action.IDLE,0,0,2,.8F,0,0,0,0);
  check(Math.abs(rig.nodes().get("shin_left0").zRot-rig.nodes().get("shin_left1").zRot)>.01,"synchronous legs");
  // Verify analytic IK endpoint and sole orientation for both sides through swing.
  for(int side:new int[]{-1,1})for(int i=0;i<64;i++){
   ModelPart yaw=new ModelPart(List.of(),Map.of()),upper=new ModelPart(List.of(),Map.of()),shin=new ModelPart(List.of(),Map.of()),foot=new ModelPart(List.of(),Map.of());float phase=i*.1F;
   MobMotion.leg(yaw,upper,shin,foot,side,13,10,8,11,phase,.8F,.72F,0);
   float alpha=side*upper.zRot,beta=side*shin.zRot+(float)Math.PI/2;
   float drop=(float)(8*Math.sin(alpha)+11*Math.sin(alpha+beta));float expected=10-MobMotion.step(phase,.72F,7,3.5F).lift()*.8F;
   check(Math.abs(drop-expected)<.001,"IK sole height");check(Math.abs(upper.zRot+shin.zRot+foot.zRot)<.001,"sole not parallel");
  }
  var sheptun=new HallsMesh("sheptun");for(int v=0;v<4;v++){sheptun.poseMob("sheptun",0,0,0,0,0,0,false,false,v);for(int j=0;j<4;j++)check(sheptun.parts.get("variant_hood"+j).visible==(j==v),"variant geometry leak");}
  var bat=new HallsMesh("gulokryl");bat.poseMob("gulokryl",0,0,4,0,0,0,true,false,0);float folded=bat.parts.get("wing_left").zRot;bat.poseMob("gulokryl",0,0,4,0,0,0,false,true,0);check(Math.abs(folded-bat.parts.get("wing_left").zRot)>.5,"sit/glide same");
  System.out.println("PASS production gait: 24/30/60 temporal sampling, stance/swing IK sole height, eight independent bone knees/toes, four Sheptun variants, bat sit/glide. Runtime FPS/ground collision NOT_RUN.");
 }
}
