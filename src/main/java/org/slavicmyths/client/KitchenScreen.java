package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.*;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.util.text.*;
import org.slavicmyths.kitchen.*;
public final class KitchenScreen extends ContainerScreen<KitchenMenu>{
 private final Map<Button,Integer> recipes=new LinkedHashMap<>();
 public KitchenScreen(KitchenMenu m,PlayerInventory inv,ITextComponent title){super(m,inv,title);imageWidth=240;imageHeight=232;}
 @Override protected void init(){super.init();recipes.clear();for(int i=0;i<KitchenRecipes.COUNT;i++){final int id=i;Button b=addButton(new Button(leftPos+8+i%2*114,topPos+54+i/2*15,110,14,KitchenRecipes.output(i).getDescription(),button->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id)));recipes.put(b,i);}}
 @Override protected void renderBg(MatrixStack m,float f,int x,int y){fill(m,leftPos,topPos,leftPos+240,topPos+232,0xff4b3627);for(net.minecraft.inventory.container.Slot s:menu.slots)fill(m,leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xffaa9276);}
 @Override protected void renderLabels(MatrixStack m,int x,int y){font.draw(m,title,8,5,0xffeee0c0);font.draw(m,new TranslationTextComponent("kitchen.slavicmyths.slots"),8,18,0xffeee0c0);}
 @Override public void render(MatrixStack m,int x,int y,float f){renderBackground(m);super.render(m,x,y,f);renderTooltip(m,x,y);for(Map.Entry<Button,Integer> e:recipes.entrySet()){Button b=e.getKey();if(x>=b.x&&x<b.x+b.getWidth()&&y>=b.y&&y<b.y+14){int id=e.getValue();List<ITextComponent> tips=new ArrayList<>();Item[] ingredients=KitchenRecipes.ingredients(id);for(int i=0;i<3;i++)tips.add(new StringTextComponent((i+1)+": "+KitchenRecipes.count(id,i)+" × "+ingredients[i].getDescription().getString()));tips.add(KitchenRecipes.tool(id).getDescription());tips.add(new TranslationTextComponent("kitchen.slavicmyths.yield",KitchenRecipes.amount(id)));renderComponentTooltip(m,tips,x,y);}}}
}
