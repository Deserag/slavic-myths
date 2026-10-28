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
 static float sole(ModelPart root,String name,float height){float[] y={Float.NaN};root.visit(new com.mojang.blaze3d.vertex.PoseStack(),(pose,path,index,cube)->{if(path.endsWith("/"+name))y[0]=pose.pose().transformPosition(0,height/16,0,new org.joml.Vector3f()).y*16;});check(Float.isFinite(y[0]),"missing sole "+name);return y[0];}
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
  var coal=new org.slavicmyths.blackcinder.client.CinderModel(com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(MobMotionOffline.class.getResourceAsStream("/assets/slavicmyths/cinder_rigs/coal_crawler.json"),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject());
  for(int sample=0;sample<192;sample++){float walk=sample*.2F;NavyaPose.pose(rig,NavyaContent.Kind.CRAWLER,NavyaMob.Action.IDLE,0,sample,walk,.8F,0,0,0,0);coal.pose(org.slavicmyths.blackcinder.CinderContent.Kind.COAL_CRAWLER,org.slavicmyths.blackcinder.CinderMob.Action.IDLE,0,false,false,walk,.8F,sample,0,0,0,0);
   for(int side=0;side<2;side++)for(int leg=0;leg<4;leg++){float phase=walk*.8F+((side+leg)%2)*(float)Math.PI;float y=sole(rig.root(),"toe_"+(side==0?"left":"right")+leg,2);if(!MobMotion.step(phase,.6F,7,3.5F).swing())check(Math.abs(y-24)<.002,"bone sole drift "+y);}
   for(int leg=0;leg<4;leg++){float phase=walk*.55F+((leg==0||leg==3)?0:(float)Math.PI);float y=sole(coal.root(),"foot"+leg,1);if(!MobMotion.step(phase,.72F,7,3.5F).swing())check(Math.abs(y-24)<.002,"coal sole drift "+y);}
  }
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
  System.out.println("PASS production gait: 24/30/60 temporal sampling, IK and actual ModelPart stance soles at Y24 over three cycles, eight independent bone knees/toes, four Sheptun variants, bat sit/glide. Runtime FPS/world-ground collision NOT_RUN.");
 }
}
