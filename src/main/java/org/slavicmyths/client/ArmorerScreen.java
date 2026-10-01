package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.slavicmyths.armorer.ArmorerMenu;
public final class ArmorerScreen extends ContainerScreen<ArmorerMenu> {
    public ArmorerScreen(ArmorerMenu m,PlayerInventory i,ITextComponent title){super(m,i,title);imageWidth=176;imageHeight=193;inventoryLabelY=99;}
    @Override protected void renderBg(MatrixStack p,float partial,int mx,int my){
        fill(p,leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff32251c);
        fill(p,leftPos+4,topPos+4,leftPos+172,topPos+189,0xff64513b);
        for(int x:new int[]{0,166})for(int y:new int[]{0,183})fill(p,leftPos+x,topPos+y,leftPos+x+10,topPos+y+10,0xff92918a);
        for(net.minecraft.inventory.container.Slot s:menu.slots){fill(p,leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xffb2a58e);fill(p,leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xff29231d);}
        font.draw(p,"→",leftPos+113,topPos+52,0xffdfc8a0);
        for(int x=28;x<95;x+=8){fill(p,leftPos+x,topPos+94,leftPos+x+3,topPos+96,0xffba9a66);}
    }
    @Override protected void renderLabels(MatrixStack m,int x,int y){font.draw(m,title,8,7,0xffeee1c6);font.draw(m,inventory.getDisplayName(),8,99,0xffeee1c6);}
    @Override public void render(MatrixStack p,int x,int y,float f){renderBackground(p);super.render(p,x,y,f);renderTooltip(p,x,y);}
}
