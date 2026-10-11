package org.slavicmyths.equipment.mixin;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(targets="net.minecraft.world.effect.HealOrHarmMobEffect")
public abstract class BadBloodInstantMixin {
 @Redirect(method={"applyEffectTick","applyInstantenousEffect"},at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
 private void slavicmyths$instant(LivingEntity target,float amount){org.slavicmyths.volkolak.BadBlood.instantHeal(target,amount);}
}
