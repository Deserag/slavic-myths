package org.slavicmyths.compat;

import java.util.Arrays;
import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.*;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.ritual.Rituals;

/** Discovered only by JEI. No common or client setup class references this optional plugin. */
@JeiPlugin
public final class JeiRituals implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("slavicmyths", "rituals");
    private static final RecipeType<ResourceLocation> RECIPE_TYPE=new RecipeType<>(ID,ResourceLocation.class);
    @Override public ResourceLocation getPluginUid() { return ID; }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
    }
    @Override public void registerRecipes(IRecipeRegistration registration) { registration.addRecipes(RECIPE_TYPE,Arrays.asList(
            ResourceLocation.fromNamespaceAndPath("slavicmyths", "first_ritual"), ResourceLocation.fromNamespaceAndPath("slavicmyths", "thunder_axe"),
            ResourceLocation.fromNamespaceAndPath("slavicmyths", "perun_charm"), ResourceLocation.fromNamespaceAndPath("slavicmyths", "storm_staff"),
            ResourceLocation.fromNamespaceAndPath("slavicmyths", "amber_charm"), ResourceLocation.fromNamespaceAndPath("slavicmyths", "ritual_charcoal"),
            ResourceLocation.fromNamespaceAndPath("slavicmyths", "thunder_spear"), ResourceLocation.fromNamespaceAndPath("slavicmyths", "perunite_mace"),
            ResourceLocation.fromNamespaceAndPath("slavicmyths", "ember_axe")));
        java.util.List<ResourceLocation> runes=new java.util.ArrayList<>();for(String id:org.slavicmyths.rpg.Runes.IDS)runes.add(ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_"+id));registration.addRecipes(RECIPE_TYPE,runes); }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.ALTAR.get()));
    }
    private static final class Category implements IRecipeCategory<ResourceLocation> {
        private final IDrawable icon;
        Category(IGuiHelper helper) {

            icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModItems.ALTAR.get()));
        }
        @Override public RecipeType<ResourceLocation> getRecipeType(){return RECIPE_TYPE;}
        @Override public Component getTitle(){return Component.translatable("jei.slavicmyths.rituals");}
        @Override public int getWidth(){return 150;}public int getHeight(){return 48;}
        @Override public IDrawable getIcon() { return icon; }
        @Override public void setRecipe(IRecipeLayoutBuilder layout,ResourceLocation recipe,IFocusGroup focus){
            int rite=kind(recipe);
            for(int i=0;i<Rituals.length(rite);i++)layout.addSlot(RecipeIngredientRole.INPUT,i*28,16).addItemStack(new ItemStack(Rituals.ingredient(rite,i)));
            layout.addSlot(RecipeIngredientRole.OUTPUT,126,16).addItemStack(Rituals.result(rite));
        }
        @Override public void draw(ResourceLocation recipe,IRecipeSlotsView slots,GuiGraphics graphics,double x,double y){
            graphics.drawString(Minecraft.getInstance().font,"1       2       3       4   >",4,4,0x404040,false);
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
