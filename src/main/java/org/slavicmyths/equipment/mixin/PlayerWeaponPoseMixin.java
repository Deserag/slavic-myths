package org.slavicmyths.equipment.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client list only: vanilla has already posed arms and copied sleeves here. */
@Mixin(PlayerModel.class)
public abstract class PlayerWeaponPoseMixin {
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("RETURN"))
 private void slavicmyths$weaponPose(LivingEntity entity,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci){
  org.slavicmyths.client.PlayerWeaponPose.apply((PlayerModel<?>)(Object)this,entity,age);
 }
}
