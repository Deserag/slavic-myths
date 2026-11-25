package org.slavicmyths.desert;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slavicmyths.registry.ModEffects;
@EventBusSubscriber(modid="slavicmyths")
public final class LamyaDroughtEffect extends MobEffect {
 public LamyaDroughtEffect(){super(MobEffectCategory.HARMFUL,0xc58c42);}
 public static int stage(LivingEntity e){var f=e.getEffect(ModEffects.LAMYA_DROUGHT);return f==null?0:Math.clamp(f.getAmplifier()+1,1,3);}
 public static float fireMultiplier(LivingEntity e){return 1+.15F*stage(e);}
 public static void apply(LivingEntity e){
  if(e.level().isClientSide||!e.isAlive()||e instanceof LamyaEntity||e instanceof Player p&&(p.isCreative()||p.isSpectator()))return;
  if(e.isInWater()){e.removeEffect(ModEffects.LAMYA_DROUGHT);return;}
  var old=e.getEffect(ModEffects.LAMYA_DROUGHT);int amp=old==null?0:Math.min(2,old.getAmplifier()+1);
  e.addEffect(new MobEffectInstance(ModEffects.LAMYA_DROUGHT,100,amp,false,true,true));
 }
 @SubscribeEvent public static void heal(LivingHealEvent event){
  if(!event.getEntity().level().isClientSide)event.setAmount(event.getAmount()*(1-.20F*stage(event.getEntity())));
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Post event){
  if(event.getEntity() instanceof LivingEntity e&&!e.level().isClientSide&&e.isInWater()&&e.hasEffect(ModEffects.LAMYA_DROUGHT))e.removeEffect(ModEffects.LAMYA_DROUGHT);
 }
}
