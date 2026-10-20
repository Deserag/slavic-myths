package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.client.CursedVolkolakModel;
import org.slavicmyths.volkolak.CursedVolkolak.Move;
public final class Cursed55Offline {
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
 private static String snapshot(Map<String,ModelPart> parts){var out=new StringBuilder();parts.forEach((n,p)->out.append(Arrays.toString(new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot})));return out.toString();}
 private static float bottom(Map<String,ModelPart> parts,String side,boolean hand){
  var pose=new PoseStack();String[] chain=hand?new String[]{"root","pelvis","spine","chest","upper"+side,"fore"+side,"hand"+side}:new String[]{"root","pelvis","thigh"+side,"shin"+side,"foot"+side};
  for(String name:chain)parts.get(name).translateAndRotate(pose);
  return pose.last().pose().transformPosition(new Vector3f(0,3F/16,0)).y*16;
 }
 private record Key(String name,Move action,float tick,boolean upright,boolean held,float movement){}
 @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
  var model=new CursedVolkolakModel();var parts=(Map<String,ModelPart>)field(model,"parts");check(parts.keySet().containsAll(List.of("root","pelvis","spine","chest","neck","head","muzzle","jaw","upperL","foreL","handL","upperR","foreR","handR","thighL","shinL","footL","thighR","shinR","footR","tail1","tail2","tail3","eyeL","eyeR")),"Preserved animated anchor chains; anatomy has no arbitrary minimum cube count");var keys=new ArrayList<Key>();
  keys.add(new Key("quad",Move.IDLE,20,false,false,0));keys.add(new Key("upright",Move.IDLE,20,true,false,0));keys.add(new Key("run_quad",Move.IDLE,20,false,false,.8F));keys.add(new Key("run_upright",Move.IDLE,20,true,false,.8F));
  for(var action:new Move[]{Move.SNIFF,Move.LISTEN,Move.ROAR,Move.HOWL_SHORT,Move.STAGGER,Move.ALERT,Move.SNARL})keys.add(new Key(action.name(),action,10,false,false,0));
  for(int tick:new int[]{0,4,8,12,16})keys.add(new Key("rise_"+tick,Move.RISE,tick,false,false,0));
  for(int tick:new int[]{0,6,12})keys.add(new Key("drop_"+tick,Move.DROP,tick,true,false,0));
  keys.add(new Key("clawL_10",Move.CLAW_L,10,true,false,0));keys.add(new Key("clawR_12",Move.CLAW_R,12,true,false,0));
  for(int tick:new int[]{10,20,30})keys.add(new Key("triple_"+tick,Move.TRIPLE,tick,true,false,0));
  keys.add(new Key("bite_12",Move.BITE,12,false,false,0));for(int tick:new int[]{13,20,28})keys.add(new Key("pounce_"+tick,Move.POUNCE,tick,false,false,0));
  for(int tick:new int[]{22,28,40})keys.add(new Key("grab_"+tick,Move.GRAB,tick,true,tick>=28,0));
  for(int tick:new int[]{0,19,49,87,102,131})keys.add(new Key("escape_"+tick,Move.ESCAPE,tick,false,false,0));
  var output=new JsonObject();for(var k:keys){model.sample(k.action,k.tick,k.upright,k.held,false,0,2,k.movement,20,0,0);var polygons=new JsonArray();mesh(parts.get("root"),new PoseStack(),polygons);output.add(k.name,polygons);}
  boolean baseline=Boolean.getBoolean("slavicmyths.cursed55Baseline");var path=Path.of(baseline?"docs/implementation/cursed-volkolak-55/before-native.json.gz":"build/cursed-55/after-native.json.gz");Files.createDirectories(path.getParent());if(baseline&&Files.exists(path))throw new AssertionError("Preserve existing baseline");try(var stream=new GZIPOutputStream(Files.newOutputStream(path))){stream.write(new Gson().toJson(output).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  if(!baseline){
   for(int phase=0;phase<48;phase++)for(boolean upright:new boolean[]{false,true}){
    model.sample(Move.IDLE,0,upright,false,false,0,phase*.15F,.8F,20,0,0);
    for(String side:List.of("L","R")){
     float foot=bottom(parts,side,false);check(foot<=24.01F&&foot>=21.98F,"Foot support/swing at phase "+phase);
     if(!upright){float hand=bottom(parts,side,true);check(hand<=24.01F&&hand>=21.33F,"Hand support/swing at phase "+phase);}
    }
   }
   for(var action:new Move[]{Move.RISE,Move.DROP})for(int tick=0;tick<=16;tick++){
    model.sample(action,tick,action==Move.DROP,false,false,0,0,0,20,0,0);
    for(String side:List.of("L","R"))check(Math.abs(bottom(parts,side,false)-24)<.01F,"Planted rear foot during "+action+"/"+tick);
    var transitionMesh=new JsonArray();mesh(parts.get("root"),new PoseStack(),transitionMesh);float height=action==Move.RISE?(tick<8?1.9F:2.8F):(tick<6?2.8F:1.9F);
    for(var face:transitionMesh)for(var vertex:face.getAsJsonArray())check(vertex.getAsJsonArray().get(1).getAsFloat()>=24-height*16-.01F,"Physical stance height during "+action+"/"+tick);

   }
   for(int tick=0;tick<=131;tick++){
    model.sample(Move.ESCAPE,tick,false,false,false,0,0,0,20,0,0);var polygons=new JsonArray();mesh(parts.get("root"),new PoseStack(),polygons);
    for(var face:polygons)for(var vertex:face.getAsJsonArray())check(vertex.getAsJsonArray().get(1).getAsFloat()<=24.02F,"No floor penetration in false-death tick "+tick);
   }
   for(int tick:new int[]{19,30,48}){model.sample(Move.ESCAPE,tick,false,false,false,0,0,0,20,0,0);check(!parts.get("eyeL").visible&&!parts.get("eyeR").visible,"Dormant eyes "+tick);}
   model.sample(Move.ESCAPE,49,false,false,false,0,0,0,20,0,0);check(parts.get("eyeL").visible,"Eyes revive on existing tick 49");
  }
  model.sample(Move.IDLE,20,false,false,false,0,0,0,20,0,0);String rest=snapshot(parts);for(var action:Move.values()){model.sample(action,24,true,true,true,9,2,1,90,20,10);model.sample(Move.IDLE,20,false,false,false,0,0,0,20,0,0);check(snapshot(parts).equals(rest),"No stale transforms after "+action);}
  System.out.println("CURSED_55_NATIVE "+(baseline?"BASELINE":"PASS")+" checks="+checks+" parts="+parts.size()+" poses="+keys.size()+"; no world/bootstrap");
 }
}
