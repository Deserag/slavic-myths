package org.slavicmyths.artifact;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.entity.WildlifeEntity;
import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ArtifactEvents {
 public static long now(net.minecraft.world.level.Level w){return w.getServer().overworld().getGameTime();}
 public static boolean summoned(LivingEntity e){return e.getPersistentData().hasUUID("SlavicGuardianOwner");}
 public static boolean calm(LivingEntity e){return !e.level().isClientSide&&e.getPersistentData().getLong("SlavicMusicUntil")>now(e.level());}
 public static boolean eligible(Mob e){return e.canChangeDimensions(e.level(),e.level())&&!summoned(e)&&e.getMaxHealth()<100&&!e.getTags().contains("slavic_music_immune")&&(!net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("slavicmyths")||e instanceof WildlifeEntity);}
 public static void music(Player p){
  long time=now(p.level());int count=0;
  for(Mob mob:p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(12),e->e.isAlive()&&e.distanceToSqr(p)<=144)){
   if(++count>64)break;if(!eligible(mob))continue;CompoundTag d=mob.getPersistentData();
   boolean same=d.hasUUID("SlavicMusician")&&d.getUUID("SlavicMusician").equals(p.getUUID());
   int exposure=same&&time-d.getLong("SlavicMusicLast")<=15?d.getInt("SlavicMusicExposure")+10:10;
   d.putUUID("SlavicMusician",p.getUUID());d.putLong("SlavicMusicLast",time);d.putInt("SlavicMusicExposure",Math.min(40,exposure));
   if(exposure>=30){d.putLong("SlavicMusicUntil",time+60);mob.setTarget(null);if(mob.tickCount%40<10)((ServerLevel)p.level()).sendParticles(ParticleTypes.ENCHANT,mob.getX(),mob.getY()+mob.getBbHeight(),mob.getZ(),1,.1,.1,.1,0);}
  }
  if(p.getTicksUsingItem()>=40)org.slavicmyths.progression.Knowledge.award(p,"self_playing_music");
 }
 @SubscribeEvent public static void join(EntityJoinLevelEvent event){if(!event.getLevel().isClientSide&&event.getEntity() instanceof Mob){Mob m=(Mob)event.getEntity();m.goalSelector.addGoal(-10,new CalmGoal(m));if(m instanceof WildlifeEntity)m.goalSelector.addGoal(-9,new GuardianGoal((WildlifeEntity)m));}}
 @SubscribeEvent public static void target(LivingChangeTargetEvent event){if(event.getNewAboutToBeSetTarget()!=null&&event.getEntity() instanceof Mob&&calm(event.getEntity()))event.setNewAboutToBeSetTarget(null);}
 @SubscribeEvent public static void attack(LivingIncomingDamageEvent event){Entity attacker=event.getSource().getEntity();if(attacker instanceof LivingEntity&&(calm((LivingEntity)attacker)||(summoned((LivingEntity)attacker)&&!enemy((LivingEntity)attacker,event.getEntity()))))event.setCanceled(true);}
 @SubscribeEvent public static void embers(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event){if(!(event.getEntity() instanceof LivingEntity e))return;if(e.level().isClientSide||e.tickCount%20!=0||!e.getPersistentData().contains("SlavicEmbersUntil"))return;if(now(e.level())<e.getPersistentData().getLong("SlavicEmbersUntil"))((ServerLevel)e.level()).sendParticles(ParticleTypes.FLAME,e.getX(),e.getY()+.6,e.getZ(),1,.2,.3,.2,0);else e.getPersistentData().remove("SlavicEmbersUntil");}
 @SubscribeEvent public static void drops(LivingDropsEvent event){if(summoned(event.getEntity()))event.setCanceled(true);}
 @SubscribeEvent public static void xp(LivingExperienceDropEvent event){if(summoned(event.getEntity()))event.setDroppedExperience(0);}
 public static Player owner(LivingEntity e){return summoned(e)?e.level().getPlayerByUUID(e.getPersistentData().getUUID("SlavicGuardianOwner")):null;}
 public static boolean enemy(LivingEntity guardian,LivingEntity target){Player p=owner(guardian);if(p==null||target==null||!target.isAlive()||target==p||p.isAlliedTo(target)||target.isAlliedTo(p))return false;if(target instanceof Player&&!p.canHarmPlayer((Player)target))return false;if(target instanceof TamableAnimal&&p.getUUID().equals(((TamableAnimal)target).getOwnerUUID()))return false;return !summoned(target)||!target.getPersistentData().getUUID("SlavicGuardianOwner").equals(p.getUUID());}
 private static final class CalmGoal extends Goal {
  final Mob mob;CalmGoal(Mob m){mob=m;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
  public boolean canUse(){return calm(mob);}public boolean canContinueToUse(){return canUse();}
  public void start(){mob.getNavigation().stop();mob.setTarget(null);}
  public void tick(){mob.setTarget(null);Player p=mob.level().getPlayerByUUID(mob.getPersistentData().getUUID("SlavicMusician"));if(p!=null){mob.getLookControl().setLookAt(p,15,15);if(mob.tickCount%40==0&&mob.getId()%3==0&&mob.distanceToSqr(p)>16)mob.getNavigation().moveTo(p,.45);} }
  public void stop(){mob.getNavigation().stop();}
 }
 private static final class GuardianGoal extends Goal {
  final WildlifeEntity mob;GuardianGoal(WildlifeEntity m){mob=m;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
  public boolean canUse(){return summoned(mob);}public boolean canContinueToUse(){return canUse();}
  public void tick(){Player p=owner(mob);if(now(mob.level())>=mob.getPersistentData().getLong("SlavicGuardianUntil")||p==null||!p.isAlive()){((ServerLevel)mob.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,mob.getX(),mob.getY()+.5,mob.getZ(),8,.4,.4,.4,.02);mob.discard();return;}
   LivingEntity target=p.getLastHurtMob();if(!enemy(mob,target)||mob.distanceToSqr(target)>256)target=p.getLastHurtByMob();if(!enemy(mob,target)||mob.distanceToSqr(target)>256)target=null;mob.setTarget(target);
   if(target!=null){mob.getLookControl().setLookAt(target,25,25);if(mob.tickCount%10==0)mob.getNavigation().moveTo(target,1.15);if(mob.tickCount%24==0&&mob.distanceToSqr(target)<Math.pow(mob.getBbWidth()+target.getBbWidth()+.7,2)&&mob.hasLineOfSight(target))mob.doHurtTarget(target);}
   else if(mob.tickCount%10==0){if(mob.distanceToSqr(p)>9)mob.getNavigation().moveTo(p,1.05);else mob.getNavigation().stop();}
  }
 }
}
