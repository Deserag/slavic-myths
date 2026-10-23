package org.slavicmyths.equipment.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Only the master of these existing multipart blocks owns persistent data. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MultipartBlockEntityMixin {
 @Inject(method="hasBlockEntity",at=@At("RETURN"),cancellable=true)
 private void slavicmyths$masterOnly(CallbackInfoReturnable<Boolean> result){
  if(!result.getReturnValue())return;
  BlockState state=(BlockState)(Object)this;var block=state.getBlock();
  boolean helper=block instanceof org.slavicmyths.storage.StorageBlock storage&&storage.part(state)!=0
   ||block instanceof org.slavicmyths.kurgan.BurialCoffinBlock&&state.getValue(org.slavicmyths.kurgan.BurialCoffinBlock.PART)==BedPart.HEAD
   ||block instanceof org.slavicmyths.block.KitchenTableBlock&&state.getValue(org.slavicmyths.block.KitchenTableBlock.PART)==org.slavicmyths.block.KitchenTableBlock.Part.RIGHT
   ||block instanceof org.slavicmyths.trophy.TrophyHeadBlock&&state.getValue(org.slavicmyths.trophy.TrophyHeadBlock.HALF)==DoubleBlockHalf.UPPER;
  if(helper)result.setReturnValue(false);
 }
}
