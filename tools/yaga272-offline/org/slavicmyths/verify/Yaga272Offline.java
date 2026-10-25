package org.slavicmyths.verify;

import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.advancements.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import org.slavicmyths.yaga.*;
import org.slavicmyths.tales.*;
import org.slavicmyths.hunt.*;
import org.slavicmyths.progression.BossVictoryCredit;

/** Actual advancement criteria, saved-data transactions and book wire merge; no game instances. */
public final class Yaga272Offline {
 static int checks;
 static void check(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
 static final String[] VICTORIES={"name_your_misfortune","steppe_champion","defeat_nightingale"};
 static Map<String,AdvancementProgress> advancements() throws Exception {
  var result=new HashMap<String,AdvancementProgress>();
  for(var id:VICTORIES){
   var json=JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/slavicmyths/advancement/"+id+".json"))).getAsJsonObject();
   var requirements=new ArrayList<List<String>>();
   if(json.has("requirements"))for(var row:json.getAsJsonArray("requirements")){var names=new ArrayList<String>();for(var name:row.getAsJsonArray())names.add(name.getAsString());requirements.add(names);}
   else for(var name:json.getAsJsonObject("criteria").keySet())requirements.add(List.of(name));
   var progress=new AdvancementProgress();progress.update(new AdvancementRequirements(requirements));result.put(id,progress);
  }
  return result;
 }
 static boolean completed(Map<String,AdvancementProgress> progress,String id){return progress.containsKey(id)&&progress.get(id).isDone();}
 static void win(Map<String,AdvancementProgress> progress,String id){for(String name:progress.get(id).getRemainingCriteria())check(progress.get(id).grantProgress(name),"native grant");}
 static Map<String,AdvancementProgress> reloadAdv(Map<String,AdvancementProgress> old)throws Exception {
  var fresh=advancements();
  for(var id:VICTORIES){
   var encoded=AdvancementProgress.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,old.get(id)).getOrThrow();
   var loaded=AdvancementProgress.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow();
   var rows=new ArrayList<List<String>>();for(var criterion:fresh.get(id).getRemainingCriteria())rows.add(List.of(criterion));
   loaded.update(new AdvancementRequirements(rows));fresh.put(id,loaded);
  }
  return fresh;
 }
 static TaleData reload(TaleData data){return TaleData.load(data.save(new CompoundTag(),null),null);}
 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var catalog=new TaleCatalog();var apply=TaleCatalog.class.getDeclaredMethod("apply",Map.class,net.minecraft.server.packs.resources.ResourceManager.class,net.minecraft.util.profiling.ProfilerFiller.class);apply.setAccessible(true);
  apply.invoke(catalog,Map.of(ResourceLocation.parse(YagaStoryProgress.ENTRY),JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/slavicmyths/tales/yaga_search.json")))),null,null);
  UUID player=UUID.randomUUID();
  for(var id:VICTORIES){
   var adv=advancements();var tales=new TaleData();var yaga=new YagaData();
   check(!YagaStoryProgress.reconcile(tales,yaga,player,GameType.SURVIVAL,key->completed(adv,key),0),"new survival locked");
   win(adv,id);
   check(YagaStoryProgress.reconcile(tales,yaga,player,GameType.SURVIVAL,key->completed(adv,key),1),"each single victory opens OR: "+id);
   check(tales.stage(player,YagaStoryProgress.ENTRY)==1,"real TaleData promoted");
   tales=reload(tales);var savedAdv=reloadAdv(adv);check(YagaStoryProgress.reconcile(tales,yaga,player,GameType.SURVIVAL,key->completed(savedAdv,key),2),"old saved victory restores");
   var noBookProgress=new TaleData();check(YagaStoryProgress.reconcile(noBookProgress,yaga,player,GameType.SURVIVAL,key->completed(savedAdv,key),3),"native saved victory restores absent legacy book stage");
   check(!adv.get(id).grantProgress("event"),"native award idempotent");
  }
  var adv=advancements();var tales=new TaleData();var yaga=new YagaData();
  check(YagaStoryProgress.reconcile(tales,yaga,player,GameType.CREATIVE,key->completed(adv,key),0),"immediate creative");
  check(tales.stage(player,YagaStoryProgress.ENTRY)==1,"creative durable stage");
  check(yaga.players.isEmpty(),"creative gives no trust/rewards");
  for(var p:adv.values())check(!p.hasProgress(),"creative no combat criteria");
  for(int i=0;i<20;i++){tales=reload(tales);check(YagaStoryProgress.reconcile(tales,yaga,player,i%2==0?GameType.SURVIVAL:GameType.CREATIVE,key->completed(adv,key),i),"mode/reload durability");}
  win(adv,"name_your_misfortune");check(YagaStoryProgress.reconcile(tales,yaga,player,GameType.SURVIVAL,key->completed(adv,key),30),"creative history does not block survival victory");
  var cache=new CompoundTag();var stale=new CompoundTag();stale.putInt("Stage",0);var entries=new CompoundTag();entries.put(YagaStoryProgress.ENTRY,stale);cache.put("Entries",entries);
  cache=TaleSnapshotMerge.merge(cache,YagaStoryProgress.delta(tales,player),false);check(cache.getCompound("Entries").getCompound(YagaStoryProgress.ENTRY).getInt("Stage")==1,"real wire merge repairs stale locked entry");
  for(var status:YagaStoryProgress.Status.values()){
   YagaStoryProgress.state(tales,player,status,status==YagaStoryProgress.Status.UNAVAILABLE?"timeout":"");tales=reload(tales);
   cache=TaleSnapshotMerge.merge(cache,YagaStoryProgress.delta(tales,player),false);check(cache.getCompound("YagaSearch").getString("Status").equals(status.name()),"state reaches book: "+status);
   check(cache.getCompound("Entries").getCompound(YagaStoryProgress.ENTRY).getInt("Stage")==1,"state never relocks access");
  }
  check(cache.getCompound("YagaSearch").getString("Reason").equals("timeout"),"failure reason survives reload/wire");
  var relation=yaga.progress(player);relation.active="main:0";int[] rewards={0},ends={0};
  Runnable reward=()->rewards[0]+=YagaServices.MAIN[0].amount;
  for(int i=0;i<8;i++){
   YagaServices.recognizeVictory(yaga,player,key->completed(adv,key),reward,key->{check(key.equals("main:0"),"obsolete quest only");ends[0]++;});
   var saved=yaga.save(new CompoundTag(),null);yaga=new YagaData();yaga.load(saved);
  }
  check(rewards[0]==2&&ends[0]==1,"exactly original two herbs and one quest end");check(yaga.progress(player).stage==1&&yaga.progress(player).active.isEmpty(),"guest stage/obsolete main0 persisted");
  check(YagaServices.MAIN[0].result.equals("svyazka_trav_yagi"),"original reward identity");
  check(!yaga.progress(player).accept("main:0",0)&&yaga.progress(player).accept("main:1",0),"actual stage controls quest admission");
  var legacy=new YagaData();legacy.progress(player).stage=3;legacy.progress(player).intro=2;var fresh=new TaleData();var empty=advancements();
  check(YagaStoryProgress.reconcile(fresh,legacy,player,GameType.SURVIVAL,key->completed(empty,key),0),"legacy relations recover access");check(legacy.progress(player).stage==3,"legacy trust unchanged");
  var stranger=new YagaData();win(empty,"defeat_nightingale");
  check(!YagaServices.recognizeVictory(stranger,player,key->completed(empty,key),()->{throw new AssertionError("unearned reward");},key->{throw new AssertionError("unearned quest end");}),"Nightingale opens access but keeps original trust rule");
  check(stranger.progress(player).stage==0&&stranger.progress(player).accept("main:0",0),"stranger retains implemented initial quest");
  var data=new CompoundTag();BossVictoryCredit.remember(data,player,100);check(player.equals(BossVictoryCredit.recent(data.copy(),100)),"causal owner NBT");check(player.equals(BossVictoryCredit.recent(data,300)),"200tick boundary");check(BossVictoryCredit.recent(data,301)==null&&BossVictoryCredit.recent(data,99)==null,"expired/future owner denied");
  var records=new HuntRecords();var boss=new HuntRecords.Boss("test",BossKind.LIKHO,BlockPos.ZERO,true);boss.target=UUID.randomUUID();boss.active=true;records.bosses.put(boss.key,boss);
  check(!records.finishBoss("test",UUID.randomUUID(),0),"wrong entity cannot finish encounter");check(records.finishBoss("test",boss.target,0),"real encounter commit");check(!records.finishBoss("test",boss.target,1),"no double encounter commit");
  var saved=records.save(new CompoundTag());var loaded=new HuntRecords();loaded.load(saved);check(!loaded.finishBoss("test",boss.target,2),"completed ledger survives reload");
  System.out.println("YAGA272_OFFLINE_PASS checks="+checks+" game_runs=0 (event/mixin/world runtime NOT_RUN)");
 }
}
