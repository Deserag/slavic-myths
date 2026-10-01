package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.slavicmyths.kurgan.BurialCoffinMenu;
public final class BurialCoffinScreen extends ContainerScreen<BurialCoffinMenu>{
 public BurialCoffinScreen(BurialCoffinMenu menu,PlayerInventory inv,ITextComponent title){super(menu,inv,title);imageWidth=176;imageHeight=154;inventoryLabelY=60;}
 @Override protected void renderBg(MatrixStack m,float f,int x,int y){fill(m,leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff332a23);fill(m,leftPos+3,topPos+3,leftPos+imageWidth-3,topPos+imageHeight-3,0xffb0a087);for(net.minecraft.inventory.container.Slot s:menu.slots){fill(m,leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xff544c40);fill(m,leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xff897d68);}}
 @Override public void render(MatrixStack m,int x,int y,float f){renderBackground(m);super.render(m,x,y,f);renderTooltip(m,x,y);}
}
