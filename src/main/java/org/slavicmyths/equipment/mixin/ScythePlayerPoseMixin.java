package org.slavicmyths.equipment.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client-only pose adjustment; retains the existing player renderer and its layers. */
@Mixin(PlayerModel.class)
public abstract class ScythePlayerPoseMixin {
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
 private void slavicmyths$scythePose(LivingEntity player,float walk,float speed,float age,float yaw,float pitch,CallbackInfo ci){
  org.slavicmyths.client.NightClient.poseModel((PlayerModel<?>)(Object)this,player,age);
 }
}
