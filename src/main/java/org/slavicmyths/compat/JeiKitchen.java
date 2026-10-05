package org.slavicmyths.compat;
import java.util.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.*;
import net.minecraft.network.chat.Component;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.kitchen.KitchenRecipes;
import org.slavicmyths.registry.ModItems;
@JeiPlugin
public final class JeiKitchen implements IModPlugin {
 private static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("slavicmyths","kitchen");
 private static final RecipeType<Integer> RECIPE_TYPE=new RecipeType<>(ID,Integer.class);
 public ResourceLocation getPluginUid(){return ID;}
 public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
 public void registerRecipes(IRecipeRegistration r){List<Integer> recipes=new ArrayList<>();for(int i=0;i<KitchenRecipes.COUNT;i++)recipes.add(i);r.addRecipes(RECIPE_TYPE,recipes);}
 public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(ModItems.KITCHEN_TABLE.get()),RECIPE_TYPE);}
 private static final class Category implements IRecipeCategory<Integer>{
  private final IDrawable icon;Category(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModItems.KITCHEN_TABLE.get()));}
  public RecipeType<Integer> getRecipeType(){return RECIPE_TYPE;}public Component getTitle(){return ModItems.KITCHEN_TABLE.get().getDescription();}public int getWidth(){return 152;}public int getHeight(){return 44;}public IDrawable getIcon(){return icon;}
  public void setRecipe(IRecipeLayoutBuilder layout,Integer id,IFocusGroup focus){
   for(int j=0;j<3;j++)layout.addSlot(RecipeIngredientRole.INPUT,j*26,12).addItemStack(new ItemStack(KitchenRecipes.ingredients(id)[j],KitchenRecipes.count(id,j)));
   layout.addSlot(RecipeIngredientRole.INPUT,78,12).addItemStack(new ItemStack(KitchenRecipes.tool(id)));
   layout.addSlot(RecipeIngredientRole.OUTPUT,132,12).addItemStack(new ItemStack(KitchenRecipes.output(id),KitchenRecipes.amount(id)));
  }
 }
}
