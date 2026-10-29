package org.slavicmyths.navya.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin({net.minecraft.world.level.block.MagmaBlock.class,net.minecraft.world.level.block.FireBlock.class})
public abstract class TemporaryBlockTickMixin {
 @Inject(method="tick",at=@At("HEAD"),cancellable=true) private void slavicmyths$expire(net.minecraft.world.level.block.state.BlockState state,net.minecraft.server.level.ServerLevel level,net.minecraft.core.BlockPos pos,net.minecraft.util.RandomSource random,CallbackInfo ci){if(org.slavicmyths.burningislands.TemporaryTerrain.scheduled(level,pos))ci.cancel();}
}
