package org.slavicmyths.hunt;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.slavicmyths.combat.CombatTargeting;

/** Bounded callers supply nearby entities; peaceful bystanders are never area targets. */
public final class LikhoTargets {
 public static boolean allowed(WorldBoss self,LivingEntity target){
  if(!CombatTargeting.canAttack(self,target,CombatTargeting.Context.AREA))return false;
  boolean tame=target instanceof TamableAnimal pet&&pet.isTame();
  boolean attacker=self.getLastHurtByMob()==target&&self.tickCount-self.getLastHurtByMobTimestamp()<=1200
   ||target instanceof Mob mob&&mob.getTarget()==self;
  return candidate(target instanceof Player,target instanceof Enemy,attacker,tame);
 }
 public static boolean candidate(boolean player,boolean enemy,boolean attacker,boolean tame){return !tame&&(player||enemy||attacker);}
 private LikhoTargets(){}
}
