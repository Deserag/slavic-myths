import java.util.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.client.*;
import org.slavicmyths.skim.*;
/** Production baked mesh/UV/animation sampling, no world, loader or game bootstrap. */
public final class Skim77Native {
 static String fingerprint(Map<String,ModelPart> nodes){var s=new StringBuilder();new TreeMap<>(nodes).forEach((k,p)->s.append(k).append(Arrays.toString(new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot,p.xScale,p.yScale,p.zScale})).append(p.visible).append(p.skipDraw));return s.toString();}
 static Object field(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 @SuppressWarnings("unchecked") static void mesh(String name,ModelPart part,PoseStack pose,JsonArray faces,JsonObject joints)throws Exception{
  if(!part.visible)return;pose.pushPose();part.translateAndRotate(pose);var joint=new JsonArray();var at=pose.last().pose().transformPosition(new Vector3f()).mul(16);for(float v:new float[]{at.x,at.y,at.z})joint.add(v);joints.add(name,joint);
  for(var cube:(List<?>)field(part,"cubes"))for(var face:(Object[])field(cube,"polygons")){
   var row=new JsonObject();row.addProperty("part",name);var poly=new JsonArray();
   for(var vert:(Object[])field(face,"vertices")){
    var p=pose.last().pose().transformPosition(new Vector3f((Vector3f)field(vert,"pos")).div(16)).mul(16);float u=(float)field(vert,"u"),v=(float)field(vert,"v");
    if(!Float.isFinite(p.x)||!Float.isFinite(p.y)||!Float.isFinite(p.z)||u<0||u>1||v<0||v>1)throw new AssertionError("Native finite/UV "+name+" "+u+"/"+v);
    var a=new JsonArray();for(float x:new float[]{p.x,p.y,p.z,u,v})a.add(x);poly.add(a);
   }row.add("vertices",poly);faces.add(row);
  }
  for(var e:((Map<String,ModelPart>)field(part,"children")).entrySet())mesh(e.getKey(),e.getValue(),pose,faces,joints);pose.popPose();
 }
 static Vector3f point(ModelPart root,boolean jaw,float x,float y,float z){var pose=new PoseStack();root.translateAndRotate(pose);var b=root.getChild("body");b.translateAndRotate(pose);var neck=b.getChild("neck");neck.translateAndRotate(pose);var head=neck.getChild("head");head.translateAndRotate(pose);if(jaw)head.getChild("jaw").translateAndRotate(pose);return pose.last().pose().transformPosition(new Vector3f(x/16,y/16,z/16)).mul(16);}
 static JsonObject capture(ModelPart root)throws Exception{var o=new JsonObject();var f=new JsonArray();var j=new JsonObject();mesh("root",root,new PoseStack(),f,j);o.add("faces",f);o.add("joints",j);if(root.hasChild("body")){var contact=point(root,true,0,-.3F,-11).add(point(root,false,0,5.4F,-13)).mul(.5F);var c=new JsonArray();c.add(contact.x);c.add(contact.y);c.add(contact.z);j.add("mouth_contact",c);}return o;}
 public static void main(String[] args)throws Exception{
  boolean baseline=Boolean.getBoolean("skim77.baseline"),quick=Boolean.getBoolean("skim77.quick");String label=baseline?"before":"after";var model=new SkimModel<SkimActor>(SkimMesh.create(),true);var poses=new JsonObject();
  var actions=List.of(SkimAction.IDLE,SkimAction.AWAKEN,SkimAction.CLAWS,SkimAction.LEAP,SkimAction.WHISTLE,SkimAction.ROAR,SkimAction.FLANK,SkimAction.GRIP,SkimAction.RAGE,SkimAction.EXHAUSTED,SkimAction.STAGGER,SkimAction.RECOVER,SkimAction.DEATH);
  for(var action:actions)for(int t:new TreeSet<Integer>(Arrays.asList(0,5,8,10,11,12,14,16,18,19,21,22,24,26,28,29,30,31,39,41,44,45,52,58,65,70,75,action.ticks))){
   if(t>Math.max(80,action.ticks)||quick&&!List.of(0,11,19,21,22,24,28,29,30,45,75).contains(t))continue;model.pose(action,t,0,0,20,0,0);var o=capture(model.root);o.addProperty("action",action.name());o.addProperty("tick",t);o.addProperty("rageGlow",SkimModel.rageGlow(action,t));o.addProperty("eyeGlow",SkimModel.eyeGlow(action,t));poses.add(action.name()+"_"+t,o);
  }
  for(int phase=0;phase<(quick?0:48);phase++)for(float amount:new float[]{.3F,.8F}){model.pose(SkimAction.IDLE,0,phase*.3F,amount,20,0,0);poses.add("walk_"+amount+"_"+phase,capture(model.root));}
  for(var action:List.of(SkimAction.ROAR,SkimAction.EXHAUSTED,SkimAction.DEATH,SkimAction.LEAP,SkimAction.CLAWS))for(int t=0;t<=(quick?-1:action.ticks);t++){model.pose(action,t,0,0,20,0,0);poses.add("all_"+action+"_"+t,capture(model.root));}
  if(!baseline){
   for(int t=0;t<=58;t++){model.poseBeast(SkimAction.ROAR,t,0,0,20,0,0,false,0);poses.add("low_ROAR_"+t,capture(model.root));}
   for(int t=0;t<58;t++){model.poseBeast(SkimAction.ROAR,t+.5F,0,0,20,0,0,true,0);poses.add("half_ROAR_"+t,capture(model.root));}
   for(int t=0;t<=65;t++){model.poseBeast(SkimAction.GRIP,t,0,0,20,0,0,true,0);poses.add("all_GRIP_"+t,capture(model.root));}
   for(float yaw:new float[]{-35,0,35})for(float pitch:new float[]{-20,0,20})for(float hurt:new float[]{0,8}){
    model.poseBeast(SkimAction.IDLE,0,0,0,20,yaw,pitch,true,hurt);poses.add("look_"+yaw+"_"+pitch+"_"+hurt,capture(model.root));
    model.poseBeast(SkimAction.GRIP,31,0,0,20,yaw,pitch,true,hurt);poses.add("held_look_"+yaw+"_"+pitch+"_"+hurt,capture(model.root));
    model.poseBeast(SkimAction.ROAR,28,0,0,20,yaw,pitch,true,hurt);poses.add("rear_look_"+yaw+"_"+pitch+"_"+hurt,capture(model.root));
    model.poseBeast(SkimAction.ROAR,28,0,0,20,yaw,pitch,false,hurt);poses.add("low_rear_look_"+yaw+"_"+pitch+"_"+hurt,capture(model.root));
   }
   var victim=net.minecraft.client.model.geom.builders.LayerDefinition.create(net.minecraft.client.model.HumanoidModel.createMesh(net.minecraft.client.model.geom.builders.CubeDeformation.NONE,0),64,64).bakeRoot();
   var vp=new PoseStack();vp.translate(0,(24-.55F*16)/16-24F/16*.9375F,-1.7F);vp.scale(.9375F,.9375F,.9375F);var vf=new JsonArray();var vj=new JsonObject();mesh("victim",victim,vp,vf,vj);var vo=new JsonObject();vo.add("faces",vf);vo.add("joints",vj);poses.add("victim",vo);
  }
  model.pose(SkimAction.IDLE,0,0,0,20,0,0);String reset=fingerprint(model.nodes);
  for(var action:actions){model.poseBeast(action,24,3,.8F,80,35,20,true,8);model.pose(SkimAction.IDLE,0,0,0,20,0,0);if(!fingerprint(model.nodes).equals(reset))throw new AssertionError("Stale pose "+action);}
  if(SkimModel.rageGlow(SkimAction.RAGE,0)!=0||SkimModel.rageGlow(SkimAction.RAGE,20)!=1||SkimModel.rageGlow(SkimAction.RAGE,64)!=0||SkimModel.rageGlow(SkimAction.CLAWS,22)!=0||SkimModel.eyeGlow(SkimAction.DEATH,75)!=0)throw new AssertionError("Glow clip envelope");
  var timings=new JsonObject();for(var action:List.of(SkimAction.IDLE,SkimAction.ROAR,SkimAction.GRIP,SkimAction.DEATH)){
   for(int i=0;i<200;i++)model.pose(action,i%Math.max(1,action.ticks),0,0,20,0,0);
   long start=System.nanoTime();for(int i=0;i<2000;i++)model.pose(action,i%Math.max(1,action.ticks),0,0,20,0,0);timings.addProperty(action.name()+"_microseconds_per_sample",(System.nanoTime()-start)/2000000.0);
  }
  timings.addProperty("scope","CPU pose only,200warmup+2000samples; no world, no GPU/FPS claim");
  // Alyosha source pose fingerprints detect unintended changes to the shared branch.
  var hero=new SkimModel<SkimActor>(AlyoshaMesh.create(),false);var heroes=new JsonObject();
  for(var a:(quick?new SkimAction[]{}:SkimAction.values()))for(int t:new int[]{0,8,16,24,40,65}){hero.pose(a,t,2,.6F,20,10,5);heroes.add(a+"_"+t,capture(hero.root));}
  var data=new JsonObject();data.addProperty("native",true);data.addProperty("quick",quick);var hashes=new JsonObject();for(String name:List.of("src/main/java/org/slavicmyths/client/SkimMesh.java","src/main/java/org/slavicmyths/client/SkimModel.java","src/main/java/org/slavicmyths/client/SkimBeastPose.java","src/main/java/org/slavicmyths/client/SkimBeastGlow.java","src/main/java/org/slavicmyths/client/SkimClient.java","src/main/resources/assets/slavicmyths/textures/entity/skim_beast.png","src/main/resources/assets/slavicmyths/textures/entity/skim_beast_eyes.png","src/main/resources/assets/slavicmyths/textures/entity/skim_beast_rage.png"))hashes.addProperty(name,java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of(name)))));data.add("files",hashes);data.addProperty("depthScale",1);data.add("poses",poses);data.add("alyosha",heroes);
  var dir=Path.of("docs/implementation/skim-77/"+label);Files.createDirectories(dir);var file=dir.resolve("native-poses.json.gz");if(baseline&&Files.exists(file))throw new AssertionError("Preserve baseline");
  try(var s=new GZIPOutputStream(Files.newOutputStream(file))){s.write(new Gson().toJson(data).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  Files.writeString(dir.resolve("cpu-sample.json"),new GsonBuilder().setPrettyPrinting().create().toJson(timings));
  System.out.println("SKIM77_NATIVE "+label+" poses="+poses.size()+" Alyosha="+heroes.size()+" NO_GAME_NO_WORLD");
 }
}
