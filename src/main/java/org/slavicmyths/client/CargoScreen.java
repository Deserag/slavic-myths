package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import org.slavicmyths.flight.CargoMenu;
public final class CargoScreen extends AbstractContainerScreen<CargoMenu>{
 public CargoScreen(CargoMenu m,Inventory inv,Component t){super(m,inv,t);imageWidth=176;imageHeight=134;}
 @Override protected void renderBg(GuiGraphics m,float partial,int x,int y){m.fill(leftPos,topPos,leftPos+176,topPos+134,0xff493628);for(net.minecraft.world.inventory.Slot s:menu.slots)m.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xffad987b);}
 @Override protected void renderLabels(GuiGraphics m,int x,int y){m.drawString(font,title,8,6,0xffeee0c5,false);}
 @Override public void render(GuiGraphics m,int x,int y,float partial){super.render(m,x,y,partial);renderTooltip(m,x,y);}
}
