package org.slavicmyths.compat;

import java.util.Arrays;
import java.util.Collections;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.ritual.FirstRitual;

/** Discovered only by JEI. No common or client setup class references this optional plugin. */
@JeiPlugin
public final class JeiRituals implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation("slavicmyths", "rituals");
    @Override public ResourceLocation getPluginUid() { return ID; }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
    }
    @Override public void registerRecipes(IRecipeRegistration registration) { registration.addRecipes(Collections.singletonList(ID), ID); }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.ALTAR.get()), ID);
    }
    private static final class Category implements IRecipeCategory<ResourceLocation> {
        private final IDrawable background, icon;
        Category(IGuiHelper helper) {
            background = helper.createBlankDrawable(150, 48);
            icon = helper.createDrawableIngredient(new ItemStack(ModItems.ALTAR.get()));
        }
        @Override public ResourceLocation getUid() { return ID; }
        @Override public Class<? extends ResourceLocation> getRecipeClass() { return ResourceLocation.class; }
        @Override public String getTitle() { return new TranslationTextComponent("jei.slavicmyths.rituals").getString(); }
        @Override public IDrawable getBackground() { return background; }
        @Override public IDrawable getIcon() { return icon; }
        @Override public void setIngredients(ResourceLocation recipe, IIngredients ingredients) {
            ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(new ItemStack(FirstRitual.ingredient(0)),
                    new ItemStack(FirstRitual.ingredient(1)), new ItemStack(FirstRitual.ingredient(2))));
            ingredients.setOutput(VanillaTypes.ITEM, FirstRitual.result());
        }
        @Override public void setRecipe(IRecipeLayout layout, ResourceLocation recipe, IIngredients ingredients) {
            for (int i = 0; i < 3; i++) layout.getItemStacks().init(i, true, i * 32, 16);
            layout.getItemStacks().init(3, false, 126, 16);
            layout.getItemStacks().set(ingredients);
        }
        @Override public void draw(ResourceLocation recipe, MatrixStack pose, double x, double y) {
            Minecraft.getInstance().font.draw(pose, "1       2       3     >", 4, 4, 0x404040);
        }
    }
}
