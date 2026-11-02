package org.slavicmyths.netherboss;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.entity.projectile.Projectile;
import org.slavicmyths.registry.ModEntities;

@EventBusSubscriber(modid="slavicmyths")
public final class NetherTrophies {
 private static final java.util.Map<net.minecraft.server.level.ServerLevel,java.util.Set<Mob>> CONTROLLED=new java.util.IdentityHashMap<>();
 private static void track(Mob mob){if(mob.level() instanceof net.minecraft.server.level.ServerLevel world)CONTROLLED.computeIfAbsent(world,w->java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>())).add(mob);}
 public static boolean hostile(ServerPlayer owner,LivingEntity target){return target!=owner&&!(target instanceof Player)
  &&!(target instanceof net.minecraft.world.entity.NeutralMob neutral&&(!(target instanceof Mob mob)||mob.getTarget()!=owner)&&!owner.getUUID().equals(neutral.getPersistentAngerTarget()))
  &&!(target instanceof NetherRetainer retainer&&(retainer.playerOwned()||retainer.owner()==null||owner.getUUID().equals(retainer.owner())||owner.serverLevel().getEntity(retainer.owner()) instanceof Player))
  &&!(target instanceof TamableAnimal pet&&pet.isTame())&&!target.isAlliedTo(owner)
  &&org.slavicmyths.combat.CombatTargeting.canAttack(owner,target)
  &&(target instanceof Enemy||target instanceof Mob mob&&mob.getTarget()==owner);}
 public static boolean activateEquipped(ServerPlayer p,boolean pendant){
  var item=NetherBossItems.item(pendant?"morova_silence_pendant":"fallen_druzhina_horn");String slot=pendant?"necklace":"belt";
  boolean wears=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).map(h->h.findCurios(stack->stack.is(item)).stream().anyMatch(r->r.slotContext().identifier().equals(slot))).orElse(false);
  return wears&&activate(p,pendant);
 }
 public static boolean activate(ServerPlayer p,boolean pendant){
  if(!p.isAlive()||p.isSpectator())return false;
  var state=org.slavicmyths.rpg.PathData.data(p);long now=p.server.overworld().getGameTime();String key=pendant?"MorovaPendantCD":"TriznaHornCD";
  if(state.getLong(key)>now)return false;
  java.util.List<NetherRetainer> planned=java.util.List.of();
  if(!pendant){
   if(!p.serverLevel().getEntitiesOfClass(NetherRetainer.class,p.getBoundingBox().inflate(64),r->p.getUUID().equals(r.owner())&&r.isAlive()).isEmpty())return false;
   int count=p.getRandom().nextFloat()<.25?5:4;planned=NetherRetainer.prepare(p,count,40,7);
   if(planned.size()!=count){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("netherboss.slavicmyths.retainer_space"),true);return false;}
  }
  state.putLong(key,now+3600);((org.slavicmyths.navya.mixin.PlayerListSaveInvoker)p.server.getPlayerList()).slavicmyths$save(p);
  if(pendant){
   for(var target:p.serverLevel().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(10),e->hostile(p,e)&&!e.getType().is(org.slavicmyths.combat.CombatTargeting.CONTROL_IMMUNE)&&p.distanceToSqr(e)<=100)){
    boolean boss=target instanceof NetherSummonBoss||target.getType().is(org.slavicmyths.combat.CombatTargeting.BOSS);
    var n=target.getPersistentData();if(!n.contains("MorovaSilenceUntil"))n.putBoolean("MorovaSilencePriorNoAI",target.isNoAi());
    n.putLong("MorovaSilenceUntil",Math.max(n.getLong("MorovaSilenceUntil"),now+(boss?40:120)));
    n.putInt("MorovaSilenceDebuff",boss?100:300);target.setNoAi(true);target.setTarget(null);target.getNavigation().stop();track(target);
   }
  }else{
   var added=new java.util.ArrayList<NetherRetainer>();
   for(var summon:planned)if(p.serverLevel().addFreshEntity(summon))added.add(summon);
   if(added.size()!=planned.size()){
    added.forEach(Entity::discard);state.remove(key);((org.slavicmyths.navya.mixin.PlayerListSaveInvoker)p.server.getPlayerList()).slavicmyths$save(p);return false;
   }
  }
  p.serverLevel().playSound(null,p.blockPosition(),NetherBossSounds.cue(pendant,"trophy"),net.minecraft.sounds.SoundSource.PLAYERS,1F,1F);
  p.serverLevel().sendParticles(pendant?NetherSummonBoss.plagueParticle():net.minecraft.core.particles.ParticleTypes.FLAME,p.getX(),p.getY()+1,p.getZ(),24,.7,.5,.7,.01);
  return true;
 }
 private static boolean silenced(Entity e){return e instanceof LivingEntity living&&living.getServer()!=null&&living.getPersistentData().getLong("MorovaSilenceUntil")>living.getServer().overworld().getGameTime();}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
  for(var world:java.util.List.copyOf(CONTROLLED.keySet())){
   if(world.getServer()!=e.getServer())continue;var mobs=CONTROLLED.get(world);
   for(var mob:java.util.List.copyOf(mobs)){
    if(mob.isRemoved()||!mob.isAlive()||mob.level()!=world){mobs.remove(mob);continue;}
    if(silenced(mob))continue;
    var n=mob.getPersistentData();long until=n.getLong("MorovaSilenceUntil");
    int ticks=(int)Math.clamp(until+n.getInt("MorovaSilenceDebuff")-world.getServer().overworld().getGameTime(),0,300);
    mob.setNoAi(n.getBoolean("MorovaSilencePriorNoAI"));
    n.remove("MorovaSilenceUntil");n.remove("MorovaSilencePriorNoAI");n.remove("MorovaSilenceDebuff");
    if(ticks>0){mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,ticks,1));mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,ticks));mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION,ticks));}
    mobs.remove(mob);
   }if(mobs.isEmpty())CONTROLLED.remove(world);
  }
 }
 @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent e){if(e.getEntity() instanceof Mob mob&&e.getLevel() instanceof net.minecraft.server.level.ServerLevel world){var mobs=CONTROLLED.get(world);if(mobs!=null){mobs.remove(mob);if(mobs.isEmpty())CONTROLLED.remove(world);}}}
 @SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload e){CONTROLLED.remove(e.getLevel());}
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){CONTROLLED.keySet().removeIf(w->w.getServer()==e.getServer());}
 @SubscribeEvent public static void damage(LivingIncomingDamageEvent e){if(silenced(e.getSource().getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void projectile(EntityJoinLevelEvent e){if(!e.getLevel().isClientSide&&e.getEntity() instanceof Mob mob&&mob.getPersistentData().contains("MorovaSilenceUntil"))track(mob);if(e.getEntity() instanceof Projectile projectile&&silenced(projectile.getOwner()))e.setCanceled(true);}
 @SubscribeEvent public static void drops(LivingDropsEvent e){if(e.getEntity() instanceof NetherRetainer)e.setCanceled(true);}
 @SubscribeEvent public static void xp(LivingExperienceDropEvent e){if(e.getEntity() instanceof NetherRetainer)e.setDroppedExperience(0);}
 private NetherTrophies(){}
}
