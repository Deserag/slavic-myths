package org.slavicmyths.poloz;
import net.minecraft.world.entity.*;
public final class PolozMinions {
 public static boolean enemy(org.slavicmyths.entity.LandSpiritEntity minion,LivingEntity other){return org.slavicmyths.combat.CombatTargeting.canAttack(minion,other,org.slavicmyths.combat.CombatTargeting.Context.RETALIATION)&&minion.getPersistentData().hasUUID("summonedByPolozUUID")&&other instanceof Mob m&&m.getTarget()==minion&&!(other instanceof GreatPoloz)&&!(other instanceof org.slavicmyths.entity.LandSpiritEntity s&&s.morState()!=0)&&!other.getPersistentData().hasUUID("summonedByPolozUUID");}
 private PolozMinions(){}
}
