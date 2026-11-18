package org.slavicmyths.verify.client.mixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(net.minecraft.client.Camera.class)
public abstract class SideCamera269 {
 @Shadow private boolean detached;
 @Shadow protected abstract void setPosition(double x,double y,double z);
 @Shadow protected abstract void setRotation(float yaw,float pitch);
 @Inject(method="setup",at=@At("RETURN"))
 private void qaSide(net.minecraft.world.level.BlockGetter level,net.minecraft.world.entity.Entity entity,boolean thirdPerson,boolean mirrored,float partial,CallbackInfo ci){
  if(!org.slavicmyths.verify.client.Client269.SIDE)return;
  var horse=org.slavicmyths.verify.client.Client269.POSE?org.slavicmyths.verify.client.Pose269.horse:org.slavicmyths.verify.client.Client269.horse;if(horse==null)return;detached=true;setPosition(horse.getX()+6,horse.getY()+3.3,horse.getZ()+1);setRotation(80.5F,8);
 }
}
