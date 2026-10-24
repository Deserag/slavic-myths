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
import org.slavicmyths.yaga.YagaServices;
import org.slavicmyths.registry.ModItems;
@JeiPlugin
public final class JeiYaga implements IModPlugin {
 private static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("slavicmyths","yaga_cauldron");
 private static final RecipeType<Integer> RECIPE_TYPE=new RecipeType<>(ID,Integer.class);
 public ResourceLocation getPluginUid(){return ID;}
 public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
 public void registerRecipes(IRecipeRegistration r){List<Integer> recipes=new ArrayList<>();for(int i=0;i<YagaServices.BREWS.length;i++)recipes.add(i);for(int i=0;i<YagaServices.EXCHANGES.length;i++)recipes.add(100+i);r.addRecipes(RECIPE_TYPE,recipes);}
 private static YagaServices.Entry entry(int id){return id>=100?YagaServices.EXCHANGES[id-100]:YagaServices.BREWS[id];}
 public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(ModItems.SVYAZKA_TRAV_YAGI.get()),RECIPE_TYPE);}
 private static final class Category implements IRecipeCategory<Integer>{
  private final IDrawable icon;Category(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModItems.SVYAZKA_TRAV_YAGI.get()));}
  public RecipeType<Integer> getRecipeType(){return RECIPE_TYPE;}public Component getTitle(){return Component.translatable("yaga.cauldron");}public int getWidth(){return 180;}public int getHeight(){return 100;}public IDrawable getIcon(){return icon;}
  public void setRecipe(IRecipeLayoutBuilder layout,Integer id,IFocusGroup focus){
   int j=0;for(YagaServices.Need need:entry(id).inputs)layout.addSlot(RecipeIngredientRole.INPUT,j++*26,12).addItemStack(need.stack());
   layout.addSlot(RecipeIngredientRole.OUTPUT,156,12).addItemStack(entry(id).output());
  }
  public void draw(Integer id,mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,net.minecraft.client.gui.GuiGraphics g,double x,double y){var e=entry(id);var font=net.minecraft.client.Minecraft.getInstance().font;g.drawWordWrap(font,Component.translatable(id>=100?"guide.yaga.exchange":"guide.yaga.brew",Component.translatable(e.stage>0?"yaga.quest."+YagaServices.MAIN[e.stage-1].id+".title":"yaga.favor.0")),0,40,178,0xff333333);if(e.id.equals("veles_oath_iv"))g.drawWordWrap(font,Component.translatable("yaga.fail.gorynych_claim"),0,72,178,0xff333333);}
 }
}
