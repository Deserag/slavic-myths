package org.slavicmyths.client.chudlight;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockLightEngine.class)
public abstract class LanternEmissionMixin {
 @Inject(method="getEmission",at=@At("RETURN"),cancellable=true)
 private void slavicmyths$lantern(long pos,BlockState state,CallbackInfoReturnable<Integer> result){
  int light=org.slavicmyths.client.ChudLanternLight.emission(((LightEngineAccess)this).slavicmyths$source().getLevel(),pos);
  if(light>result.getReturnValueI())result.setReturnValue(light);
 }
}
