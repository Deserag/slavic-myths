package org.slavicmyths.equipment.mixin;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Entity.class)
public abstract class Quest185ColorMixin {
 @Inject(method="getTeamColor",at=@At("HEAD"),cancellable=true)
 private void quest185$outline(CallbackInfoReturnable<Integer> ci){if(org.slavicmyths.client.Quest185Client.outlined((Entity)(Object)this))ci.setReturnValue(0xB52F40);}
}
