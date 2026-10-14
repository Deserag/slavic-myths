package org.slavicmyths.rpg.tools.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.CommonHooks;
import org.slavicmyths.rpg.tools.ToolRuneRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Runs only after the item-use event is accepted and placement snapshots/rollback are finished. */
@Mixin(ItemStack.class)
public abstract class ToolRuneUseMixin {
 @Redirect(method="useOn",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/common/CommonHooks;onPlaceItemIntoWorld(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
 private InteractionResult slavicmyths$use(UseOnContext context){var frame=ToolRuneRuntime.beforeUse(context);var result=CommonHooks.onPlaceItemIntoWorld(context);return ToolRuneRuntime.afterUse(context,frame,result);}
}
