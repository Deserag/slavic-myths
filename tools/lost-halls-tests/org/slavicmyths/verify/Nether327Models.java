package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.client.TrophyHeadsClient;
import org.slavicmyths.navya.*;
import org.slavicmyths.navya.client.*;
import org.slavicmyths.blackcinder.*;
import org.slavicmyths.blackcinder.client.*;
import org.slavicmyths.burningislands.*;
import org.slavicmyths.burningislands.client.*;
import org.slavicmyths.losthalls.client.*;
/** Native ModelPart rendering only, no client/server/world/GameTest creation. */
public final class Nether327Models {
 static JsonObject frame(ModelPart part,String action,float time){var out=new TrophyHeadsClient.Capture();part.render(new PoseStack(),out,0,0);JsonObject f=new JsonObject();f.addProperty("action",action);f.addProperty("time",time);JsonArray faces=new JsonArray();for(int i=0;i<out.vertices.size();i+=4){JsonArray face=new JsonArray();for(int j=0;j<4;j++){var v=out.vertices.get(i+j);JsonArray point=new JsonArray();for(int k=0;k<5;k++){if(!Float.isFinite(v[k])||k>=3&&(v[k]<0||v[k]>1))throw new AssertionError("Nonfinite/UV "+action);point.add(k<3?v[k]*16:v[k]);}face.add(point);}faces.add(face);}f.add("faces",faces);return f;}
 static JsonObject record(String texture,JsonArray frames){JsonObject r=new JsonObject();r.addProperty("texture",texture);r.add("frames",frames);return r;}
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();JsonObject data=new JsonObject();
  var nav=NavyaMesh.create(NavyaContent.Kind.GUARDIAN);JsonArray a=new JsonArray();
  for(var action:List.of(NavyaMob.Action.IDLE,NavyaMob.Action.MELEE,NavyaMob.Action.ROOTS,NavyaMob.Action.THROW,NavyaMob.Action.SUMMON,NavyaMob.Action.PHASE))for(float t:new float[]{0,NavyaTiming.windup(NavyaContent.Kind.GUARDIAN,action),NavyaTiming.duration(NavyaContent.Kind.GUARDIAN,action)-1}){NavyaPose.pose(nav,NavyaContent.Kind.GUARDIAN,action,t,20,1,.6F,0,0,0,0);a.add(frame(nav.root(),action.name(),t));}
  for(int state=1;state<=2;state++){NavyaPose.pose(nav,NavyaContent.Kind.GUARDIAN,NavyaMob.Action.IDLE,0,20,0,0,0,0,state==1?6:0,state==2?15:0);a.add(frame(nav.root(),state==1?"hurt":"death",0));}
  data.add("grove_guardian",record("textures/entity/grove_guardian.png",a));
  var cinder=new CinderModel(JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/slavicmyths/cinder_rigs/zharenitsa.json"))).getAsJsonObject());a=new JsonArray();
  for(var action:List.of(CinderMob.Action.IDLE,CinderMob.Action.RING,CinderMob.Action.SPIKES,CinderMob.Action.SHIFT,CinderMob.Action.VORTEX,CinderMob.Action.PHASE))for(float t:new float[]{0,CinderMob.windup(action),CinderMob.windup(action)+17}){cinder.pose(CinderContent.Kind.ZHARENITSA,action,t,true,true,1,.6F,20,0,0,0,0);a.add(frame(cinder.root(),action.name(),t));}
  for(int state=1;state<=2;state++){cinder.pose(CinderContent.Kind.ZHARENITSA,CinderMob.Action.IDLE,0,false,true,0,0,20,0,0,state==1?6:0,state==2?15:0);a.add(frame(cinder.root(),state==1?"hurt":"death",0));}
  data.add("zharenitsa",record("textures/entity/zharenitsa.png",a));
  var magma=IslandsModel.bundled(IslandsContent.Kind.MAGMA_LORD);a=new JsonArray();for(var action:MagmaCombat.Action.values())for(float t:new float[]{0,action.release,action.duration-1}){magma.pose(IslandsContent.Kind.MAGMA_LORD,action,t,true,true,1,.6F,20,0,0,0,0);a.add(frame(magma.root(),action.name(),t));}for(int state=1;state<=2;state++){magma.pose(IslandsContent.Kind.MAGMA_LORD,MagmaCombat.Action.IDLE,0,false,false,0,0,20,0,0,state==1?6:0,state==2?40:0);a.add(frame(magma.root(),state==1?"hurt":"death",0));}data.add("magma_lord",record("textures/entity/burning_islands/magma_lord.png",a));
  for(String name:List.of("sheptun","oskolnik","gulokryl","ekhovik","griboshmyg","posten")){var mesh=new HallsMesh(name);a=new JsonArray();for(int state=0;state<8;state++){mesh.pose(2,.7F,30,state>=2?16:0,state%4,state==6?6:0,state==7?15:0);a.add(frame(mesh.root,new String[]{"idle","walk","windup","release","mask_rotate","cast","hurt","death"}[state],state));}data.add(name,record("textures/entity/lost_halls/"+name+(name.equals("sheptun")?"_0":"")+".png",a));}
  for(String entity:List.of("sheptun","oskolnik")){
   var loot=JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/slavicmyths/loot_table/entities/"+entity+".json"))).getAsJsonObject();
   for(var pool:loot.getAsJsonArray("pools"))if(pool.getAsJsonObject().has("conditions"))for(var raw:pool.getAsJsonObject().getAsJsonArray("conditions")){
    var condition=raw.getAsJsonObject();if(!condition.has("enchanted_chance"))continue;
    var value=net.minecraft.world.item.enchantment.LevelBasedValue.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,condition.get("enchanted_chance")).getOrThrow();
    float cap=condition.get("unenchanted_chance").getAsFloat()>.1F?.4F:.05F;if(value.calculate(100)>cap+.00001F)throw new AssertionError("Soul chance cap");
   }
  }
  int headChecks=0;
  for(var kind:List.of(org.slavicmyths.trophy.TrophyHeads.Kind.GROVE_GUARDIAN,org.slavicmyths.trophy.TrophyHeads.Kind.ZHARENITSA,org.slavicmyths.trophy.TrophyHeads.Kind.POSTEN)){
   var h=TrophyHeadsClient.create(kind);
   for(int time=0;time<=90;time+=5)for(int degrees=0;degrees<360;degrees+=5){
    h.pose(time);var pose=new PoseStack();pose.translate(.5,0,.5);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(degrees));pose.translate(-.5,0,-.5);var cap=new TrophyHeadsClient.Capture();h.render(pose,cap,0,0);
    for(var v:cap.vertices){if(v[0]<-.001||v[0]>1.001||v[1]<-.001||v[1]>1.001||v[2]<-.001||v[2]>1.001)throw new AssertionError("Head rotated bounds: "+kind+" "+degrees+" "+Arrays.toString(v));headChecks++;}
   }
  }System.out.println("New trophy native rotated vertex checks: "+headChecks);
  String label=args.length==0?"after":args[0];Path dir=Path.of("docs/implementation/p0-bosses-327/native-"+label);Files.createDirectories(dir);Files.writeString(dir.resolve("models.json"),new Gson().toJson(data));
  for(var r:data.entrySet()){var texture=Path.of("src/main/resources/assets/slavicmyths/"+r.getValue().getAsJsonObject().get("texture").getAsString());Files.copy(texture,dir.resolve(r.getKey()+".png"),StandardCopyOption.REPLACE_EXISTING);}
  System.out.println("Native meshes exported: "+data.size()+"; finite vertices/UV PASS; gameplay/ART original match NOT_RUN.");
 }
}
