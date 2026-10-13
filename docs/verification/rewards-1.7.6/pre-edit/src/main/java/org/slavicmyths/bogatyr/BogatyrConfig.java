package org.slavicmyths.bogatyr;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class BogatyrConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue DEBUG,HUD,HINTS;
    public static final ModConfigSpec.IntValue PARTICLES;
    static {var b=new ModConfigSpec.Builder();DEBUG=b.define("operatorDebugCommands",true);HUD=b.define("companionHud",true);HINTS=b.define("combatHints",true);PARTICLES=b.defineInRange("particlesPerEvent",40,14,120);SPEC=b.build();}
    private BogatyrConfig(){}
}
