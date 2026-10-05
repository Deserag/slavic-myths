package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import org.slavicmyths.armorer.ArmorerMenu;
public final class ArmorerScreen extends AbstractContainerScreen<ArmorerMenu> {
    public ArmorerScreen(ArmorerMenu m,Inventory i,Component title){super(m,i,title);imageWidth=176;imageHeight=193;inventoryLabelY=99;}
    @Override protected void renderBg(GuiGraphics p,float partial,int mx,int my){
        p.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff32251c);
        p.fill(leftPos+4,topPos+4,leftPos+172,topPos+189,0xff64513b);
        for(int x:new int[]{0,166})for(int y:new int[]{0,183})p.fill(leftPos+x,topPos+y,leftPos+x+10,topPos+y+10,0xff92918a);
        for(net.minecraft.world.inventory.Slot s:menu.slots){p.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xffb2a58e);p.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xff29231d);}
        p.drawString(font,"→",leftPos+113,topPos+52,0xffdfc8a0,false);
        for(int x=28;x<95;x+=8){p.fill(leftPos+x,topPos+94,leftPos+x+3,topPos+96,0xffba9a66);}
    }
    @Override protected void renderLabels(GuiGraphics m,int x,int y){m.drawString(font,title,8,7,0xffeee1c6,false);m.drawString(font,playerInventoryTitle,8,99,0xffeee1c6,false);}
    @Override public void render(GuiGraphics p,int x,int y,float f){super.render(p,x,y,f);renderTooltip(p,x,y);}
}
