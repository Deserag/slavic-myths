package org.slavicmyths.hunt;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slavicmyths.entity.LandSpiritEntity;

/** Optional 200-tick post-mitigation damage summaries; does not affect combat. */
@EventBusSubscriber(modid="slavicmyths")
public final class LikhoDpsProbe {
 private static final boolean ENABLED=Boolean.getBoolean("slavicmyths.likho.dps");
 private static final long WINDOW_TICKS=200;
 private static final Map<WorldBoss,Window> WINDOWS=new WeakHashMap<>();
 private static final class Window {
  final long start;final boolean mor;
  final Map<String,Integer> casts=new LinkedHashMap<>();
  final Set<UUID> playerVictims=new HashSet<>();
  long targetTicks;
  double dealtPlayers,dealtOthers,taken,takenFromPlayers,lastToughness;
  int playerHitEvents,otherHitEvents,takenHitEvents,lastArmor;
  String lastAttackerItem="none";
  Window(long start,boolean mor){this.start=start;this.mor=mor;}
 }
 private static Window window(WorldBoss boss){
  return WINDOWS.computeIfAbsent(boss,b->new Window(b.level().getGameTime(),b.morState()==2));
 }
 public static void begin(LandSpiritEntity entity,String ability){
  if(!ENABLED||entity.level().isClientSide||!(entity instanceof WorldBoss boss)||boss.kind()!=BossKind.LIKHO)return;
  Window w=window(boss);
  if(w.casts.containsKey(ability)||w.casts.size()<16)w.casts.merge(ability,1,Integer::sum);
 }
 @SubscribeEvent public static void damaged(LivingDamageEvent.Post event){
  if(!ENABLED||event.getEntity().level().isClientSide||event.getNewDamage()<=0)return;
  var source=event.getSource().getEntity();
  if(source instanceof WorldBoss boss&&boss.kind()==BossKind.LIKHO){
   Window w=window(boss);
   if(event.getEntity() instanceof Player p){
    w.dealtPlayers+=event.getNewDamage();w.playerHitEvents++;
    if(w.playerVictims.size()<8)w.playerVictims.add(p.getUUID());
    w.lastArmor=p.getArmorValue();w.lastToughness=p.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
   }else{w.dealtOthers+=event.getNewDamage();w.otherHitEvents++;}
  }
  if(event.getEntity() instanceof WorldBoss boss&&boss.kind()==BossKind.LIKHO){
   Window w=window(boss);w.taken+=event.getNewDamage();w.takenHitEvents++;
   if(source instanceof Player p){
    w.takenFromPlayers+=event.getNewDamage();
    w.lastAttackerItem=BuiltInRegistries.ITEM.getKey(p.getMainHandItem().getItem()).toString();
   }
  }
 }
 public static void sample(WorldBoss boss){
  if(!ENABLED||boss.level().isClientSide||boss.kind()!=BossKind.LIKHO||boss.tickCount%20!=0)return;
  Window w=WINDOWS.get(boss);
  if(w==null){if(boss.getTarget()==null)return;w=window(boss);}
  long now=boss.level().getGameTime();
  if(w.mor!=(boss.morState()==2)){
   report(boss,w,now);WINDOWS.put(boss,new Window(now,boss.morState()==2));return;
  }
  if(boss.getTarget()!=null)w.targetTicks+=20;
  if(now-w.start>=WINDOW_TICKS){
   report(boss,w,now);
   if(boss.getTarget()==null&&w.casts.isEmpty()&&w.takenHitEvents==0&&w.playerHitEvents==0&&w.otherHitEvents==0)WINDOWS.remove(boss);
   else WINDOWS.put(boss,new Window(now,boss.morState()==2));
  }
 }
 private static void report(WorldBoss boss,Window w,long now){
  if(w.targetTicks==0&&w.casts.isEmpty()&&w.takenHitEvents==0&&w.playerHitEvents==0&&w.otherHitEvents==0)return;
  long ticks=Math.max(1,now-w.start);
  com.mojang.logging.LogUtils.getLogger().info(
   "LIKHO_DPS boss={} form={} windowTicks={} targetTicksApprox={} starts={} playerVictims={} lastVictimArmor={} lastVictimToughness={} outgoingPlayerHP={} outgoingOtherHP={} incomingHP={} incomingFromPlayersHP={} playerDPS={} bossTakenDPS={} playerHitEvents={} otherHitEvents={} takenHitEvents={} lastPlayerWeapon={}",
   boss.getUUID(),w.mor?"mor":"normal",ticks,w.targetTicks,w.casts,w.playerVictims.size(),w.lastArmor,w.lastToughness,
   w.dealtPlayers,w.dealtOthers,w.taken,w.takenFromPlayers,20.0*w.dealtPlayers/ticks,20.0*w.taken/ticks,
   w.playerHitEvents,w.otherHitEvents,w.takenHitEvents,w.lastAttackerItem);
 }
 private LikhoDpsProbe(){}
}
