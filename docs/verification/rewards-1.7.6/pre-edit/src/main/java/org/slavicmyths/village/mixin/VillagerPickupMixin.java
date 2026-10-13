package org.slavicmyths.village.mixin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.village.WorkPolicy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=Villager.class,remap=false)
public abstract class VillagerPickupMixin {
    @Inject(method="wantsToPickUp",at=@At("RETURN"),cancellable=true)
    private void physicalInputs(ItemStack stack,CallbackInfoReturnable<Boolean> ci){Villager v=(Villager)(Object)this;if(!ci.getReturnValue()&&!v.isBaby()&&WorkPolicy.input(v,stack)&&v.getInventory().canAddItem(stack))ci.setReturnValue(true);}
}
