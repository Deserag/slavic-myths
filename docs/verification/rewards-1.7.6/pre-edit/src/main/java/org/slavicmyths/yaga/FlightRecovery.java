package org.slavicmyths.yaga;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import org.slavicmyths.registry.ModItems;
/** Compatibility extension: only a short repack/relaunch recovery; vehicle motion is unchanged. */
public final class FlightRecovery {
 public static boolean ready(Player p){return p.level().getGameTime()>=p.getPersistentData().getLong("YagaFlightRecovery");}
 public static void repack(Player p){int ticks=p.getPersistentData().getLong("YagaFlightSalve")>p.level().getGameTime()?30:60;set(p,ticks);}
 private static void set(Player p,int ticks){p.getPersistentData().putLong("YagaFlightRecovery",p.level().getGameTime()+ticks);p.getCooldowns().addCooldown(ModItems.FLYING_BROOM.get(),ticks);p.getCooldowns().addCooldown(ModItems.FLYING_MORTAR.get(),ticks);}
 public static void apply(Player p){CompoundTag n=p.getPersistentData();long now=p.level().getGameTime();n.putLong("YagaFlightSalve",now+6000);long remaining=n.getLong("YagaFlightRecovery")-now;if(remaining>0)set(p,Math.max(1,(int)((remaining+1)/2)));}
 private FlightRecovery(){}
}
