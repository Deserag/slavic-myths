package org.slavicmyths.compat;
import java.util.*;
import mezz.jei.api.*;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.armorer.*;
import org.slavicmyths.registry.ModItems;
@JeiPlugin
public final class JeiArmorer implements IModPlugin {
    private static final ResourceLocation ID=new ResourceLocation("slavicmyths","armorer");
    public ResourceLocation getPluginUid(){return ID;}
    public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
    public void registerRecipes(IRecipeRegistration r){if(Minecraft.getInstance().level!=null)r.addRecipes(Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ArmorerRecipe.TYPE),ID);}
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(ModItems.ARMORER_TABLE.get()),ID);}
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration r){r.addRecipeTransferHandler(ArmorerMenu.class,ID,1,16,17,36);}
    private static final class Category implements IRecipeCategory<ArmorerRecipe>{
        private final IDrawable background,icon;Category(IGuiHelper h){background=h.createBlankDrawable(132,76);icon=h.createDrawableIngredient(new ItemStack(ModItems.ARMORER_TABLE.get()));}
        public ResourceLocation getUid(){return ID;}public Class<? extends ArmorerRecipe> getRecipeClass(){return ArmorerRecipe.class;}
        public String getTitle(){return ModItems.ARMORER_TABLE.get().getDescription().getString();}public IDrawable getBackground(){return background;}public IDrawable getIcon(){return icon;}
        public void setIngredients(ArmorerRecipe r,IIngredients i){i.setInputIngredients(r.getIngredients());i.setOutput(mezz.jei.api.constants.VanillaTypes.ITEM,r.getResultItem());}
        public void setRecipe(IRecipeLayout layout,ArmorerRecipe recipe,IIngredients ingredients){
            for(int i=0;i<16;i++){layout.getItemStacks().init(i,true,(i%4)*18,(i/4)*18);}
            for(int i=0;i<recipe.getIngredients().size();i++){int slot=recipe.shapeless?i:(i/recipe.width)*4+i%recipe.width;layout.getItemStacks().set(slot,Arrays.asList(recipe.getIngredients().get(i).getItems()));}
            layout.getItemStacks().init(16,false,108,27);layout.getItemStacks().set(16,recipe.getResultItem());
        }
    }
}
