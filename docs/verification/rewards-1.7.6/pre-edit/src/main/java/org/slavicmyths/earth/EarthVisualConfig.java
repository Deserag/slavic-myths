package org.slavicmyths.earth;
import net.neoforged.neoforge.common.ModConfigSpec;
/** No Minecraft client types; registration is safe from the common constructor. */
public final class EarthVisualConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHAKE;
    static {var b=new ModConfigSpec.Builder();SHAKE=b.comment("Small local Earth Spirit camera shake; false disables it completely.").define("bossCameraShake",true);SPEC=b.build();}
    private EarthVisualConfig(){}
}
