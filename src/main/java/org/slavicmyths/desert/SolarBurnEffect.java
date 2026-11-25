package org.slavicmyths.desert;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slavicmyths.combat.MythDamageSources;
import org.slavicmyths.registry.ModEffects;
@EventBusSubscriber(modid="slavicmyths")
public final class SolarBurnEffect extends MobEffect {
 private static final String HEAT="HalaSolarHeat",LAST="HalaSolarLastExposure",COOL="HalaSolarNextCool",PULSE="HalaSolarNextPulse";
 public SolarBurnEffect(){super(MobEffectCategory.HARMFUL,0xde581c);}
 private static boolean allowed(LivingEntity e){return e.isAlive()&&!(e instanceof HalaEntity)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()));}
 public static int heat(LivingEntity e){return e.getEffect(ModEffects.SOLAR_BURN)==null?0:Math.clamp(e.getPersistentData().getInt(HEAT),0,100);}
 public static int stage(LivingEntity e){var effect=e.getEffect(ModEffects.SOLAR_BURN);return effect==null?0:Math.clamp(effect.getAmplifier()+1,1,3);}
 public static void apply(LivingEntity e,int amount){
  if(!(e.level() instanceof ServerLevel w)||!allowed(e)||amount<=0)return;
  int before=heat(e),after=HalaRules.addHeat(before,amount);var n=e.getPersistentData();long now=w.getGameTime();
  n.putLong(LAST,now);if(before==0){n.putLong(COOL,now+20);n.putLong(PULSE,now+40);}
  sync(e,after);
 }
 private static void sync(LivingEntity e,int heat){
  int stage=HalaRules.stage(heat);var n=e.getPersistentData();n.putInt(HEAT,heat);
  var old=e.getEffect(ModEffects.SOLAR_BURN);
  if(stage==0){e.removeEffect(ModEffects.SOLAR_BURN);n.remove(HEAT);n.remove(LAST);n.remove(COOL);n.remove(PULSE);return;}
  if(old==null||old.getAmplifier()!=stage-1){e.removeEffect(ModEffects.SOLAR_BURN);e.addEffect(new MobEffectInstance(ModEffects.SOLAR_BURN,1200,stage-1,false,true,true));}
  else if(old.getDuration()<200)e.addEffect(new MobEffectInstance(ModEffects.SOLAR_BURN,1200,stage-1,false,true,true));
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof LivingEntity e)||!(e.level() instanceof ServerLevel w))return;
  var n=e.getPersistentData();if(!n.contains(HEAT))return;
  if(!allowed(e)||e.getEffect(ModEffects.SOLAR_BURN)==null){sync(e,0);return;}
  long now=w.getGameTime();int heat=heat(e);
  if(now>=n.getLong(COOL)){n.putLong(COOL,now+20);if(e.isInWater()||now-n.getLong(LAST)>=100){heat=HalaRules.cool(heat,e.isInWater());sync(e,heat);}}
  int stage=HalaRules.stage(heat);
  if(stage>0&&now>=n.getLong(PULSE)){n.putLong(PULSE,now+40);float amount=stage*2F;if(e.hasEffect(MobEffects.FIRE_RESISTANCE))amount*=.5F;e.hurt(MythDamageSources.unattributed("solar_burn",e),amount);}
 }
 @SubscribeEvent public static void outgoing(LivingIncomingDamageEvent event){
  if(event.getEntity().level().isClientSide||!(event.getSource().getEntity() instanceof LivingEntity attacker))return;
  int stage=stage(attacker);if(stage<2)return;
  float factor=HalaRules.outgoing(stage,attacker.getRandom().nextFloat());
  if(factor==0){event.setCanceled(true);return;}event.setAmount(event.getAmount()*factor);
 }
 @SubscribeEvent public static void healing(LivingHealEvent event){
  if(!event.getEntity().level().isClientSide)event.setAmount(event.getAmount()*HalaRules.healing(stage(event.getEntity())));
 }
}
