package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.blackcinder.*;
import org.slavicmyths.blackcinder.client.*;
/** Native model/UV/pose data only. No Minecraft client, server, world or GameTest. */
public final class CinderOffline {
 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var result=new JsonObject();int frames=0;
  for(var kind:CinderContent.Kind.values()){
   String name=kind.name().toLowerCase(Locale.ROOT);var source=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/slavicmyths/cinder_rigs/"+name+".json"))).getAsJsonObject();var model=new CinderModel(source);var all=new JsonArray();
   for(var action:CinderMob.Action.values())for(float time:new float[]{0,CinderMob.windup(action)*.5F,CinderMob.windup(action),CinderMob.windup(action)+5}){
    model.pose(kind,action,time,true,kind==CinderContent.Kind.ASH_SPIRIT||kind==CinderContent.Kind.SPARKLING||kind==CinderContent.Kind.ZHARENITSA,2,.6F,20,15,5,0,0);all.add(frame(model,action.name(),time));frames++;
   }
   for(String state:List.of("hurt","death")){model.pose(kind,CinderMob.Action.IDLE,0,false,true,0,0,20,0,0,state.equals("hurt")?6:0,state.equals("death")?15:0);all.add(frame(model,state,0));frames++;}
   var data=new JsonObject();data.add("source",source);data.add("frames",all);result.add(name,data);
  }
  for(int count=-1;count<=5;count++){int want=count<=0?0:count>=4?5:count;if(ZharenitsaSeals.bonusForPieces(count)!=want)throw new AssertionError("seal bonus");}
  var lookup=net.minecraft.data.registries.VanillaRegistries.createLookup();
  for(var facing:net.minecraft.core.Direction.Plane.HORIZONTAL){var shape=CinderBlocks.Head.shape(facing);if(shape.bounds().minX<0||shape.bounds().maxX>1||shape.bounds().minZ<0||shape.bounds().maxZ>1||shape.toAabbs().isEmpty())throw new AssertionError("head collision");}
  for(var item:List.of(net.minecraft.world.item.Items.IRON_HELMET,net.minecraft.world.item.Items.IRON_CHESTPLATE,net.minecraft.world.item.Items.IRON_LEGGINGS,net.minecraft.world.item.Items.IRON_BOOTS,net.minecraft.world.item.Items.DIAMOND_HELMET,net.minecraft.world.item.Items.DIAMOND_CHESTPLATE,net.minecraft.world.item.Items.DIAMOND_LEGGINGS,net.minecraft.world.item.Items.DIAMOND_BOOTS)){
   var source=new net.minecraft.world.item.ItemStack(item);source.setDamageValue(37);var tag=new net.minecraft.nbt.CompoundTag();tag.putString("ExistingMetadata","preserved");source.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));
   source.enchant(lookup.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION),3);
   source.set(net.minecraft.core.component.DataComponents.TRIM,new net.minecraft.world.item.armortrim.ArmorTrim(lookup.lookupOrThrow(net.minecraft.core.registries.Registries.TRIM_MATERIAL).getOrThrow(net.minecraft.world.item.armortrim.TrimMaterials.REDSTONE),lookup.lookupOrThrow(net.minecraft.core.registries.Registries.TRIM_PATTERN).getOrThrow(net.minecraft.world.item.armortrim.TrimPatterns.SENTRY)));
   var sealed=ZharenitsaSeals.seal(source);if(sealed.isEmpty()||sealed.getItem()!=source.getItem()||sealed.getDamageValue()!=37||ZharenitsaSeals.upgraded(source)||!ZharenitsaSeals.upgraded(sealed)||!sealed.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getString("ExistingMetadata").equals("preserved")||!ZharenitsaSeals.seal(sealed).isEmpty()||!sealed.getEnchantments().equals(source.getEnchantments())||!sealed.get(net.minecraft.core.component.DataComponents.TRIM).equals(source.get(net.minecraft.core.component.DataComponents.TRIM)))throw new AssertionError("seal preserves stack/enchants/trim and rejects duplicates");
  }
  for(var item:List.of(net.minecraft.world.item.Items.GOLDEN_HELMET,net.minecraft.world.item.Items.NETHERITE_HELMET,net.minecraft.world.item.Items.LEATHER_BOOTS))if(!ZharenitsaSeals.seal(new net.minecraft.world.item.ItemStack(item)).isEmpty())throw new AssertionError("unsupported armor");
  var ids=Set.of("ash_mantle");if(!Arrays.asList(org.slavicmyths.rpg.Runes.IDS).containsAll(ids))throw new AssertionError("rune integration");
  var journal=new CinderEncounters();var owner=UUID.randomUUID();
  if(journal.claim(999999,owner) || journal.claim(999999,null))throw new AssertionError("missing arena can never claim reward");
  if(!journal.register(203,owner)||journal.register(203,UUID.randomUUID())||journal.claim(203,UUID.randomUUID())
      ||journal.claim(203,null)||!journal.claim(203,owner)||journal.claim(203,owner))
   throw new AssertionError("encounter ownership/duplicate claim");
  var restored=CinderEncounters.load(journal.save(new net.minecraft.nbt.CompoundTag(),lookup),lookup);if(restored.register(203,owner)||restored.claim(203,owner))throw new AssertionError("defeated journal NBT");
  // Lattice coverage is arithmetic, not natural worldgen evidence.
  for(int start=-2048;start<2048;start++){
   int count=0;for(int x=Math.floorDiv(start,256)*256+8;x<=start+512;x+=256)if(x>=start+16&&x<=start+496)count++;
   if(count<1)throw new AssertionError("arena core coverage");
  }
  int minHeight=999,maxHeight=-999,maxSlope=0;
  for(long seed:new long[]{0,1,-8675309,20261024,987654321})for(int region=0;region<4;region++)for(int x=0;x<128;x++)for(int z=0;z<128;z++){
   int px=x+region*8192-16384,pz=z-region*4096;int h=CinderTerrain.height(seed,px,pz);minHeight=Math.min(minHeight,h);maxHeight=Math.max(maxHeight,h);maxSlope=Math.max(maxSlope,Math.abs(h-CinderTerrain.height(seed,px+1,pz)));
   if(h<33||h>100)throw new AssertionError("terrain range");
  }
  for(int i=0;i<1500;i++){var plan=new CinderTreePlan(i);var seen=new HashSet<CinderTreePlan.Cell>();var queue=new ArrayDeque<CinderTreePlan.Cell>();queue.add(new CinderTreePlan.Cell(0,0,0));while(!queue.isEmpty()){var at=queue.remove();if(!seen.add(at))continue;for(var d:new int[][]{{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}}){var n=new CinderTreePlan.Cell(at.x()+d[0],at.y()+d[1],at.z()+d[2]);if(plan.logs.containsKey(n)&&!seen.contains(n))queue.add(n);}}if(seen.size()!=plan.logs.size())throw new AssertionError("disconnected tree "+i);for(var cell:plan.logs.keySet())if(Math.abs(cell.x())>5||Math.abs(cell.z())>5)throw new AssertionError("tree chunk envelope");}
  var terrain=new JsonObject();terrain.addProperty("scope","Pure production height/profile and tree plans only; no generated worlds");terrain.addProperty("seeds",5);terrain.addProperty("windowsPerSeed",4);terrain.addProperty("sampleColumns",5*4*128*128);terrain.addProperty("minHeight",minHeight);terrain.addProperty("maxHeight",maxHeight);terrain.addProperty("maxAdjacentSlope",maxSlope);terrain.addProperty("connectedTreePlans",1500);Files.writeString(Path.of("docs/implementation/black-cinder-production/offline-terrain.json"),new GsonBuilder().setPrettyPrinting().create().toJson(terrain));
  var output=Path.of("docs/implementation/black-cinder-production/native-rigs.json");Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(result));
  System.out.println("PASS native baked ModelPart/UV finite checks: "+frames+" frames; seal count0/1/2/3/4; arena lattice arithmetic. Runtime worlds=0.");
 }
 private static JsonObject frame(CinderModel model,String action,float time){var result=new JsonObject();result.addProperty("action",action);result.addProperty("time",time);var faces=new JsonArray();
  model.root().visit(new PoseStack(),(pose,path,index,cube)->{try{var field=ModelPart.Cube.class.getDeclaredField("polygons");field.setAccessible(true);for(Object polygon:(Object[])field.get(cube)){var vertices=polygon.getClass().getDeclaredField("vertices");vertices.setAccessible(true);var face=new JsonArray();for(Object vertex:(Object[])vertices.get(polygon)){var pf=vertex.getClass().getDeclaredField("pos");var uf=vertex.getClass().getDeclaredField("u");var vf=vertex.getClass().getDeclaredField("v");pf.setAccessible(true);uf.setAccessible(true);vf.setAccessible(true);var p=(Vector3f)pf.get(vertex);var v=pose.pose().transformPosition(p.x/16,p.y/16,p.z/16,new Vector3f()).mul(16);var point=new JsonArray();for(float value:new float[]{v.x,v.y,v.z,uf.getFloat(vertex),vf.getFloat(vertex)}){if(!Float.isFinite(value))throw new AssertionError("Nonfinite "+path);point.add(value);}if(uf.getFloat(vertex)<0||uf.getFloat(vertex)>1||vf.getFloat(vertex)<0||vf.getFloat(vertex)>1)throw new AssertionError("UV "+path);face.add(point);}faces.add(face);}}catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}});result.add("faces",faces);return result;}
}
