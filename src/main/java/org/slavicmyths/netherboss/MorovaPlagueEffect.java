package org.slavicmyths.netherboss;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Milk-removable Morova disease, with a fixed cadence independent of refreshes. */
public final class MorovaPlagueEffect extends MobEffect {
 public MorovaPlagueEffect() {
  super(MobEffectCategory.HARMFUL,0x75816c);
  addAttributeModifier(Attributes.MOVEMENT_SPEED,ResourceLocation.fromNamespaceAndPath("slavicmyths","morova_plague_slow"),
   AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,a->a>=2?-.20:a==1?-.15:-.10);
 }
 @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
 @Override public boolean applyEffectTick(LivingEntity target,int amplifier) {
  if(target.level().isClientSide||amplifier<1)return true;
  var data=target.getPersistentData();long now=target.level().getGameTime();
  long next=data.getLong("MorovaPlaguePulse");
  if(next==0||next>now+60){data.putLong("MorovaPlaguePulse",now+60);return true;}
  if(now>=next){data.putLong("MorovaPlaguePulse",now+60);target.hurt(target.damageSources().magic(),amplifier>=2?2:1);}
  return true;
 }
}
