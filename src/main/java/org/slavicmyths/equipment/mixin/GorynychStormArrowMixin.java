package org.slavicmyths.equipment.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.slavicmyths.gorynych.GorynychEquipment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class GorynychStormArrowMixin {
    @Inject(method="onHitEntity",at=@At("RETURN"))
    private void slavicmyths$stormAfterArrow(EntityHitResult hit,CallbackInfo ci){
        GorynychEquipment.afterArrowHit((AbstractArrow)(Object)this,hit.getLocation());
    }
}
