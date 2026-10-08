package org.slavicmyths.rpg;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.item.ItemState;
import org.slavicmyths.armorer.ArmorerRecipe;

/** Repair keeps the existing rune component; incompatible second rune-bearing items are never consumed. */
@EventBusSubscriber(modid="slavicmyths")
public final class RuneRepair extends RepairItemRecipe {
    public static final DeferredHolder<RecipeSerializer<?>,SimpleCraftingRecipeSerializer<RuneRepair>> SERIALIZER=ArmorerRecipe.SERIALIZERS.register("rune_safe_repair",()->new SimpleCraftingRecipeSerializer<>(RuneRepair::new));
    public static void init() { }
    public RuneRepair(CraftingBookCategory category){super(category);}
    private static boolean hasState(ItemStack stack){return !stack.isEmpty()&&ItemState.runes(stack).slots()>0;}
    private static boolean incompatible(ItemStack a,ItemStack b){return hasState(a)&&hasState(b)&&(!ItemState.runes(a).equals(ItemState.runes(b))||!ItemState.legacyRuneSpecials(a).equals(ItemState.legacyRuneSpecials(b)));}
    @Override public boolean matches(CraftingInput input,Level level) {
        if(!super.matches(input,level))return false;
        ItemStack first=ItemStack.EMPTY;
        for(ItemStack stack:input.items())if(!stack.isEmpty()){if(!first.isEmpty()&&incompatible(first,stack))return false;first=stack;}
        return true;
    }
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries) {
        ItemStack result=super.assemble(input,registries),source=ItemStack.EMPTY;
        if(result.isEmpty())return result;
        for(ItemStack stack:input.items())if(hasState(stack)){source=stack;break;}
        if(source.isEmpty())return result;
        ItemStack preserved=source.copyWithCount(1);
        preserved.setDamageValue(result.getDamageValue());
        // Craft-table repairs keep vanilla curse/disenchantment rules, not a free enchantment merge.
        preserved.set(DataComponents.ENCHANTMENTS,result.getOrDefault(DataComponents.ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY));
        return preserved;
    }
    @Override public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}
    @SubscribeEvent public static void anvil(AnvilUpdateEvent event) {
        // Vanilla copies the left stack. Put the rune-bearing weapon on the left or repair with material.
        if(hasState(event.getRight())&&(!hasState(event.getLeft())||incompatible(event.getLeft(),event.getRight())))event.setCanceled(true);
    }
    @SubscribeEvent public static void grindstone(GrindstoneEvent.OnPlaceItem event) {
        if(hasState(event.getBottomItem())&&(!hasState(event.getTopItem())||incompatible(event.getTopItem(),event.getBottomItem())))event.setCanceled(true);
    }
}
