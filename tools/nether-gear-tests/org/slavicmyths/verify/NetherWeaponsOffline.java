package org.slavicmyths.verify;
import org.slavicmyths.nethersmithing.NetherWeaponRules;
public final class NetherWeaponsOffline {
 public static void main(String[] args){int checks=0;
  // Native arrow rounding at different flight speeds; raw damage must not grow with speed or be rounded down.
  for(double speed=.01;speed<=4;speed+=.013)for(double raw:new double[]{1,3.3,10,13}){
   double base=NetherWeaponRules.bowBase(raw,speed);if(Math.ceil(base*speed)!=Math.ceil(raw))throw new AssertionError("arrow damage drift "+speed+" "+raw);checks++;
  }
  for(String n:NetherWeaponRules.IDS){if(NetherWeaponRules.cooldown(n)<200||NetherWeaponRules.durability(n)<1200||NetherWeaponRules.charge(n)<10)throw new AssertionError(n);checks++;}
  System.out.println("Nether weapon pure arrow-speed/rounding and declared bounds PASS "+checks+"; not projectile/gameplay test.");
 }
}
