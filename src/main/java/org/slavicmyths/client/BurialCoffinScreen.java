package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import org.slavicmyths.kurgan.BurialCoffinMenu;
public final class BurialCoffinScreen extends AbstractContainerScreen<BurialCoffinMenu>{
 public BurialCoffinScreen(BurialCoffinMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=154;inventoryLabelY=60;}
 @Override protected void renderBg(GuiGraphics m,float f,int x,int y){m.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff332a23);m.fill(leftPos+3,topPos+3,leftPos+imageWidth-3,topPos+imageHeight-3,0xffb0a087);for(net.minecraft.world.inventory.Slot s:menu.slots){m.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xff544c40);m.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xff897d68);}}
 @Override public void render(GuiGraphics m,int x,int y,float f){super.render(m,x,y,f);renderTooltip(m,x,y);}
}
