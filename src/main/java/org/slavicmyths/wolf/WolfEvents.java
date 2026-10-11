package org.slavicmyths.wolf;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.living.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class WolfEvents {
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingDamageEvent.Pre e){if(e.getEntity() instanceof GreyWolf wolf){if(!wolf.active()){e.setNewDamage(0);return;}if(e.getNewDamage()>0&&wolf.getHealth()-e.getNewDamage()<=50){e.setNewDamage(0);wolf.finish();}}}
 @SubscribeEvent public static void drops(LivingDropsEvent e){if(e.getEntity() instanceof GreyWolf||e.getEntity().getPersistentData().getBoolean("summonedByGreyWolf"))e.getDrops().clear();}
 @SubscribeEvent public static void xp(LivingExperienceDropEvent e){if(e.getEntity() instanceof GreyWolf||e.getEntity().getPersistentData().getBoolean("summonedByGreyWolf"))e.setDroppedExperience(0);}
 @SubscribeEvent public static void expired(MobEffectEvent.Expired e){
  if(e.getEffectInstance()==null||!e.getEffectInstance().is(org.slavicmyths.registry.ModEffects.WOLF_CUT)||!(e.getEntity().level() instanceof net.minecraft.server.level.ServerLevel w))return;
  var target=e.getEntity();w.getServer().tell(new net.minecraft.server.TickTask(w.getServer().getTickCount()+1,()->{if(!target.isAlive()||org.slavicmyths.combat.RareBleedEffect.current(target)!=null)return;var d=target.getPersistentData();long now=org.slavicmyths.combat.RareCombat.now(target);d.remove("RareBleedWolfUntil");d.remove("RareBleedWolfStacks");d.remove("RareBleedWolfActor");
   int n=d.getLong("RareBleedNativeUntil")>now?d.getInt("RareBleedNative"):0,r=d.getLong("RareBleedRuneUntil")>now?d.getInt("RareBleedRune"):0,stacks=org.slavicmyths.rpg.RuneBalance.bleedStacks(n,r);int remaining=(int)(Math.max(d.getLong("RareBleedNativeUntil"),d.getLong("RareBleedRuneUntil"))-now);
   if(stacks>0&&remaining>0)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(org.slavicmyths.registry.ModEffects.RARE_BLEED,remaining,stacks-1,false,true,true));
  }));
 }
 private WolfEvents(){}
}
