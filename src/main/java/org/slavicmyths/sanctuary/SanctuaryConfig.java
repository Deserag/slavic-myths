package org.slavicmyths.sanctuary;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class SanctuaryConfig {
 public static final ModConfigSpec SPEC;public static final ModConfigSpec.IntValue RARITY,SLOPE,WARD_SECONDS,TEAR_COOLDOWN;public static final ModConfigSpec.DoubleValue GUARD,FLAME_DAMAGE;
 static{var b=new ModConfigSpec.Builder();RARITY=b.comment("Deterministic extra rarity divisor over native spacing48/separation20; 1 keeps every accepted candidate. Native spacing is datapack configurable.").defineInRange("rarityDivisor",1,1,16);SLOPE=b.defineInRange("maximumSlope",7,0,12);WARD_SECONDS=b.defineInRange("wardSeconds",40,1,120);TEAR_COOLDOWN=b.defineInRange("tearCooldownSeconds",45,8,180);GUARD=b.defineInRange("damageAndMorDurationReduction",.3,0,.7);FLAME_DAMAGE=b.defineInRange("flameVialImpactDamage",3d,1d,10d);SPEC=b.build();}
 private SanctuaryConfig(){}
}
