package org.slavicmyths.client;

import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.net.URLClassLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.mor.*;

/** Executable production policy/schema/native bake checks. Does not construct a world or launch a game. */
public final class MorChecks {
 static int checks;static Field cubes,children,polygons;
 static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 @SuppressWarnings("unchecked") static void export(ModelPart part,PoseStack stack,String path,List<Map<String,Object>> out)throws Exception{
  stack.pushPose();part.translateAndRotate(stack);
  for(var cube:(List<?>)cubes.get(part)){var faces=new ArrayList<List<float[]>>();for(var face:(Object[])polygons.get(cube)){var vf=face.getClass().getDeclaredField("vertices");vf.setAccessible(true);var vertices=new ArrayList<float[]>();for(var vertex:(Object[])vf.get(face)){var pos=vertex.getClass().getDeclaredField("pos");var u=vertex.getClass().getDeclaredField("u");var v=vertex.getClass().getDeclaredField("v");pos.setAccessible(true);u.setAccessible(true);v.setAccessible(true);var xyz=stack.last().pose().transformPosition(new org.joml.Vector3f((org.joml.Vector3f)pos.get(vertex)).div(16));float uv=u.getFloat(vertex),vv=v.getFloat(vertex);check(uv>=0&&uv<=1&&vv>=0&&vv<=1,"UV outside atlas "+path+" "+uv+","+vv);check(Float.isFinite(xyz.x)&&Float.isFinite(xyz.y)&&Float.isFinite(xyz.z),"Nonfinite vertex");vertices.add(new float[]{xyz.x*16,xyz.y*16,xyz.z*16,uv,vv});}faces.add(vertices);}out.add(Map.of("part",path,"faces",faces));}
  for(var entry:((Map<String,ModelPart>)children.get(part)).entrySet())export(entry.getValue(),stack,path+"/"+entry.getKey(),out);stack.popPose();
 }
 static List<ModelPart> roots(Object model,String... fields)throws Exception{List<ModelPart> parts=new ArrayList<>();for(String name:fields){var f=model.getClass().getDeclaredField(name);f.setAccessible(true);parts.add((ModelPart)f.get(model));}return parts;}
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  for(long day:new long[]{0,6*24000,7*24000-1,7*24000,9*24000})for(boolean dimension:new boolean[]{true,false})for(boolean reason:new boolean[]{true,false})for(double distance:new double[]{0,160*160,160*160+.1})for(int roll=0;roll<100;roll++)check(MorRules.natural(dimension,day,distance,reason,roll)==(day>=7*24000&&dimension&&reason&&distance>25600&&roll<4),"Natural boundary");
  int r=50;for(int t=1;t<=50;t++){r=MorRules.transformTick(r);check((r==0)==(t==50),"Transform 50 ticks");}check(MorRules.transformTick(0)==0,"Clamp terminal tick");
  for(float max:new float[]{36,60,100,155})for(float fraction:new float[]{0,.1F,.5F,1})for(float factor:new float[]{2,2.5F})check(Math.abs(MorRules.health(max*fraction,max,max*factor)-max*factor*fraction)<.001,"Fraction maintained");check(MorRules.health(10,0,100)==0,"Invalid old max safe");
  for(long time:new long[]{5799,5800,6600,6601,29800})check(MorRules.noon(true,time)==(Math.floorMod(time,24000)>=5800&&Math.floorMod(time,24000)<=6600),"Noon inclusive");check(!MorRules.noon(false,6000),"Noon dimension");
  for(int kind=-1;kind<=5;kind++)for(int state=0;state<=2;state++){check(MorRules.canApply(kind,state,true,false)==(kind>=1&&kind<=4&&state==0),"Whitelist/idempotence");check(!MorRules.canApply(kind,state,true,true)&&!MorRules.canApply(kind,state,false,false),"Protected/dead targets");}
  check(MorRules.flaskRange(9)&&!MorRules.flaskRange(9.00001),"Flask Euclidean radius includes 3, excludes beyond");
  check(MorRules.clonesNeeded(false,false,50,100)&&!MorRules.clonesNeeded(false,false,50.01F,100),"Clone half HP boundary");check(!MorRules.clonesNeeded(true,false,1,100)&&!MorRules.clonesNeeded(false,true,1,100),"No recursion or resummoning after death");
  int[][] pulses={{},{24},{20},{20},{19,25},{32},{16},{22,27,32},{18,19,20,21,22,23,24,25,26,27,28},{30}};
  for(int skill=1;skill<=9;skill++)for(int tick=0;tick<=50;tick++){int at=tick;check(MorRules.release(skill,tick)==Arrays.stream(pulses[skill]).anyMatch(x->x==at),"Installed skill release schedule "+skill+":"+tick);}
  var hp=new AttributeInstance(Attributes.MAX_HEALTH,h->{});hp.setBaseValue(100);var id=ResourceLocation.fromNamespaceAndPath("slavicmyths","mor_health");hp.addPermanentModifier(new AttributeModifier(id,1.5,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));check(hp.getValue()==250&&hp.getBaseValue()==100,"Real installed modifier API preserves base");CompoundTag attr=hp.save();var reloaded=new AttributeInstance(Attributes.MAX_HEALTH,h->{});reloaded.load(attr);check(reloaded.getValue()==250&&reloaded.hasModifier(id),"Real attribute persistence without accumulation");
  UUID parent=UUID.randomUUID(),a=UUID.randomUUID(),b=UUID.randomUUID();int[] cooldown=new int[]{0,200,360,0,210,260,320,180,240,0};
  for(int state=0;state<3;state++)for(int remaining:new int[]{1,25,50})for(boolean clone:new boolean[]{true,false}){var snapshot=new MorSnapshot(state,remaining,clone,true,1790,parent,cooldown,List.of(a,b),Map.of(a,new net.minecraft.core.BlockPos(20,70,20)),9,21,parent,new Vec3(1,0,0),new Vec3(3,4,5),new Vec3(8,9,10),20,true,2,true,true);var tag=new CompoundTag();tag.putString("UserData","retained");snapshot.write(tag);var back=MorSnapshot.read(tag);check(back.state()==state&&back.remaining()==(state==1?remaining:0),"State roundtrip");check(back.summoned()&&back.isClone()==clone&&back.parent().equals(parent)&&back.age()==1790,"One-shot/clone identity persisted");check(back.children().equals(List.of(a,b))&&back.positions().get(a).getY()==70,"Mixed parent child snapshot");check(back.skill()==9&&back.castTick()==21&&back.focus().equals(parent)&&back.mark().equals(new Vec3(3,4,5)),"Interrupted cast retained");check(Arrays.equals(back.cooldowns(),cooldown)&&back.retry()==20&&back.retried()&&back.createdCount()==2&&back.diveHit()&&back.hitSoundPlayed(),"Cooldown/retry roundtrip");check(tag.getString("UserData").equals("retained"),"Other NBT retained");}
  check(MorSnapshot.read(new CompoundTag()).state()==0,"Legacy absence NORMAL");
  cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");for(var f:List.of(cubes,children,polygons))f.setAccessible(true);
  var output=new LinkedHashMap<String,List<Map<String,Object>>>();
  var prior=new URLClassLoader(new java.net.URL[]{Path.of("release/slavicmyths-1.7.9.jar").toUri().toURL()},MorChecks.class.getClassLoader()){
   @Override protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{if(Set.of("org.slavicmyths.client.LeshyModel","org.slavicmyths.client.PoludnitsaModel","org.slavicmyths.client.KikimoraModel","org.slavicmyths.client.ElementModel").contains(name)){var c=findLoadedClass(name);if(c==null)c=findClass(name);if(resolve)resolveClass(c);return c;}return super.loadClass(name,resolve);}
  };
  for(String name:List.of("leshy","poludnitsa","kikimora","fire_serpent")){int normalCount=0;for(boolean mor:new boolean[]{false,true}){
   Object model=switch(name){case "leshy"->new LeshyModel(mor);case "poludnitsa"->new PoludnitsaModel(mor);case "kikimora"->new KikimoraModel(mor);default->new ElementModel<>(true,mor);};
   if(mor)model.getClass().getMethod("poseMor",float.class,int.class,float.class,float.class).invoke(model,10F,0,0F,0F);
   String[] fields=name.equals("leshy")?new String[]{"head","body","leftArm","rightArm","leftLeg","rightLeg"}:name.equals("poludnitsa")?new String[]{"rRoot"}:new String[]{"root"};
   var mesh=new ArrayList<Map<String,Object>>();for(var root:roots(model,fields))export(root,new PoseStack(),"root",mesh);if(!mor){normalCount=mesh.size();var legacy=prior.loadClass(model.getClass().getName());Object old=name.equals("fire_serpent")?legacy.getConstructor(boolean.class).newInstance(true):legacy.getConstructor().newInstance();var before=new ArrayList<Map<String,Object>>();for(var root:roots(old,fields))export(root,new PoseStack(),"root",before);check(new com.google.gson.Gson().toJson(before).equals(new com.google.gson.Gson().toJson(mesh)),"NORMAL geometry/UV unchanged from production 1.7.9: "+name);}else check(mesh.size()>normalCount+10,"Individual additional silhouette "+name);output.put(name+(mor?"_MOR":"_NORMAL"),mesh);
   if(mor){int[] skills=switch(name){case "leshy"->new int[]{1,2};case "poludnitsa"->new int[]{3,4};case "kikimora"->new int[]{5,6};default->new int[]{7,8,9};};for(int skill:skills)for(int tick:new int[]{0,MorRules.windup(skill)-1,MorRules.windup(skill),MorRules.duration(skill)}){model.getClass().getMethod("poseMor",float.class,int.class,float.class,float.class).invoke(model,(float)tick,skill,(float)tick,0F);var posed=new ArrayList<Map<String,Object>>();for(var root:roots(model,fields))export(root,new PoseStack(),"root",posed);output.put(name+"_MOR_SKILL_"+skill+"_"+tick,posed);}for(int tick:new int[]{0,15,16,34,35,49}){model.getClass().getMethod("poseMor",float.class,int.class,float.class,float.class).invoke(model,(float)tick,0,0F,tick/50F);var posed=new ArrayList<Map<String,Object>>();for(var root:roots(model,fields))export(root,new PoseStack(),"root",posed);output.put(name+"_MOR_TRANSFORM_"+tick,posed);}}
  }}
  var rootMesh=new ArrayList<Map<String,Object>>();export(MorBoltRenderer.createRoot(),new PoseStack(),"root",rootMesh);check(rootMesh.size()==3,"Transient root native geometry");output.put("effect_ROOT",rootMesh);
  Files.writeString(Path.of("work/mor-1.8.1/meshes.json"),new com.google.gson.Gson().toJson(output));System.out.println("MOR OFFLINE PASS checks="+checks+" native_meshes="+output.size()+" NOT_GAME_TEST");
 }
}
