package org.slavicmyths.verify;
import org.slavicmyths.item.StormStaffItem;
/** Calls the actual production damage/cooldown functions; no world or entities. */
public final class Weapons87Balance {
 public static void main(String[] args) {
  int values=0;
  for(boolean power:new boolean[]{false,true})for(boolean rune:new boolean[]{false,true}) {
   float expected=6*(power?1.12F:1)*(rune?1.15F:1);float actual=StormStaffItem.pulseDamage(power,rune);
   if(Math.abs(actual-expected)>1e-6F)throw new AssertionError("ApprovedB pulse or existing multiplier changed");values++;
  }
  if(StormStaffItem.cooldownTicks(false)!=48||StormStaffItem.cooldownTicks(true)!=42)throw new AssertionError("Current production48/42 cooldown; historical60/54 test was stale");
  System.out.println("PASS:4 actual production pulse values6/6.72/6.9/7.728, cooldown48/42; no world/entity/game.");
 }
}
