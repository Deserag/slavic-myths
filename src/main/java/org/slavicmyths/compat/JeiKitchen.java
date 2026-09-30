package org.slavicmyths.compat;
import java.util.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.kitchen.KitchenRecipes;
import org.slavicmyths.registry.ModItems;
@JeiPlugin
public final class JeiKitchen implements IModPlugin {
 private static final ResourceLocation ID=new ResourceLocation("slavicmyths","kitchen");
 public ResourceLocation getPluginUid(){return ID;}
 public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
 public void registerRecipes(IRecipeRegistration r){List<Integer> recipes=new ArrayList<>();for(int i=0;i<KitchenRecipes.COUNT;i++)recipes.add(i);r.addRecipes(recipes,ID);}
 public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(ModItems.KITCHEN_TABLE.get()),ID);}
 private static final class Category implements IRecipeCategory<Integer>{
  private final IDrawable bg,icon;Category(IGuiHelper h){bg=h.createBlankDrawable(152,44);icon=h.createDrawableIngredient(new ItemStack(ModItems.KITCHEN_TABLE.get()));}
  public ResourceLocation getUid(){return ID;}public Class<? extends Integer> getRecipeClass(){return Integer.class;}public String getTitle(){return ModItems.KITCHEN_TABLE.get().getDescription().getString();}public IDrawable getBackground(){return bg;}public IDrawable getIcon(){return icon;}
  public void setIngredients(Integer id,IIngredients ing){List<ItemStack> inputs=new ArrayList<>();for(int j=0;j<3;j++)inputs.add(new ItemStack(KitchenRecipes.ingredients(id)[j],KitchenRecipes.count(id,j)));inputs.add(new ItemStack(KitchenRecipes.tool(id)));ing.setInputs(VanillaTypes.ITEM,inputs);ing.setOutput(VanillaTypes.ITEM,new ItemStack(KitchenRecipes.output(id),KitchenRecipes.amount(id)));}
  public void setRecipe(IRecipeLayout l,Integer id,IIngredients ing){for(int j=0;j<4;j++)l.getItemStacks().init(j,true,j*26,12);l.getItemStacks().init(4,false,132,12);l.getItemStacks().set(ing);}
 }
}
