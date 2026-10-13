package org.slavicmyths.equipment.mixin;
import java.util.Optional;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** These two personal unlock recipes use player crafting menus, never an ownerless crafter. */
@Mixin(CrafterBlock.class)
public abstract class Quest185CrafterMixin {
 @Inject(method="getPotentialResults",at=@At("RETURN"),cancellable=true)
 private static void quest185$personal(Level level,CraftingInput input,CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> ci){var recipe=ci.getReturnValue();if(recipe.isEmpty())return;var id=recipe.get().id();if(id.getNamespace().equals("slavicmyths")&&java.util.Set.of("mor_ward","tracker_sense","two_winters_original","two_winters_copy").contains(id.getPath()))ci.setReturnValue(Optional.empty());}
}
