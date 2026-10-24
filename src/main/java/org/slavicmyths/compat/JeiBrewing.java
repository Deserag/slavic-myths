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
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.brewing.*;

/** Uses loaded vat recipes and the production beverage/quality functions. Client-only JEI entry. */
@JeiPlugin public final class JeiBrewing implements IModPlugin {
    private static final RecipeType<VatRecipe> VAT=new RecipeType<>(Brewing.id("vat"),VatRecipe.class);
    private static final RecipeType<Process> PROCESS=new RecipeType<>(Brewing.id("brew_process"),Process.class);
    public record Process(List<ItemStack> input,List<ItemStack> output,String equipment,String instruction){}
    public ResourceLocation getPluginUid(){return Brewing.id("brewing_guide");}
    public void registerCategories(IRecipeCategoryRegistration r){var h=r.getJeiHelpers().getGuiHelper();r.addRecipeCategories(new Vat(h),new Steps(h));}
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(Brewing.item("fermentation_vat")),VAT);for(String s:List.of("fermentation_vat","fruit_press","small_keg"))r.addRecipeCatalyst(new ItemStack(Brewing.item(s)),PROCESS);r.addRecipeCatalyst(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(Brewing.id("wooden_barrel"))),PROCESS);}
    public void registerRecipes(IRecipeRegistration r){
        var world=Minecraft.getInstance().level;if(world!=null)r.addRecipes(VAT,world.getRecipeManager().getAllRecipesFor(VatRecipe.TYPE.get()).stream().map(h->h.value()).toList());
        var steps=new ArrayList<Process>();
        steps.add(new Process(List.of(new ItemStack(Items.APPLE,4)),List.of(new ItemStack(Brewing.item("apple_juice_bottle")),new ItemStack(Brewing.item("fruit_pomace"))),"fruit_press","guide.brewing.press"));
        for(ItemStack berry:net.minecraft.world.item.crafting.Ingredient.of(org.slavicmyths.kitchen.KitchenII.tag("berries")).getItems())steps.add(new Process(List.of(berry.copyWithCount(4)),List.of(new ItemStack(Brewing.item("berry_mash"),2),new ItemStack(Brewing.item("fruit_pomace"))),"fruit_press","guide.brewing.press"));
        Brewing.BATCHES.forEach((batch,beverage)->{
            String vessel=BeverageItem.mugAllowed(beverage)?"wooden_mug":"dark_bottle";
            for(String quality:List.of("young","ready","aged")){
                var out=BeverageItem.filled(beverage,quality,vessel,1);out.setCount(8);
                steps.add(new Process(List.of(new ItemStack(Brewing.item(batch)),new ItemStack(Brewing.item(vessel),8)),List.of(out),"fermentation_vat","guide.brewing."+quality));
                if(quality.equals("ready"))steps.add(new Process(List.of(out.copy()),List.of(out.copy()),"small_keg","guide.brewing.keg"));
            }
        });
        r.addRecipes(PROCESS,steps);
        for(var h:Brewing.ITEMS.values())r.addIngredientInfo(new ItemStack(h.get()),VanillaTypes.ITEM_STACK,Component.translatable("guide.brewing.overview"));
    }
    private static final class Vat implements IRecipeCategory<VatRecipe>{
        final IDrawable icon;Vat(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(Brewing.item("fermentation_vat")));}
        public RecipeType<VatRecipe> getRecipeType(){return VAT;}public Component getTitle(){return Component.translatable("block.slavicmyths.fermentation_vat");}public IDrawable getIcon(){return icon;}public int getWidth(){return 180;}public int getHeight(){return 100;}
        public void setRecipe(IRecipeLayoutBuilder b,VatRecipe r,IFocusGroup f){for(int i=0;i<r.parts().size();i++){var part=r.parts().get(i);b.addSlot(RecipeIngredientRole.INPUT,i*20,4).addItemStacks(Arrays.stream(part.ingredient().getItems()).map(s->s.copyWithCount(part.count())).toList());}if(!r.catalyst().isEmpty())b.addSlot(r.retainCatalyst()?RecipeIngredientRole.CATALYST:RecipeIngredientRole.INPUT,90,4).addIngredients(r.catalyst());b.addSlot(RecipeIngredientRole.OUTPUT,156,4).addItemStack(r.result());for(int i=0;i<r.remainders().size();i++)b.addSlot(RecipeIngredientRole.OUTPUT,130+i*22,27).addItemStack(r.remainders().get(i));}
        public void draw(VatRecipe r,mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,GuiGraphics g,double x,double y){var font=Minecraft.getInstance().font;g.drawString(font,Component.translatable("guide.brewing.time",r.time()/20),0,50,0xff333333,false);g.drawWordWrap(font,Component.translatable("guide.brewing.vat"),0,64,178,0xff333333);}
    }
    private static final class Steps implements IRecipeCategory<Process>{
        final IDrawable icon;Steps(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(Brewing.item("fruit_press")));}
        public RecipeType<Process> getRecipeType(){return PROCESS;}public Component getTitle(){return Component.translatable("guide.brewing.title");}public IDrawable getIcon(){return icon;}public int getWidth(){return 180;}public int getHeight(){return 112;}
        public void setRecipe(IRecipeLayoutBuilder b,Process r,IFocusGroup f){for(int i=0;i<r.input.size();i++)b.addSlot(RecipeIngredientRole.INPUT,i*22,4).addItemStack(r.input.get(i));for(int i=0;i<r.output.size();i++)b.addSlot(RecipeIngredientRole.OUTPUT,130+i*22,4).addItemStack(r.output.get(i));b.addSlot(RecipeIngredientRole.CATALYST,88,4).addItemStack(new ItemStack(Brewing.item(r.equipment)));}
        public void draw(Process r,mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,GuiGraphics g,double x,double y){g.drawWordWrap(Minecraft.getInstance().font,Component.translatable(r.instruction),0,30,178,0xff333333);}
    }
}
