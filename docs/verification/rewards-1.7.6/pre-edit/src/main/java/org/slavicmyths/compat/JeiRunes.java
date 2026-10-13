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
import org.slavicmyths.rpg.*;
import org.slavicmyths.registry.ModItems;

@JeiPlugin
public final class JeiRunes implements IModPlugin {
    private static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_crafting");
    private static final RecipeType<RecipeHolder<RuneCraftRecipe>> TYPE=RecipeType.createRecipeHolderType(ID);
    public ResourceLocation getPluginUid(){return ID;}
    public void registerItemSubtypes(ISubtypeRegistration r){
        for(int tier=1;tier<=3;tier++)r.registerSubtypeInterpreter(RuneFoundation.item("blank_rune_"+tier),new mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<ItemStack>(){
            public Object getSubtypeData(ItemStack s,mezz.jei.api.ingredients.subtypes.UidContext c){return s.get(org.slavicmyths.item.ItemState.RUNE_BASE.get());}
            public String getLegacyStringSubtypeInfo(ItemStack s,mezz.jei.api.ingredients.subtypes.UidContext c){var b=s.get(org.slavicmyths.item.ItemState.RUNE_BASE.get());return b==null?"":b.tier()+":"+b.material();}
        });
    }
    public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
    public void registerRecipes(IRecipeRegistration r){if(Minecraft.getInstance().level!=null)r.addRecipes(TYPE,Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RuneCraftRecipe.TYPE.get()));}
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(ModItems.RUNIC_ANVIL.get()),TYPE);}
    private static final class Category implements IRecipeCategory<RecipeHolder<RuneCraftRecipe>> {
        private final IDrawable icon;
        Category(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModItems.RUNIC_ANVIL.get()));}
        public RecipeType<RecipeHolder<RuneCraftRecipe>> getRecipeType(){return TYPE;}
        public ResourceLocation getRegistryName(RecipeHolder<RuneCraftRecipe> r){return r.id();}
        public Component getTitle(){return ModItems.RUNIC_ANVIL.get().getDescription();}
        public int getWidth(){return 150;}public int getHeight(){return 52;}public IDrawable getIcon(){return icon;}
        public void setRecipe(IRecipeLayoutBuilder b,RecipeHolder<RuneCraftRecipe> holder,IFocusGroup focus){var r=holder.value();for(int n=0;n<r.inputs().size();n++){var c=r.inputs().get(n);b.addSlot(RecipeIngredientRole.INPUT,n*28,5).addItemStacks(java.util.Arrays.asList(c.displayItems()));}if(r.chisel())b.addSlot(RecipeIngredientRole.CATALYST,84,5).addItemStack(new ItemStack(RuneFoundation.item("rune_chisel")));b.addSlot(RecipeIngredientRole.OUTPUT,128,5).addItemStack(r.getResultItem(null));}
        public void draw(RecipeHolder<RuneCraftRecipe> holder,mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,GuiGraphics g,double mx,double my){if(holder.value().chisel())g.drawString(Minecraft.getInstance().font,Component.translatable("rune_foundation.slavicmyths.durability"),0,30,0xff333333,false);}
    }
}
