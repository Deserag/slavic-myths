package org.slavicmyths.hunt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.item.*;
import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class StarCharmEffects {
 private static final String READY="HuntStarReady";
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){Player p=event.getEntity();if(p.level().isClientSide||!p.isAlive())return;ServerLevel w=p.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);long now=w.getGameTime();CompoundTag n=p.getPersistentData();
  if(FolkEquipmentEffects.wears(p,ModItems.OBEREG_PADAYUSCHEY_ZVEZDY.get())&&now>=n.getLong(READY)&&!p.onGround()&&!p.isInWater()&&!p.getAbilities().flying&&!p.isPassenger()&&ElementRules.dangerousFall(p.fallDistance,p.getDeltaMovement().y,p.isFallFlying())){p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,100));p.fallDistance=0;n.putLong(READY,now+1200);((ServerLevel)p.level()).sendParticles(ParticleTypes.FLAME,p.getX(),p.getY()+.5,p.getZ(),8,.3,.4,.3,.015);p.level().playSound(null,p.blockPosition(),SoundEvents.FIREWORK_ROCKET_TWINKLE,SoundSource.PLAYERS,.6F,.6F);FolkAccessoryItem.award(p,"falling_star");}
 }
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){e.getEntity().getPersistentData().putLong(READY,e.getOriginal().getPersistentData().getLong(READY));}
}
