import java.util.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.client.CursedVolkolakModel;
import org.slavicmyths.volkolak.CursedVolkolak.Move;

/** Real baked ModelPart/UV and production animation sampling. No world, bootstrap or game. */
public final class Cursed76Native {
 static String snapshot(Map<String,ModelPart> parts){var out=new StringBuilder();parts.forEach((n,p)->out.append(Arrays.toString(new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot,p.xScale,p.yScale,p.zScale})).append(p.visible).append(p.skipDraw));return out.toString();}
 static Object field(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 @SuppressWarnings("unchecked") static void mesh(String name,ModelPart part,PoseStack pose,JsonArray faces,JsonObject joints,Map<ModelPart,String> names)throws Exception{
  if(!part.visible)return;pose.pushPose();part.translateAndRotate(pose);
  var joint=new JsonArray();var at=pose.last().pose().transformPosition(new Vector3f()).mul(16);for(float v:new float[]{at.x,at.y,at.z})joint.add(v);joints.add(name,joint);
  if(!part.skipDraw)for(var cube:(List<?>)field(part,"cubes"))for(var face:(Object[])field(cube,"polygons")){
   var row=new JsonObject();row.addProperty("part",name);var polygon=new JsonArray();
   for(var vertex:(Object[])field(face,"vertices")){
    var p=pose.last().pose().transformPosition(new Vector3f((Vector3f)field(vertex,"pos")).div(16)).mul(16);float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");
    if(!Float.isFinite(p.x)||!Float.isFinite(p.y)||!Float.isFinite(p.z)||u<0||u>1||v<0||v>1)throw new AssertionError("baked nonfinite/UV");
    var a=new JsonArray();for(float k:new float[]{p.x,p.y,p.z,u,v})a.add(k);polygon.add(a);
   }row.add("vertices",polygon);faces.add(row);
  }
  for(var child:((Map<String,ModelPart>)field(part,"children")).values())mesh(names.getOrDefault(child,"victim"),child,pose,faces,joints,names);pose.popPose();
 }
 record Key(String name,Move move,float tick,boolean upright,boolean held,float walk,float amount){}
 @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
  boolean baseline=Boolean.getBoolean("cursed76.baseline");String label=baseline?"before":"after";
  var model=new CursedVolkolakModel();var parts=(Map<String,ModelPart>)field(model,"parts");var names=new IdentityHashMap<ModelPart,String>();parts.forEach((n,p)->names.put(p,n));
  var keys=new ArrayList<Key>();keys.add(new Key("quad",Move.IDLE,0,false,false,0,0));keys.add(new Key("upright",Move.IDLE,0,true,false,0,0));
  for(boolean upright:new boolean[]{false,true})for(int phase=0;phase<8;phase++)keys.add(new Key("walk_"+(upright?"up":"quad")+phase,Move.IDLE,0,upright,false,phase*.8F,.6F));
  for(float t:new float[]{0,4,7,8,11,12,15,16})keys.add(new Key("rise_"+t,Move.RISE,t,false,false,0,0));
  for(float t:new float[]{0,3,6,9,12})keys.add(new Key("drop_"+t,Move.DROP,t,true,false,0,0));
  for(var move:Move.values())if(move!=Move.RISE&&move!=Move.DROP&&move!=Move.IDLE){
   for(int t:new TreeSet<Integer>(Arrays.asList(0,7,10,12,13,14,19,20,21,22,24,25,28,30,36,45,49,60,move.length)))if(t<=move.length)keys.add(new Key(move.name()+"_"+t,move,t,move==Move.CLAW_L||move==Move.CLAW_R||move==Move.DOUBLE||move==Move.TRIPLE||move==Move.GRAB,move==Move.GRAB&&t>=24&&t<=45,0,0));
  }
  for(int t:new int[]{19,30,48,49,60,87,102,131})keys.add(new Key("ESCAPE_"+t,Move.ESCAPE,t,false,false,0,0));
  for(int t=0;t<=16;t++)keys.add(new Key("rise_all_"+t,Move.RISE,t,false,false,0,0));
  for(int t=0;t<=12;t++)keys.add(new Key("drop_all_"+t,Move.DROP,t,true,false,0,0));
  for(int t=0;t<=131;t++)keys.add(new Key("escape_all_"+t,Move.ESCAPE,t,false,false,0,0));
  for(int t:new int[]{0,21,24,28,30,45,60})for(boolean held:new boolean[]{false,true})keys.add(new Key("grab_"+held+"_"+t,Move.GRAB,t,true,held,0,0));
  for(boolean up:new boolean[]{false,true})for(int phase=0;phase<48;phase++)keys.add(new Key("walk_all_"+up+"_"+phase,Move.IDLE,0,up,false,phase*.15F,.8F));
  var export=new JsonObject();export.addProperty("native",true);export.addProperty("parts",parts.size());export.addProperty("depthScale",org.slavicmyths.client.CursedVolkolakRenderer.DEPTH_SCALE);var poses=new JsonObject();
  for(var key:keys){
   model.sample(key.move,key.tick,key.upright,key.held,false,0,key.walk,key.amount,20,0,0);
   var faces=new JsonArray();var joints=new JsonObject();mesh("root",parts.get("root"),new PoseStack(),faces,joints,names);
   var out=new JsonObject();out.addProperty("move",key.move.name());out.addProperty("tick",key.tick);out.addProperty("upright",key.upright);out.addProperty("held",key.held);out.add("faces",faces);out.add("joints",joints);poses.add(key.name,out);
  }
  // Exercise the same blend entry point setupAnim calls, with a plain identity and no world.
  var identity=new Object();float age=100;
  for(var action:new Move[]{Move.IDLE,Move.RISE,Move.CLAW_L,Move.CLAW_R,Move.GRAB,Move.DROP,Move.ESCAPE,Move.IDLE}){
   for(int frame=0;frame<=6;frame++){
    model.sample(action,frame,action==Move.CLAW_L||action==Move.CLAW_R||action==Move.GRAB,action==Move.GRAB,false,0,0,0,age,0,0);
    model.blendPose(identity,action.ordinal()*10,age++);
    var faces=new JsonArray();var joints=new JsonObject();mesh("root",parts.get("root"),new PoseStack(),faces,joints,names);
    var out=new JsonObject();out.add("faces",faces);out.add("joints",joints);poses.add("blend_"+action+"_"+frame,out);
   }
  }
  for(boolean rising:new boolean[]{true,false}){
   Object id=new Object();float clock=300;
   for(int tick=0;tick<=(rising?16:12);tick++){
    var action=rising?Move.RISE:Move.DROP;
    model.sample(action,tick,!rising,false,false,0,0,0,clock,0,0);model.blendPose(id,action.ordinal()*10,clock++);
   }
   for(int tick=0;tick<=6;tick++){
    model.sample(Move.IDLE,tick,rising,false,false,0,0,0,clock,0,0);model.blendPose(id,rising?1:0,clock++);
    var faces=new JsonArray();var joints=new JsonObject();mesh("root",parts.get("root"),new PoseStack(),faces,joints,names);var out=new JsonObject();out.add("faces",faces);out.add("joints",joints);poses.add("boundary_"+(rising?"rise":"drop")+"_idle_"+tick,out);
   }
  }
  for(boolean upright:new boolean[]{false,true})for(int yaw:new int[]{-25,0,25})for(int pitch:new int[]{-30,0,30}){
   model.sample(Move.IDLE,0,upright,false,false,8,0,0,20,yaw,pitch);var faces=new JsonArray();var joints=new JsonObject();mesh("root",parts.get("root"),new PoseStack(),faces,joints,names);var out=new JsonObject();out.add("faces",faces);out.add("joints",joints);poses.add("look_"+upright+"_"+yaw+"_"+pitch,out);
  }
  model.sample(Move.IDLE,0,false,false,false,0,0,0,20,0,0);String reset=snapshot(parts);
  for(var action:Move.values()){
   model.sample(action,24,true,true,true,9,2,1,90,25,30);
   model.sample(Move.IDLE,0,false,false,false,0,0,0,20,0,0);
   if(!snapshot(parts).equals(reset))throw new AssertionError("Stale pose after "+action);
  }
  // Frozen server held-feet anchor .7 forward / .18 up; vanilla baked 64px humanoid fixture.
  var victim=net.minecraft.client.model.geom.builders.LayerDefinition.create(net.minecraft.client.model.HumanoidModel.createMesh(net.minecraft.client.model.geom.builders.CubeDeformation.NONE,0),64,64).bakeRoot();
  var vp=new PoseStack();vp.translate(0,-.18F,-.7F/org.slavicmyths.client.CursedVolkolakRenderer.DEPTH_SCALE);
  var vf=new JsonArray();var vj=new JsonObject();mesh("victim",victim,vp,vf,vj,new IdentityHashMap<>());
  var vo=new JsonObject();vo.add("faces",vf);vo.add("joints",vj);poses.add("victim",vo);
  var wolf=new org.slavicmyths.client.GreyWolfModel();wolf.sample(org.slavicmyths.wolf.WolfTimeline.State.TRIAL_ACTIVE,org.slavicmyths.wolf.WolfTimeline.Move.NONE,20,false,0,0,0,20,0,0);
  var wp=(Map<String,ModelPart>)field(wolf,"parts");var wn=new IdentityHashMap<ModelPart,String>();wp.forEach((n,p)->wn.put(p,n));
  var wf=new JsonArray();var wj=new JsonObject();mesh("root",wp.get("root"),new PoseStack(),wf,wj,wn);var wo=new JsonObject();wo.add("faces",wf);wo.add("joints",wj);poses.add("greywolf_reference",wo);
  export.add("poses",poses);var dir=Path.of("docs/implementation/volkolak-76/"+label);Files.createDirectories(dir);var file=dir.resolve("native-poses.json.gz");if(baseline&&Files.exists(file))throw new AssertionError("preserve baseline");
  try(var stream=new GZIPOutputStream(Files.newOutputStream(file))){stream.write(new Gson().toJson(export).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  var timings=new JsonObject();
  for(var move:new Move[]{Move.IDLE,Move.RISE,Move.GRAB,Move.ESCAPE}){
   for(int i=0;i<150;i++)model.sample(move,i%Math.max(1,move.length),false,move==Move.GRAB,false,0,0,0,20,0,0);
   long start=System.nanoTime();for(int i=0;i<1500;i++)model.sample(move,i%Math.max(1,move.length),false,move==Move.GRAB,false,0,0,0,20,0,0);
   timings.addProperty(move.name()+"_microseconds_per_sample",(System.nanoTime()-start)/1500000.0);
  }
  timings.addProperty("scope","CPU model.sample only; warmup150, samples1500; not FPS, MSPT or in-game benchmark");Files.writeString(dir.resolve("cpu-sample.json"),new GsonBuilder().setPrettyPrinting().create().toJson(timings));
  System.out.println("CURSED76_NATIVE "+label+" parts="+parts.size()+" poses="+keys.size()+"; no Minecraft/world launch");
 }
}

