package org.slavicmyths.equipment.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EquipmentHealthMixin {
    @Inject(method="setHealth",at=@At("RETURN"))
    private void slavicmyths$health(float health,CallbackInfo ci){org.slavicmyths.equipment.UniqueEquipmentEvents.healthChanged((LivingEntity)(Object)this);}
}
