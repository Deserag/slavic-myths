package org.slavicmyths.mor;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.entity.LandSpiritEntity;

/** Server-owned, local mor Likho aura; shares periodic hit throttling with its ground/cloud zones. */
public final class MorPresence {
 private static final Map<LivingEntity,Long> NEXT_HIT=new WeakHashMap<>();
 private record Exposure(long enteredAt,long groundedAt){}
 private final LandSpiritEntity mob;
 private final Map<UUID,Exposure> present=new HashMap<>();
 public MorPresence(LandSpiritEntity mob){this.mob=mob;}
 public void clear(){present.clear();}
 public void tick(){
  if(!(mob.level() instanceof ServerLevel level)||mob.mor.kind()!=5||mob.morState()!=2||!mob.isAlive()||mob.isRemoved()||mob.level().getDifficulty()==Difficulty.PEACEFUL){clear();return;}
  if(mob.tickCount%MorPresenceRules.SAMPLE_TICKS!=0)return;
  long now=level.getGameTime();
  Set<UUID> observed=new HashSet<>();
  DamageSource curse=new DamageSource(mob.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MAGIC),null,mob);
  for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,mob.getBoundingBox().inflate(MorPresenceRules.RADIUS,2.5,MorPresenceRules.RADIUS),e->HigherMor.enemy(mob,e))){
   if(!MorPresenceRules.near(target.getX()-mob.getX(),target.getY()-mob.getY(),target.getZ()-mob.getZ())||!mob.clearLineForMor(mob.position().add(0,.3,0),target.position().add(0,.3,0)))continue;
   UUID id=target.getUUID();
   Exposure previous=present.get(id);
   if(!target.onGround()){
    if(previous!=null&&MorPresenceRules.retainInAir(now,previous.groundedAt()))observed.add(id);
    continue;
   }
   observed.add(id);
   Exposure current=new Exposure(previous==null?now:previous.enteredAt(),now);
   present.put(id,current);
   if(!MorPresenceRules.ready(now,current.enteredAt()))continue;
   boolean hit=hurt(mob,target,curse,1.0F,MorPresenceRules.PERIOD_TICKS);
   if(hit){level.sendParticles(MorContent.ASH.get(),target.getX(),target.getY()+.15,target.getZ(),2,.18,.05,.18,0);org.slavicmyths.hunt.LikhoCombatTrace.event(mob,"presence.damage victim="+id+" accepted=true");}
  }
  present.keySet().retainAll(observed);
 }
 /** Accepted damage is shared between passive presence and Likho zones, never the unrelated Ovinnik zones. */
 public static boolean hurt(LandSpiritEntity owner,LivingEntity target,DamageSource source,float amount,int cooldown){
  if(owner.level().isClientSide||owner.level().getDifficulty()==Difficulty.PEACEFUL||!owner.isAlive()||owner.isRemoved()||owner.level()!=target.level()||!HigherMor.enemy(owner,target))return false;
  long now=owner.level().getGameTime();
  if(!MorPresenceRules.mayHit(now,NEXT_HIT.getOrDefault(target,Long.MIN_VALUE)))return false;
  boolean hit=target.hurt(source,amount);
  if(hit)NEXT_HIT.put(target,now+Math.max(MorPresenceRules.SAMPLE_TICKS*2,cooldown));
  return hit;
 }
}
