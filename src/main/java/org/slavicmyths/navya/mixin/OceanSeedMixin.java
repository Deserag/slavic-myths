package org.slavicmyths.navya.mixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.level.levelgen.*;
import org.slavicmyths.burningislands.OceanTerrain;
@Mixin(RandomState.class)
public abstract class OceanSeedMixin implements OceanTerrain.SeedAccess {
 @Unique private long slavicmyths$seed;
 @Inject(method="<init>",at=@At("TAIL")) private void slavicmyths$seed(NoiseGeneratorSettings settings,net.minecraft.core.HolderGetter<net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters> noises,long seed,CallbackInfo ci){slavicmyths$seed=seed;}
 public long slavicmyths$seed(){return slavicmyths$seed;}
}
