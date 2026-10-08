package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slavicmyths.village.SettlementMenu;
public final class SettlementScreen extends AbstractContainerScreen<SettlementMenu>{
    public SettlementScreen(SettlementMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=SettlementMenu.COLUMNS*18+16;imageHeight=SettlementMenu.rows()*18+126;inventoryLabelX=(imageWidth-162)/2;inventoryLabelY=SettlementMenu.rows()*18+30;}
    protected void renderBg(GuiGraphics g,float partial,int mx,int my){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff302820);g.fill(leftPos+3,topPos+3,leftPos+imageWidth-3,topPos+imageHeight-3,0xffc6c6c6);for(var s:menu.slots){int x=leftPos+s.x,y=topPos+s.y;g.fill(x-1,y-1,x+17,y+17,0xff373737);g.fill(x,y,x+17,y+17,0xff8b8b8b);g.fill(x,y+16,x+17,y+17,0xffffffff);g.fill(x+16,y,x+17,y+17,0xffffffff);}}
    public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
