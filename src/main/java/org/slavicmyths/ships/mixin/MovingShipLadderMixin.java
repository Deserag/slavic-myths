package org.slavicmyths.ships.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.slavicmyths.ships.ShipDynamicCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep the vanilla ladder movement rules when its blocks live in a palette. */
@Mixin(LivingEntity.class)
public abstract class MovingShipLadderMixin {
    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void slavicmyths$movingShipLadder(CallbackInfoReturnable<Boolean> result) {
        if (!result.getReturnValue() && (Object)this instanceof Player player
                && ShipDynamicCollision.onMovingLadder(player)) result.setReturnValue(true);
    }
}
