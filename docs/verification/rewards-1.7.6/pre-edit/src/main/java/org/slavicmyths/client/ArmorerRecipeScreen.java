package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slavicmyths.armorer.ArmorerRecipe;

/** Read-only view of the actual synchronized bench recipes, available without optional JEI. */
public final class ArmorerRecipeScreen extends Screen {
    private final ArmorerScreen parent;
    private final List<RecipeHolder<ArmorerRecipe>> recipes;
    private int selected,left,top;
    public ArmorerRecipeScreen(ArmorerScreen parent) {
        super(Component.translatable("weapon.slavicmyths.recipes"));this.parent=parent;
        var level=Minecraft.getInstance().level;
        recipes=level==null?List.of():level.getRecipeManager().getAllRecipesFor(ArmorerRecipe.TYPE.get()).stream().sorted(Comparator.comparing(h->h.id().toString())).toList();
    }
    @Override protected void init() {
        left=(width-210)/2;top=(height-174)/2;
        addRenderableWidget(Button.builder(Component.literal("<"),b->{selected=Math.floorMod(selected-1,recipes.size());}).bounds(left+10,top+144,25,20).build()).active=!recipes.isEmpty();
        addRenderableWidget(Button.builder(Component.literal(">"),b->{selected=(selected+1)%recipes.size();}).bounds(left+175,top+144,25,20).build()).active=!recipes.isEmpty();
    }
    private ItemStack ingredient(ArmorerRecipe recipe,int cell) {
        int index=recipe.shapeless?cell:(cell/4<recipe.height&&cell%4<recipe.width?(cell/4)*recipe.width+cell%4:-1);
        if(index<0||index>=recipe.getIngredients().size())return ItemStack.EMPTY;
        var alternatives=recipe.getIngredients().get(index).getItems();
        return alternatives.length==0?ItemStack.EMPTY:alternatives[(int)(minecraft.level.getGameTime()/20%alternatives.length)];
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderTransparentBackground(g);g.fill(left,top,left+210,top+174,0xff423021);g.fill(left+3,top+3,left+207,top+171,0xffd8c39b);
        g.drawString(font,title,left+10,top+8,0xff30261d,false);
        if(!recipes.isEmpty()) {
            var recipe=recipes.get(selected).value();ItemStack output=recipe.getResultItem(minecraft.level.registryAccess());
            FolkUi.text(g,font,output.getHoverName(),left+10,top+22,190,0xff30261d,2);
            ItemStack hover=ItemStack.EMPTY;
            for(int n=0;n<16;n++) {
                int x=left+18+n%4*18,y=top+48+n/4*18;g.fill(x-1,y-1,x+17,y+17,0xff64513b);ItemStack item=ingredient(recipe,n);
                if(!item.isEmpty())g.renderItem(item,x,y);
                if(mx>=x&&mx<x+16&&my>=y&&my<y+16)hover=item;
            }
            g.drawString(font,"→",left+105,top+79,0xff30261d,false);g.renderItem(output,left+144,top+76);
            if(mx>=left+144&&mx<left+160&&my>=top+76&&my<top+92)hover=output;
            g.drawString(font,Component.translatable(recipe.shapeless?"weapon.slavicmyths.shapeless":"weapon.slavicmyths.shaped"),left+10,top+124,0xff30261d,false);
            if(recipe.copyComponents)g.drawString(font,Component.translatable("weapon.slavicmyths.keeps_data"),left+10,top+135,0xff30261d,false);
            g.drawCenteredString(font,(selected+1)+" / "+recipes.size(),left+105,top+151,0xff30261d);
            super.render(g,mx,my,partial);
            if(!hover.isEmpty())g.renderTooltip(font,hover,mx,my);
        }else {g.drawString(font,Component.translatable("weapon.slavicmyths.no_recipes"),left+10,top+50,0xff30261d,false);super.render(g,mx,my,partial);}
    }
    @Override public void tick(){if(minecraft.player==null||minecraft.player.containerMenu!=parent.getMenu())minecraft.setScreen(null);}
    @Override public void onClose(){if(minecraft.player!=null&&minecraft.player.containerMenu==parent.getMenu())minecraft.setScreen(parent);else minecraft.setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial) { }
}
