package org.slavicmyths.hunt;
import java.util.UUID;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.item.FolkEquipmentEffects;
import org.slavicmyths.registry.ModItems;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class HuntEffects {
 public static final UUID BELT=UUID.fromString("80a75c68-a23d-4c29-ac8e-e90ee3f6dd43");
 public static void removeBelt(net.minecraft.entity.LivingEntity e){ModifiableAttributeInstance a=e.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(BELT);}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){PlayerEntity p=event.player;if(event.phase!=TickEvent.Phase.END||p.level.isClientSide)return;
  int fire=p.getRemainingFireTicks(),previous=p.getPersistentData().getInt("HuntLastFire");if(fire>previous+1&&FolkEquipmentEffects.wears(p,ModItems.ZOLNY_OBEREG.get())){fire=(int)Math.ceil(fire*.6);p.setRemainingFireTicks(fire);}p.getPersistentData().putInt("HuntLastFire",fire);
  if(p.tickCount%20==0){ModifiableAttributeInstance a=p.getAttribute(Attributes.MOVEMENT_SPEED);double value=FolkEquipmentEffects.wears(p,ModItems.VOLCHIY_POYAS.get())?HuntRules.belt(!p.level.isDay(),p.level.getMoonPhase()==0):0;AttributeModifier old=a.getModifier(BELT);if(old!=null&&(value==0||Math.abs(old.getAmount()-value)>.00001))a.removeModifier(BELT);if(value>0&&a.getModifier(BELT)==null)a.addTransientModifier(new AttributeModifier(BELT,"Wolf belt night stride",value,AttributeModifier.Operation.MULTIPLY_TOTAL));}
 }
 @SubscribeEvent public static void fire(LivingHurtEvent event){if(event.getEntityLiving().level.isClientSide||!event.getSource().isFire()||!FolkEquipmentEffects.wears(event.getEntityLiving(),ModItems.ZOLNY_OBEREG.get()))return;event.setAmount(event.getAmount()*.8F);net.minecraft.entity.LivingEntity e=event.getEntityLiving();long now=e.level.getGameTime();if(now>=e.getPersistentData().getLong("HuntAshFeedback")){e.getPersistentData().putLong("HuntAshFeedback",now+20);((ServerWorld)e.level).sendParticles(ParticleTypes.ASH,e.getX(),e.getY()+.5,e.getZ(),2,.15,.2,.15,.01);e.playSound(SoundEvents.FIRE_AMBIENT,.15F,.6F);}}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void silver(LivingDamageEvent event){if(!event.getEntityLiving().level.isClientSide&&event.getEntityLiving() instanceof org.slavicmyths.entity.VolkolakEntity&&org.slavicmyths.item.SilverCombat.silverStrike(event.getSource()))event.setAmount(event.getAmount()*1.25F);}
}
