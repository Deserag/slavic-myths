package org.slavicmyths.yaga;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import org.slavicmyths.registry.ModItems;
/** Compatibility extension: only a short repack/relaunch recovery; vehicle motion is unchanged. */
public final class FlightRecovery {
 public static boolean ready(PlayerEntity p){return p.level.getGameTime()>=p.getPersistentData().getLong("YagaFlightRecovery");}
 public static void repack(PlayerEntity p){int ticks=p.getPersistentData().getLong("YagaFlightSalve")>p.level.getGameTime()?30:60;set(p,ticks);}
 private static void set(PlayerEntity p,int ticks){p.getPersistentData().putLong("YagaFlightRecovery",p.level.getGameTime()+ticks);p.getCooldowns().addCooldown(ModItems.FLYING_BROOM.get(),ticks);p.getCooldowns().addCooldown(ModItems.FLYING_MORTAR.get(),ticks);}
 public static void apply(PlayerEntity p){CompoundNBT n=p.getPersistentData();long now=p.level.getGameTime();n.putLong("YagaFlightSalve",now+6000);long remaining=n.getLong("YagaFlightRecovery")-now;if(remaining>0)set(p,Math.max(1,(int)((remaining+1)/2)));}
 private FlightRecovery(){}
}
