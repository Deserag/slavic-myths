package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.registry.ModEffects;
import org.slavicmyths.combat.CutEffect;
import org.slavicmyths.combat.RareBleedEffect;
import java.util.*;

@EventBusSubscriber(modid="slavicmyths")
public final class MarineAccessoryAbilities {
 private record Water(ServerLevel level, BlockPos pos, long until) {}
 private static final List<Water> WATER=new ArrayList<>();
 private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
 private static boolean wears(ServerPlayer p,String id){return p.isAlive()&&!p.isSpectator()&&MarineAccessories.wears(p,id);}
 private static boolean ready(ServerPlayer p,String key){return now(p)>=p.getPersistentData().getLong("MarineAccessory_"+key);}
 private static void cooldown(ServerPlayer p,String key,int ticks){p.getPersistentData().putLong("MarineAccessory_"+key,now(p)+ticks);}
 private static void splash(ServerPlayer p){p.serverLevel().sendParticles(ParticleTypes.SPLASH,p.getX(),p.getY()+.2,p.getZ(),30,.6,.3,.6,.1);}
 @SubscribeEvent public static void cleanup(ServerTickEvent.Post event){
  WATER.removeIf(w->{if(w.level.getServer()!=event.getServer())return true;if(w.level.getGameTime()<w.until)return false;if(w.level.getBlockState(w.pos).is(Blocks.WATER))w.level.setBlockAndUpdate(w.pos,Blocks.AIR.defaultBlockState());return true;});
 }
 @SubscribeEvent public static void fall(LivingFallEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p)||event.getDistance()<=3||!wears(p,"tide_ring")||!ready(p,"tide"))return;
  event.setDamageMultiplier(0);cooldown(p,"tide",2400);splash(p);
  BlockPos pos=p.blockPosition();
  if(!p.level().dimensionType().ultraWarm()&&p.level().getBlockState(pos).isAir()&&p.mayUseItemAt(pos,net.minecraft.core.Direction.UP,p.getMainHandItem())&&p.level().mayInteract(p,pos)){
   p.level().setBlockAndUpdate(pos,Blocks.WATER.defaultBlockState());WATER.add(new Water(p.serverLevel(),pos.immutable(),p.level().getGameTime()+2));
  }
 }
 private static boolean bleed(net.minecraft.core.Holder<MobEffect> effect){return effect.is(ModEffects.CUT.getKey())||effect.is(ModEffects.RARE_BLEED.getKey())||effect.is(ModEffects.WOLF_CUT.getKey());}
 @SubscribeEvent public static void immune(MobEffectEvent.Applicable event){if(event.getEntity() instanceof ServerPlayer p&&wears(p,"tsar_pike_belt")&&bleed(event.getEffectInstance().getEffect()))event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;
  if(wears(p,"tsar_pike_belt")){p.removeEffect(ModEffects.CUT);p.removeEffect(ModEffects.RARE_BLEED);p.removeEffect(ModEffects.WOLF_CUT);}
  if(wears(p,"abyss_necklace")&&p.isInWaterOrBubble()&&p.tickCount%2==0)p.setAirSupply(Math.min(p.getMaxAirSupply(),p.getAirSupply()+1));
 }
 @SubscribeEvent public static void defend(LivingDamageEvent.Pre event){
  if(!(event.getEntity() instanceof ServerPlayer p)||event.getNewDamage()<=0||event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
  if(wears(p,"zhelva_amulet")){
   if(event.getNewDamage()>=6&&ready(p,"shell")){cooldown(p,"shell",600);p.getPersistentData().putLong("MarineAccessory_shellUntil",now(p)+80);p.getPersistentData().putFloat("MarineAccessory_shellPool",8);}
   if(p.getPersistentData().getLong("MarineAccessory_shellUntil")>now(p)){
    float pool=p.getPersistentData().getFloat("MarineAccessory_shellPool"),saved=Math.min(pool,event.getNewDamage());event.setNewDamage(event.getNewDamage()-saved);p.getPersistentData().putFloat("MarineAccessory_shellPool",pool-saved);
   }
  }
 }
 @SubscribeEvent public static void hit(LivingDamageEvent.Post event){
  if(event.getNewDamage()<=0)return;
  if(event.getEntity() instanceof ServerPlayer victim&&wears(victim,"abyss_necklace")&&event.getNewDamage()>=4&&ready(victim,"wave")){
   cooldown(victim,"wave",400);splash(victim);
   for(var target:victim.serverLevel().getEntitiesOfClass(LivingEntity.class,victim.getBoundingBox().inflate(4),e->e!=victim&&e.isAlive()&&!e.isAlliedTo(victim)&&(!(e instanceof net.minecraft.world.entity.player.Player other)||victim.canHarmPlayer(other)&&victim.server.isPvpAllowed()))){
    target.knockback(1.2,victim.getX()-target.getX(),victim.getZ()-target.getZ());target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0));
   }
  }
  if(!(event.getSource().getEntity() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator())return;
  if(p.getPersistentData().getBoolean("MarineAccessory_lightningHit"))return;
  boolean attack=event.getSource().is(DamageTypes.PLAYER_ATTACK)&&event.getSource().getDirectEntity()==p||event.getSource().getDirectEntity() instanceof AbstractArrow;
  var target=event.getEntity();if(!attack||target==p||target.isAlliedTo(p)||target instanceof net.minecraft.world.entity.player.Player other&&(!p.server.isPvpAllowed()||!p.canHarmPlayer(other)))return;
  if(wears(p,"tsar_pike_belt")&&ready(p,"bleed")&&p.getRandom().nextFloat()<.2F&&CutEffect.permitted(p,target)){cooldown(p,"bleed",40);RareBleedEffect.applyNativeLevel(p,target,1);}
  if(wears(p,"storm_bracelet")&&ready(p,"storm")&&p.getRandom().nextFloat()<.3F){
   cooldown(p,"storm",160);
   var bolt=EntityType.LIGHTNING_BOLT.create(p.serverLevel());if(bolt!=null){bolt.moveTo(target.position());bolt.setVisualOnly(true);bolt.setCause(p);p.serverLevel().addFreshEntity(bolt);}
   int immunity=target.invulnerableTime;
   p.getPersistentData().putBoolean("MarineAccessory_lightningHit",true);
   try {target.invulnerableTime=0;target.hurt(p.damageSources().playerAttack(p),4);}
   finally {target.invulnerableTime=Math.max(immunity,target.invulnerableTime);p.getPersistentData().remove("MarineAccessory_lightningHit");}
  }
 }
}
