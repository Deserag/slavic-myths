package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slavicmyths.brewing.VatMenu;
public final class VatScreen extends AbstractContainerScreen<VatMenu>{
 public VatScreen(VatMenu m,Inventory i,Component c){super(m,i,c);imageWidth=244;imageHeight=198;inventoryLabelX=32;inventoryLabelY=102;}
 protected void renderBg(GuiGraphics g,float partial,int mx,int my){g.fill(leftPos,topPos,leftPos+244,topPos+198,0xff342c24);g.fill(leftPos+3,topPos+3,leftPos+241,topPos+195,0xffd5c7a1);for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff75674f);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xffb5a582);}int total=menu.data.get(1),progress=menu.data.get(0);if(total>0){g.fill(leftPos+154,topPos+79,leftPos+224,topPos+85,0xff665238);g.fill(leftPos+154,topPos+79,leftPos+154+70*progress/total,topPos+85,0xff859847);}}
 protected void renderLabels(GuiGraphics g,int mx,int my){super.renderLabels(g,mx,my);String[]keys={"inputs","returns","outputs","catalyst"};int[]xs={16,112,176,98};int[]ys={28,28,28,64};for(int i=0;i<4;i++)g.drawString(font,Component.translatable("brewing.slavicmyths."+keys[i]),xs[i],ys[i],0xff493d2b,false);int mode=menu.data.get(2);if(mode>0)g.drawString(font,Component.translatable("brewing.slavicmyths.mode_"+mode),154,91,0xff493d2b,false);}
 public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
