package org.slavicmyths.combat.mixin;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="net.minecraft.world.effect.PoisonMobEffect",remap=false)
public abstract class PoisonPhysiologyMixin {
 @Inject(method="applyEffectTick",at=@At("HEAD"),cancellable=true)
 private void slavicPhysiology(LivingEntity target,int amplifier,CallbackInfoReturnable<Boolean> callback){
  if(!org.slavicmyths.combat.CreatureProperties.isPhysiologicalPoisonTarget(target))callback.setReturnValue(false);
 }
}
