package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.kitchen.*;
public final class KitchenScreen extends AbstractContainerScreen<KitchenMenu>{
 private Button serve;
 public KitchenScreen(KitchenMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=256;imageHeight=212;inventoryLabelX=47;inventoryLabelY=119;}
 protected void init(){super.init();serve=addRenderableWidget(Button.builder(Component.translatable("kitchen.slavicmyths.serve"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+158,topPos+101,86,18).build());}
 protected void renderBg(GuiGraphics g,float f,int x,int y){g.blit(KitchenII.id("textures/gui/kitchen_ii.png"),leftPos,topPos,0,0,256,212,256,256);g.drawCenteredString(font,title,leftPos+128,topPos+6,0xfff0dec0);
  g.drawString(font,Component.translatable("item.slavicmyths.rolling_pin"),leftPos+9,topPos+14,0xff34291f,false);g.drawString(font,Component.translatable("item.slavicmyths.metal_pot"),leftPos+150,topPos+14,0xff34291f,false);
  int total=menu.data.get(1);if(total>0)g.fill(leftPos+132,topPos+52,leftPos+132+Math.min(35,35*menu.data.get(0)/total),topPos+58,0xff947947);
  int n=menu.data.get(2);serve.active=n>0;g.drawString(font,Component.translatable("kitchen.slavicmyths.servings",n,menu.data.get(3)),leftPos+154,topPos+91,0xff34291f,false);
  if(minecraft.level.getBlockEntity(menu.pos)instanceof KitchenTile t&&!t.dish.isEmpty())g.renderItem(t.dish,leftPos+220,topPos+51);
 }
 protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xff34291f,false);}
 public void render(GuiGraphics g,int x,int y,float f){super.render(g,x,y,f);renderTooltip(g,x,y);}
}
