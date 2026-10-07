package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slavicmyths.kitchen.*;
/** Grouped slots share coordinates with KitchenMenu; process and serving remain server-owned. */
public final class KitchenScreen extends AbstractContainerScreen<KitchenMenu> {
 private Button serve;
 public KitchenScreen(KitchenMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=278;imageHeight=234;inventoryLabelX=58;inventoryLabelY=137;}
 private Component tr(String key,Object...args){return Component.translatable("screen.slavicmyths."+key,args);}
 protected void init(){super.init();serve=addRenderableWidget(Button.builder(Component.translatable("kitchen.slavicmyths.serve"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+174,topPos+111,96,18).build());}
 private void label(GuiGraphics g,String key,int x,int y){g.drawString(font,tr(key),leftPos+x,topPos+y,0xff34291f,false);}
 protected void renderBg(GuiGraphics g,float f,int x,int y){
  g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xffc6af89);g.fill(leftPos+4,topPos+4,leftPos+274,topPos+22,0xff604633);
  g.drawCenteredString(font,tr("kitchen_table"),leftPos+139,topPos+8,0xfff0dec0);
  label(g,"tools",12,27);label(g,"ingredients",96,27);label(g,"cooking",145,87);label(g,"result",204,27);label(g,"container_returns",172,64);
  for(var slot:menu.slots){int sx=leftPos+slot.x,sy=topPos+slot.y;g.fill(sx-1,sy-1,sx+17,sy+17,0xff665640);g.fill(sx,sy,sx+16,sy+16,0xffaa9776);}
  int total=menu.data.get(1);g.fill(leftPos+150,topPos+44,leftPos+188,topPos+50,0xff79674d);if(total>0)g.fill(leftPos+150,topPos+44,leftPos+150+Math.min(38,38*menu.data.get(0)/total),topPos+50,0xffae773e);
  int n=menu.data.get(2);serve.active=n>0;label(g,"pot",12,102);g.drawString(font,tr("servings",n,menu.data.get(3)),leftPos+58,topPos+102,0xff34291f,false);
  if(minecraft.level.getBlockEntity(menu.pos) instanceof KitchenTile t&&!t.dish.isEmpty())g.renderItem(t.dish,leftPos+20,topPos+114);
 }
 protected void renderLabels(GuiGraphics g,int x,int y){label(g,"inventory",inventoryLabelX,inventoryLabelY);}
 public void render(GuiGraphics g,int x,int y,float f){super.render(g,x,y,f);renderTooltip(g,x,y);}
}
