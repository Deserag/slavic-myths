package org.slavicmyths.hunt;

import java.util.WeakHashMap;
import org.slavicmyths.entity.LandSpiritEntity;

/** Opt-in diagnosis only: -Dslavicmyths.likho.trace=true, at most 160 lines per entity. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class LikhoCombatTrace {
 private static final boolean ENABLED=Boolean.getBoolean("slavicmyths.likho.trace");
 private static final WeakHashMap<LandSpiritEntity,Integer> COUNTS=new WeakHashMap<>();
 @net.neoforged.bus.api.SubscribeEvent
 public static void actualDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e){
  if(!ENABLED)return;
  if(e.getSource().getEntity() instanceof WorldBoss boss&&boss.kind()==BossKind.LIKHO)event(boss,"actualDamage victim="+e.getEntity().getUUID()+" final="+e.getNewDamage());
  if(e.getEntity() instanceof WorldBoss boss&&boss.kind()==BossKind.LIKHO)event(boss,"incomingDamage final="+e.getNewDamage());
 }
 public static void event(LandSpiritEntity mob,String event){
  if(!ENABLED||mob.level().isClientSide||!(mob instanceof WorldBoss boss)||boss.kind()!=BossKind.LIKHO)return;
  int count=COUNTS.getOrDefault(mob,0);if(count>=160)return;COUNTS.put(mob,count+1);
  com.mojang.logging.LogUtils.getLogger().info("LIKHO93 id={} tick={} move={} elapsed={} morSkill={} target={} {}{}",mob.getUUID(),mob.tickCount,boss.move(),boss.animationTime(0),mob.morSkill(),mob.getTarget()==null?"none":mob.getTarget().getUUID(),event,count==159?" [trace limit reached]":"");
 }
 private LikhoCombatTrace(){}
}
