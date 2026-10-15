package org.slavicmyths.polozaudit.mixin;
import org.slavicmyths.polozaudit.PressureObserver;
import net.minecraft.world.level.Level;import net.minecraft.core.BlockPos;import net.minecraft.world.entity.Entity;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=Level.class,remap=false)
public abstract class WorldMixin {
 @Inject(method="destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",at=@At("RETURN"))private void destroyed(BlockPos p,boolean drop,Entity source,int recursion,CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValueZ())PressureObserver.destroy(source);}
}
