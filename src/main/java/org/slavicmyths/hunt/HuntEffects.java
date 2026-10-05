package org.slavicmyths.hunt;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.item.FolkEquipmentEffects;
import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class HuntEffects {
 public static final net.minecraft.resources.ResourceLocation BELT=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","80a75c68-a23d-4c29-ac8e-e90ee3f6dd43");
 public static void removeBelt(net.minecraft.world.entity.LivingEntity e){AttributeInstance a=e.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(BELT);}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){Player p=event.getEntity();if(p.level().isClientSide)return;
  int fire=p.getRemainingFireTicks(),previous=p.getPersistentData().getInt("HuntLastFire");if(fire>previous+1&&FolkEquipmentEffects.wears(p,ModItems.ZOLNY_OBEREG.get())){fire=(int)Math.ceil(fire*.6);p.setRemainingFireTicks(fire);}p.getPersistentData().putInt("HuntLastFire",fire);
  if(p.tickCount%20==0){AttributeInstance a=p.getAttribute(Attributes.MOVEMENT_SPEED);double value=FolkEquipmentEffects.wears(p,ModItems.VOLCHIY_POYAS.get())?HuntRules.belt(!p.level().isDay(),p.level().getMoonPhase()==0):0;AttributeModifier old=a.getModifier(BELT);if(old!=null&&(value==0||Math.abs(old.amount()-value)>.00001))a.removeModifier(BELT);if(value>0&&a.getModifier(BELT)==null)a.addTransientModifier(new AttributeModifier(BELT,value,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
 }
 @SubscribeEvent public static void fire(LivingIncomingDamageEvent event){if(event.getEntity().level().isClientSide||!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)||!FolkEquipmentEffects.wears(event.getEntity(),ModItems.ZOLNY_OBEREG.get()))return;event.setAmount(event.getAmount()*.8F);net.minecraft.world.entity.LivingEntity e=event.getEntity();long now=e.level().getGameTime();if(now>=e.getPersistentData().getLong("HuntAshFeedback")){e.getPersistentData().putLong("HuntAshFeedback",now+20);((ServerLevel)e.level()).sendParticles(ParticleTypes.ASH,e.getX(),e.getY()+.5,e.getZ(),2,.15,.2,.15,.01);e.playSound(SoundEvents.FIRE_AMBIENT,.15F,.6F);}}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void silver(LivingDamageEvent.Pre event){if(!event.getEntity().level().isClientSide&&event.getEntity() instanceof org.slavicmyths.entity.VolkolakEntity&&org.slavicmyths.item.SilverCombat.silverStrike(event.getSource()))event.setNewDamage(event.getNewDamage()*1.25F);}
}
