package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.util.RandomSource;
/** Native vanilla ore_drops algorithm; no fake in-world loot tests. */
public final class Nether324Offline {
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var type=Class.forName("net.minecraft.world.level.storage.loot.functions.ApplyBonusCount$OreDrops");var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);var formula=ctor.newInstance();var method=type.getDeclaredMethod("calculateNewCount",RandomSource.class,int.class,int.class);method.setAccessible(true);
  JsonObject report=new JsonObject();
  for(String ore:List.of("livingstone_ore","alatyrite_ore","runnik_ore","infernal_ore","sulfur_deposit")){
   var table=JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/slavicmyths/loot_table/blocks/"+ore+".json"))).getAsJsonObject();var children=table.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().getAsJsonArray("children");
   if(children.size()!=2||!children.get(0).getAsJsonObject().get("name").getAsString().equals("slavicmyths:"+ore))throw new AssertionError("Silk must precede raw in one alternatives path");
   JsonArray means=new JsonArray();double previous=0;
   for(int fortune=0;fortune<=3;fortune++){var random=RandomSource.create(324L+fortune);long sum=0;for(int i=0;i<10000;i++){int base=ore.equals("livingstone_ore")?1+random.nextInt(2):ore.equals("sulfur_deposit")?1+random.nextInt(3):1;sum+=(int)method.invoke(formula,random,base,fortune);}double mean=sum/10000.;if(fortune>0&&mean<=previous)throw new AssertionError("Fortune mean regression "+ore);previous=mean;means.add(mean);}report.add(ore,means);
  }
  var limits=new JsonArray();for(int min:new int[]{-64,0,16})for(int max:new int[]{128,224,256}){int low=org.slavicmyths.netherores.NetherOreFeature.low(min,24),high=org.slavicmyths.netherores.NetherOreFeature.high(max,176);if(low<min+5||high>max-6)throw new AssertionError("unsafe height");limits.add(low+":"+high);}report.add("clampedHeightRanges",limits);
  report.addProperty("scope","PASS native vanilla ore_drops simulation 10000/material/Fortune; structural alternatives/height checks. Actual LootContext, mining tool, worldgen, furnace, relog and MP NOT_RUN.");
  Files.createDirectories(Path.of("docs/implementation/nether-ores-2.0.x"));Files.writeString(Path.of("docs/implementation/nether-ores-2.0.x/native-loot.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));System.out.println(report);
 }
}
