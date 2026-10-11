package org.slavicmyths.client;
import java.util.*;
import java.lang.reflect.*;
import java.nio.file.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.nochnitsa.NochnitsaRules;
import org.slavicmyths.nochnitsa.NochnitsaRules.*;
import org.slavicmyths.underground.EncounterRules;

/** Tests the live state clock and native model; does not simulate a Minecraft session. */
public final class NochnitsaChecks {
 static int checks;static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static Field cubes,children,polygons; static Object readAlpha(Object m)throws Exception{var f=m.getClass().getDeclaredField("alpha");f.setAccessible(true);return f.get(m);}
 static void export(ModelPart p,PoseStack stack,String name,List<Map<String,Object>> result)throws Exception{
  if(!p.visible)return;stack.pushPose();p.translateAndRotate(stack);
  for(var cube:(List<?>)cubes.get(p)){var faces=new ArrayList<List<float[]>>();for(var face:(Object[])polygons.get(cube))if(face!=null){var f=face.getClass().getDeclaredField("vertices");f.setAccessible(true);var verts=new ArrayList<float[]>();for(var v:(Object[])f.get(face)){var pos=v.getClass().getDeclaredField("pos");pos.setAccessible(true);var u=v.getClass().getDeclaredField("u");u.setAccessible(true);var t=v.getClass().getDeclaredField("v");t.setAccessible(true);var xyz=stack.last().pose().transformPosition(new org.joml.Vector3f((org.joml.Vector3f)pos.get(v)).div(16));float uv=u.getFloat(v),vv=t.getFloat(v);check(uv>=0&&uv<=1&&vv>=0&&vv<=1,"UV inside 128 atlas");verts.add(new float[]{xyz.x*16,xyz.y*16,xyz.z*16,uv,vv});}faces.add(verts);}result.add(Map.of("part",name,"faces",faces));}
  for(var e:((Map<String,ModelPart>)children.get(p)).entrySet())export(e.getValue(),stack,name+"/"+e.getKey(),result);stack.popPose();
 }
 public static void main(String[] args)throws Exception{
  check(!NochnitsaRules.night(12999)&&NochnitsaRules.night(13000)&&NochnitsaRules.night(22999)&&!NochnitsaRules.night(23000)&&NochnitsaRules.night(37000),"night inclusive/exclusive boundaries");
  check(NochnitsaRules.inRadius(31.9*31.9)&&!NochnitsaRules.inRadius(32.1*32.1),"actual spherical range boundary");
  var c=new Clock();c.begin(State.PURSUE);c.claw();check(c.state==State.CLAW_LEFT,"first claw left");for(int t=1;t<=24;t++){Event e=c.tick();check((e==Event.HIT)==(t==10),"single claw contact at tick 10");}c.claw();check(c.state==State.CLAW_RIGHT,"second claw right after 24 ticks");
  for(int hit=1;hit<30;hit++){c=new Clock();c.begin(State.WINDUP);for(int t=1;t<hit;t++)check(c.tick()!=Event.SCREAM,"no premature release");check(c.hurt()&&c.cooldown==60,"damage interrupts windup including tick29");for(int t=0;t<45;t++)check(c.tick()!=Event.SCREAM,"interrupted clock never releases");}
  c=new Clock();c.begin(State.WINDUP);int releases=0,inhales=0;for(int t=1;t<=45;t++){Event e=c.tick();if(e==Event.SCREAM){releases++;check(t==30,"release exactly tick30");}if(e==Event.INHALE)inhales++;}check(releases==1&&inhales==1&&c.cooldown==585,"one release / one inhale / 600 cooldown");
  c=new Clock();c.begin(State.WINDUP);c.abort();check(c.state==State.PURSUE&&c.cooldown==60,"target invalid/rune cancellation quiet retry");c.begin(State.DAWN);for(int t=1;t<=30;t++)check((c.tick()==Event.DAWN_REMOVE)==(t==30),"dawn30");
  for(int runes=0;runes<=4;runes++){check(EncounterRules.protectedByRunes(50,runes,false,false)==(runes>=2),"existing aura50 boundary");check(!EncounterRules.protectedByRunes(51,runes,false,false)&&!EncounterRules.protectedByRunes(50,runes,true,false)&&!EncounterRules.protectedByRunes(50,runes,false,true),"existing exclusions and retaliation preserved");}
  // Config construction validates defaults, including melee30 now inside its permitted range.
  check(org.slavicmyths.earth.EarthConfig.SPEC!=null,"Earth config builds with fast cooldown defaults");earthConfiguration();
  cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");for(var f:List.of(cubes,children,polygons))f.setAccessible(true);
  var m=new NochnitsaClient.Model();check(m.nodes.keySet().stream().filter(n->n.startsWith("hair_")&&!n.endsWith("_tip")&&!n.equals("hair_cap")).count()==22,"22 separate hair strands");for(String s:List.of("l","r"))for(int i=1;i<=4;i++)check(m.nodes.containsKey("claw_"+s+"_"+i),"four fingers each hand");
  m.pose(State.PURSUE,5,0,1,20,0,0);float hip=m.nodes.get("thigh_l").xRot;check(Math.abs(hip)>.4,"visible chase stride");m.pose(State.PURSUE,5,.5F,1,20,0,0);check(hip*m.nodes.get("thigh_l").xRot<0,"opposite gait phase");
  m.pose(State.WINDUP,29,0,0,40,0,0);check(m.nodes.get("jaw").xRot>.45&&Math.abs(m.nodes.get("shoulder_l").zRot)>1,"scream jaw and raised arms");m.pose(State.DAWN,15,0,0,60,0,0);check(Math.abs(((Float)readAlpha(m))-.5)<.001,"dawn fade midpoint");
  var output=new LinkedHashMap<String,List<Map<String,Object>>>();for(var state:List.of(State.WANDER,State.PURSUE,State.CLAW_LEFT,State.WINDUP,State.DEATH)){m.pose(state,state==State.WINDUP?29:state==State.CLAW_LEFT?10:12,0,state==State.PURSUE?1:0,20,0,0);var mesh=new ArrayList<Map<String,Object>>();export(m.root,new PoseStack(),"root",mesh);output.put(state.name(),mesh);check(mesh.size()>=130,"real mesh cubes");}
  m.pose(State.WANDER,0,0,0,0,0,0);int expected=0;for(var p:m.root.getAllParts().toList())expected+=((List<?>)cubes.get(p)).size()*24;int[] count={0},bright={0},current={0};var consumer=(VertexConsumer)Proxy.newProxyInstance(NochnitsaChecks.class.getClassLoader(),new Class[]{VertexConsumer.class},(proxy,method,a)->{if(method.getName().equals("addVertex")){count[0]++;if(a.length==11&&(Integer)a[7]==15728880)bright[0]++;}if(method.getName().equals("setLight")){current[0]=(Integer)a[0];if(current[0]==15728880)bright[0]++;}return method.getReturnType()==VertexConsumer.class?proxy:null;});m.renderToBuffer(new PoseStack(),consumer,123,0,-1);check(count[0]==expected,"no duplicate emissive geometry");check(bright[0]==48,"fullbright only two eye cubes");
  Files.writeString(Path.of("work/nochnitsa/meshes.json"),new com.google.gson.Gson().toJson(output));System.out.println("NOCHNITSA OFFLINE PASS: "+checks+" assertions; no gameplay or server launch.");
 }
 static void earthConfiguration()throws Exception{
  var spec=org.slavicmyths.earth.EarthConfig.SPEC;var constructor=net.neoforged.fml.config.ModConfig.class.getDeclaredConstructors()[0];constructor.setAccessible(true);
  int[] saves={0};var info=(net.neoforged.neoforgespi.language.IModInfo)Proxy.newProxyInstance(NochnitsaChecks.class.getClassLoader(),new Class[]{net.neoforged.neoforgespi.language.IModInfo.class},(p,m,a)->m.getName().equals("getModId")?"slavicmyths":null);var bus=(net.neoforged.bus.api.IEventBus)Proxy.newProxyInstance(NochnitsaChecks.class.getClassLoader(),new Class[]{net.neoforged.bus.api.IEventBus.class},(p,m,a)->{if(m.getName().equals("post")){saves[0]++;return a[a.length-1];}return null;});var container=new net.neoforged.fml.ModContainer(info){public net.neoforged.bus.api.IEventBus getEventBus(){return bus;}};var config=(net.neoforged.fml.config.ModConfig)constructor.newInstance(net.neoforged.fml.config.ModConfig.Type.COMMON,spec,container,"offline-earth.toml",new java.util.concurrent.locks.ReentrantLock());
  var data=com.electronwill.nightconfig.core.CommentedConfig.inMemory();spec.correct(data);data.set("volleyShotTicks",14);data.set("cooldowns.melee",50);data.set("cooldowns.pillar",560);data.set("cooldowns.volley",360);data.set("cooldowns.counter",400);data.set("cooldowns.tremor",440);data.set("cooldowns.spikes",777);
  var loaded=Class.forName("net.neoforged.fml.config.LoadedConfig").getDeclaredConstructors()[0];loaded.setAccessible(true);spec.acceptConfig((net.neoforged.fml.config.IConfigSpec.ILoadedConfig)loaded.newInstance(data,null,config));
  org.slavicmyths.earth.EarthConfig.load(new net.neoforged.fml.event.config.ModConfigEvent.Loading(config));
  check(org.slavicmyths.earth.EarthRules.SHOT_INTERVAL==8&&org.slavicmyths.earth.EarthRules.Action.MELEE.cooldown==30,"actual config load upgrades shipped shot and melee defaults");check(org.slavicmyths.earth.EarthRules.Action.PILLAR.cooldown==280&&org.slavicmyths.earth.EarthRules.Action.VOLLEY.cooldown==120&&org.slavicmyths.earth.EarthRules.Action.COUNTER.cooldown==180&&org.slavicmyths.earth.EarthRules.Action.TREMOR.cooldown==180,"actual config load upgrades heavy cooldowns");check(org.slavicmyths.earth.EarthRules.Action.SPIKES.cooldown==777,"custom cooldown preserved");check(saves[0]==1&&data.getInt("balanceRevision")==1,"migration saved once");
  data.set("volleyShotTicks",14);data.set("cooldowns.melee",50);spec.afterReload();org.slavicmyths.earth.EarthConfig.load(new net.neoforged.fml.event.config.ModConfigEvent.Loading(config));check(org.slavicmyths.earth.EarthRules.SHOT_INTERVAL==14&&org.slavicmyths.earth.EarthRules.Action.MELEE.cooldown==50&&saves[0]==1,"after revision1 explicit retuning never remigrated");
  spec.acceptConfig(null);org.slavicmyths.earth.EarthRules.SHOT_INTERVAL=8;org.slavicmyths.earth.EarthRules.Action.MELEE.cooldown=30;org.slavicmyths.earth.EarthRules.Action.SPIKES.cooldown=500;
 }
}
