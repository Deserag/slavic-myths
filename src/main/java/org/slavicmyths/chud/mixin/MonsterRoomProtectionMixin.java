package org.slavicmyths.chud.mixin;

import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.chud.ChudFeatureProtection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MonsterRoomFeature.class)
public abstract class MonsterRoomProtectionMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void slavicmyths$protect(FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> result) {
        var p=context.origin();
        if (ChudFeatureProtection.intersects(context.level(), new BoundingBox(p.getX()-4,p.getY()-1,p.getZ()-4,p.getX()+4,p.getY()+4,p.getZ()+4))) result.setReturnValue(false);
    }
}
