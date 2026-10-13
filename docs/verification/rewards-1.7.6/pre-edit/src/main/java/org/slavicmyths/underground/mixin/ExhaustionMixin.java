package org.slavicmyths.underground.mixin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Player.class)
public abstract class ExhaustionMixin {
 @ModifyVariable(method="causeFoodExhaustion",at=@At("HEAD"),argsOnly=true)
 private float slavicmyths$aura(float amount){return org.slavicmyths.underground.OminousAura.exhaustion((Player)(Object)this,amount);}
}
