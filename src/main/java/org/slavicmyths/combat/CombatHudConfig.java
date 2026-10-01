package org.slavicmyths.combat;
import net.minecraftforge.common.ForgeConfigSpec;
public final class CombatHudConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue X,Y;
    static {ForgeConfigSpec.Builder b=new ForgeConfigSpec.Builder();ENABLED=b.define("combatHud.enabled",true);X=b.defineInRange("combatHud.x",8,0,4096);Y=b.defineInRange("combatHud.y",8,0,4096);SPEC=b.build();}
}
