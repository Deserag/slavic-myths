package org.slavicmyths.mor;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.entity.LandSpiritEntity;
import org.slavicmyths.combat.CombatTargeting;
/** Recent real attackers can provoke a response; passive bystanders are not new prey. */
public final class MorRetaliation {
 public static boolean valid(LandSpiritEntity self,LivingEntity target){
  return target!=null&&target==self.getLastHurtByMob()&&target.isAlive()
   &&self.tickCount-self.getLastHurtByMobTimestamp()<=200
   &&target!=self&&!self.isAlliedTo(target)
   &&!(target instanceof LandSpiritEntity spirit&&spirit.morState()!=0)
   &&CombatTargeting.canAttack(self,target,CombatTargeting.Context.AREA);
 }
 private MorRetaliation(){}
}