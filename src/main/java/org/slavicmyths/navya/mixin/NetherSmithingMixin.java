package org.slavicmyths.navya.mixin;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Scoped to six new results; vanilla and all older upgrades retain their behavior. */
@Mixin(SmithingTransformRecipe.class)
public abstract class NetherSmithingMixin {
 @Inject(method="assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",at=@At("RETURN"))
 private void slavicmyths$wear(SmithingRecipeInput input,HolderLookup.Provider lookup,CallbackInfoReturnable<ItemStack> result){if(org.slavicmyths.nethersmithing.NetherSmithing.ordinary(result.getReturnValue())||result.getReturnValue().getItem() instanceof org.slavicmyths.nethersmithing.NetherArmor.Piece)org.slavicmyths.nethersmithing.NetherSmithing.normalizeWear(input.base(),result.getReturnValue());}
}
