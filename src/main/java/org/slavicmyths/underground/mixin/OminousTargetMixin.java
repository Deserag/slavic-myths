package org.slavicmyths.underground.mixin;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(TargetingConditions.class)
public abstract class OminousTargetMixin {
 @Shadow @Final private boolean isCombat;
 @Inject(method="test",at=@At("HEAD"),cancellable=true)
 private void slavicmyths$ominous(LivingEntity attacker,LivingEntity target,CallbackInfoReturnable<Boolean> cir){if(isCombat&&attacker instanceof Mob mob&&target instanceof Player p&&org.slavicmyths.underground.OminousAura.ignores(mob,p))cir.setReturnValue(false);}
}
