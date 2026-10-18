package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
import java.lang.reflect.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.client.GreyWolfModel;
import org.slavicmyths.wolf.WolfTimeline;
public final class GreyWolf54Offline {
 private static int checks;
 private static Object field(Object o,String key)throws Exception{var f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);}
 private static void check(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
 @SuppressWarnings("unchecked") private static void mesh(ModelPart p,PoseStack pose,JsonArray out)throws Exception{
  pose.pushPose();p.translateAndRotate(pose);
  for(var cube:(List<?>)field(p,"cubes"))for(var face:(Object[])field(cube,"polygons")){
   var polygon=new JsonArray();for(var vertex:(Object[])field(face,"vertices")){
    float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");var pos=pose.last().pose().transformPosition(new Vector3f((Vector3f)field(vertex,"pos")).div(16)).mul(16);
    check(u>=0&&u<=1&&v>=0&&v<=1&&Float.isFinite(pos.x)&&Float.isFinite(pos.y)&&Float.isFinite(pos.z),"UV and finite vertices");
    var row=new JsonArray();for(float x:new float[]{pos.x,pos.y,pos.z,u,v})row.add(x);polygon.add(row);
   }out.add(polygon);
  }
  for(var child:((Map<String,ModelPart>)field(p,"children")).values())mesh(child,pose,out);pose.popPose();
 }
 private static float footHeight(Map<String,ModelPart> parts,String name){
  var pose=new PoseStack();parts.get("root").translateAndRotate(pose);if(name.charAt(0)=='b')parts.get("pelvis").translateAndRotate(pose);
  for(String n:List.of(name,name+"Lower",name+"Foot"))parts.get(n).translateAndRotate(pose);
  return pose.last().pose().transformPosition(new Vector3f(0,3F/16,0)).y*16;
 }
 private static String transforms(Map<String,ModelPart> parts){var s=new StringBuilder();parts.forEach((k,p)->s.append(Arrays.toString(new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot})));return s.toString();}
 @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
  var model=new GreyWolfModel();var parts=(Map<String,ModelPart>)field(model,"parts");check(parts.size()==30,"Existing 30-joint schema preserved");
  String[] names={"idle","walk","run","watch","bite_12","claw_14","claw_33","pounce_12","pounce_20","pounce_27","dash_27","howl_31","howl_80","complete_70","sit"};
  WolfTimeline.Move[] actions={WolfTimeline.Move.NONE,WolfTimeline.Move.NONE,WolfTimeline.Move.NONE,WolfTimeline.Move.NONE,WolfTimeline.Move.BITE,WolfTimeline.Move.DOUBLE_CLAW,WolfTimeline.Move.DOUBLE_CLAW,WolfTimeline.Move.POUNCE,WolfTimeline.Move.POUNCE,WolfTimeline.Move.POUNCE,WolfTimeline.Move.FLANK_DASH,WolfTimeline.Move.HOWL_SUMMON,WolfTimeline.Move.HOWL_SUMMON,WolfTimeline.Move.NONE,WolfTimeline.Move.NONE};
  float[] times={20,20,20,20,12,14,33,12,20,27,27,31,80,70,50};var data=new JsonObject();
  for(int i=0;i<names.length;i++){
   var state=i==13?WolfTimeline.State.TRIAL_COMPLETE_CINEMATIC:i==14?WolfTimeline.State.POST_TRIAL_NPC:i==3?WolfTimeline.State.WAITING_DECISION:WolfTimeline.State.TRIAL_ACTIVE;
   model.sample(state,actions[i],times[i],i==2,0,2,i==1||i==2?.8F:0,20,i==3?20:0,0);
   var polygons=new JsonArray();mesh(parts.get("root"),new PoseStack(),polygons);data.add(names[i],polygons);
  }
  boolean baseline=Boolean.getBoolean("slavicmyths.wolf54Baseline");var path=Path.of(baseline?"docs/implementation/grey-wolf-54/before-native.json.gz":"build/grey-wolf-54/after-native.json.gz");Files.createDirectories(path.getParent());
  if(baseline&&Files.exists(path))throw new AssertionError("Preserve recorded baseline");
  try(var stream=new GZIPOutputStream(Files.newOutputStream(path))){stream.write(new Gson().toJson(data).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  model.sample(WolfTimeline.State.TRIAL_ACTIVE,WolfTimeline.Move.NONE,20,false,0,0,0,20,0,0);String rest=transforms(parts);
  for(var action:WolfTimeline.Move.values()){model.sample(WolfTimeline.State.TRIAL_ACTIVE,action,20,true,9,1,1,40,20,10);model.sample(WolfTimeline.State.TRIAL_ACTIVE,WolfTimeline.Move.NONE,20,false,0,0,0,20,0,0);check(transforms(parts).equals(rest),"Absolute reset after "+action);}
  if(!baseline){
   for(boolean run:new boolean[]{false,true})for(int n=0;n<24;n++){
    model.sample(WolfTimeline.State.TRIAL_ACTIVE,WolfTimeline.Move.NONE,20,run,0,n*.4F,.8F,20,0,0);
    for(String leg:List.of("fl","fr","bl","br")){float y=footHeight(parts,leg);check(y<=24.01F&&y>=19.99F,"Actual transformed foot stays at/above ground through gait: "+leg+" "+y);}
   }
   model.sample(WolfTimeline.State.POST_TRIAL_NPC,WolfTimeline.Move.NONE,50,false,0,0,0,20,0,0);
   for(String leg:List.of("fl","fr","bl","br"))check(Math.abs(footHeight(parts,leg)-24)<.01,"NPC sit has four real supports");
   model.sample(WolfTimeline.State.TRIAL_ACTIVE,WolfTimeline.Move.POUNCE,20,false,0,0,0,20,0,0);
   for(String leg:List.of("fl","fr","bl","br"))check(footHeight(parts,leg)<22,"Mid-pounce paws visibly airborne: "+leg);
   parts.get("head").visible=false;parts.get("head").skipDraw=true;model.sample(WolfTimeline.State.TRIAL_ACTIVE,WolfTimeline.Move.NONE,20,false,0,0,0,20,0,0);check(parts.get("head").visible&&!parts.get("head").skipDraw,"Pose visibility reset");
  }
  check(WolfTimeline.hit(WolfTimeline.Move.BITE,12)&&WolfTimeline.hit(WolfTimeline.Move.DOUBLE_CLAW,14)&&WolfTimeline.hit(WolfTimeline.Move.DOUBLE_CLAW,33)&&WolfTimeline.hit(WolfTimeline.Move.POUNCE,27),"Existing real hit clocks");
  System.out.println("GREY_WOLF_54_NATIVE "+(baseline?"BASELINE":"PASS")+" checks="+checks+" poses="+names.length+"; no world bootstrap");
 }
}
