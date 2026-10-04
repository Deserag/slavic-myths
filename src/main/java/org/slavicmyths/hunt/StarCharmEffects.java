package org.slavicmyths.hunt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.item.*;
import org.slavicmyths.registry.ModItems;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class StarCharmEffects {
 private static final String READY="HuntStarReady";
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){PlayerEntity p=event.player;if(event.phase!=TickEvent.Phase.END||p.level.isClientSide||!p.isAlive())return;ServerWorld w=p.getServer().getLevel(net.minecraft.world.World.OVERWORLD);long now=w.getGameTime();CompoundNBT n=p.getPersistentData();
  if(FolkEquipmentEffects.wears(p,ModItems.OBEREG_PADAYUSCHEY_ZVEZDY.get())&&now>=n.getLong(READY)&&!p.isOnGround()&&!p.isInWater()&&!p.abilities.flying&&!p.isPassenger()&&ElementRules.dangerousFall(p.fallDistance,p.getDeltaMovement().y,p.isFallFlying())){p.addEffect(new EffectInstance(Effects.SLOW_FALLING,100));p.fallDistance=0;n.putLong(READY,now+1200);((ServerWorld)p.level).sendParticles(ParticleTypes.FLAME,p.getX(),p.getY()+.5,p.getZ(),8,.3,.4,.3,.015);p.level.playSound(null,p.blockPosition(),SoundEvents.FIREWORK_ROCKET_TWINKLE,SoundCategory.PLAYERS,.6F,.6F);FolkAccessoryItem.award(p,"falling_star");}
 }
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){e.getPlayer().getPersistentData().putLong(READY,e.getOriginal().getPersistentData().getLong(READY));}
}
