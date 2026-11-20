package org.slavicmyths.village.mixin;

import net.minecraft.world.entity.npc.Villager;
import org.slavicmyths.buyan.BuyanTrades;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Island villager offers are generated per entity, without altering global trade pools. */
@Mixin(Villager.class)
public abstract class BuyanVillagerTradeMixin {
    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void slavicmyths$buyanCoins(CallbackInfo callback) {
        if (BuyanTrades.populate((Villager) (Object) this)) callback.cancel();
    }
}
