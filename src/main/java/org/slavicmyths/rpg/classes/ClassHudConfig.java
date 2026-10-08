package org.slavicmyths.rpg.classes;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Contains no Minecraft client types; safe to register from the common mod constructor. */
public final class ClassHudConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW, BERSERKER_SCREEN_EFFECT;
    public static final ModConfigSpec.DoubleValue BERSERKER_INTENSITY;
    static {var b=new ModConfigSpec.Builder();SHOW=b.comment("Show the class ability HUD. Does not affect abilities or key mappings.").translation("classes.slavicmyths.show_hud").define("showSkillHud",true);BERSERKER_SCREEN_EFFECT=b.define("berserker_screen_effect",true);BERSERKER_INTENSITY=b.defineInRange("berserker_screen_intensity",.65,0,1);SPEC=b.build();}
    private ClassHudConfig() { }
}
