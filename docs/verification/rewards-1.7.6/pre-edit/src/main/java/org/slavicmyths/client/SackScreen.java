package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slavicmyths.storage.SackMenu;
public final class SackScreen extends AbstractContainerScreen<SackMenu>{
 public SackScreen(SackMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=143;inventoryLabelY=49;}
 protected void renderBg(GuiGraphics g,float partial,int mx,int my){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff5d4e36);g.fill(leftPos+3,topPos+3,leftPos+imageWidth-3,topPos+imageHeight-3,0xffd0c5a7);for(var s:menu.slots){int x=leftPos+s.x,y=topPos+s.y;g.fill(x-1,y-1,x+17,y+17,0xff524935);g.fill(x,y,x+17,y+17,0xff94856b);g.fill(x,y+16,x+17,y+17,0xffede4cc);}}
 public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
