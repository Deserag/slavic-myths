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
 public static boolean hostile(ServerPlayer owner,LivingEntity target){return target!=owner&&!(target instanceof Player)
  &&!(target instanceof net.minecraft.world.entity.NeutralMob neutral&&(!(target instanceof Mob mob)||mob.getTarget()!=owner)&&!owner.getUUID().equals(neutral.getPersistentAngerTarget()))
  &&!(target instanceof NetherRetainer retainer&&(retainer.owner()==null||owner.getUUID().equals(retainer.owner())||owner.serverLevel().getEntity(retainer.owner()) instanceof Player))
  &&!(target instanceof TamableAnimal pet&&pet.isTame())&&!target.isAlliedTo(owner)
  &&org.slavicmyths.combat.CombatTargeting.canAttack(owner,target)
  &&(target instanceof Enemy||target instanceof Mob mob&&mob.getTarget()==owner);}
 public static boolean activate(ServerPlayer p,boolean pendant){
  if(!p.isAlive()||p.isSpectator())return false;
  var state=org.slavicmyths.rpg.PathData.data(p);long now=p.server.overworld().getGameTime();String key=pendant?"MorovaPendantCD":"TriznaHornCD";
  if(state.getLong(key)>now)return false;
  state.putLong(key,now+3600);p.server.getPlayerList().save(p);
  if(pendant){
   for(var target:p.serverLevel().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(10),e->hostile(p,e)&&!e.getType().is(org.slavicmyths.combat.CombatTargeting.CONTROL_IMMUNE)&&p.distanceToSqr(e)<=100)){
    boolean boss=target instanceof NetherSummonBoss||target.getType().is(org.slavicmyths.combat.CombatTargeting.BOSS);
    var n=target.getPersistentData();if(!n.contains("MorovaSilenceUntil"))n.putBoolean("MorovaSilencePriorNoAI",target.isNoAi());
    n.putLong("MorovaSilenceUntil",Math.max(n.getLong("MorovaSilenceUntil"),now+(boss?40:120)));
    n.putInt("MorovaSilenceDebuff",boss?100:300);target.setNoAi(true);target.setTarget(null);target.getNavigation().stop();
   }
  }else{
   int count=p.getRandom().nextFloat()<.25?5:4;
   for(int i=0;i<count;i++){
    var summon=ModEntities.NETHER_RETAINER.get().create(p.serverLevel());double angle=i*Math.PI*2/count;
    summon.moveTo(p.getX()+Math.cos(angle)*2,p.getY(),p.getZ()+Math.sin(angle)*2,0,0);
    if(!p.serverLevel().noCollision(summon))continue;
    summon.configure(p,40,7,1200);p.serverLevel().addFreshEntity(summon);
   }
  }return true;
 }
 private static boolean silenced(Entity e){return e instanceof LivingEntity living&&living.getServer()!=null&&living.getPersistentData().getLong("MorovaSilenceUntil")>living.getServer().overworld().getGameTime();}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre e){
  if(!(e.getEntity() instanceof Mob mob)||mob.level().isClientSide)return;
  var n=mob.getPersistentData();if(!n.contains("MorovaSilenceUntil"))return;
  if(!silenced(mob)){
   mob.setNoAi(n.getBoolean("MorovaSilencePriorNoAI"));int ticks=n.getInt("MorovaSilenceDebuff");
   n.remove("MorovaSilenceUntil");n.remove("MorovaSilencePriorNoAI");n.remove("MorovaSilenceDebuff");
   mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,ticks,1));mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,ticks));mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION,ticks));
  }
 }
 @SubscribeEvent public static void damage(LivingIncomingDamageEvent e){if(silenced(e.getSource().getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void projectile(EntityJoinLevelEvent e){if(e.getEntity() instanceof Projectile projectile&&silenced(projectile.getOwner()))e.setCanceled(true);}
 @SubscribeEvent public static void drops(LivingDropsEvent e){if(e.getEntity() instanceof NetherRetainer)e.setCanceled(true);}
 @SubscribeEvent public static void xp(LivingExperienceDropEvent e){if(e.getEntity() instanceof NetherRetainer)e.setDroppedExperience(0);}
 private NetherTrophies(){}
}
