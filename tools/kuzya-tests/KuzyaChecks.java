package org.slavicmyths.client;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.kuzya.*;

/** Evaluates production joint curves and native meshes; not an in-game acceptance test. */
public final class KuzyaChecks {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Field cubes,children,polygons;
 static void export(ModelPart p,PoseStack stack,String name,List<Map<String,Object>> result)throws Exception{
  if(!p.visible)return;stack.pushPose();p.translateAndRotate(stack);
  for(var cube:(List<?>)cubes.get(p)){var faces=new ArrayList<List<float[]>>();for(var face:(Object[])polygons.get(cube))if(face!=null){var f=face.getClass().getDeclaredField("vertices");f.setAccessible(true);var verts=new ArrayList<float[]>();for(var v:(Object[])f.get(face)){var pos=v.getClass().getDeclaredField("pos");pos.setAccessible(true);var u=v.getClass().getDeclaredField("u");u.setAccessible(true);var t=v.getClass().getDeclaredField("v");t.setAccessible(true);var xyz=stack.last().pose().transformPosition(new org.joml.Vector3f((org.joml.Vector3f)pos.get(v)).div(16));float uv=u.getFloat(v),vv=t.getFloat(v);check(uv>=0&&uv<=1&&vv>=0&&vv<=1,"UV inside atlas: "+name);verts.add(new float[]{xyz.x*16,xyz.y*16,xyz.z*16,uv,vv});}faces.add(verts);}result.add(Map.of("part",name,"faces",faces));}
  for(var e:((Map<String,ModelPart>)children.get(p)).entrySet())export(e.getValue(),stack,name+"/"+e.getKey(),result);stack.popPose();
 }
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  // Isolated vanilla registry fixture with the six actual hut block implementations.
  var registry=(net.minecraft.core.MappedRegistry<net.minecraft.world.level.block.Block>)net.minecraft.core.registries.BuiltInRegistries.BLOCK;registry.unfreeze();
  registerBlock("yaga_cauldron",new org.slavicmyths.yaga.YagaCauldronBlock());registerBlock("yaga_chicken_leg",new org.slavicmyths.yaga.YagaLegBlock());
  registerBlock("pine_table",new org.slavicmyths.furniture.TableBlock());
  for(String name:List.of("yaga_dried_herbs","yaga_bone_charm","ritual_candle"))registerBlock(name,new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().noCollission().noOcclusion()));
  var plant=Class.forName("org.slavicmyths.registry.ModBlocks$WildPlantBlock").getDeclaredConstructors()[0];plant.setAccessible(true);registerBlock("wormwood",(net.minecraft.world.level.block.Block)plant.newInstance(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().noCollission().noOcclusion()));registry.freeze();
  registry.bindTags(Map.of(net.minecraft.tags.BlockTags.DIRT,List.of(net.minecraft.world.level.block.Blocks.GRASS_BLOCK.builtInRegistryHolder(),net.minecraft.world.level.block.Blocks.DIRT.builtInRegistryHolder(),net.minecraft.world.level.block.Blocks.PODZOL.builtInRegistryHolder(),net.minecraft.world.level.block.Blocks.COARSE_DIRT.builtInRegistryHolder()),net.minecraft.tags.BlockTags.MUSHROOM_GROW_BLOCK,List.of(net.minecraft.world.level.block.Blocks.PODZOL.builtInRegistryHolder())));
  cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");for(var f:List.of(cubes,children,polygons))f.setAccessible(true);
  var m=new KuzyaClient.Model();check(m.nodes.size()>=65,"dedicated native hierarchy");check(m.nodes.keySet().stream().filter(n->n.matches("hair_\\d+")).count()>=28,"28+ volumetric locks");
  check(KuzyaAssets.CLIPS.length==33,"33 frame contracts");var output=new LinkedHashMap<String,List<Map<String,Object>>>();
  for(String clip:KuzyaAssets.CLIPS){int id=KuzyaAssets.index(clip);float[] first=KuzyaClient.Model.sample(clip,0),last=first;boolean changed=false;for(int i=1;i<=50;i++){float seconds=(KuzyaAssets.TICKS[id]/20F)*i/51;float[] frame=KuzyaClient.Model.sample(clip,seconds);check(frame.length==15,"all curve channels");for(int j=0;j<frame.length;j++){check(Float.isFinite(frame[j]),"finite curve "+clip);changed|=Math.abs(frame[j]-last[j])>.0001;}last=frame.clone();m.pose(clip,frame,-1,0,0,0,0,seconds);for(var n:m.nodes.values())check(Float.isFinite(n.xRot)&&Float.isFinite(n.yRot)&&Float.isFinite(n.zRot),"finite transformed joint");}check(changed,"clip has actual movement: "+clip);float time=KuzyaAssets.TICKS[id]/40F;m.pose(clip,KuzyaClient.Model.sample(clip,time),-1,0,0,0,0,time);var mesh=new ArrayList<Map<String,Object>>();export(m.root,new PoseStack(),"root",mesh);output.put(clip,mesh);}
  m.pose("idle_stand",KuzyaClient.Model.sample("idle_stand",0),-1,0,0,0,0,0);var neutral=new ArrayList<Map<String,Object>>();export(m.root,new PoseStack(),"root",neutral);double minY=100,maxY=-100;for(var c:neutral)for(var face:(List<List<float[]>>)c.get("faces"))for(float[] v:face){minY=Math.min(minY,v[1]);maxY=Math.max(maxY,v[1]);}check(Math.abs(maxY-24)<.1,"feet touch model ground");check((24-minY)/16>=.62&&(24-minY)/16<=.70,"canonical small silhouette: "+(24-minY)/16);
  m.pose("sleep_loop",KuzyaClient.Model.sample("sleep_loop",1),-1,0,0,0,0,1);check(m.nodes.get("eyelid_L").yScale>.98,"sleep lids close");check(Math.abs(m.nodes.get("pelvis").zRot)>1.5,"sleep side pose");
  m.pose("eat_bite",KuzyaClient.Model.sample("eat_bite",.45F),-1,0,0,0,0,.45F);check(m.nodes.get("food_prop").visible,"visible held food");check(Math.abs(m.nodes.get("hip_R").xRot)>1,"food scene remains seated");m.pose("domestic_sweep",KuzyaClient.Model.sample("domestic_sweep",1),-1,0,0,0,0,1);check(m.nodes.get("broom_prop").visible,"visible domestic broom");
  double initial=Double.NaN;for(int i=0;i<60;i++){float phase=i/100F;float seconds=phase*.9F;m.pose("walk",KuzyaClient.Model.sample("walk",seconds),-1,0,0,0,0,seconds);var footMesh=new ArrayList<Map<String,Object>>();export(m.root,new PoseStack(),"root",footMesh);double sole=-100,minZ=100,maxZ=-100;for(var cube:footMesh)if(cube.get("part").toString().endsWith("/foot_R"))for(var face:(List<List<float[]>>)cube.get("faces"))for(var v:face){sole=Math.max(sole,v[1]);minZ=Math.min(minZ,v[2]);maxZ=Math.max(maxZ,v[2]);}check(Math.abs(sole-24)<.015,"stance sole pinned to ground");double worldZ=(minZ+maxZ)/2-phase*3.52;if(Double.isNaN(initial))initial=worldZ;check(Math.abs(worldZ-initial)<.015,"distance-driven stance drift below 5%");}
  for(int i=0;i<KuzyaAssets.VOICES.length;i++){var env=KuzyaAssets.ENVELOPES[i];check(env.length>25&&Arrays.toString(env).contains("1.0"),"decoded spoken envelope "+i);check(KuzyaAssets.VOICE_TICKS[i]>20,"measured voice duration");}
  Files.createDirectories(Path.of("work/kuzya"));Files.writeString(Path.of("work/kuzya/meshes.json"),new com.google.gson.Gson().toJson(output));
  org.slavicmyths.verify.YagaHeadless.main(new String[]{"work/kuzya/hut.json"});
  org.slavicmyths.verify.YagaPlacementHeadless.main(new String[0]);
  var d=new org.slavicmyths.yaga.YagaData();d.anchor=new net.minecraft.core.BlockPos(30,50,70);d.npc=UUID.randomUUID();d.kuzya=UUID.randomUUID();d.hutId=UUID.randomUUID();d.interiorRevision=1;d.placed=true;var saved=d.save(new net.minecraft.nbt.CompoundTag(),net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY));var copy=new org.slavicmyths.yaga.YagaData();copy.load(saved);check(copy.kuzya.equals(d.kuzya)&&copy.hutId.equals(d.hutId)&&copy.interiorRevision==1,"resident SavedData roundtrip");saved.remove("Kuzya");saved.remove("HutId");saved.remove("InteriorRevision");copy.load(saved);check(copy.placed&&copy.kuzya==null&&copy.interiorRevision==0,"legacy Yaga data preserved");
  System.out.println("KUZYA OFFLINE PASS: "+checks+" checks; native pose/UV + original placement fixtures; no Minecraft launch.");
 }
 static void registerBlock(String name,net.minecraft.world.level.block.Block block){net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",name),block);}
}
