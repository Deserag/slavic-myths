package org.slavicmyths.compat;
import java.util.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import org.slavicmyths.winters.*;
import org.slavicmyths.gorynych.*;

/** Display for existing special serializers. Excluded crossbow work is deliberately absent. */
@JeiPlugin public final class JeiSpecialProcesses implements IModPlugin {
 public record Display(List<ItemStack> grid,ItemStack result,String hint){}
 private static final RecipeType<Display> TYPE=new RecipeType<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","special_processes"),Display.class);
 public ResourceLocation getPluginUid(){return ResourceLocation.fromNamespaceAndPath("slavicmyths","special_process_guide");}
 public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
 public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE),TYPE);}
 public void registerRecipes(IRecipeRegistration r){var list=new ArrayList<Display>();
  for(boolean copy:List.of(false,true))list.add(new Display(Arrays.stream(WintersRecipe.displayInputs(copy)).map(ItemStack::new).toList(),new ItemStack(WintersContent.AMULET.get()),copy?"guide.special.winters_copy":"guide.special.winters"));
  for(var base:List.of(org.slavicmyths.registry.ModItems.PERUNITE_HELMET,org.slavicmyths.registry.ModItems.PERUNITE_CHESTPLATE,org.slavicmyths.registry.ModItems.PERUNITE_LEGGINGS,org.slavicmyths.registry.ModItems.PERUNITE_BOOTS)){
   var input=new ItemStack(base.get());var target=GorynychUpgrade.target(input);if(target==null)continue;
   list.add(new Display(List.of(input,new ItemStack(GorynychContent.item("gorynych_scale")),new ItemStack(org.slavicmyths.registry.ModItems.PERUNITE.get()),new ItemStack(org.slavicmyths.registry.ModItems.PERUNITE.get()),new ItemStack(Items.DIAMOND),new ItemStack(Items.DIAMOND)),GorynychUpgrade.copy(input,target),"guide.special.armor"));
  }
  list.add(new Display(List.of(new ItemStack(org.slavicmyths.guardians.GuardianContent.FIRE_FRAGMENT.get()),new ItemStack(org.slavicmyths.guardians.GuardianContent.WATER_FRAGMENT.get()),new ItemStack(org.slavicmyths.poloz.PolozContent.EYE.get())),new ItemStack(org.slavicmyths.quest185.Quest185Content.WARD.get()),"guide.special.ward"));r.addRecipes(TYPE,list);
 }
 private static final class Category implements IRecipeCategory<Display>{
  final IDrawable icon;Category(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(Items.CRAFTING_TABLE));}
  public RecipeType<Display> getRecipeType(){return TYPE;}public Component getTitle(){return Component.translatable("guide.special.title");}public IDrawable getIcon(){return icon;}public int getWidth(){return 180;}public int getHeight(){return 125;}
  public void setRecipe(IRecipeLayoutBuilder b,Display r,IFocusGroup f){for(int i=0;i<r.grid.size();i++)b.addSlot(RecipeIngredientRole.INPUT,(i%3)*20,(i/3)*20).addItemStack(r.grid.get(i));b.addSlot(RecipeIngredientRole.OUTPUT,152,20).addItemStack(r.result);}
  public void draw(Display r,mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,GuiGraphics g,double x,double y){g.drawWordWrap(Minecraft.getInstance().font,Component.translatable(r.hint),0,66,178,0xff333333);}
 }
}
