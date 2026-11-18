package org.slavicmyths.progression;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Bounded causal credit for the three Yaga gate bosses; no polling or global player scan. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BossVictoryCredit {
 private static final String KEY="slavicmyths:yaga_victory_credit";
 public static void remember(CompoundTag data,UUID id,long now){var n=new CompoundTag();n.putUUID("Player",id);n.putLong("At",now);data.put(KEY,n);}
 public static UUID recent(CompoundTag data,long now){var n=data.getCompound(KEY);long at=n.getLong("At");return n.hasUUID("Player")&&now>=at&&now-at<=200?n.getUUID("Player"):null;}
 private static ServerPlayer owner(DamageSource source){
  if(source.getEntity() instanceof ServerPlayer p)return p;
  if(source.getEntity() instanceof org.slavicmyths.entity.WildlifeEntity wolf)return wolf.ringOwner();
  if(source.getEntity() instanceof OwnableEntity pet&&pet.getOwner() instanceof ServerPlayer p)return p;
  return null;
 }
 @SubscribeEvent public static void damage(LivingDamageEvent.Post e){
  var boss=e.getEntity();if(!(boss instanceof org.slavicmyths.hunt.WorldBoss||boss instanceof org.slavicmyths.bandit.NightingaleEntity)||!(boss.level() instanceof ServerLevel w)||!Float.isFinite(e.getNewDamage())||e.getNewDamage()<=0)return;
  var p=owner(e.getSource());if(p==null)return;
  if(AdvancementMilestones.eligible(p)&&AdvancementMilestones.combatDeath(e.getSource()))remember(boss.getPersistentData(),p.getUUID(),w.getGameTime());else boss.getPersistentData().remove(KEY);
 }
 public static ServerPlayer winner(LivingEntity boss,DamageSource source){
  if(!AdvancementMilestones.combatDeath(source)||!(boss.level() instanceof ServerLevel w))return null;
  var direct=owner(source);if(direct!=null)return AdvancementMilestones.eligible(direct)&&direct.level()==w?direct:null;
  // Another mob's killing blow must not inherit an earlier player's credit.
  if(source.getEntity()!=null)return null;
  var id=recent(boss.getPersistentData(),w.getGameTime());if(id==null)return null;
  var p=w.getServer().getPlayerList().getPlayer(id);
  return AdvancementMilestones.eligible(p)&&p.level()==w&&boss.distanceToSqr(p)<=96*96?p:null;
 }
 private BossVictoryCredit(){}
}
