package org.slavicmyths.wolf;
import net.minecraft.world.effect.*;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.registry.ModEffects;
import org.slavicmyths.combat.RareBleedEffect;
/** Shares the existing cadence and cap: the strongest active source governs all occupied levels, never two DoTs. */
public final class WolfBleeding extends MobEffect {
 public WolfBleeding(){super(MobEffectCategory.HARMFUL,0x981f2c);}
 public static void apply(net.minecraft.world.entity.LivingEntity wolf,ServerPlayer target){
  if(target.isCreative()||target.isSpectator()||!target.isAlive()||!org.slavicmyths.combat.CreatureProperties.canBleed(target))return;
  var old=RareBleedEffect.current(target);var d=target.getPersistentData();long now=org.slavicmyths.combat.RareCombat.now(target);
  if(old==null){for(String key:new String[]{"RareBleedNative","RareBleedRune","RareBleedNativeUntil","RareBleedRuneUntil","RareBleedWolfStacks","RareBleedWolfUntil"})d.remove(key);d.putLong("RareBleedPulse",now+40);}
  int strong=d.getLong("RareBleedWolfUntil")>now?d.getInt("RareBleedWolfStacks"):0;strong=Math.min(3,strong+1);d.putInt("RareBleedWolfStacks",strong);d.putLong("RareBleedWolfUntil",now+160);d.putUUID("RareBleedWolfActor",wolf.getUUID());d.putBoolean("RareBleedSources",true);
  int weak=Math.max(d.getLong("RareBleedNativeUntil")>now?d.getInt("RareBleedNative"):0,d.getLong("RareBleedRuneUntil")>now?d.getInt("RareBleedRune"):0);int stacks=Math.min(3,Math.max(strong,weak));int duration=160;
  target.removeEffect(ModEffects.RARE_BLEED);target.removeEffect(ModEffects.WOLF_CUT);target.addEffect(new MobEffectInstance(ModEffects.WOLF_CUT,duration,stacks-1,false,true,true),wolf);
 }
 @Override public boolean shouldApplyEffectTickThisTick(int ticks,int amplifier){return true;}
 @Override public boolean applyEffectTick(net.minecraft.world.entity.LivingEntity target,int amplifier){return ((RareBleedEffect)ModEffects.RARE_BLEED.get()).applyEffectTick(target,amplifier);}
}
