package org.slavicmyths.village.mixin;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=VillagerHostilesSensor.class,remap=false)
public abstract class BanditThreatMixin {
 @Inject(method="isMatchingEntity",at=@At("HEAD"),cancellable=true)
 private void bandits(LivingEntity v,LivingEntity enemy,CallbackInfoReturnable<Boolean> ci){if(enemy.getType().is(org.slavicmyths.military.Military.BANDITS))ci.setReturnValue(v.distanceToSqr(enemy)<=15*15);}
}
