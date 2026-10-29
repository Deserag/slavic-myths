package org.slavicmyths.netherstructures.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla protects nether-brick fortresses, but did not know our crafted
 * blackstone floor palette. A neighbouring delta could turn it into lava. */
@Mixin(DeltaFeature.class)
public abstract class DeltaStructureProtectionMixin {
    @Inject(method="isClear",at=@At("HEAD"),cancellable=true)
    private static void slavicmyths$protectCraftedFloor(LevelAccessor world,BlockPos pos,
            DeltaFeatureConfiguration config,CallbackInfoReturnable<Boolean> result) {
        if(org.slavicmyths.netherstructures.NetherStructureProtection.protectedAt(world,pos))result.setReturnValue(false);
    }
}
