package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.slavicmyths.hunt.PouchMenu;
public final class PouchScreen extends ContainerScreen<PouchMenu>{
 public PouchScreen(PouchMenu m,PlayerInventory inv,ITextComponent t){super(m,inv,t);imageWidth=176;imageHeight=134;}
 @Override protected void renderBg(MatrixStack m,float partial,int x,int y){fill(m,leftPos,topPos,leftPos+176,topPos+134,0xff493628);for(net.minecraft.inventory.container.Slot s:menu.slots)fill(m,leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xffad987b);}
 @Override protected void renderLabels(MatrixStack m,int x,int y){font.draw(m,title,8,6,0xffeee0c5);}
 @Override public void render(MatrixStack m,int x,int y,float partial){renderBackground(m);super.render(m,x,y,partial);renderTooltip(m,x,y);}
}
