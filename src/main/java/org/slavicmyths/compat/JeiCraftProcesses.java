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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.storage.DryingRecipe;
import org.slavicmyths.rpg.RuneFoundation;

@JeiPlugin public final class JeiCraftProcesses implements IModPlugin {
    public record Process(Ingredient input,int count,ItemStack output,String station,int seconds,String hint){}
    private static final RecipeType<Process> TYPE=new RecipeType<>(id("craft_processes"),Process.class);
    static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
    static ItemStack stack(String s){return new ItemStack(BuiltInRegistries.ITEM.get(id(s)));}
    public ResourceLocation getPluginUid(){return id("craft_process_guide");}
    public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category(r.getJeiHelpers().getGuiHelper()));}
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r){for(String s:List.of("drying_rack","flax_breaker","spinning_wheel","loom_table"))r.addRecipeCatalyst(stack(s),TYPE);}
    public void registerRecipes(IRecipeRegistration r){
        var list=new ArrayList<Process>();var world=Minecraft.getInstance().level;
        if(world!=null)for(var h:world.getRecipeManager().getAllRecipesFor(DryingRecipe.TYPE.get())){var d=h.value();list.add(new Process(d.ingredient(),1,d.result(),"drying_rack",d.dryingTime()/20,"guide.process.drying"));}
        list.add(new Process(Ingredient.of(stack("flax_stalk")),4,stack("flax_fiber").copyWithCount(4),"flax_breaker",0,"guide.process.breaker"));
        list.add(new Process(Ingredient.of(stack("flax_fiber")),2,stack("linen_thread"),"spinning_wheel",5,"guide.process.textile"));
        list.add(new Process(Ingredient.of(stack("linen_thread")),4,stack("linen_cloth"),"loom_table",8,"guide.process.textile"));r.addRecipes(TYPE,list);
        for(var h:RuneFoundation.ITEMS.values())r.addIngredientInfo(new ItemStack(h.get()),VanillaTypes.ITEM_STACK,Component.translatable("guide.runes"));
        r.addIngredientInfo(new ItemStack(RuneFoundation.item("soul_fragment")),VanillaTypes.ITEM_STACK,Component.translatable("guide.runes.fragments"));
        for(String s:List.of("silver_ingot","silver_ore","ancient_sign"))r.addIngredientInfo(stack(s),VanillaTypes.ITEM_STACK,Component.translatable("guide.runes"));
        for(String s:org.slavicmyths.garden.Gardens.BERRIES){r.addIngredientInfo(stack(s),VanillaTypes.ITEM_STACK,Component.translatable("guide.farming"));r.addIngredientInfo(stack(s+"_sapling"),VanillaTypes.ITEM_STACK,Component.translatable("guide.farming"));}
    }
    private static final class Category implements IRecipeCategory<Process>{
        final IDrawable icon;Category(IGuiHelper h){icon=h.createDrawableIngredient(VanillaTypes.ITEM_STACK,stack("spinning_wheel"));}
        public RecipeType<Process> getRecipeType(){return TYPE;}public Component getTitle(){return Component.translatable("guide.process.title");}public IDrawable getIcon(){return icon;}public int getWidth(){return 180;}public int getHeight(){return 100;}
        public void setRecipe(IRecipeLayoutBuilder b,Process r,IFocusGroup f){b.addSlot(RecipeIngredientRole.INPUT,4,4).addItemStacks(Arrays.stream(r.input.getItems()).map(s->s.copyWithCount(r.count)).toList());b.addSlot(RecipeIngredientRole.CATALYST,78,4).addItemStack(stack(r.station));b.addSlot(RecipeIngredientRole.OUTPUT,154,4).addItemStack(r.output);}
        public void draw(Process r,mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,GuiGraphics g,double x,double y){var font=Minecraft.getInstance().font;if(r.seconds>0)g.drawString(font,Component.translatable("guide.brewing.time",r.seconds),0,29,0xff333333,false);g.drawWordWrap(font,Component.translatable(r.hint),0,44,178,0xff333333);}
    }
}
