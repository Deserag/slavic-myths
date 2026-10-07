package org.slavicmyths.compat;
import java.util.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.kitchen.KitchenRecipe;
import org.slavicmyths.registry.ModItems;
/** Only JEI discovers this class; common mod does not load its types. */
@JeiPlugin public final class JeiKitchen implements IModPlugin{
 private static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("slavicmyths","kitchen");private static final RecipeType<KitchenRecipe> TYPE=new RecipeType<>(ID,KitchenRecipe.class);
 public ResourceLocation getPluginUid(){return ID;}
 public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
 public void registerRecipes(IRecipeRegistration r){var level=Minecraft.getInstance().level;if(level!=null)r.addRecipes(TYPE,level.getRecipeManager().getAllRecipesFor(KitchenRecipe.TYPE.get()).stream().map(h->h.value()).toList());}
 public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(ModItems.KITCHEN_TABLE.get()),TYPE);}
 private static final class Category implements IRecipeCategory<KitchenRecipe>{private final IDrawable icon;Category(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModItems.KITCHEN_TABLE.get()));}
 public RecipeType<KitchenRecipe> getRecipeType(){return TYPE;}public Component getTitle(){return ModItems.KITCHEN_TABLE.get().getDescription();}public int getWidth(){return 160;}public int getHeight(){return 62;}public IDrawable getIcon(){return icon;}
 public void setRecipe(IRecipeLayoutBuilder layout,KitchenRecipe r,IFocusGroup focus){for(int i=0;i<r.parts().size();i++){var part=r.parts().get(i);layout.addSlot(RecipeIngredientRole.INPUT,i%2*20,i/2*20).addItemStacks(Arrays.stream(part.ingredient().getItems()).map(s->s.copyWithCount(part.count())).toList());}layout.addSlot(RecipeIngredientRole.INPUT,64,12).addIngredients(r.tool());layout.addSlot(RecipeIngredientRole.OUTPUT,132,12).addItemStack(r.result());}
 public void draw(KitchenRecipe r,IRecipeSlotsView slots,GuiGraphics g,double x,double y){if(r.pot())g.drawString(Minecraft.getInstance().font,Component.translatable("kitchen.slavicmyths.servings",r.servings(),r.servings()),48,47,0xff333333,false);}
 }
}
