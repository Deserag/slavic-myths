package org.slavicmyths.compat;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.armorer.*;
import org.slavicmyths.registry.ModItems;
@JeiPlugin
public final class JeiArmorer implements IModPlugin {
    private static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("slavicmyths","armorer");
    private static final RecipeType<RecipeHolder<ArmorerRecipe>> RECIPE_TYPE=RecipeType.createRecipeHolderType(ID);
    public ResourceLocation getPluginUid(){return ID;}
    public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
    public void registerRecipes(IRecipeRegistration r){if(Minecraft.getInstance().level!=null)r.addRecipes(RECIPE_TYPE,Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ArmorerRecipe.TYPE.get()));}
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(ModItems.ARMORER_TABLE.get()),RECIPE_TYPE);}
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration r){r.addRecipeTransferHandler(ArmorerMenu.class,ArmorerMenu.TYPE.get(),RECIPE_TYPE,1,16,17,36);}
    private static final class Category implements IRecipeCategory<RecipeHolder<ArmorerRecipe>> {
        private final IDrawable icon;
        Category(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModItems.ARMORER_TABLE.get()));}
        public RecipeType<RecipeHolder<ArmorerRecipe>> getRecipeType(){return RECIPE_TYPE;}
        public ResourceLocation getRegistryName(RecipeHolder<ArmorerRecipe> holder){return holder.id();}
        public Component getTitle(){return ModItems.ARMORER_TABLE.get().getDescription();}
        public int getWidth(){return 132;}public int getHeight(){return 76;}public IDrawable getIcon(){return icon;}
        public void setRecipe(IRecipeLayoutBuilder layout,RecipeHolder<ArmorerRecipe> holder,IFocusGroup focus){
            ArmorerRecipe recipe=holder.value();
            for(int i=0;i<16;i++){
                var slot=layout.addSlot(RecipeIngredientRole.INPUT,(i%4)*18,(i/4)*18);
                int index=recipe.shapeless?i:(i/4<recipe.height&&i%4<recipe.width?(i/4)*recipe.width+i%4:-1);
                if(index>=0&&index<recipe.getIngredients().size())slot.addIngredients(recipe.getIngredients().get(index));
            }
            layout.addSlot(RecipeIngredientRole.OUTPUT,108,27).addItemStack(recipe.getResultItem(null));
        }
    }
}
