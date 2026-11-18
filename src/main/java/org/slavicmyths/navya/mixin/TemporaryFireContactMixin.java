package org.slavicmyths.navya.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(net.minecraft.world.level.block.BaseFireBlock.class)
public abstract class TemporaryFireContactMixin {
 @Inject(method="entityInside",at=@At("HEAD"),cancellable=true) private void slavicmyths$allies(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level world,net.minecraft.core.BlockPos pos,net.minecraft.world.entity.Entity entity,CallbackInfo ci){if(world instanceof net.minecraft.server.level.ServerLevel s&&org.slavicmyths.burningislands.TemporaryTerrain.protectedFromFire(s,pos,entity))ci.cancel();}
}
