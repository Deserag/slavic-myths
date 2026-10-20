package org.slavicmyths.bogatyr;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Client placement only; no Minecraft client classes, safe common registration. */
public final class HeroHudConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue X, Y;
    static {
        var builder = new ModConfigSpec.Builder();
        X = builder.comment("Horizontal GUI-pixel offset for the companion HUD. Clamped to screen.")
                .defineInRange("horizontalOffset", 0, -4096, 4096);
        Y = builder.comment("Vertical GUI-pixel offset, within the reserved HUD area. Useful for custom minimaps.")
                .defineInRange("verticalOffset", 0, -4096, 4096);
        SPEC = builder.build();
    }
    private HeroHudConfig() { }
}
