package org.slavicmyths.item;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class FolkEquipmentEffects {
 private static final net.minecraft.resources.ResourceLocation SPEED=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","e058a1ec-73b8-4309-92d7-e917d4fdbf52");
 public static boolean wears(LivingEntity p,net.minecraft.world.item.Item item){return !FolkAccessoryItem.equipped(p,item).isEmpty();}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){
  Player p=event.getEntity();if(p.level().isClientSide)return;
  CompoundTag data=p.getPersistentData();AttributeInstance speed=p.getAttribute(Attributes.MOVEMENT_SPEED);
  boolean boots=p.getItemBySlot(EquipmentSlot.FEET).getItem()==ModItems.SEVEN_LEAGUE_BOOTS.get();
  int ramp=data.getInt("SlavicBootRamp");
  if(boots&&p.isSprinting()&&p.onGround()&&!p.isInWater()&&Math.abs(Mth.wrapDegrees(p.getYRot()-data.getFloat("SlavicBootYaw")))<18&&p.getDeltaMovement().lengthSqr()>.006)ramp=Math.min(60,ramp+1);else ramp=0;
  if(ramp!=data.getInt("SlavicBootRamp")){
   speed.removeModifier(SPEED);if(ramp>0)speed.addTransientModifier(new AttributeModifier(SPEED,.3*ramp/60,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
   data.putInt("SlavicBootRamp",ramp);
  }else if(!boots&&speed.getModifier(SPEED)!=null)speed.removeModifier(SPEED);
  if(boots)data.putFloat("SlavicBootYaw",p.getYRot());
  if(ramp>30&&p.tickCount%8==0)((ServerLevel)p.level()).sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.1,p.getZ(),1,.08,.02,.08,.005);
 }
 @SubscribeEvent public static void hurt(LivingIncomingDamageEvent event){
  LivingEntity victim=event.getEntity();if(victim.level().isClientSide)return;
  if(event.getSource().is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)&&wears(victim,ModItems.PERUN_RING.get()))event.setAmount(event.getAmount()*.65F);
  Entity source=event.getSource().getEntity();
  if(source instanceof LivingEntity&&source!=victim){
   LivingEntity attacker=(LivingEntity)source;
   if(event.getSource().getDirectEntity()==source&&wears(attacker,ModItems.PERUN_RING.get())){
    long now=attacker.level().getGameTime();CompoundTag d=attacker.getPersistentData();
    if(now>=d.getLong("SlavicPerunReady")){event.setAmount(event.getAmount()+2);d.putLong("SlavicPerunReady",now+60);((ServerLevel)victim.level()).sendParticles(ParticleTypes.CRIT,victim.getX(),victim.getY()+1,victim.getZ(),6,.2,.3,.2,.1);}
   }
   if(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)&&victim instanceof org.slavicmyths.entity.WildlifeEntity&&wears(attacker,ModItems.HUNTER_BELT.get()))event.setAmount(event.getAmount()*1.1F);
   attacker.getPersistentData().putLong("SlavicCapBroken",attacker.level().getGameTime()+100);
  }
  victim.getPersistentData().putLong("SlavicCapBroken",victim.level().getGameTime()+100);
 }
 @SubscribeEvent public static void death(LivingDeathEvent event){
  LivingEntity e=event.getEntity();if(e.level().isClientSide||event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD))return;
  ItemStack charm=FolkAccessoryItem.equipped(e,ModItems.RETRIBUTION_CHARM.get());if(charm.isEmpty())return;
  if(!(e instanceof Player)||event.isCanceled())return;
  charm.shrink(1);event.setCanceled(true);e.setHealth(1);e.invulnerableTime=40;e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,1));
  ServerLevel world=(ServerLevel)e.level();
  net.minecraft.core.particles.DustParticleOptions red=new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.35F,.025F,.035F),1);
  world.sendParticles(red,e.getX(),e.getY()+1,e.getZ(),14,.3,.4,.3,0);
  world.playSound(null,e.blockPosition(),net.minecraft.sounds.SoundEvents.SHIELD_BREAK,net.minecraft.sounds.SoundSource.PLAYERS,.8F,.65F);
  Entity enemy=event.getSource().getEntity();
  if(!"slavic_retribution".equals(event.getSource().getMsgId())&&enemy instanceof LivingEntity&&enemy!=e&&enemy.isAlive()&&!e.isAlliedTo(enemy)){
   LivingEntity target=(LivingEntity)enemy;
   // Ordinary attributed damage: armor, resistance and boss-specific hurt rules remain in force.
   target.hurt(org.slavicmyths.combat.MythDamageSources.caused("slavic_retribution",e),50);
   net.minecraft.world.phys.Vec3 delta=target.position().subtract(e.position());
   for(int i=1;i<=6;i++){double f=i/7.0;world.sendParticles(red,e.getX()+delta.x*f,e.getY()+1+delta.y*f,e.getZ()+delta.z*f,1,.025,.025,.025,0);}
  }
  org.slavicmyths.progression.Knowledge.award((Player)e,"not_today");
 }
 private FolkEquipmentEffects(){}
}
