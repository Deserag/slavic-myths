package org.slavicmyths.savanna;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slavicmyths.registry.ModEffects;
@EventBusSubscriber(modid="slavicmyths")
public final class SkarapeyaPoison {
 private static final ResourceLocation MOVE=ResourceLocation.fromNamespaceAndPath("slavicmyths","skarapeya_stone_move"), SPEED=ResourceLocation.fromNamespaceAndPath("slavicmyths","skarapeya_stone_attack");
 public static final class Stone extends MobEffect {public Stone(){super(MobEffectCategory.HARMFUL,0xA0A9A8);}}
 public static final class BlackBlood extends MobEffect {
  public BlackBlood(){super(MobEffectCategory.HARMFUL,0xA52E38);}
  @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return duration%40==0;}
  @Override public boolean applyEffectTick(LivingEntity x,int amplifier){if(valid(x))x.hurt(x.damageSources().magic(),2);return true;}
 }
 static boolean valid(LivingEntity x){return x.isAlive()&&!(x instanceof Skarapeya)&&(!(x instanceof Player p)||!p.isCreative()&&!p.isSpectator());}
 public static void apply(LivingEntity x,int p){
  if(x.level().isClientSide||!valid(x))return;
  switch(p){
   case 0->x.addEffect(new MobEffectInstance(MobEffects.POISON,160,1));
   case 1->x.addEffect(new MobEffectInstance(MobEffects.WITHER,160,0));
   case 2->x.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,2));
   case 3->x.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,200,1));
   case 4->{x.addEffect(new MobEffectInstance(MobEffects.CONFUSION,100,0));x.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100,0));}
   case 5->{if(!x.hasEffect(ModEffects.SKARAPEYA_STONE))x.addEffect(new MobEffectInstance(ModEffects.SKARAPEYA_STONE,160,0));}
   case 6->{if(!x.hasEffect(ModEffects.SKARAPEYA_BLOOD))x.addEffect(new MobEffectInstance(ModEffects.SKARAPEYA_BLOOD,240,0));}
  }
 }
 private static void modifier(AttributeInstance a,ResourceLocation id,double amount){if(a==null)return;a.removeModifier(id);if(amount!=0)a.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
 @SubscribeEvent public static void tick(EntityTickEvent.Post e){
  if(!(e.getEntity() instanceof LivingEntity x)||x.level().isClientSide)return;
  var effect=x.getEffect(ModEffects.SKARAPEYA_STONE);var move=x.getAttribute(Attributes.MOVEMENT_SPEED);var speed=x.getAttribute(Attributes.ATTACK_SPEED);
  if(effect==null){if(move!=null&&move.hasModifier(MOVE))move.removeModifier(MOVE);if(speed!=null&&speed.hasModifier(SPEED))speed.removeModifier(SPEED);return;}
  if(x.tickCount%5!=0)return;double p=Math.clamp((160-effect.getDuration())/100D,0,1);modifier(move,MOVE,-.60*p);modifier(speed,SPEED,-.40*p);
 }
 @SubscribeEvent public static void heal(LivingHealEvent e){if(!e.getEntity().level().isClientSide&&e.getEntity().hasEffect(ModEffects.SKARAPEYA_BLOOD))e.setAmount(e.getAmount()*.5F);}
 private SkarapeyaPoison(){}
}
