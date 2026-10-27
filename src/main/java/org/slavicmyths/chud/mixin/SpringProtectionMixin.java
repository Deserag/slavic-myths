package org.slavicmyths.chud.mixin;

import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.chud.ChudFeatureProtection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpringFeature.class)
public abstract class SpringProtectionMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void slavicmyths$protect(FeaturePlaceContext<SpringConfiguration> context, CallbackInfoReturnable<Boolean> result) {
        var p=context.origin();
        if (ChudFeatureProtection.intersects(context.level(), new BoundingBox(p.getX()-1,p.getY()-1,p.getZ()-1,p.getX()+1,p.getY()+1,p.getZ()+1))) result.setReturnValue(false);
    }
}
