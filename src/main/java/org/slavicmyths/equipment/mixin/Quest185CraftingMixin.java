package org.slavicmyths.equipment.mixin;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Native 2x2 and 3x3 menus share this result calculation. Ingredients remain untouched. */
@Mixin(CraftingMenu.class)
public abstract class Quest185CraftingMixin {
 @Inject(method="slotChangedCraftingGrid",at=@At("TAIL"))
 private static void quest185$gate(AbstractContainerMenu menu,Level world,Player player,CraftingContainer input,ResultContainer result,RecipeHolder<CraftingRecipe> recipe,CallbackInfo ci){if(!(player instanceof ServerPlayer p)||p.isCreative())return;var stack=result.getItem(0);var n=org.slavicmyths.quest185.Quest185Story.data(p);boolean utility=stack.is(org.slavicmyths.quest185.Quest185Content.WARD.get())||stack.is(org.slavicmyths.quest185.Quest185Content.TRACKER.get());if(utility&&(!n.getBoolean("UtilityRecipes")||stack.is(org.slavicmyths.quest185.Quest185Content.WARD.get())&&!n.getBoolean("Likho"))||!org.slavicmyths.bogatyr.HeroIntroductions.canCraft(p,stack)){result.setItem(0,ItemStack.EMPTY);menu.setRemoteSlot(0,ItemStack.EMPTY);p.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(menu.containerId,menu.incrementStateId(),0,ItemStack.EMPTY));}}
}
