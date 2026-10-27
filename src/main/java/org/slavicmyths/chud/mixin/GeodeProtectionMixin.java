package org.slavicmyths.chud.mixin;

import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.chud.ChudFeatureProtection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GeodeFeature.class)
public abstract class GeodeProtectionMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void slavicmyths$protect(FeaturePlaceContext<GeodeConfiguration> context, CallbackInfoReturnable<Boolean> result) {
        var p = context.origin();
        // Vanilla's inclusive generation cube, plus one neighbor for budding crystals.
        int lo = context.config().minGenOffset - 1, hi = context.config().maxGenOffset + 1;
        if (ChudFeatureProtection.intersects(context.level(), new BoundingBox(p.getX()+lo,p.getY()+lo,p.getZ()+lo,p.getX()+hi,p.getY()+hi,p.getZ()+hi))) result.setReturnValue(false);
    }
}
