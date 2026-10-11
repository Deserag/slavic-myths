package org.slavicmyths.volkolak;

import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import org.slavicmyths.registry.ModEffects;

/** No periodic damage. Native healing and the actual, capped food gains are reduced once. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BadBlood extends MobEffect {
 private static final ThreadLocal<Integer> INSTANT=ThreadLocal.withInitial(()->0);
 public BadBlood(){super(MobEffectCategory.HARMFUL,0x542d39);}
 public static void apply(LivingEntity target,LivingEntity source){target.addEffect(new MobEffectInstance(ModEffects.BAD_BLOOD,500,0,false,true,true),source);}
 public static void instantHeal(LivingEntity target,float amount){int old=INSTANT.get();INSTANT.set(old+1);try{target.heal(amount);}finally{if(old==0)INSTANT.remove();else INSTANT.set(old);}}
 @SubscribeEvent public static void heal(LivingHealEvent event){if(INSTANT.get()==0&&event.getEntity().hasEffect(ModEffects.BAD_BLOOD)&&event.getAmount()>0)event.setAmount(event.getAmount()*.5F);}
 public static void food(Player player,FoodData food,FoodProperties properties){int before=food.getFoodLevel();float saturation=food.getSaturationLevel();food.eat(properties);if(player.level().isClientSide||!player.hasEffect(ModEffects.BAD_BLOOD)){player.getPersistentData().remove("VolkolakFoodHalf");return;}
  // Half-point remainder permits two one-point foods to restore one point, including native cap clipping.
  int gain=Math.max(0,food.getFoodLevel()-before),half=gain+Math.clamp(player.getPersistentData().getInt("VolkolakFoodHalf"),0,1);
  int after=Math.min(20,before+half/2);player.getPersistentData().putInt("VolkolakFoodHalf",half%2);float gainSat=Math.max(0,food.getSaturationLevel()-saturation);
  food.setFoodLevel(after);food.setSaturation(Math.min(after,saturation+gainSat*.5F));
 }
 private static void clear(LivingEntity e){e.getPersistentData().remove("VolkolakFoodHalf");}
 @SubscribeEvent public static void expired(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired e){if(e.getEffectInstance().is(ModEffects.BAD_BLOOD))clear(e.getEntity());}
 @SubscribeEvent public static void removed(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove e){if(e.getEffect()!=null&&e.getEffect().equals(ModEffects.BAD_BLOOD))clear(e.getEntity());}
}
