package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Last observed real bosses. Explicit lookup reads SavedData/loaded entities, never generates chunks. */
@EventBusSubscriber(modid="slavicmyths")
public final class PosadBossSearch extends SavedData {
 public record Trace(UUID boss,PosadTestProofs.Kind kind,BlockPos position){}
 private final LinkedHashMap<UUID,Trace> traces=new LinkedHashMap<>();
 public static PosadBossSearch get(ServerLevel world){return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PosadBossSearch::new,(tag,registries)->load(tag)),"slavicmyths_posad_boss_traces");}
 private static PosadBossSearch load(CompoundTag tag){
  var data=new PosadBossSearch();for(var raw:tag.getList("Traces",Tag.TAG_COMPOUND)){
   if(data.traces.size()>=512)break;var row=(CompoundTag)raw;if(!row.hasUUID("Boss"))continue;
   try{var kind=PosadTestProofs.Kind.valueOf(row.getString("Kind"));var pos=BlockPos.of(row.getLong("Pos"));data.traces.put(row.getUUID("Boss"),new Trace(row.getUUID("Boss"),kind,pos));}catch(IllegalArgumentException invalid){/* Fail closed for unknown future boss kinds. */}
  }return data;
 }
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registry){
  var rows=new ListTag();for(var trace:traces.values()){var row=new CompoundTag();row.putUUID("Boss",trace.boss());row.putString("Kind",trace.kind().name());row.putLong("Pos",trace.position().asLong());rows.add(row);}tag.put("Traces",rows);return tag;
 }
 private void remember(LivingEntity entity){
  var kind=PosadTestProofs.kind(entity);if(kind==null)return;
  traces.remove(entity.getUUID());traces.put(entity.getUUID(),new Trace(entity.getUUID(),kind,entity.blockPosition()));
  while(traces.size()>512)traces.remove(traces.keySet().iterator().next());setDirty();
 }
 private void forget(UUID id){if(traces.remove(id)!=null)setDirty();}
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
 public static void joined(EntityJoinLevelEvent event){
  if(!event.isCanceled()&&event.getLevel() instanceof ServerLevel world&&world.dimension()==Level.NETHER&&event.getEntity() instanceof LivingEntity entity&&PosadTestProofs.kind(entity)!=null)get(world).remember(entity);
 }
 @SubscribeEvent public static void left(EntityLeaveLevelEvent event){
  if(!(event.getLevel() instanceof ServerLevel world)||world.dimension()!=Level.NETHER||!(event.getEntity() instanceof LivingEntity entity)||PosadTestProofs.kind(entity)==null)return;
  var reason=entity.getRemovalReason();if(reason!=null&&reason.shouldDestroy())get(world).forget(entity.getUUID());else get(world).remember(entity);
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST,receiveCanceled=true)
 public static void died(LivingDropsEvent event){if(event.getEntity().level() instanceof ServerLevel world&&world.dimension()==Level.NETHER&&PosadTestProofs.kind(event.getEntity())!=null)get(world).forget(event.getEntity().getUUID());}
 public Optional<Trace> nearest(ServerLevel world,BlockPos origin,PosadTestProofs.Kind kind){
  // Refresh only the bounded index; a missing unloaded UUID is not a proven death.
  for(var trace:List.copyOf(traces.values()))if(world.getEntity(trace.boss()) instanceof LivingEntity entity){if(entity.isAlive())remember(entity);else forget(trace.boss());}
  return traces.values().stream().filter(trace->trace.kind()==kind).min(Comparator.comparingDouble(trace->trace.position().distSqr(origin)));
 }
 public static PosadResidentEntity leader(ServerPlayer player){
  return player.serverLevel().getEntitiesOfClass(PosadResidentEntity.class,player.getBoundingBox().inflate(8),npc->npc.isAlive()&&npc.city()!=null&&npc.role()==PosadPopulationPlan.Role.POSADNIK&&npc.distanceToSqr(player)<=64&&!PosadGrievances.get(player.serverLevel()).hostile(npc.city(),player.getUUID()))
   .stream().min(Comparator.comparingDouble(npc->npc.distanceToSqr(player))).orElse(null);
 }
 public static int find(CommandSourceStack source,PosadTestProofs.Kind kind)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
  var player=source.getPlayerOrException();if(player.isSpectator()||leader(player)==null){source.sendFailure(Component.translatable("posad.test.near_leader"));return 0;}
  if(player.level().dimension()!=Level.NETHER){source.sendFailure(Component.translatable("posad.test.nether"));return 0;}
  var trace=get(player.serverLevel()).nearest(player.serverLevel(),player.blockPosition(),kind);
  if(trace.isEmpty()){source.sendFailure(Component.translatable("posad.test.no_trace",kind.title()));return 0;}
  var found=trace.get();boolean loaded=player.serverLevel().getEntity(found.boss())!=null;
  source.sendSuccess(()->Component.translatable("posad.test.trace",kind.title(),found.position().getX(),found.position().getZ(),48,Component.translatable(loaded?"posad.test.trace.loaded":"posad.test.trace.unloaded")),false);return 1;
 }
 public static void dialogue(ServerPlayer player){
  player.sendSystemMessage(Component.translatable("posad.test.dialogue"));
  for(var kind:PosadTestProofs.Kind.values())player.sendSystemMessage(Component.literal("• ").append(kind.title()).withStyle(style->style.withColor(net.minecraft.ChatFormatting.GOLD).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/slavicmyths hero posad find "+kind.name().toLowerCase(Locale.ROOT)))));
  player.sendSystemMessage(Component.translatable("posad.test.exchange_hint"));
 }
 public static void register(org.slavicmyths.commands.CommandTree tree){
  var find=Commands.literal("find");for(var kind:PosadTestProofs.Kind.values())find.then(Commands.literal(kind.name().toLowerCase(Locale.ROOT)).executes(c->find(c.getSource(),kind)));
  tree.add(Commands.literal("slavicmyths").then(Commands.literal("hero").then(Commands.literal("posad").then(find))));
 }
}
