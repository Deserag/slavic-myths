package org.slavicmyths.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.slavicmyths.equipment.UniqueEquipment;

/** Optional JEI-only source information; upgrades use the existing Armorer category. */
@JeiPlugin
public final class JeiUniqueEquipment implements IModPlugin {
    public ResourceLocation getPluginUid(){return ResourceLocation.fromNamespaceAndPath("slavicmyths","unique_equipment");}
    public void registerRecipes(IRecipeRegistration registration){registration.addIngredientInfo(new ItemStack(UniqueEquipment.HARDENED_IRON.get()),VanillaTypes.ITEM_STACK,Component.translatable("unique.slavicmyths.iron_source"));}
}
