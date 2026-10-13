package org.slavicmyths.finist;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.effect.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class FinistEffects {
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void multiplier(LivingIncomingDamageEvent e){if(e.getAmount()>0&&e.getSource().getDirectEntity() instanceof FinistArrow a&&a.getPersistentData().getBoolean("FinistEnhanced")&&!a.getPersistentData().getBoolean("FinistApplied")){a.getPersistentData().putBoolean("FinistApplied",true);e.setAmount(e.getAmount()*1.5F);}}
 @SubscribeEvent public static void landed(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||!(e.getSource().getDirectEntity() instanceof AbstractArrow a)||!a.getPersistentData().getBoolean("FinistPair"))return;var weapon=a.getWeaponItem();if(weapon==null||!weapon.is(FinistItems.BOW.get()))return;FinistMarks.hit(p,e.getEntity(),a.getPersistentData().getBoolean("FinistManual"));if(!(a instanceof FinistArrow)||!a.getPersistentData().getBoolean("FinistEnhanced")||!GreyPackRing.enemy(p,e.getEntity()))return;
  var target=e.getEntity();boolean inverted=target.isInvertedHealAndHarm()||target.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","finist_inverted_heal_harm")));boolean immune=target.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","finist_immune_to_effect")));
  // Never apply a beneficial effect. The inverted policy uses a hostile instant-health-type source.
  if(!immune){if(inverted){int window=target.invulnerableTime;target.invulnerableTime=0;try{target.hurt(p.damageSources().indirectMagic(p,p),2);}finally{target.invulnerableTime=Math.max(window,target.invulnerableTime);}}else target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0),p);}
  p.serverLevel().playSound(null,target.blockPosition(),FinistSounds.get(inverted?"finist_inverted_impact":"finist_magic_impact"),net.minecraft.sounds.SoundSource.PLAYERS,.45F,inverted?.9F:1);p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,target.getX(),target.getY()+.8,target.getZ(),3,.1,.1,.1,0);
 }
 private FinistEffects(){}
}
