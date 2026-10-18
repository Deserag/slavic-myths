package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.client.*;
import org.slavicmyths.entity.AttackTimeline.Phase;
import org.slavicmyths.water.*;

public final class QualityCorrectionsOffline {
 private static int checks;private static final JsonObject ANCHORS=new JsonObject();
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static Object field(Object object,String name)throws Exception{var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
 static JsonArray mesh(ModelPart root)throws Exception{var method=RusalkaOffline.class.getDeclaredMethod("mesh",ModelPart.class);method.setAccessible(true);return (JsonArray)method.invoke(null,root);}
 static ModelPart part(Object model,String name)throws Exception{return (ModelPart)field(model,name);}
 @SuppressWarnings("unchecked") static void anchors(ModelPart current,com.mojang.blaze3d.vertex.PoseStack pose,Map<String,ModelPart> targets,JsonObject result)throws Exception{
  pose.pushPose();current.translateAndRotate(pose);
  for(var entry:targets.entrySet())if(current==entry.getValue()){
   var pos=pose.last().pose().transformPosition(new org.joml.Vector3f()).mul(16);var v=new JsonArray();v.add(pos.x);v.add(pos.y);v.add(pos.z);result.add(entry.getKey(),v);
  }
  for(var child:((Map<String,ModelPart>)field(current,"children")).values())anchors(child,pose,targets,result);pose.popPose();
 }
 static void export(JsonObject output,String name,PolevikModel model,ModelPart root)throws Exception{
  output.add(name,mesh(root));var targets=new LinkedHashMap<String,ModelPart>();for(String n:List.of("body","head","LeftLeg","RightLeg","LeftToe1","RightToe1"))targets.put(n,part(model,n));
  var points=new JsonObject();anchors(root,new com.mojang.blaze3d.vertex.PoseStack(),targets,points);ANCHORS.add(name,points);
 }
 public static void main(String[] args)throws Exception{
  var encounter=new RusalkaEncounter();check(!encounter.observe()&&encounter.observe(),"One second of visible observation, not hidden five second wait");encounter.begin(200);
  for(int tick=200;tick<300;tick++){check(!encounter.warning(tick,4),"First notes protected at 2 blocks");check(!encounter.hostile(tick),"No early proximity aggression");}
  check(encounter.warning(300,4)&&!encounter.hostile(300),"Explicit close warning begins after intro grace");check(!encounter.hostile(339)&&encounter.hostile(340),"Two seconds readable close warning");
  for(double dist:new double[]{6.25,36,144,400,Double.POSITIVE_INFINITY}){check(!encounter.warning(341,dist)&&!encounter.hostile(400),"Leaving proximity/LOS resets warning");}
  encounter.warning(410,4);check(!encounter.hostile(410),"Reapproach gets new warning, not old stale clock");encounter.reset();check(!encounter.hostile(1000),"Cancellation clears hostility");
  check(RusalkaEncounter.vocal(0,true)==RusalkaEncounter.Vocal.INTRO&&RusalkaEncounter.vocal(59,true)==RusalkaEncounter.Vocal.INTRO,"Intro first, once, independent of intensity");
  check(RusalkaEncounter.vocal(60,false)==RusalkaEncounter.Vocal.LOOP&&RusalkaEncounter.vocal(60,true)==RusalkaEncounter.Vocal.INTENSE,"True calm/intense phase transition");
  var r=new RusalkaModel();var output=new JsonObject();
  for(int action:new int[]{RusalkaActions.SONG_START,RusalkaActions.LURE,RusalkaActions.INTENSE,RusalkaActions.WARNING,RusalkaActions.BREAK}){r.sample(action,24,0,0,20,0,0,false,0,0);output.add("rusalka_"+action,mesh(r.parts.get("body")));}
  var p=new PolevikModel();var root=part(p,"root");
  for(int state=0;state<=3;state++){p.sample(0,state,Phase.IDLE,0,0,20,0,0);if(state==1){part(p,"body").y=8;part(p,"LeftLeg").xRot=part(p,"RightLeg").xRot=-.95F;part(p,"LeftShin").xRot=part(p,"RightShin").xRot=.7F;}export(output,"polevik_before_"+state,p,root);}
  for(float crop:new float[]{0,.25F,.5F,.75F,1}){
   p.sample(crop,1,Phase.IDLE,0,0,20,0,0);var body=part(p,"body");var hip=part(p,"LeftLeg");
   check(Math.abs(hip.y-10-body.y)<.001,"Hip follows body, no split hierarchy compression");check(Math.abs(hip.z-body.z)<.001,"Forward/back contact preserved");
   float thigh=hip.xRot,total=thigh+part(p,"LeftShin").xRot;
   double footY=hip.y+7*Math.cos(thigh)+6.5*Math.cos(total);
   check(Math.abs(footY-23.5)<.001,"Analytic foot ground contact throughout crouch");check(body.y<=4.5,"No eight-pixel torso crush");
   export(output,"polevik_crop_"+crop,p,root);
  }
  for(var phase:Phase.values()){p.sample(1,3,phase,0,0,20,0,0);export(output,"polevik_phase_"+phase.name(),p,root);}
  p.sample(0,0,Phase.IDLE,0,0,20,0,0);check(part(p,"body").y==0&&part(p,"LeftLeg").y==10&&part(p,"LeftShin").xRot==0,"Full return to standing after crop/attack/gift");
  var dir=Path.of("build/quality-corrections");Files.createDirectories(dir);Files.writeString(dir.resolve("meshes.json"),new Gson().toJson(output));Files.writeString(dir.resolve("anchors.json"),new Gson().toJson(ANCHORS));
  System.out.println("QUALITY_CORRECTIONS_OFFLINE_PASS checks="+checks+" actual native poses="+output.size()+"; no game bootstrap");
 }
}
