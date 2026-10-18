package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.client.WorldBossModel;
import org.slavicmyths.hunt.BossRules.Move;
public final class Tugarin56Offline {
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
 private static PoseStack chain(Map<String,ModelPart> parts,String... names){var pose=new PoseStack();for(String name:names)parts.get(name).translateAndRotate(pose);return pose;}
 private static void supports(WorldBossModel model,Map<String,ModelPart> parts,Move move,float tick,float walk,float amount){
  model.sample(move,tick,Move.IDLE,0,0,0,0,walk,amount,20,0,0);
  int planted=0;for(String side:List.of("L","R")){
   var pose=chain(parts,"root","pelvis","leg"+side,"knee"+side,"ankle"+side);
   float y=pose.last().pose().transformPosition(new Vector3f(0,4F/16,0)).y*16;
   check(y<=24.01F&&y>=21.58F,"Actual boot support/swing "+move+"/"+tick);if(Math.abs(y-24)<.01F)planted++;
  }
  check(planted>=1,"At least one support foot "+move+"/"+tick);
 }
 private record Key(String name,Move action,float tick,Move previous){}
 @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
  boolean baseline=Boolean.getBoolean("slavicmyths.tugarin56Baseline");var output=new JsonObject();
  var keys=new ArrayList<Key>();
  for(int t:new int[]{0,8,15,16,22,28,34,40,48,51})keys.add(new Key("spin_"+t,Move.SPIN,t,Move.IDLE));
  for(int t:new int[]{0,12,24})keys.add(new Key("recovery_spin_"+t,Move.RECOVERY,t,Move.SPIN));
  for(int t:new int[]{0,7,10,16,22,28,34,40})keys.add(new Key("combo_"+t,Move.BLADE_COMBO,t,Move.IDLE));
  for(int t:new int[]{0,5,10,16,22})keys.add(new Key("charge_"+t,Move.CHARGE,t,Move.IDLE));
  for(int t:new int[]{0,20,30,40,60})keys.add(new Key("transition_"+t,Move.TRANSITION,t,Move.IDLE));
  for(int t:new int[]{0,8,20,36})keys.add(new Key("wing_"+t,Move.WING_DIVE,t,Move.IDLE));
  keys.add(new Key("landing",Move.RECOVERY,8,Move.WING_DIVE));
  for(var move:new Move[]{Move.IDLE,Move.PUNCH,Move.SWEEP,Move.STONE,Move.SLAM,Move.BREATH,Move.SERPENT_LUNGE,Move.SMOKE_BURST})keys.add(new Key(move.name(),move,10,Move.IDLE));
  for(String form:List.of("warrior","serpent","likho","mor_likho")){
   var model=new WorldBossModel(form.contains("likho"),form.equals("serpent"),form.equals("mor_likho"));var parts=(Map<String,ModelPart>)field(model,"parts");var poses=new JsonObject();
   for(var key:keys){model.sample(key.action,key.tick,key.previous,0,0,0,0,2,.0F,20,0,0);var polygons=new JsonArray();mesh(parts.get("root"),new PoseStack(),polygons);poses.add(key.name,polygons);}
   output.add(form,poses);
   if(!baseline&&form.equals("serpent")){
    var firePoses=new JsonObject();for(int tick:new int[]{18,30,41}){
     model.sample(Move.BREATH,tick,Move.IDLE,0,0,0,0,0,0,20,0,0);
     var pose=chain(parts,"root","body","neck","neckUpper","head","mouthGlow");
     var mouth=pose.last().pose().transformPosition(new Vector3f(0,4.5F/16,-23.75F/16)).mul(16);
     check(Math.abs(mouth.x)<.01F&&Math.abs(mouth.y-(24-3.95F*16))<.01F&&Math.abs(mouth.z+16)<.01F,"Mouth matches existing server fire origin tick "+tick);
     var polygons=new JsonArray();mesh(parts.get("root"),new PoseStack(),polygons);firePoses.add("fire_"+tick,polygons);
     for(var face:polygons)for(var v:face.getAsJsonArray())check(v.getAsJsonArray().get(1).getAsFloat()>=24-4.85F*16-.01F,"Serpent fire pose fits existing height tick "+tick);
     model.sample(Move.BREATH,tick,Move.IDLE,6,0,0,0,0,0,20,0,0);
     var hurtPose=chain(parts,"root","body","neck","neckUpper","head","mouthGlow");var hurtMouth=hurtPose.last().pose().transformPosition(new Vector3f(0,4.5F/16,-23.75F/16)).mul(16);
     check(Math.abs(hurtMouth.x)<.01F&&Math.abs(hurtMouth.y-(24-3.95F*16))<.01F&&Math.abs(hurtMouth.z+16)<.01F,"Fire origin during hurt tick "+tick);
    }output.add("fire_release",firePoses);
   }
   if(!baseline&&form.equals("warrior")){
    model.sample(Move.SPIN,100,Move.IDLE,0,0,0,0,0,0,20,0,0);check(Math.abs(parts.get("pelvis").yRot-3*Math.PI*2)<.001,"No fourth revolution during delayed sync");
    for(int tick=0;tick<52;tick++)supports(model,parts,Move.SPIN,tick,0,0);
    for(int phase=0;phase<48;phase++)for(var move:new Move[]{Move.IDLE,Move.CHARGE})supports(model,parts,move,16,phase*.15F,.8F);
    for(var key:keys){
     model.sample(key.action,key.tick,key.previous,0,0,0,0,2,0,20,0,0);
     for(String side:List.of("L","R")){
      var wrist=chain(parts,"root","pelvis","body","arm"+side,"fore"+side,"wrist"+side);
      var inverse=new org.joml.Matrix4f(wrist.last().pose()).invert();parts.get("blade"+side).translateAndRotate(wrist);
      var point=wrist.last().pose().transformPosition(new Vector3f(0,1.6F/16,0));inverse.transformPosition(point);point.mul(16);
      check(Math.abs(point.x)<=4.5F&&point.y>=-2&&point.y<=4&&Math.abs(point.z)<=3.5F,"Actual grip inside gauntlet "+key.name+side);
     }
    }
   }
  }
  var path=Path.of(baseline?"docs/implementation/tugarin-56/before-native.json.gz":"build/tugarin-56/after-native.json.gz");Files.createDirectories(path.getParent());if(baseline&&Files.exists(path))throw new AssertionError("Preserve existing baseline");try(var out=new GZIPOutputStream(Files.newOutputStream(path))){out.write(new Gson().toJson(output).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  System.out.println("TUGARIN_56_NATIVE "+(baseline?"BASELINE":"PASS")+" checks="+checks+" forms=4 poses="+keys.size()+"; no world/bootstrap");
 }
}
