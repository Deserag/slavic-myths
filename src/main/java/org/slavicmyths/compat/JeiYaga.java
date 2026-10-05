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
import org.slavicmyths.yaga.YagaServices;
import org.slavicmyths.registry.ModItems;
@JeiPlugin
public final class JeiYaga implements IModPlugin {
 private static final ResourceLocation ID=new ResourceLocation("slavicmyths","yaga_cauldron");
 public ResourceLocation getPluginUid(){return ID;}
 public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
 public void registerRecipes(IRecipeRegistration r){List<Integer> recipes=new ArrayList<>();for(int i=0;i<YagaServices.BREWS.length;i++)recipes.add(i);r.addRecipes(recipes,ID);}
 public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(ModItems.SVYAZKA_TRAV_YAGI.get()),ID);}
 private static final class Category implements IRecipeCategory<Integer>{
  private final IDrawable bg,icon;Category(IGuiHelper h){bg=h.createBlankDrawable(152,44);icon=h.createDrawableIngredient(new ItemStack(ModItems.SVYAZKA_TRAV_YAGI.get()));}
  public ResourceLocation getUid(){return ID;}public Class<? extends Integer> getRecipeClass(){return Integer.class;}public String getTitle(){return new net.minecraft.util.text.TranslationTextComponent("yaga.cauldron").getString();}public IDrawable getBackground(){return bg;}public IDrawable getIcon(){return icon;}
  public void setIngredients(Integer id,IIngredients ing){List<ItemStack> in=new ArrayList<>();for(YagaServices.Need n:YagaServices.BREWS[id].inputs)in.add(n.stack());ing.setInputs(VanillaTypes.ITEM,in);ing.setOutput(VanillaTypes.ITEM,YagaServices.BREWS[id].output());}
  public void setRecipe(IRecipeLayout l,Integer id,IIngredients ing){for(int j=0;j<4;j++)l.getItemStacks().init(j,true,j*26,12);l.getItemStacks().init(4,false,132,12);l.getItemStacks().set(ing);}
 }
}
