package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.*;

/** Crisp native pixel materials. Ornaments occupy the frame, never the reading surface. */
final class FolkUi {
 static final int INK=0xff36251b, RED=0xff842b25, GOLD=0xffd8b36a, GREEN=0xff385b32;
 static void panel(GuiGraphics g,int x,int y,int w,int h,int color){g.fill(x,y,x+w,y+h,0xff503525);g.fill(x+1,y+1,x+w-1,y+h-1,GOLD);g.fill(x+2,y+2,x+w-2,y+h-2,color);}
 static void frame(GuiGraphics g,int x,int y,int w,int h){
  g.fill(x,y,x+w,y+h,0xff291d17);g.fill(x+2,y+2,x+w-2,y+h-2,0xff69452d);
  for(int dy=4;dy<h-3;dy+=4)g.fill(x+3,y+dy,x+w-3,y+dy+1,dy%12==0?0xff3e2b20:0xff815637);
  panel(g,x+7,y+23,w-14,h-30,0xffead9b5);
  for(int dx:new int[]{2,w-10})for(int dy:new int[]{2,h-10}){g.fill(x+dx,y+dy,x+dx+8,y+dy+8,0xff494b49);g.fill(x+dx+3,y+dy+3,x+dx+5,y+dy+5,0xffb2ada0);}
  for(int dy=33;dy<h-14;dy+=14){g.fill(x+8,y+dy,x+10,y+dy+3,RED);g.fill(x+w-10,y+dy,x+w-8,y+dy+3,RED);}
 }
 static int text(GuiGraphics g,Font font,Component c,int x,int y,int width,int color,int maxLines){int n=0;for(var line:font.split(c,Math.max(20,width))){if(n>=maxLines)break;g.drawString(font,line,x,y+n*10,color,false);n++;}return n*10;}
 static void slot(GuiGraphics g,int x,int y,int size,boolean selected){panel(g,x,y,size,size,selected?0xff675842:0xffa28c6e);}
 static void reposition(AbstractContainerMenu menu,int index,int x,int y){Slot old=menu.slots.get(index);if(old instanceof PositionedSlot positioned)old=positioned.original;Slot replacement=new PositionedSlot(old,x,y);replacement.index=old.index;menu.slots.set(index,replacement);}
 private static final class PositionedSlot extends Slot {
  final Slot original;
  PositionedSlot(Slot old,int x,int y){super(old.container,old.getContainerSlot(),x,y);original=old;}
  @Override public boolean mayPlace(net.minecraft.world.item.ItemStack s){return original.mayPlace(s);}
  @Override public boolean mayPickup(net.minecraft.world.entity.player.Player p){return original.mayPickup(p);}
  @Override public int getMaxStackSize(){return original.getMaxStackSize();}
  @Override public int getMaxStackSize(net.minecraft.world.item.ItemStack s){return original.getMaxStackSize(s);}
  @Override public boolean isActive(){return original.isActive();}
 }
 static Button button(int x,int y,int w,int h,Component label,boolean selected,Button.OnPress press){return new PaintedButton(x,y,w,h,label,selected,press);}
 static Button iconButton(int x,int y,int w,int h,Component label,boolean selected,net.minecraft.world.item.ItemStack icon,Button.OnPress press){return new PaintedButton(x,y,w,h,label,selected,press,icon);}
 private static class PaintedButton extends Button {
  private final boolean selected;private final net.minecraft.world.item.ItemStack icon;
  PaintedButton(int x,int y,int w,int h,Component c,boolean selected,OnPress press){this(x,y,w,h,c,selected,press,net.minecraft.world.item.ItemStack.EMPTY);}
  PaintedButton(int x,int y,int w,int h,Component c,boolean selected,OnPress press,net.minecraft.world.item.ItemStack icon){super(x,y,w,h,c,press,DEFAULT_NARRATION);this.selected=selected;this.icon=icon;}
  @Override protected void renderWidget(GuiGraphics g,int mx,int my,float delta){
   int background=!active?0xff62574a:selected?RED:isHoveredOrFocused()?0xff765139:0xff443023;
   panel(g,getX(),getY(),getWidth(),getHeight(),background);
   var font=Minecraft.getInstance().font;int inset=icon.isEmpty()?0:19;if(!icon.isEmpty())g.renderItem(icon,getX()+4,getY()+(getHeight()-16)/2);var lines=font.split(getMessage(),getWidth()-10-inset);int rows=Math.min(lines.size(),Math.max(1,(getHeight()-4)/10));int y=getY()+(getHeight()-rows*10)/2+1;
   for(int i=0;i<rows;i++)g.drawString(font,lines.get(i),getX()+5+inset,y+i*10,active?0xfff3e2bf:0xffc5b99e,false);
  }
 }
 private FolkUi(){}
}
