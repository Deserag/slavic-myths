package org.slavicmyths.verify;
import java.nio.file.*;import java.util.*;import java.util.zip.GZIPOutputStream;
import com.google.gson.*;import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.navya.*;import org.slavicmyths.navya.client.*;
import org.slavicmyths.blackcinder.*;import org.slavicmyths.blackcinder.client.*;
import org.slavicmyths.burningislands.*;import org.slavicmyths.burningislands.client.*;
import org.slavicmyths.losthalls.client.HallsMesh;
import org.slavicmyths.client.TrophyHeadsClient;
public final class Mob332Export {
 interface Pose {void apply(String state,float t,float walk,float amount);}
 record Rig(ModelPart root,Map<String,ModelPart> nodes,String texture,Pose pose){}
 @SuppressWarnings("unchecked") static Map<String,ModelPart> nodes(Object o)throws Exception{var f=o.getClass().getDeclaredField("nodes");f.setAccessible(true);return (Map<String,ModelPart>)f.get(o);}
 static JsonObject frame(Rig rig,String state,float t,float walk,float amount){rig.pose.apply(state,t,walk,amount);var cap=new TrophyHeadsClient.Capture();rig.root.render(new PoseStack(),cap,0,0);JsonObject d=new JsonObject();d.addProperty("state",state);d.addProperty("time",t);d.addProperty("walk",walk);d.addProperty("amount",amount);JsonArray faces=new JsonArray();for(int i=0;i<cap.vertices.size();i+=4){JsonArray face=new JsonArray();for(int j=0;j<4;j++){JsonArray v=new JsonArray();var raw=cap.vertices.get(i+j);for(int k=0;k<5;k++){if(!Float.isFinite(raw[k]))throw new AssertionError("nonfinite");v.add(k<3?raw[k]*16:raw[k]);}face.add(v);}faces.add(face);}d.add("faces",faces);JsonObject transforms=new JsonObject();rig.nodes.forEach((n,p)->transforms.add(n,new Gson().toJsonTree(new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot,p.xScale,p.yScale,p.zScale})));d.add("parts",transforms);return d;}
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  Map<String,Rig> rigs=new LinkedHashMap<>();
  for(var k:NavyaContent.Kind.values()){if(k==NavyaContent.Kind.GUARDIAN)continue;var m=NavyaMesh.create(k);String n=NavyaMesh.name(k);rigs.put(n,new Rig(m.root(),m.nodes(),"textures/entity/"+n+".png",(s,t,w,a)->{var act=s.equals("attack")?k==NavyaContent.Kind.NAVKA?NavyaMob.Action.SHOT:k==NavyaContent.Kind.ROOTLING?NavyaMob.Action.ROOTS:NavyaMob.Action.MELEE:NavyaMob.Action.IDLE;NavyaPose.pose(m,k,act,t,t,w,a,0,0,s.equals("hurt")?6:0,s.equals("death")?t:0);}));}
  for(var k:CinderContent.Kind.values()){if(k==CinderContent.Kind.ZHARENITSA)continue;String n=CinderContent.id(k);var m=new CinderModel(JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/slavicmyths/cinder_rigs/"+n+".json"))).getAsJsonObject());rigs.put(n,new Rig(m.root(),nodes(m),"textures/entity/"+n+".png",(s,t,w,a)->m.pose(k,s.equals("attack")?k==CinderContent.Kind.COAL_CRAWLER?CinderMob.Action.BITE:k==CinderContent.Kind.ASH_SPIRIT?CinderMob.Action.PULSE:CinderMob.Action.COAL:CinderMob.Action.IDLE,t,false,k==CinderContent.Kind.ASH_SPIRIT||k==CinderContent.Kind.SPARKLING,w,a,t,0,0,s.equals("hurt")?6:0,s.equals("death")?(int)t:0)));}
  for(String n:List.of("sheptun","oskolnik","gulokryl","ekhovik","griboshmyg"))for(int variant=0;variant<(n.equals("sheptun")?4:1);variant++){
   final int v=variant;var m=new HallsMesh(n);String key=n+(n.equals("sheptun")?"_"+v:"");rigs.put(key,new Rig(m.root,m.parts,"textures/entity/lost_halls/"+key+".png",(s,t,w,a)->{
    try{m.getClass().getMethod("poseMob",String.class,float.class,float.class,float.class,float.class,int.class,int.class,boolean.class,boolean.class,int.class).invoke(m,n,w,a,t,s.equals("attack")?t:0,s.equals("hurt")?6:0,s.equals("death")?(int)t:0,s.equals("sit"),s.equals("glide"),v);}catch(NoSuchMethodException missing){m.pose(w,a,t,s.equals("attack")?t:0,0,s.equals("hurt")?6:0,s.equals("death")?(int)t:0);}catch(Exception e){throw new RuntimeException(e);}
   }));}
  for(var k:IslandsContent.Kind.values()){if(k==IslandsContent.Kind.MAGMA_LORD)continue;String n=IslandsContent.id(k);var m=IslandsModel.bundled(k);rigs.put(n,new Rig(m.root(),nodes(m),"textures/entity/burning_islands/"+n+".png",(s,t,w,a)->m.pose(k,s.equals("attack")?MagmaCombat.Action.CLAWS:s.equals("alert")?MagmaCombat.Action.WARN:MagmaCombat.Action.IDLE,t,false,false,w,a,t,0,0,s.equals("hurt")?6:0,s.equals("death")?(int)t:0)));}
  Path out=Path.of("docs/implementation/mob332/"+(args.length==0?"after":args[0]));Files.createDirectories(out);
  for(var e:rigs.entrySet()){
   JsonObject d=new JsonObject();d.addProperty("id",e.getKey());d.addProperty("texture",e.getValue().texture);JsonArray frames=new JsonArray();
   for(String state:List.of("idle","walk","run","stop","alert","attack","hurt","death","sit","glide")){
    int count=state.equals("walk")?48:state.equals("attack")?24:state.equals("run")?24:8;
    for(int i=0;i<count;i++){float t=i*2,w=state.equals("walk")?i*(float)(6*Math.PI/.8/48):state.equals("run")?i*.8F:state.equals("stop")?24:0;float a=state.equals("walk")?.7F:state.equals("run")?1:state.equals("stop")?(1-i/7F)*.7F:0;frames.add(frame(e.getValue(),state,t,w,a));}
   }
   if(e.getKey().equals("coal_crawler"))for(int i=0;i<96;i++)frames.add(frame(e.getValue(),"walk_long",i*2,i*(float)(6*Math.PI/.55/96),.7F));
   d.add("frames",frames);try(var stream=new GZIPOutputStream(Files.newOutputStream(out.resolve(e.getKey()+".json.gz")))){stream.write(new Gson().toJson(d).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
   Files.copy(Path.of("src/main/resources/assets/slavicmyths/"+e.getValue().texture),out.resolve(e.getKey()+".png"),StandardCopyOption.REPLACE_EXISTING);
  }System.out.println("MOB332 native export: "+rigs.size()+" variants, real ModelPart UV/texture/poses; no game/world/server started.");
 }
}
