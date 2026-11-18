package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import org.slavicmyths.sanctuary.*;
import static org.slavicmyths.verify.Stabilization269.*;

/** Actual naturally generated caches in disposable worlds; server methods, not network MP. */
final class NativeRitual269 {
 static final Path WITNESS=Path.of("native-ritual-witnesses.json");
 static void run(boolean restart)throws Exception{
  current="native_ritual";var a=player("NativeRitual269_A");var b=player("NativeRitual269_B");
  if(restart){
   for(var raw:com.google.gson.JsonParser.parseString(Files.readString(WITNESS)).getAsJsonArray()){
    var row=raw.getAsJsonObject();var at=BlockPos.of(row.get("position").getAsLong());w.getChunkAt(at);
    var cache=(SanctuaryChest)w.getBlockEntity(at);
    check(cache!=null,"native ritual cache persisted "+at);
    check(cache.canOpen(a)&&cache.canOpen(b),"native ritual unlock survives new server process "+at);
    check(cache.getLootTable()==null&&cache.isEmpty(),"native emptied reward persists "+at);
    cache.unpackLootTable(a);cache.unpackLootTable(b);
    check(cache.getLootTable()==null&&cache.isEmpty(),"native reward cannot reroll after real restart "+at);
   }
   return;
  }
  var source=Path.of(System.getProperty("slavicmyths.qa269.ritualSource"));
  var report=com.google.gson.JsonParser.parseString(Files.readString(source)).getAsJsonObject();
  check(report.get("seed").getAsLong()==w.getSeed(),"natural receipt belongs to current world seed");
  var witnesses=new com.google.gson.JsonArray();
  for(var raw:report.getAsJsonArray("natural_samples")){
   var row=raw.getAsJsonObject();if(!row.has("actual_structure"))continue;
   String id=row.get("actual_structure").getAsString();boolean spring=id.endsWith("abode_pure_spring");
   if(!spring&&!id.endsWith("sanctuary_unfading_fire"))continue;
   current=id;var box=row.getAsJsonArray("bounds");var caches=new ArrayList<SanctuaryChest>();var hearts=new ArrayList<SanctuaryHeart>();
   for(int x=box.get(0).getAsInt()>>4;x<=box.get(3).getAsInt()>>4;x++)for(int z=box.get(2).getAsInt()>>4;z<=box.get(5).getAsInt()>>4;z++)
    for(var tile:w.getChunk(x,z).getBlockEntities().values()){
     if(tile instanceof SanctuaryChest cache)caches.add(cache);
     if(tile instanceof SanctuaryHeart heart)hearts.add(heart);
    }
   check(caches.size()==1,"one physical native ritual cache "+id);var cache=caches.getFirst();
   var tag=cache.saveWithFullMetadata(w.registryAccess());var instance=tag.getUUID("SanctuaryInstance");hearts.removeIf(h->!instance.equals(h.instance));
   check(!cache.canOpen(a)&&!cache.canOpen(b),"both server players denied before native ritual");
   var pending=cache.getLootTable();check(pending!=null,"native sealed reward retains pending table");
   cache.unpackLootTable(a);
   check(pending.equals(cache.getLootTable())&&cache.getItem(0).isEmpty()&&cache.removeItem(0,64).isEmpty()&&cache.removeItemNoUpdate(0).isEmpty()&&!cache.canPlaceItem(0,new ItemStack(Items.DIRT)),"native sealed cache denies unpack read extract insert");
   a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIRT));for(var heart:hearts)if(heart.pool>=0||!spring)heart.interact(a,InteractionHand.MAIN_HAND);
   check(!cache.canOpen(a),"wrong item cannot bypass native ritual");
   if(spring){
    var pools=hearts.stream().filter(h->h.pool>=0).sorted(Comparator.comparingInt(h->h.pool)).toList();check(pools.size()==2,"two native spring ritual nodes");
    a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("slavicmyths:cleansing_powder")),2));
    pools.get(0).interact(a,InteractionHand.MAIN_HAND);check(!cache.canOpen(b),"one native pool insufficient");pools.get(1).interact(a,InteractionHand.MAIN_HAND);check(a.getMainHandItem().isEmpty(),"native spring consumes two powders");
   }else{
    var heart=hearts.stream().filter(h->!h.spring).findFirst().orElseThrow();a.moveTo(heart.getBlockPos().getCenter().add(1,0,0));
    var ember=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("slavicmyths:sacred_ember"));a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ember));heart.interact(a,InteractionHand.MAIN_HAND);
    check(!a.getMainHandItem().is(ember)&&SanctuaryRecords.get(w).restored(instance),"native fire consumes ember and restores instance");
   }
   check(cache.canOpen(a)&&cache.canOpen(b),"native ritual opens cache for both server players");cache.unpackLootTable(a);
   check(cache.getLootTable()==null&&!cache.isEmpty(),"native ritual gives real nonempty loot");
   cache.clearContent();cache.setChanged();w.getChunkAt(cache.getBlockPos()).setUnsaved(true);cache.unpackLootTable(b);
   check(cache.getLootTable()==null&&cache.isEmpty(),"second server player cannot regenerate emptied native reward");
   var witness=new com.google.gson.JsonObject();witness.addProperty("position",cache.getBlockPos().asLong());witness.addProperty("structure",id);witness.addProperty("instance",instance.toString());witnesses.add(witness);
  }
  check(!witnesses.isEmpty(),"at least one actual native ritual tested");Files.writeString(WITNESS,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(witnesses));
 }
}
