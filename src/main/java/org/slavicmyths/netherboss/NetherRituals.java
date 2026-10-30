package org.slavicmyths.netherboss;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.registry.ModEntities;

@EventBusSubscriber(modid="slavicmyths")
public final class NetherRituals {
 private record Pending(ServerPlayer player,ServerLevel world,BlockPos altar,InteractionHand hand,boolean morova,long ends,String id){}
 private static final Map<UUID,Pending> ACTIVE=new HashMap<>();
 private static boolean fail(ServerPlayer p,String key){p.sendSystemMessage(Component.translatable("netherboss.slavicmyths."+key));return false;}
 public static boolean validAltar(ServerLevel w,BlockPos pos,boolean morova){
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(!w.hasChunkAt(pos.offset(x,0,z)))return false;
  if(morova){
   if(!w.getBlockState(pos).is(Blocks.SOUL_SAND))return false;
   for(var delta:List.of(new BlockPos(2,0,0),new BlockPos(-2,0,0),new BlockPos(0,0,2),new BlockPos(0,0,-2)))
    if(!(w.getBlockState(pos.offset(delta).above()).getBlock() instanceof CandleBlock))return false;
  }else{
   if(!(w.getBlockState(pos).getBlock() instanceof CampfireBlock))return false;
   for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0){
    var s=w.getBlockState(pos.offset(x,0,z));if(!(s.is(Blocks.BLACKSTONE)||s.is(Blocks.POLISHED_BLACKSTONE)||s.is(Blocks.BASALT)||s.is(Blocks.NETHERRACK)))return false;
   }
  }return true;
 }
 public static boolean begin(ServerPlayer p,BlockPos pos,InteractionHand hand,boolean morova){
  ServerLevel w=p.serverLevel();if(w.dimension()!=Level.NETHER)return fail(p,"nether_only");
  if(!p.isAlive()||p.isSpectator()||ACTIVE.containsKey(p.getUUID()))return fail(p,"busy");
  // All inspected altar/chunk cells are already loaded; never request a remote chunk.
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(!w.hasChunkAt(pos.offset(x,0,z)))return fail(p,"unloaded");
  if(!validAltar(w,pos,morova))return fail(p,"altar");
  if(!w.getEntitiesOfClass(NetherSummonBoss.class,new AABB(pos).inflate(96),e->e.isAlive()).isEmpty())return fail(p,"busy");
  var ledger=NetherBossLedger.get(w);
  for(String key:ledger.records.getAllKeys()){
   var row=ledger.records.getCompound(key);if(!row.getString("State").equals("ENDED")&&BlockPos.of(row.getLong("Anchor")).distSqr(pos)<96*96)return fail(p,"busy");
  }
  String id=UUID.randomUUID().toString();var row=new CompoundTag();row.putString("State","RESERVED");row.putLong("Anchor",pos.asLong());row.putUUID("Owner",p.getUUID());ledger.records.put(id,row);ledger.flush(w);
  ACTIVE.put(p.getUUID(),new Pending(p,w,pos.immutable(),hand,morova,w.getGameTime()+200,id));return true;
 }
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){
  for(var ritual:List.copyOf(ACTIVE.values())){
   var p=ritual.player;var w=ritual.world;if(w.getServer()!=e.getServer())continue;
   if(p.serverLevel()!=w||!p.isAlive()||p.hasDisconnected()||w.dimension()!=Level.NETHER||p.blockPosition().distSqr(ritual.altar)>64||!validAltar(w,ritual.altar,ritual.morova)){
    cancel(ritual);continue;
   }
   if(w.getGameTime()<ritual.ends){
    if(w.getGameTime()%5==0)w.sendParticles(ritual.morova?net.minecraft.core.particles.ParticleTypes.SOUL:net.minecraft.core.particles.ParticleTypes.FLAME,ritual.altar.getX()+.5,ritual.altar.getY()+1.2,ritual.altar.getZ()+.5,6,.7,.5,.7,.01);
    continue;
   }
   var stack=p.getItemInHand(ritual.hand);var required=NetherBossItems.item(ritual.morova?"amulet_of_the_faded":"last_watch_horn");
   if(!stack.is(required)){cancel(ritual);continue;}
   var boss=(ritual.morova?ModEntities.MOROVA_DEVA:ModEntities.TRIZNA_PRINCE).get().create(w);
   boss.moveTo(ritual.altar.getX()+.5,ritual.altar.getY()+1,ritual.altar.getZ()+.5,p.getYRot()+180,0);
   if(!w.noCollision(boss)){cancel(ritual);fail(p,"space");continue;}
   var ledger=NetherBossLedger.get(w);var row=ledger.records.getCompound(ritual.id);
   row.putString("State","SPAWNING");row.putUUID("Boss",boss.getUUID());ledger.flush(w);
   stack.shrink(1);p.getInventory().setChanged();p.server.getPlayerList().save(p);
   boss.getPersistentData().putString("RiteEncounter",ritual.id);boss.setTarget(p);
   if(w.addFreshEntity(boss)){row.putString("State","ACTIVE");ledger.flush(w);}
   // Failed spawn stays reserved: no automatic retry or duplicate token/refund on unknown outcomes.
   ACTIVE.remove(p.getUUID());
  }
 }
 private static void cancel(Pending p){var w=p.world;NetherBossLedger.get(w).records.getCompound(p.id).putString("State","ENDED");NetherBossLedger.get(w).flush(w);ACTIVE.remove(p.player.getUUID());}
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var pending:List.copyOf(ACTIVE.values()))if(pending.world.getServer()==e.getServer())cancel(pending);}
 private NetherRituals(){}
}
