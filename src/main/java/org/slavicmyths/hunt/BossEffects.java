package org.slavicmyths.hunt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.item.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BossEffects {
 public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.effect.MobEffect,net.minecraft.world.effect.MobEffect> ILL_FATE=org.slavicmyths.registry.ModEffects.ILL_FATE;
 public static void init(){}
 public static final ResourceLocation NAPOR=ResourceLocation.fromNamespaceAndPath("slavicmyths","6d5945a5-a47e-42da-bb13-fd812d2c649f");
 public static void clearBelt(LivingEntity p){AttributeInstance a=p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);if(a!=null)a.removeModifier(NAPOR);p.getPersistentData().remove("NaporTicks");p.getPersistentData().remove("NaporUntil");}
 public static boolean napor(Player p){return FolkEquipmentEffects.wears(p,ModItems.POYAS_TUGARINA.get())&&p.getPersistentData().getLong("NaporUntil")>p.level().getGameTime();}
 @SubscribeEvent public static void healing(LivingHealEvent e){if(e.getEntity().hasEffect(ILL_FATE))e.setAmount(e.getAmount()*.8F);}
 @SubscribeEvent public static void knock(LivingKnockBackEvent e){LivingEntity victim=e.getEntity();if(victim.level().isClientSide)return;if(victim.hasEffect(ILL_FATE))e.setStrength(e.getStrength()*1.15F);net.minecraft.nbt.CompoundTag n=victim.getPersistentData();if(n.getLong("NaporHitTick")==victim.level().getGameTime()&&n.hasUUID("NaporAttacker")){Player player=victim.level().getPlayerByUUID(n.getUUID("NaporAttacker"));if(player!=null&&napor(player))e.setStrength(e.getStrength()*1.15F);}}
 @SubscribeEvent public static void melee(LivingIncomingDamageEvent e){if(e.getEntity().level().isClientSide)return;if(e.getSource().getEntity() instanceof Player&&e.getSource().getEntity()==e.getSource().getDirectEntity()){Player p=(Player)e.getSource().getEntity();if(napor(p)){e.getEntity().getPersistentData().putLong("NaporHitTick",p.level().getGameTime());e.getEntity().getPersistentData().putUUID("NaporAttacker",p.getUUID());}}}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(e.getEntity().level().isClientSide)return;Player p=e.getEntity();net.minecraft.nbt.CompoundTag n=p.getPersistentData();boolean equipped=FolkEquipmentEffects.wears(p,ModItems.POYAS_TUGARINA.get());if(!p.isAlive()||!equipped||p.isSwimming()||p.isInWater()||p.isFallFlying()||p.getAbilities().flying){clearBelt(p);return;}long now=p.level().getGameTime();int ticks=p.isSprinting()?n.getInt("NaporTicks")+1:0;n.putInt("NaporTicks",Math.min(40,ticks));if(ticks>=40)n.putLong("NaporUntil",now+20);AttributeInstance a=p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);boolean active=n.getLong("NaporUntil")>now;if(active&&a.getModifier(NAPOR)==null){a.addTransientModifier(new AttributeModifier(NAPOR,.2,AttributeModifier.Operation.ADD_VALUE));p.level().playSound(null,p.blockPosition(),SoundEvents.IRON_GOLEM_STEP,SoundSource.PLAYERS,.6F,.7F);((ServerLevel)p.level()).sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.1,p.getZ(),4,.2,.04,.2,.02);FolkAccessoryItem.award(p,"full_speed_ahead");}if(!active)a.removeModifier(NAPOR);}
 @SubscribeEvent public static void eye(MobEffectEvent.Applicable e){LivingEntity target=e.getEntity();if(e.getResult()==MobEffectEvent.Applicable.Result.DO_NOT_APPLY||!(target instanceof ServerPlayer))return;ServerPlayer p=(ServerPlayer)target;MobEffectInstance incoming=e.getEffectInstance();net.minecraft.core.Holder<MobEffect> effect=incoming.getEffect();net.minecraft.nbt.CompoundTag n=p.getPersistentData();long now=p.server.getLevel(Level.OVERWORLD).getGameTime();if(n.getBoolean("EyeProcGuard")||now<n.getLong("EyeProcReady")||!FolkEquipmentEffects.wears(p,ModItems.ODNOGLAZYY_OBEREG.get())||effect.value().getCategory()!=MobEffectCategory.HARMFUL||effect.value().isInstantenous()||incoming.getDuration()<=1||incoming.getDuration()>72000||effect==MobEffects.BAD_OMEN||effect.equals(org.slavicmyths.kurgan.KurganCurse.CURSE)&&(incoming.getAmplifier()>0||org.slavicmyths.kurgan.KurganCurse.persistent(p))||p.getRandom().nextFloat()>=.2F)return;
  e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);n.putBoolean("EyeProcGuard",true);try{p.addEffect(new MobEffectInstance(effect,Math.max(1,incoming.getDuration()/2),incoming.getAmplifier(),incoming.isAmbient(),incoming.isVisible(),incoming.showIcon()));}finally{n.remove("EyeProcGuard");}n.putLong("EyeProcReady",now+600);p.level().playSound(null,p.blockPosition(),SoundEvents.ENDER_EYE_DEATH,SoundSource.PLAYERS,.35F,.65F);((ServerLevel)p.level()).sendParticles(ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),3,.2,.3,.2,.01);
 }
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){e.getEntity().getPersistentData().putLong("EyeProcReady",e.getOriginal().getPersistentData().getLong("EyeProcReady"));clearBelt(e.getEntity());}
}
