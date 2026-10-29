package org.slavicmyths.navya.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(net.minecraft.world.level.chunk.LevelChunk.class)
public abstract class NaturalGroundWriteMixin {
 @Inject(method="setBlockState",at=@At("HEAD")) private void slavicmyths$invalidate(net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState state,boolean moving,CallbackInfoReturnable<net.minecraft.world.level.block.state.BlockState> cir){org.slavicmyths.burningislands.TemporaryTerrain.externalWrite((net.minecraft.world.level.chunk.LevelChunk)(Object)this,p);}
}
