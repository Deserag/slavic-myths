package org.slavicmyths.military;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.*;
import org.slavicmyths.bandit.BanditEntity;
@EventBusSubscriber(modid="slavicmyths")
public final class MilitaryEvents {
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getLevel() instanceof ServerLevel l&&e.getEntity() instanceof BanditEntity b&&b.getPersistentData().getBoolean("MilitaryRaider")&&!BanditRaids.get(l).active(b.getPersistentData().getLong("RaidAnchor"),b.getUUID())){b.getPersistentData().remove("MilitaryRaider");b.home=null;b.clearRestriction();}
if(!e.getLevel().isClientSide&&e.getEntity() instanceof IronGolem g)g.targetSelector.addGoal(2,new VillageBanditTargetGoal(g));}
 @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity().level() instanceof ServerLevel l){ServerPlayer p=e.getSource().getEntity() instanceof ServerPlayer s?s:null;if(p!=null)MilitaryQuests.killed(p,e.getEntity());if(e.getEntity() instanceof BanditEntity b)BanditRaids.get(l).killed(l,b,p);}}
 @SubscribeEvent public static void damage(LivingIncomingDamageEvent e){if(!(e.getEntity().level() instanceof ServerLevel l))return;var victim=e.getEntity();if(e.getSource().getEntity() instanceof Villager source&&Military.guard(source)&&!GuardBehavior.hostile(source,victim)){e.setCanceled(true);return;}if(victim instanceof Villager v&&Military.guard(v)&&v.getPersistentData().getLong("GuardBlockUntil")>=l.getGameTime()&&e.getSource().getSourcePosition()!=null){var from=e.getSource().getSourcePosition().subtract(v.position()).normalize();if(v.getLookAngle().dot(from)>.25)e.setAmount(e.getAmount()*.45F);}
  if(victim instanceof Villager&&e.getSource().getEntity() instanceof LivingEntity attacker)for(Villager guard:l.getEntitiesOfClass(Villager.class,victim.getBoundingBox().inflate(48),Military::guard)){if(!(attacker instanceof net.minecraft.world.entity.player.Player p)||( !p.isCreative()&&!p.isSpectator())){guard.getPersistentData().putUUID("GuardAggressor",attacker.getUUID());guard.getPersistentData().putLong("GuardAggressorUntil",l.getGameTime()+1200);}guard.setLastHurtByMob(attacker);}
 }
 @SubscribeEvent public static void entity(EntityTickEvent.Post e){if(e.getEntity() instanceof BanditEntity b&&b.level() instanceof ServerLevel l){long now=l.getGameTime();if(Math.floorMod(now+b.getId(),400)==0)BanditRaids.get(l).discover(l,b);if(b.getPersistentData().getBoolean("MilitaryRaider")&&now%40==0&&b.getTarget()==null){var target=l.getEntitiesOfClass(LivingEntity.class,b.getBoundingBox().inflate(24),t->t instanceof Villager||t instanceof IronGolem).stream().min(java.util.Comparator.comparingDouble(b::distanceToSqr)).orElse(null);if(target!=null)b.engage(target);else b.getNavigation().moveTo(b.home.getX()+.5,b.home.getY(),b.home.getZ()+.5,1);}}}
 @SubscribeEvent public static void player(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%20==0&&!MilitaryQuests.active(p).isEmpty())MilitaryQuests.visit(p);}
 @SubscribeEvent public static void level(LevelTickEvent.Post e){if(e.getLevel() instanceof ServerLevel l&&l.getGameTime()%200==0)BanditRaids.get(l).tick(l);}
 @SubscribeEvent public static void conversion(LivingConversionEvent.Post e){if(e.getEntity() instanceof Villager||e.getOutcome() instanceof Villager)e.getOutcome().setData(Military.ARCHETYPE,e.getEntity().getData(Military.ARCHETYPE));}
}
