package org.slavicmyths.chud;
import net.neoforged.neoforge.common.ModConfigSpec;
/** New starts only; saved extensions are never rerolled after configuration changes. */
public final class AbandonedConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue ATTACHED_CHANCE;
    static {var b=new ModConfigSpec.Builder();ATTACHED_CHANCE=b.comment("Eligibility of abandoned extensions in newly generated Chud cities; existing cities stay sealed.").defineInRange("attachedChance",.35,0,1);SPEC=b.build();}
    private AbandonedConfig(){}
}
