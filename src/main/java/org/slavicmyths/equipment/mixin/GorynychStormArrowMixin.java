package org.slavicmyths.equipment.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.slavicmyths.gorynych.StormCrossbow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class GorynychStormArrowMixin {
    @Inject(method="onHitEntity",at=@At("RETURN"))
    private void slavicmyths$stormAfterArrow(EntityHitResult hit,CallbackInfo ci){
        StormCrossbow.afterHit((AbstractArrow)(Object)this,hit.getLocation());
    }
}
