package org.slavicmyths.client;
import org.slavicmyths.underground.EncounterRules;
import net.minecraft.world.damagesource.*;
/** Pure admission rules and formula audit against installed CombatRules source; no game bootstrap. */
public final class UndergroundRuleChecks {
 static int n;static void check(boolean b,String message){n++;if(!b)throw new AssertionError(message);}
 static float armor(float raw,float defense,float toughness){float f=2+toughness/4;float protectedArmor=Math.max(defense*.2F,Math.min(20,defense-raw/f));return raw*(1-protectedArmor/25);}
 public static int run(){float heavy=armor(org.slavicmyths.underground.EncounterRules.HEAVY_DAMAGE,20,8),rock=armor(org.slavicmyths.underground.EncounterRules.STONE_DAMAGE,15,0);
  check(heavy>=15&&heavy<=17,"diamond heavy "+heavy);check(rock>=2&&rock<=3,"iron rock "+rock);check(armor(org.slavicmyths.underground.EncounterRules.HEAVY_DAMAGE,20,12)<heavy,"netherite toughness remains useful");check(CombatRules.getDamageAfterMagicAbsorb(heavy,20)<heavy,"Protection reduces heavy");
  check(EncounterRules.protectedByRunes(50,2,false,false),"50 max HP boundary");check(!EncounterRules.protectedByRunes(50.01,2,false,false),"max HP rather than injured current HP");check(!EncounterRules.protectedByRunes(20,1,false,false),"one incomplete rune");check(!EncounterRules.protectedByRunes(20,2,true,false),"tagged exception");check(!EncounterRules.protectedByRunes(20,2,false,true),"retaliation exception");check(EncounterRules.protectedByRunes(20,3,false,false),"extra copies do not raise threshold");
  check(EncounterRules.inCone(1,3,false),"front inside");check(!EncounterRules.inCone(-1,2,false),"behind misses");check(!EncounterRules.inCone(0,2,false),"side dodges");check(!EncounterRules.inCone(1,3.2,false),"outside reach misses");check(!EncounterRules.inCone(1,2,true),"no heavy during knockdown");check(EncounterRules.exhaustion(.1F,true)==.3F,"3x actual exhaustion");check(EncounterRules.exhaustion(0,true)==0,"standing still no artificial hunger");check(EncounterRules.exhaustion(.1F,false)==.1F,"outside aura unchanged");
  System.out.println("RULE PASS: "+n+" assertions; installed armor formula audit: diamond heavy="+heavy+", iron rock="+rock+"; not an in-game damage test");return n;}
}
