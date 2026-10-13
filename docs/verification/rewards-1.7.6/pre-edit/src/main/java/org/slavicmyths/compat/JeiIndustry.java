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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.industry.*;
@JeiPlugin
public final class JeiIndustry implements IModPlugin {
    private static final RecipeType<RecipeHolder<MachineRecipe>> BLOOMERY=RecipeType.createRecipeHolderType(Industry.id("chud_bloomery")),HEARTH=RecipeType.createRecipeHolderType(Industry.id("refining_hearth"));
    public ResourceLocation getPluginUid(){return Industry.id("industry");}
    public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper(),false),new Category(r.getJeiHelpers().getGuiHelper(),true));}
    public void registerRecipes(IRecipeRegistration r){if(Minecraft.getInstance().level!=null){var manager=Minecraft.getInstance().level.getRecipeManager();r.addRecipes(BLOOMERY,manager.getAllRecipesFor(MachineRecipe.BLOOMERY.get()));r.addRecipes(HEARTH,manager.getAllRecipesFor(MachineRecipe.HEARTH.get()));}}
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(Industry.BLOOMERY_ITEM.get()),BLOOMERY);r.addRecipeCatalyst(new ItemStack(Industry.HEARTH_ITEM.get()),HEARTH);}
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration r){r.addRecipeTransferHandler(MachineMenu.class,MachineMenu.BLOOMERY.get(),BLOOMERY,0,1,3,36);r.addRecipeTransferHandler(MachineMenu.class,MachineMenu.HEARTH.get(),HEARTH,0,2,4,36);}
    private static final class Category implements IRecipeCategory<RecipeHolder<MachineRecipe>> {
        final boolean refining;final IDrawable icon;
        Category(IGuiHelper h,boolean refining){this.refining=refining;icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(refining?Industry.HEARTH_ITEM.get():Industry.BLOOMERY_ITEM.get()));}
        public RecipeType<RecipeHolder<MachineRecipe>> getRecipeType(){return refining?HEARTH:BLOOMERY;}
        public Component getTitle(){return Component.translatable("block.slavicmyths."+(refining?"refining_hearth":"chud_bloomery"));}
        public ResourceLocation getRegistryName(RecipeHolder<MachineRecipe> r){return r.id();}
        public int getWidth(){return 140;}public int getHeight(){return 60;}public IDrawable getIcon(){return icon;}
        public void setRecipe(IRecipeLayoutBuilder b,RecipeHolder<MachineRecipe> holder,IFocusGroup f){var r=holder.value();b.addSlot(RecipeIngredientRole.INPUT,5,8).addIngredients(r.input());if(refining)b.addSlot(RecipeIngredientRole.INPUT,28,8).addIngredients(r.flux());b.addSlot(RecipeIngredientRole.OUTPUT,110,8).addItemStack(r.result());}
        public void draw(RecipeHolder<MachineRecipe> holder,mezz.jei.api.gui.ingredient.IRecipeSlotsView view,GuiGraphics g,double mx,double my){var font=Minecraft.getInstance().font;g.drawString(font,"→",72,12,0xffdddddd,false);g.drawString(font,Component.translatable("industry.slavicmyths.processing",holder.value().duration()/20),5,36,0xffdddddd,false);}
    }
}
