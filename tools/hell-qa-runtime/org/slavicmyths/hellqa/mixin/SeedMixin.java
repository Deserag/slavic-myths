package org.slavicmyths.hellqa.mixin;
import net.minecraft.gametest.framework.GameTestServer;import net.minecraft.world.level.levelgen.WorldOptions;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Disposable fixture seed/structure options only, never packaged in Slavic Myths. */
@Mixin(GameTestServer.class)
public abstract class SeedMixin {
 @Shadow @Final @Mutable private static WorldOptions WORLD_OPTIONS;
 @Inject(method="<clinit>",at=@At("TAIL")) private static void seed(CallbackInfo ci){WORLD_OPTIONS=new WorldOptions(Long.getLong("hellqa.seed",0),true,false);}
}
