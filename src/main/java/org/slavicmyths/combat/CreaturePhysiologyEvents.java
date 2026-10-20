package org.slavicmyths.combat;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.registry.ModEffects;
/** Only named physiological effects and accepted fire damage. Never BAD_BLOOD, curses or magic. */
@EventBusSubscriber(modid="slavicmyths")
public final class CreaturePhysiologyEvents {
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void applicable(MobEffectEvent.Applicable e){
  var effect=e.getEffectInstance().getEffect();
  boolean bleed=effect.is(ModEffects.CUT.getKey())||effect.is(ModEffects.RARE_BLEED.getKey())||effect.is(ModEffects.WOLF_CUT.getKey());
  if(bleed&&!CreatureProperties.canBleed(e.getEntity())||effect==MobEffects.POISON&&!CreatureProperties.isPhysiologicalPoisonTarget(e.getEntity()))e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
 }
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void fire(LivingDamageEvent.Pre e){
  if(e.getEntity().level().isClientSide||e.getNewDamage()<=0||!e.getSource().is(DamageTypeTags.IS_FIRE))return;
  // This is the only hook applying the nature coefficient. Native immunity/phase filters have already accepted the hit.
  e.setNewDamage(e.getNewDamage()*CreatureProperties.fireMultiplier(e.getEntity()));
 }
 private CreaturePhysiologyEvents(){}
}
