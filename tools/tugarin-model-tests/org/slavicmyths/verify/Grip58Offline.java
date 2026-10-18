package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.client.WorldBossModel;
import org.slavicmyths.client.NightingaleModel;
import org.slavicmyths.bandit.NightingaleEntity;
import static org.slavicmyths.bandit.NightingaleEntity.*;
import org.slavicmyths.hunt.BossRules.Move;
public final class Grip58Offline {
 private static int checks;
 private static Object field(Object o,String key)throws Exception{var f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);}
 private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 @SuppressWarnings("unchecked") private static void mesh(ModelPart part,PoseStack pose,JsonArray out)throws Exception{
  if(!part.visible)return;
  pose.pushPose();part.translateAndRotate(pose);
  for(var cube:(List<?>)field(part,"cubes"))for(var face:(Object[])field(cube,"polygons")){
   var polygon=new JsonArray();for(var vertex:(Object[])field(face,"vertices")){
    var v=pose.last().pose().transformPosition(new Vector3f((Vector3f)field(vertex,"pos")).div(16)).mul(16);float u=(float)field(vertex,"u"),w=(float)field(vertex,"v");
    check(Float.isFinite(v.x)&&Float.isFinite(v.y)&&Float.isFinite(v.z)&&u>=0&&u<=1&&w>=0&&w<=1,"Finite baked UV/vertices");var row=new JsonArray();for(float f:new float[]{v.x,v.y,v.z,u,w})row.add(f);polygon.add(row);
   }out.add(polygon);
  }
  for(var child:((Map<String,ModelPart>)field(part,"children")).values())mesh(child,pose,out);pose.popPose();
 }

 private static org.joml.Matrix4f chain(ModelPart... parts){var pose=new PoseStack();for(var p:parts)p.translateAndRotate(pose);return new org.joml.Matrix4f(pose.last().pose());}
 private static void contact(Object model,boolean frost,int action,int phase,int tick)throws Exception{
  var torso=(ModelPart)model.getClass().getField("torso").get(model);var right=(ModelPart)model.getClass().getField("right").get(model);var left=(ModelPart)model.getClass().getField("left").get(model);
  var fore=right.getChild("forearm_right");var hand=fore.getChild("hand_right");var weapon=hand.getChild(frost?"staff_root":"scythe");
  for(var cube:(List<?>)field(hand,"cubes"))for(float x:new float[]{-1,0,1})for(float z:new float[]{-1,0,1}){
   float radius=frost?2.25F:1.5F,px=x*radius,pz=z*radius;
   check(!(px>(float)field(cube,"minX")&&px<(float)field(cube,"maxX")&&2.5F>(float)field(cube,"minY")&&2.5F<(float)field(cube,"maxY")&&pz>(float)field(cube,"minZ")&&pz<(float)field(cube,"maxZ")),"Grip gap clears actual shaft/band radius");
  }
  var handM=chain(right,fore,hand);var weaponM=chain(right,fore,hand,weapon);var q=new org.joml.Matrix4f(handM).invert().transformPosition(weaponM.transformPosition(new Vector3f(0,0,0))).mul(16);
  check(Math.abs(q.x)<.01F&&Math.abs(q.z)<.01F&&Math.abs(q.y-2)<.01F,"Right palm/shaft axis");
  for(int y=-24;y<=18;y++){
   var pt=weaponM.transformPosition(new Vector3f(0,y/16F,0)).mul(16);float half=frost?11:8,radius=frost?2.3F:1.6F;
   check(!(Math.abs(pt.x)<half+radius&&pt.y> -2&&pt.y<21&&pt.z> -4-radius&&pt.z<6+radius),"Shaft crosses torso frost="+frost+" action="+action+" tick="+tick+" right="+right.xRot+","+right.yRot+","+right.zRot+" fore="+fore.xRot+" weapon="+weapon.xRot+","+weapon.yRot+","+weapon.zRot+" y="+y+" xyz="+pt);
  }
  if(!frost&&phase>=1&&phase<=3&&action>=1&&action<=3){
   var f=left.getChild("forearm_left");var h=f.getChild("hand_left");var palm=chain(left,f,h).transformPosition(new Vector3f(0,2.5F/16,0));var shaft=weaponM.transformPosition(new Vector3f(0,-6F/16,0));check(palm.distance(shaft)<.001F,"Actual second palm contact action="+action+" tick="+tick+" distance="+palm.distance(shaft));
  }
 }
 private static JsonArray markers(Object model,boolean frost)throws Exception{
  var root=(ModelPart)model.getClass().getField("root").get(model);var pelvis=root.getChild("pelvis");var torso=pelvis.getChild("torso");var result=new JsonArray();
  for(String side:new String[]{"right","left"}){
   var arm=torso.getChild((frost?"upper_arm_":"arm_")+side);var fore=arm.getChild("forearm_"+side);var hand=fore.getChild("hand_"+side);
   var pt=chain(root,pelvis,torso,arm,fore,hand).transformPosition(new Vector3f(0,2.5F/16,0)).mul(16);var row=new JsonArray();row.add(side);row.add(pt.x);row.add(pt.y);row.add(pt.z);result.add(row);
  }return result;
 }
 @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
  boolean baseline=Boolean.getBoolean("slavicmyths.grip58Baseline");var output=new JsonObject();var allMarkers=new JsonObject();
  for(boolean frost:new boolean[]{false,true}){
   Object model=frost?new org.slavicmyths.client.MorozkoModel():new org.slavicmyths.client.KarachunModel();
   var root=(ModelPart)model.getClass().getField("root").get(model);var sample=model.getClass().getMethod("sample",int.class,float.class,int.class,int.class,int.class,int.class,boolean.class,float.class,float.class,float.class,float.class,float.class);var poses=new JsonObject();var markerPoses=new JsonObject();
   for(int action=0;action<=(frost?8:9);action++)for(int tick:(baseline?new int[]{0,5,10,13,14,16,20,24,28,40,48,50}:new int[]{0,5,7,10,13,14,16,20,24,28,30,40,44,48,50,56})){
    sample.invoke(model,action,(float)tick,2,80,0,0,false,0F,0F,20F,0F,0F);var faces=new JsonArray();mesh(root,new PoseStack(),faces);poses.add("a"+action+"_"+tick,faces);if(!baseline){contact(model,frost,action,2,tick);markerPoses.add("a"+action+"_"+tick,markers(model,frost));}
   }
   for(int phase:new int[]{0,4,5,6})for(boolean ice:new boolean[]{false,true}){
    sample.invoke(model,0,20F,phase,20,0,0,ice,0F,0F,20F,0F,0F);var faces=new JsonArray();mesh(root,new PoseStack(),faces);poses.add("phase"+phase+"_ice"+ice,faces);
   }
   sample.invoke(model,0,0F,2,80,0,0,false,2F,.5F,20F,0F,0F);var faces=new JsonArray();mesh(root,new PoseStack(),faces);poses.add("walk",faces);
   if(!baseline)for(int action=0;action<=(frost?8:9);action++)for(int tick=0;tick<=64;tick++){
    sample.invoke(model,action,(float)tick,2,80,0,0,false,0F,0F,20F,0F,0F);contact(model,frost,action,2,tick);
   }
   output.add(frost?"morozko":"karachun",poses);allMarkers.add(frost?"morozko":"karachun",markerPoses);
  }
  var path=Path.of(baseline?"docs/implementation/weapon-grip-58/before-native.json.gz":"build/weapon-grip-58/after-native.json.gz");Files.createDirectories(path.getParent());if(baseline&&Files.exists(path))throw new AssertionError("Preserve baseline");try(var out=new GZIPOutputStream(Files.newOutputStream(path))){out.write(new Gson().toJson(output).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  if(!baseline)Files.writeString(Path.of("build/weapon-grip-58/contact-points.json"),new Gson().toJson(allMarkers));
  System.out.println("GRIP_58_NATIVE "+(baseline?"BASELINE":"PASS")+" checks="+checks+" no game/world launch");
 }
}
