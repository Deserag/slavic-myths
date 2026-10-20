package org.slavicmyths.combat;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.animal.IronGolem;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
/** Event boundary for native HurtBy/taunt goals too; no new goals on wildlife or story actors. */
@EventBusSubscriber(modid="slavicmyths")
public final class SettlementTargetSafety {
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void target(LivingChangeTargetEvent e){
  var actor=e.getEntity();var target=e.getNewAboutToBeSetTarget();if(target==null)return;
  boolean allowed=true;
  if(actor instanceof org.slavicmyths.industry.ChudPerson c&&c.role()==3)allowed=CombatTargeting.isThreatToSettlement(c,target);
  else if(actor instanceof Villager v&&org.slavicmyths.military.Military.guard(v))allowed=org.slavicmyths.military.GuardBehavior.hostile(v,target);
  else if(actor instanceof IronGolem g)allowed=target instanceof Player?CombatTargeting.canAttack(g,target):CombatTargeting.isThreatToSettlement(g,target);
  if(!allowed)e.setNewAboutToBeSetTarget(null);
 }
 private SettlementTargetSafety(){}
}
