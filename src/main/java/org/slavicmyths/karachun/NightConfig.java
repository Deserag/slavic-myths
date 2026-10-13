package org.slavicmyths.karachun;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.*;
/** Server balance proposals; qualitative limits (5%, two allies, three blades) stay fixed. */
public final class NightConfig {
 public static final ModConfigSpec SPEC;
 private static final Map<String,ModConfigSpec.DoubleValue> VALUES=new LinkedHashMap<>();
 static {var b=new ModConfigSpec.Builder();
  for(var e:Map.ofEntries(Map.entry("health",1200d),Map.entry("arena",40d),Map.entry("aura",10d),Map.entry("nearGain",8d),Map.entry("midGain",5d),Map.entry("farGain",2d),Map.entry("decay",6d),Map.entry("fearDecay",8d),Map.entry("ravenHealth",20d),Map.entry("ravenDamage",3d),Map.entry("ravenCooldown",100d),Map.entry("resummonCooldown",700d),Map.entry("nightDuration",300d),Map.entry("nightCooldown",700d),Map.entry("hourWindup",120d),Map.entry("hourCooldown",800d),Map.entry("hungerCooldown",260d),Map.entry("frostCooldown",300d),Map.entry("deathCooldown",360d),Map.entry("sweepCooldown",240d),Map.entry("fearCooldown",440d),Map.entry("scytheChance",.15d),Map.entry("wingDuration",400d),Map.entry("wingCooldown",3000d),Map.entry("baseDamage",12d),Map.entry("lifeHeal",2d),Map.entry("lifeCooldown",100d),Map.entry("soulWindow",160d),Map.entry("breathRadius",4d),Map.entry("breathCooldown",360d),Map.entry("harvestCooldown",440d),Map.entry("undeadBonus",.25d),Map.entry("undeadHeal",3d),Map.entry("runeChance",.2d),Map.entry("runeGain",25d)).entrySet())VALUES.put(e.getKey(),b.defineInRange(e.getKey(),e.getValue(),0d,e.getKey().contains("Chance")||e.getKey().equals("undeadBonus")?1d:10000d));
  SPEC=b.build();}
 public static double value(String s){return VALUES.get(s).get();}public static int ticks(String s){return Math.max(1,(int)value(s));}
 private NightConfig(){}
}
