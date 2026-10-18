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
public final class Nightingale57Offline {
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
 private static Vector3f point(Map<String,ModelPart> p,String[] chain,Vector3f local){var pose=new PoseStack();for(String n:chain)p.get(n).translateAndRotate(pose);return pose.last().pose().transformPosition(new Vector3f(local).div(16)).mul(16);}
 private static void grip(Map<String,ModelPart> p,String side){
  String[] chain={"root","pelvis","body","arm"+side,"fore"+side,"wrist"+side};var pose=new PoseStack();for(String n:chain)p.get(n).translateAndRotate(pose);
  var inverse=new org.joml.Matrix4f(pose.last().pose()).invert();p.get("blade"+side).translateAndRotate(pose);
  for(float y:new float[]{.5F,1.5F}){var q=inverse.transformPosition(pose.last().pose().transformPosition(new Vector3f(0,y/16,0))).mul(16);check(Math.abs(q.x)<1.6F&&q.y>=-.8F&&q.y<=2.6F&&Math.abs(q.z)<1.7F,"Actual shaft inside palm "+side);}
 }
 private record Key(String name,int action,float tick,boolean grounded,float speed,float stride,float amount,int death){}
 @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
  boolean baseline=Boolean.getBoolean("slavicmyths.nightingale57Baseline");var output=new JsonObject();var keys=new ArrayList<Key>();
  keys.add(new Key("idle",IDLE,0,true,0,0,0,0));keys.add(new Key("walk",IDLE,0,true,0,2,.5F,0));
  keys.add(new Key("quick_10",QUICK,10,true,0,0,0,0));
  for(int t:new int[]{0,8,20,32,36})keys.add(new Key("combo_"+t,COMBO,t,true,0,0,0,0));
  for(int a:new int[]{INHALE,WAVE,WHIRLWIND})for(int t:new int[]{0,8,15,16,22,28})keys.add(new Key("wind"+a+"_"+t,a,t,true,0,0,0,0));
  for(int t:new int[]{0,8,18,22})keys.add(new Key("dash_"+t,DASH,t,true,0,t*.45F,t>=8&&t<=18?.7F:0,0));
  for(int t:new int[]{0,8,10,18,32})keys.add(new Key("leap_"+t,LEAP_ATTACK,t,t<10||t==32,t>=10&&t<32?-.2F:0,0,0,0));
  for(int t:new int[]{0,10,18,36,44})keys.add(new Key("air_"+t,AIR_DIVE,t,false,t>=10&&t<36?-.6F:.1F,0,0,0));
  for(int t:new int[]{8,18,22,34,46})keys.add(new Key("onslaught_"+t,ONSLAUGHT,t,true,0,t*.4F,t<=18?.7F:0,0));
  for(int t:new int[]{0,19,20,30,40})keys.add(new Key("transform_"+t,TRANSFORM,t,true,0,0,0,0));
  for(int t:new int[]{0,8,18,24})keys.add(new Key("break_"+t,BREAK_AWAY,t,true,0,0,0,0));
  for(int t:new int[]{0,10,30,45})keys.add(new Key("death_"+t,DYING,t,true,0,0,0,t));
  for(boolean second:new boolean[]{false,true}){
   var model=new NightingaleModel(second);var parts=(Map<String,ModelPart>)field(model,"parts");var poses=new JsonObject();
   for(var k:keys){model.sample(k.action,k.tick,second&&k.action!=TRANSFORM,k.grounded,k.speed,0,k.death,k.stride,k.amount,20,0,0);var polygons=new JsonArray();mesh(parts.get("root"),new PoseStack(),polygons);poses.add(k.name,polygons);if(!baseline){grip(parts,"L");grip(parts,"R");}}output.add(second?"second":"first",poses);
  }
  if(!baseline)for(boolean second:new boolean[]{false,true}){
   var model=new NightingaleModel(second);var parts=(Map<String,ModelPart>)field(model,"parts");
   for(int tick=1;tick<=55;tick++){
    model.sample(DYING,tick,second,true,0,0,tick,0,0,20,0,0);var faces=new JsonArray();mesh(parts.get("root"),new PoseStack(),faces);
    for(var face:faces)for(var vertex:face.getAsJsonArray())check(vertex.getAsJsonArray().get(1).getAsFloat()<=24.02F,"Death intersects floor form="+second+" tick="+tick);
   }
   for(int frame=0;frame<48;frame++){
    model.sample(IDLE,0,second,true,0,0,0,frame*.25F,.65F,20,0,0);float support=0;
    for(String side:new String[]{"L","R"}){var q=point(parts,new String[]{"root","pelvis","leg"+side,"knee"+side,"ankle"+side},new Vector3f(0,second?1.6F:1.5F,0));check(q.y<=24.02F&&q.y>=22.58F,"Walk foot floor clearance");support=Math.max(support,q.y);}check(support>=23.99F,"At least one grounded support foot");
   }
   model.sample(IDLE,0,second,true,0,0,0,0,0,20,0,0);var clean=new JsonArray();mesh(parts.get("root"),new PoseStack(),clean);
   for(var part:parts.values()){part.xScale=2;part.yScale=2;part.zScale=2;part.visible=false;part.skipDraw=true;}
   model.sample(IDLE,0,second,true,0,0,0,0,0,20,0,0);var reset=new JsonArray();mesh(parts.get("root"),new PoseStack(),reset);check(clean.equals(reset),"Full pose reset");
  }
  var controls=new JsonObject();for(boolean serpent:new boolean[]{false,true}){
   var model=new WorldBossModel(false,serpent);var parts=(Map<String,ModelPart>)field(model,"parts");var poses=new JsonObject();
   for(var m:new Move[]{Move.IDLE,Move.SPIN,Move.BREATH}){model.sample(m,18,Move.IDLE,0,0,0,0,0,0,20,0,0);var polygons=new JsonArray();mesh(parts.get("root"),new PoseStack(),polygons);poses.add(m.name(),polygons);}controls.add(serpent?"serpent":"warrior",poses);
  }output.add("tugarin_controls",controls);
  var path=Path.of(baseline?"docs/implementation/nightingale-57/before-native.json.gz":"build/nightingale-57/after-native.json.gz");Files.createDirectories(path.getParent());if(baseline&&Files.exists(path))throw new AssertionError("Preserve existing baseline");try(var out=new GZIPOutputStream(Files.newOutputStream(path))){out.write(new Gson().toJson(output).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  System.out.println("NIGHTINGALE_57_NATIVE "+(baseline?"BASELINE":"PASS")+" checks="+checks+" forms=2 poses="+keys.size()+" + 6 Tugarin controls; no world/bootstrap");
 }
}
