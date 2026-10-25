package org.slavicmyths.worldgen;
import java.util.*;
import java.util.function.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Command and NPC adapters to the existing construction queue, never a second search. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class OnDemandStructures {
 private record Completion(ServerLevel world,String family,int tier,List<Consumer<ManualStructureJobs.Result>> callbacks){}
 private static final Map<UUID,Completion> PENDING=new LinkedHashMap<>();
 public static int create(CommandSourceStack source,String family,int tier,Consumer<ManualStructureJobs.Result> complete){
  for(var pending:PENDING.values())if(pending.world==source.getLevel()&&pending.family.equals(family)&&pending.tier==tier){pending.callbacks.add(complete);return 1;}
  int accepted=ManualStructureJobs.start(source,family,tier,source.getLevel().getSeed()^BlockPos.containing(source.getPosition()).asLong());
  if(accepted!=0)PENDING.put(ManualStructureJobs.lastJob,new Completion(source.getLevel(),family,tier,new ArrayList<>(List.of(complete))));return accepted;
 }
 public static int locate(CommandSourceStack source,String id){
  String family=id.startsWith("kurgan_")?"kurgan":id.startsWith("bandit_camp_")?"bandit_camp":null;
  if(family==null)return TemplateSiteJobs.create(source,id);
  int tier=id.endsWith("small")?0:id.endsWith("great")||id.endsWith("large")?2:1;
  var origin=BlockPos.containing(source.getPosition());var known=ManualStructureRecords.get(source.getLevel()).entries().stream().filter(e->e.family().equals(family)&&e.tier()==tier&&e.arrival().distSqr(origin)<=1024.*1024).min(java.util.Comparator.comparingDouble(e->e.arrival().distSqr(origin)));
  if(known.isPresent()){source.sendSuccess(()->net.minecraft.network.chat.Component.literal(id+": "+known.get().arrival().toShortString()),false);return 1;}
  if(family.equals("kurgan")){var burial=org.slavicmyths.kurgan.BurialRecords.get(source.getLevel()).instances.values().stream().filter(e->e.plan.tier==tier&&e.origin.distSqr(origin)<=1024.*1024).min(java.util.Comparator.comparingDouble(e->e.origin.distSqr(origin)));if(burial.isPresent()){var e=burial.get();var at=e.origin.offset(0,1,e.plan.radius+2);source.sendSuccess(()->net.minecraft.network.chat.Component.literal(id+": "+at.toShortString()),false);return 1;}}
  return create(source,family,tier,result->{if(result.success())source.sendSuccess(()->net.minecraft.network.chat.Component.literal(id+": "+result.entrance().toShortString()),false);});
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
  for(var entry:List.copyOf(PENDING.entrySet())){if(entry.getValue().world.getServer()!=e.getServer())continue;var result=ManualStructureJobs.result(entry.getKey());if(result!=null){PENDING.remove(entry.getKey());for(var callback:entry.getValue().callbacks)try{callback.accept(result);}catch(RuntimeException failure){com.mojang.logging.LogUtils.getLogger().warn("Structure completion callback",failure);}}}
 }
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){PENDING.entrySet().removeIf(entry->entry.getValue().world.getServer()==e.getServer());}
 private OnDemandStructures(){}
}
