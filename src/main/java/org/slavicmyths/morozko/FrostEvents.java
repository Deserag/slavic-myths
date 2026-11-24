package org.slavicmyths.morozko;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
@EventBusSubscriber(modid="slavicmyths")
public final class FrostEvents {
 // Aiming the staff at an entity uses the weapon instead of trading/inspecting it.
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void staffInteract(PlayerInteractEvent.EntityInteract e){if(e.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND&&e.getItemStack().is(FrostContent.STAFF.get())){e.setCanceled(true);e.setCancellationResult(FrostContent.STAFF.get().use(e.getLevel(),e.getEntity(),e.getHand()).getResult());}}
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void staffInteractAt(PlayerInteractEvent.EntityInteractSpecific e){if(e.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND&&e.getItemStack().is(FrostContent.STAFF.get())){e.setCanceled(true);e.setCancellationResult(FrostContent.STAFF.get().use(e.getLevel(),e.getEntity(),e.getHand()).getResult());}}
 @SubscribeEvent public static void mirrorFriendly(LivingIncomingDamageEvent e){if(e.getSource().getEntity() instanceof PlayerDouble c&&!c.allowedTarget(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void clamp(LivingDamageEvent.Pre e){if(e.getEntity() instanceof Morozko b&&!(b instanceof IceDouble)&&b.fighting())e.setNewDamage(FrostRules.clampedDamage(b.getHealth(),b.getMaxHealth(),e.getNewDamage()));}
 @SubscribeEvent public static void damage(LivingDamageEvent.Post e){
  if(e.getNewDamage()<=0||e.getEntity().level().isClientSide)return;
  var from=e.getSource().getEntity();var target=e.getEntity();
  if(target instanceof Morozko boss&&boss.fighting()&&from!=null&&FrostWorld.owner(from)!=null&&from.position().distanceToSqr(boss.anchor)<=1600)boss.credit(FrostWorld.owner(from),e.getNewDamage());
  if(from instanceof PlayerDouble c&&e.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow)proc(c,target);
  if(!(from instanceof ServerPlayer p))return;
  if(e.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow){
   if(!org.slavicmyths.combat.CombatTargeting.canAttack(p,target,org.slavicmyths.combat.CombatTargeting.Context.PROJECTILE))return;
   FrostSkills.crown(p);
   dispatch(p,target,arrow);
  }else if(e.getSource().getDirectEntity()==p&&!e.getSource().getMsgId().equals("morozko_stream")){
   proc(p,target);
   if(!org.slavicmyths.combat.CombatTargeting.canAttack(p,target,org.slavicmyths.combat.CombatTargeting.Context.DIRECT))return;
   FrostSkills.crown(p);
   dispatch(p,target,null);
  }
 }
 private static void dispatch(ServerPlayer p,LivingEntity target,net.minecraft.world.entity.projectile.AbstractArrow arrow){
  var clones=FrostSkills.copies(p);
  if(clones.isEmpty())return;
  var n=p.getPersistentData();int seq=n.getInt("FrostAttackSequence")+1;n.putInt("FrostAttackSequence",seq);
  for(var clone:clones){
   if(arrow!=null)clone.ranged(arrow);
   clone.mirrorHit(target,seq,arrow!=null);
  }
 }
 public static void proc(LivingEntity e,LivingEntity t){if(!org.slavicmyths.rpg.Runes.has(e.getMainHandItem(),"frost")||!FrostWorld.hostile(t,e))return;var n=e.getPersistentData();long now=e.level().getGameTime();if(now<n.getLong("FrostRuneReady"))return;n.putLong("FrostRuneReady",now+12);if(e.getRandom().nextFloat()<.25f&&!FrostCold.resistant(t)){t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80));t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80));if(t.getPersistentData().contains(FrostCold.KEY))FrostCold.add(t,10);}}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post e){if(e.getEntity() instanceof LivingEntity l)FrostCold.tick(l);if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%5==0){if(p.getPersistentData().getBoolean("FrostStreaming")&&(!p.isUsingItem()||!p.getUseItem().is(FrostContent.STAFF.get())))FrostSkills.stopStream(p);var ledger=FrostLedger.get(p.serverLevel());if(p.tickCount%20==0&&!ledger.player(p.getUUID()).getList("Pending",10).isEmpty())ledger.collect(p);boolean active=p.getPersistentData().contains(FrostCold.KEY)||!org.slavicmyths.item.FolkAccessoryItem.equipped(p,FrostContent.MIRROR.get()).isEmpty()||p.getMainHandItem().is(FrostContent.STAFF.get());if(active||p.getPersistentData().getBoolean("FrostHUD")){FrostNetwork.send(p);p.getPersistentData().putBoolean("FrostHUD",active);}}}
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p){p.getPersistentData().putFloat("FrostAttackScale",p.getAttackStrengthScale(.5f));p.getPersistentData().putBoolean("FrostCritical",p.fallDistance>0&&!p.onGround()&&!p.onClimbable()&&!p.isInWater()&&!p.isPassenger()&&!p.isSprinting()&&!p.hasEffect(MobEffects.BLINDNESS));}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){p.getPersistentData().remove(FrostCold.KEY);FrostLedger.get(p.serverLevel()).collect(p);FrostLedger.get(p.serverLevel()).progress(p);FrostNetwork.send(p);}}
 private static void leave(ServerPlayer p){FrostCold.stop(p);p.getPersistentData().remove(FrostCold.KEY);FrostSkills.stopStream(p);for(var c:FrostSkills.copies(p))c.discard();}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 private FrostEvents(){}
}
