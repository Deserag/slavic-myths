package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import java.util.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import org.slavicmyths.kitchen.*;
public final class KitchenScreen extends AbstractContainerScreen<KitchenMenu>{
 private final Map<Button,Integer> recipes=new LinkedHashMap<>();
 public KitchenScreen(KitchenMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=240;imageHeight=232;}
 @Override protected void init(){super.init();recipes.clear();for(int i=0;i<KitchenRecipes.COUNT;i++){final int id=i;Button b=addRenderableWidget(Button.builder(KitchenRecipes.output(i).getDescription(),button->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id)).bounds(leftPos+8+i%2*114,topPos+54+i/2*15,110,14).build());recipes.put(b,i);}}
 @Override protected void renderBg(GuiGraphics m,float f,int x,int y){m.fill(leftPos,topPos,leftPos+240,topPos+232,0xff4b3627);for(net.minecraft.world.inventory.Slot s:menu.slots)m.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xffaa9276);}
 @Override protected void renderLabels(GuiGraphics m,int x,int y){m.drawString(font,title,8,5,0xffeee0c0,false);m.drawString(font,Component.translatable("kitchen.slavicmyths.slots"),8,18,0xffeee0c0,false);}
 @Override public void render(GuiGraphics m,int x,int y,float f){super.render(m,x,y,f);renderTooltip(m,x,y);for(Map.Entry<Button,Integer> e:recipes.entrySet()){Button b=e.getKey();if(x>=b.getX()&&x<b.getX()+b.getWidth()&&y>=b.getY()&&y<b.getY()+14){int id=e.getValue();List<Component> tips=new ArrayList<>();Item[] ingredients=KitchenRecipes.ingredients(id);for(int i=0;i<3;i++)tips.add(Component.literal((i+1)+": "+KitchenRecipes.count(id,i)+" × "+ingredients[i].getDescription().getString()));tips.add(KitchenRecipes.tool(id).getDescription());tips.add(Component.translatable("kitchen.slavicmyths.yield",KitchenRecipes.amount(id)));m.renderComponentTooltip(font,tips,x,y);}}}
}
