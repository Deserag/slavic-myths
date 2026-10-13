package org.slavicmyths.equipment.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class ClearSightMixin {
    @Inject(method="shouldEntityAppearGlowing",at=@At("HEAD"),cancellable=true)
    private void slavicmyths$sight(Entity entity,CallbackInfoReturnable<Boolean> ci){if(org.slavicmyths.client.ClearSightClient.outlined(entity)||org.slavicmyths.client.Quest185Client.outlined(entity))ci.setReturnValue(true);}
}
