package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import static org.slavicmyths.verify.Stabilization269.*;
/** Loaded native recipes; supplied ingredient witnesses, not material acquisition or station GUI. */
final class Recipes269 {
 static ItemStack witness(Ingredient ingredient){if(ingredient.isEmpty())return ItemStack.EMPTY;var options=ingredient.getItems();return options.length==0?ItemStack.EMPTY:options[0].copyWithCount(1);}
 static RecipeInput custom(Recipe<?> recipe){
  if(recipe instanceof org.slavicmyths.armorer.ArmorerRecipe r){var stacks=new ArrayList<ItemStack>(Collections.nCopies(16,ItemStack.EMPTY));var ingredients=r.getIngredients();for(int i=0;i<ingredients.size();i++)stacks.set(r.shapeless?i:(i/r.width)*4+i%r.width,witness(ingredients.get(i)));return new org.slavicmyths.armorer.ArmorerInput(4,4,stacks);}
  if(recipe instanceof org.slavicmyths.rpg.RuneCraftRecipe r){var stacks=new ArrayList<ItemStack>(Collections.nCopies(4,ItemStack.EMPTY));for(int i=0;i<r.inputs().size();i++){var choices=r.inputs().get(i).displayItems();if(choices.length>0)stacks.set(i,choices[0]);}if(r.chisel())stacks.set(3,new ItemStack(org.slavicmyths.rpg.RuneFoundation.item("rune_chisel")));return new org.slavicmyths.rpg.RuneCraftRecipe.Input(stacks);}
  if(recipe instanceof org.slavicmyths.kitchen.KitchenRecipe r){var stacks=new ArrayList<ItemStack>(Collections.nCopies(4,ItemStack.EMPTY));for(int i=0;i<r.parts().size();i++)stacks.set(i,witness(r.parts().get(i).ingredient()).copyWithCount(r.parts().get(i).count()));return new org.slavicmyths.kitchen.KitchenInput(stacks);}
  if(recipe instanceof org.slavicmyths.brewing.VatRecipe r){var stacks=new ArrayList<ItemStack>(Collections.nCopies(4,ItemStack.EMPTY));for(int i=0;i<r.parts().size();i++)stacks.set(i,witness(r.parts().get(i).ingredient()).copyWithCount(r.parts().get(i).count()));return new org.slavicmyths.kitchen.KitchenInput(stacks);}
  if(recipe instanceof org.slavicmyths.storage.DryingRecipe r)return new SingleRecipeInput(witness(r.ingredient()));
  if(recipe instanceof org.slavicmyths.industry.MachineRecipe r)return new org.slavicmyths.industry.MachineRecipe.Input(witness(r.input()),witness(r.flux()));
  if(recipe instanceof org.slavicmyths.gorynych.GorynychUpgrade r){var stacks=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));if(r.category()==CraftingBookCategory.MISC){stacks.set(0,new ItemStack(org.slavicmyths.guardians.GuardianContent.FIRE_FRAGMENT.get()));stacks.set(1,new ItemStack(org.slavicmyths.guardians.GuardianContent.WATER_FRAGMENT.get()));stacks.set(2,new ItemStack(org.slavicmyths.poloz.PolozContent.EYE.get()));}else{Item[] items={org.slavicmyths.registry.ModItems.PERUNITE_HELMET.get(),org.slavicmyths.gorynych.GorynychContent.item("gorynych_scale"),org.slavicmyths.registry.ModItems.PERUNITE.get(),org.slavicmyths.registry.ModItems.PERUNITE.get(),Items.DIAMOND,Items.DIAMOND};for(int i=0;i<items.length;i++)stacks.set(i,new ItemStack(items[i]));}return CraftingInput.of(3,3,stacks);}
  if(recipe instanceof org.slavicmyths.winters.WintersRecipe r&&r.category()!=CraftingBookCategory.MISC)return CraftingInput.of(3,3,Arrays.stream(org.slavicmyths.winters.WintersRecipe.displayInputs(false)).map(ItemStack::new).toList());
  return null;
 }
 @SuppressWarnings({"rawtypes","unchecked"}) static void run(){
  current="native_loaded_recipes";var manager=w.getRecipeManager();check(!manager.hadErrorsLoading(),"native RecipeManager loaded without recipe errors");int total=0,tested=0;var skipped=new ArrayList<String>();var types=new TreeMap<String,Integer>();
  for(var holder:manager.getRecipes()){
   if(!holder.id().getNamespace().equals("slavicmyths"))continue;total++;Recipe<?> recipe=holder.value();types.merge(recipe.getClass().getSimpleName(),1,Integer::sum);RecipeInput input=null;var ingredients=recipe.getIngredients();
   if(recipe instanceof ShapedRecipe shaped){var stacks=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));for(int y=0;y<shaped.getHeight();y++)for(int x=0;x<shaped.getWidth();x++)stacks.set(y*3+x,witness(ingredients.get(y*shaped.getWidth()+x)));input=CraftingInput.of(3,3,stacks);}
   else if(recipe instanceof ShapelessRecipe){var stacks=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));for(int i=0;i<ingredients.size();i++)stacks.set(i,witness(ingredients.get(i)));input=CraftingInput.of(3,3,stacks);}
   else if(recipe instanceof AbstractCookingRecipe||recipe instanceof SingleItemRecipe)input=new SingleRecipeInput(witness(ingredients.getFirst()));
   else if(recipe instanceof SmithingRecipe&&ingredients.size()==3)input=new SmithingRecipeInput(witness(ingredients.get(0)),witness(ingredients.get(1)),witness(ingredients.get(2)));
   if(input==null)input=custom(recipe);
   if(input==null){skipped.add(holder.id()+" "+recipe.getClass().getName());continue;}
   tested++;boolean matches=((Recipe)recipe).matches(input,w);check(matches,"native ingredient witness matches "+holder.id());if(!matches)continue;var result=((Recipe)recipe).assemble(input,w.registryAccess());check(!result.isEmpty()&&result.getCount()<=result.getMaxStackSize(),"native legal assembly "+holder.id());var expected=recipe.getResultItem(w.registryAccess());if(!expected.isEmpty())check(ItemStack.isSameItemSameComponents(expected,result)&&expected.getCount()==result.getCount(),"native result matches declaration "+holder.id());
   if(recipe instanceof AbstractCookingRecipe cooking)check(cooking.getCookingTime()>0&&cooking.getExperience()>=0,"cooking time/experience valid "+holder.id());
   if(input instanceof CraftingInput craft){var remains=((CraftingRecipe)recipe).getRemainingItems(craft);check(remains.size()==craft.size(),"native remainder grid size "+holder.id());}
  }
  out.put("recipe_total",total);out.put("recipe_tested",tested);out.put("recipe_types",types);out.put("recipe_dynamic_NOT_RUN",skipped);check(total>0&&tested>0,"native namespace recipes actually exercised");
 }
}
