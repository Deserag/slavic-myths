package org.slavicmyths.morozko;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Server-owned initial balance. Fixed 5%, clone HP and reward probabilities are not exposed. */
public final class FrostConfig {
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.DoubleValue HEALTH;
 static{var b=new ModConfigSpec.Builder();HEALTH=b.comment("Morozko maximum HP; trial ends at 5%.").defineInRange("maxHealth",1400d,100d,10000d);SPEC=b.build();}
 public static float health(){return HEALTH.get().floatValue();}
 private FrostConfig(){}
}
