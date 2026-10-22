package org.slavicmyths.mor;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import org.slavicmyths.entity.LandSpiritEntity;
import org.slavicmyths.hunt.EmberClump;
import org.slavicmyths.registry.ModEntities;

/** Local, bounded arena scheduler. Ground is an expiring entity overlay, never a block edit. */
public final class HigherMor {
 private final LandSpiritEntity mob;
 private int clock,wait,beamWait,cloudWait,groundWait;
 private boolean phase;
 private final MorPresence presence;
 private Vec3 heading=Vec3.ZERO;
 public HigherMor(LandSpiritEntity mob){this.mob=mob;this.presence=new MorPresence(mob);}
 public boolean phase(){return phase;}
 public static boolean higher(LivingEntity e){return e instanceof LandSpiritEntity s&&s.mor.kind()>=5&&s.morState()==2;}
 public static boolean enemy(LandSpiritEntity self,LivingEntity e){return e!=null&&org.slavicmyths.combat.CombatTargeting.canAttack(self,e,org.slavicmyths.combat.CombatTargeting.Context.AREA)&&e.isAlive()&&!self.isAlliedTo(e)&&(MorController.valid(e)||(e instanceof net.minecraft.world.entity.monster.Enemy||org.slavicmyths.bogatyr.HeroThreats.availableHero(e))&&!higher(e)&&!(e instanceof LandSpiritEntity s&&s.morState()!=0));}
 public void tick(){
  if(mob.morState()!=2){presence.clear();return;}
  presence.tick();
  if(mob.tickCount%240==0)mob.playSound(mob.mor.kind()==5?SoundEvents.WITCH_AMBIENT:SoundEvents.RAVAGER_AMBIENT,.4F,.55F);
  if(!phase&&HigherRules.phase2(mob.getHealth(),mob.getMaxHealth())&&(mob.mor.kind()!=5||mob.morSkill()==0)){phase=true;org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.phase");mob.morSync(2,0,15);clock=0;cloudWait=0;mob.playSound(mob.mor.kind()==5?SoundEvents.WITCH_CELEBRATE:SoundEvents.RAVAGER_ROAR,.7F,.55F);}
  if(groundWait>0)groundWait--;else {groundWait=HigherRules.groundPeriod(mob.mor.kind(),phase);zone(false);}
  wait=Math.max(0,wait-1);beamWait=Math.max(0,beamWait-1);cloudWait=Math.max(0,cloudWait-1);
  if(mob.tickCount%10==0&&(mob.mor.kind()!=5||mob.morSkill()==0)){Player player=mob.level().getNearestPlayer(mob.getX(),mob.getY(),mob.getZ(),24,e->e instanceof Player p&&MorController.valid(p)&&!mob.isAlliedTo(p));if(player!=null)mob.setTarget(player);else if(!enemy(mob,mob.getTarget())){LivingEntity attacker=mob.getLastHurtByMob();mob.setTarget(enemy(mob,attacker)&&mob.distanceToSqr(attacker)<=256?attacker:null);}}
  LivingEntity target=mob.getTarget();int skill=mob.morSkill();
  if(skill!=0){mob.getNavigation().stop();mob.setDeltaMovement(new Vec3(0,mob.getDeltaMovement().y,0));int t=++clock;
   if(skill==15&&t==20){org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.release phase_cloud");zone(true);}
   if(skill==12&&t==24){org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.release cloud");zone(true);}
   if(skill==10&&t==30){org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.release beam");mob.playSound(SoundEvents.EVOKER_CAST_SPELL,.65F,.6F);beam();}
   if(skill==13&&HigherRules.ember(t)&&enemy(mob,target)){EmberClump ember=new EmberClump(ModEntities.EMBER_CLUMP.get(),mob.level());ember.setOwner(mob);ember.setPos(mob.getEyePosition());Vec3 v=target.getBoundingBox().getCenter().subtract(ember.position());ember.shoot(v.x,v.y,v.z,.85F,0);mob.level().addFreshEntity(ember);mob.playSound(SoundEvents.BLAZE_SHOOT,.5F,.65F);}
   if(skill==11&&t==22){org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.release sweep");mob.playSound(SoundEvents.PLAYER_ATTACK_SWEEP,.7F,.65F);for(LivingEntity e:mob.level().getEntitiesOfClass(LivingEntity.class,mob.getBoundingBox().inflate(4),e->enemy(mob,e)))if(mob.distanceToSqr(e)<=16&&mob.getSensing().hasLineOfSight(e)&&heading.multiply(1,0,1).normalize().dot(e.position().subtract(mob.position()).multiply(1,0,1).normalize())>.25){boolean hit=e.hurt(mob.damageSources().mobAttack(mob),mob.mor.kind()==5?14:12);org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.sweep victim="+e.getUUID()+" accepted="+hit);}}
   if(t>=(skill==13?48:skill==10?46:42)){org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.recover skill="+skill);mob.morSync(2,0,0);clock=0;wait=phase?22:32;}return;
  }
  if(!enemy(mob,target))return;
  mob.getLookControl().setLookAt(target,25,25);
  if(wait==0&&mob.getSensing().hasLineOfSight(target)){
   int next=0;double d=mob.distanceTo(target);
   if(mob.mor.kind()==5&&d<=4&&mob.level().getEntitiesOfClass(LivingEntity.class,mob.getBoundingBox().inflate(4),e->enemy(mob,e)&&mob.distanceToSqr(e)<=16&&mob.getSensing().hasLineOfSight(e)).size()>=2){next=11;}
   else if(cloudWait==0&&(phase||mob.mor.kind()==6)){next=12;cloudWait=240;}
   else if(d<=4){next=11;}
   else if(d<=18&&beamWait==0){next=mob.mor.kind()==5?10:13;beamWait=phase?130:190;}
   if(next!=0){org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.begin skill="+next);org.slavicmyths.hunt.LikhoDpsProbe.begin(mob,"MOR_"+next);heading=target.getBoundingBox().getCenter().subtract(mob.getEyePosition()).normalize();mob.morSync(2,0,next);clock=0;mob.getNavigation().stop();mob.playSound(next==10?SoundEvents.EVOKER_PREPARE_ATTACK:next==13?SoundEvents.FIRE_AMBIENT:next==12?SoundEvents.FIRE_EXTINGUISH:SoundEvents.PLAYER_ATTACK_STRONG,.55F,.6F);return;}
  }
  if(mob.tickCount%10==0)mob.getNavigation().moveTo(target,phase?1.25:mob.mor.kind()==5?.8:1);
 }
 private void beam(){ServerLevel w=(ServerLevel)mob.level();Vec3 eye=mob.getEyePosition();double length=18;for(double d=.5;d<=18;d+=.5){Vec3 p=eye.add(heading.scale(d));if(!w.hasChunkAt(net.minecraft.core.BlockPos.containing(p))){length=d-.5;break;}if(!w.getBlockState(net.minecraft.core.BlockPos.containing(p)).getCollisionShape(w,net.minecraft.core.BlockPos.containing(p)).isEmpty()){length=d;break;}w.sendParticles(MorContent.SPARK.get(),p.x,p.y,p.z,2,.06,.06,.06,0);}final double end=length;for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,mob.getBoundingBox().inflate(18),e->enemy(mob,e))){Vec3 delta=e.getBoundingBox().getCenter().subtract(eye);double along=delta.dot(heading);if(along>=0&&along<=end&&delta.subtract(heading.scale(along)).lengthSqr()<=.7&&mob.getSensing().hasLineOfSight(e)){boolean hit=e.hurt(mob.damageSources().mobAttack(mob),12);org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"higher.beam victim="+e.getUUID()+" accepted="+hit);}}}
 private void zone(boolean cloud){ServerLevel w=(ServerLevel)mob.level();if(w.getEntitiesOfClass(MorZone.class,mob.getBoundingBox().inflate(32),e->mob.getUUID().equals(e.owner())).size()>=HigherRules.MAX_ZONES)return;MorZone z=new MorZone(MorContent.ZONE.get(),w);z.configure(mob,cloud);z.setPos(mob.position());w.addFreshEntity(z);if(cloud)mob.playSound(SoundEvents.FIRE_EXTINGUISH,.6F,.6F);}
 public void save(CompoundTag n){new HigherSnapshot(phase,clock,wait,beamWait,cloudWait,groundWait,mob.morSkill(),heading).write(n);}
 public void load(CompoundTag n){var s=HigherSnapshot.read(n);phase=s.phase();clock=s.clock();wait=s.waitTicks();beamWait=s.beam();cloudWait=s.cloud();groundWait=s.ground();heading=s.heading();mob.morSync(mob.morState(),mob.morRemaining(),s.skill());mob.morSetSkillElapsed(clock);}
}
