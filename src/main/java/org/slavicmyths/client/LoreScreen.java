package org.slavicmyths.client;

import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.FormattedCharSequence;
import java.util.List;

/** Opaque manuscript page; navigation and article scroll independently. */
public final class LoreScreen extends Screen {
 private final int mask;
 private int article, lineOffset, navOffset, left, top, bookWidth, bookHeight, navWidth;
 private static final String[] IDS={"intro","domovoy","leshy","shrine","altar","ritual","paths","silver","amber","thunder_axe","storm_staff","charms","herbs","ritual_tools","runes","reforging","water_fish","fishing_net","vodyanoy","rusalka","elder_vodyanoy","deep_pool","pool_pearl","pool_spear","depth_amulet","swamp_hut","abandoned_settlement","bog_causeway","flooded_shrine","fishing_camp","underwater_ruins","bandit_gangs","bandit_small","bandit_medium","ataman","arms_craft","woodlands","forest_whistle","nightingale"};
 public LoreScreen(int mask){super(Component.translatable("item.slavicmyths.lore_book"));this.mask=mask;}
 public static void open(int mask){Minecraft.getInstance().setScreen(new LoreScreen(mask));}
 private boolean unlocked(int i){return i==0||i>=6&&i<16||(mask&(1<<(i<6?i-1:i-11)))!=0;}
 private int navRows(){return Math.max(1,(bookHeight-48)/20);}
 private int pageRows(){return Math.max(1,(bookHeight-66)/12);}
 private List<FormattedCharSequence> lines(){return font.split(Component.translatable("book.slavicmyths."+IDS[article]+".text"),bookWidth-navWidth-40);}
 @Override protected void init(){
  bookWidth=Math.min(680,width-24);bookHeight=Math.min(440,height-24);left=(width-bookWidth)/2;top=(height-bookHeight)/2;navWidth=bookWidth/4;
  navOffset=Math.max(0,Math.min(navOffset,IDS.length-navRows()));clearWidgets();
  for(int row=0;row<navRows()&&navOffset+row<IDS.length;row++){
   final int selected=navOffset+row;boolean available=unlocked(selected);
   Button b=Button.builder(Component.translatable(available?"book.slavicmyths."+IDS[selected]+".title":"book.slavicmyths.locked"),button->{article=selected;lineOffset=0;init();}).bounds(left+8,top+32+row*20,navWidth-16,18).build();b.active=available;addRenderableWidget(b);
  }
 }
 @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
  if(x>=left&&x<left+navWidth){navOffset-= (int)Math.signum(vertical);init();}
  else lineOffset=Math.max(0,Math.min(Math.max(0,lines().size()-pageRows()),lineOffset-(int)Math.signum(vertical)*3));
  return true;
 }
 @Override public void render(GuiGraphics g,int x,int y,float partial){
  renderBackground(g,x,y,partial);
  g.fill(left,top,left+bookWidth,top+bookHeight,0xff423021);
  g.fill(left+4,top+4,left+bookWidth-4,top+bookHeight-4,0xffe9d9b6);
  g.fill(left+4,top+4,left+navWidth,top+bookHeight-4,0xffcfba95);
  g.drawString(font,title,left+10,top+12,0xff39271d,false);
  int textX=left+navWidth+16,textY=top+46,contentWidth=bookWidth-navWidth-40;
  var headings=font.split(Component.translatable("book.slavicmyths."+IDS[article]+".title"),contentWidth);
  g.drawString(font,headings.get(0),textX,top+15,0xff39271d,false);
  g.fill(textX,top+32,left+bookWidth-18,top+34,0xff84362e);
  var lines=lines();lineOffset=Math.max(0,Math.min(lineOffset,Math.max(0,lines.size()-pageRows())));
  g.enableScissor(textX,textY,left+bookWidth-18,top+bookHeight-12);
  for(int i=0;i<pageRows()&&lineOffset+i<lines.size();i++)g.drawString(font,lines.get(lineOffset+i),textX,textY+i*12,0xff30261d,false);
  g.disableScissor();
  if(lines.size()>pageRows()){
   int track=bookHeight-66,thumb=Math.max(12,track*pageRows()/lines.size()),offset=(track-thumb)*lineOffset/(lines.size()-pageRows());
   g.fill(left+bookWidth-12,textY,left+bookWidth-9,textY+track,0xffc1aa87);g.fill(left+bookWidth-12,textY+offset,left+bookWidth-9,textY+offset+thumb,0xff84362e);
  }
  int row=article-navOffset;if(row>=0&&row<navRows())g.fill(left+5,top+32+row*20,left+7,top+50+row*20,0xff84362e);
  super.render(g,x,y,partial);
 }
 @Override public boolean isPauseScreen(){return false;}
}
