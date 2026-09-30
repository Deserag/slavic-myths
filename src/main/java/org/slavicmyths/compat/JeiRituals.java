package org.slavicmyths.compat;

import java.util.Arrays;
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
import org.slavicmyths.ritual.Rituals;

/** Discovered only by JEI. No common or client setup class references this optional plugin. */
@JeiPlugin
public final class JeiRituals implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation("slavicmyths", "rituals");
    @Override public ResourceLocation getPluginUid() { return ID; }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
    }
    @Override public void registerRecipes(IRecipeRegistration registration) { registration.addRecipes(Arrays.asList(
            new ResourceLocation("slavicmyths", "first_ritual"), new ResourceLocation("slavicmyths", "thunder_axe"),
            new ResourceLocation("slavicmyths", "perun_charm"), new ResourceLocation("slavicmyths", "storm_staff"),
            new ResourceLocation("slavicmyths", "amber_charm"), new ResourceLocation("slavicmyths", "ritual_charcoal"),
            new ResourceLocation("slavicmyths", "thunder_spear"), new ResourceLocation("slavicmyths", "perunite_mace"),
            new ResourceLocation("slavicmyths", "ember_axe")), ID);
        java.util.List<ResourceLocation> runes=new java.util.ArrayList<>();for(String id:org.slavicmyths.rpg.Runes.IDS)runes.add(new ResourceLocation("slavicmyths","rune_"+id));registration.addRecipes(runes,ID); }
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
            int rite = kind(recipe);
            java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
            for (int i = 0; i < Rituals.length(rite); i++) inputs.add(new ItemStack(Rituals.ingredient(rite, i)));
            ingredients.setInputs(VanillaTypes.ITEM, inputs);
            ingredients.setOutput(VanillaTypes.ITEM, Rituals.result(rite));
        }
        @Override public void setRecipe(IRecipeLayout layout, ResourceLocation recipe, IIngredients ingredients) {
            for (int i = 0; i < Rituals.length(kind(recipe)); i++) layout.getItemStacks().init(i, true, i * 28, 16);
            layout.getItemStacks().init(Rituals.length(kind(recipe)), false, 126, 16);
            layout.getItemStacks().set(ingredients);
        }
        @Override public void draw(ResourceLocation recipe, MatrixStack pose, double x, double y) {
            Minecraft.getInstance().font.draw(pose, "1       2       3       4   >", 4, 4, 0x404040);
        }
        private int kind(ResourceLocation recipe) {
            for(int i=0;i<8;i++)if(recipe.getPath().equals("rune_"+org.slavicmyths.rpg.Runes.IDS[i]))return 9+i;
            if ("ember_axe".equals(recipe.getPath())) return Rituals.EMBER_AXE;
            if ("thunder_spear".equals(recipe.getPath())) return Rituals.THUNDER_SPEAR;
            if ("perunite_mace".equals(recipe.getPath())) return Rituals.PERUNITE_MACE;
            return "thunder_axe".equals(recipe.getPath()) ? Rituals.AXE :
                    "perun_charm".equals(recipe.getPath()) ? Rituals.CHARM :
                    "storm_staff".equals(recipe.getPath()) ? Rituals.STORM_STAFF :
                    "amber_charm".equals(recipe.getPath()) ? Rituals.AMBER_CHARM :
                    "ritual_charcoal".equals(recipe.getPath()) ? Rituals.CHARCOAL : Rituals.FIRST;
        }
    }
}
