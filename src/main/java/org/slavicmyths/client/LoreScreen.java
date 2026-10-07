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
 private int navRows(){return Math.max(1,(bookHeight-56)/24);}
 private int pageRows(){return Math.max(1,(bookHeight-articleTop()-16)/12);}
 private int articleTop(){return 27+font.split(Component.translatable("book.slavicmyths."+IDS[article]+".title"),bookWidth-navWidth-40).size()*10;}
 private List<FormattedCharSequence> lines(){return font.split(Component.translatable("book.slavicmyths."+IDS[article]+".text"),bookWidth-navWidth-40);}
 @Override protected void init(){
  bookWidth=Math.min(680,width-24);bookHeight=Math.min(440,height-24);left=(width-bookWidth)/2;top=(height-bookHeight)/2;navWidth=bookWidth/4;
  navOffset=Math.max(0,Math.min(navOffset,IDS.length-navRows()));clearWidgets();
  for(int row=0;row<navRows()&&navOffset+row<IDS.length;row++){
   final int selected=navOffset+row;boolean available=unlocked(selected);
   Button b=FolkUi.button(left+8,top+40+row*24,navWidth-16,22,Component.translatable(available?"book.slavicmyths."+IDS[selected]+".title":"book.slavicmyths.locked"),article==selected,button->{article=selected;lineOffset=0;init();});b.active=available;addRenderableWidget(b);
  }
 }
 @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
  if(x>=left&&x<left+navWidth){navOffset-= (int)Math.signum(vertical);init();}
  else lineOffset=Math.max(0,Math.min(Math.max(0,lines().size()-pageRows()),lineOffset-(int)Math.signum(vertical)*3));
  return true;
 }
 @Override public void render(GuiGraphics g,int x,int y,float partial){
  renderTransparentBackground(g);
  g.fill(left,top,left+bookWidth,top+bookHeight,0xff423021);
  g.fill(left+4,top+4,left+bookWidth-4,top+bookHeight-4,0xffe9d9b6);
  g.fill(left+4,top+4,left+navWidth,top+bookHeight-4,0xffcfba95);
  FolkUi.text(g,font,title,left+10,top+12,navWidth-20,FolkUi.INK,2);
  int textX=left+navWidth+16,textY=top+articleTop(),contentWidth=bookWidth-navWidth-40;
  var headings=font.split(Component.translatable("book.slavicmyths."+IDS[article]+".title"),contentWidth);
  for(int i=0;i<headings.size();i++)g.drawString(font,headings.get(i),textX,top+12+i*10,0xff39271d,false);
  g.fill(textX,textY-6,left+bookWidth-18,textY-4,0xff84362e);
  var lines=lines();lineOffset=Math.max(0,Math.min(lineOffset,Math.max(0,lines.size()-pageRows())));
  g.enableScissor(textX,textY,left+bookWidth-18,top+bookHeight-12);
  for(int i=0;i<pageRows()&&lineOffset+i<lines.size();i++)g.drawString(font,lines.get(lineOffset+i),textX,textY+i*12,0xff30261d,false);
  g.disableScissor();
  if(lines.size()>pageRows()){
   int track=bookHeight-articleTop()-16,thumb=Math.max(12,track*pageRows()/lines.size()),offset=(track-thumb)*lineOffset/(lines.size()-pageRows());
   g.fill(left+bookWidth-12,textY,left+bookWidth-9,textY+track,0xffc1aa87);g.fill(left+bookWidth-12,textY+offset,left+bookWidth-9,textY+offset+thumb,0xff84362e);
  }
  if(IDS.length>navRows()){int track=bookHeight-56,thumb=Math.max(12,track*navRows()/IDS.length),offset=(track-thumb)*navOffset/(IDS.length-navRows());g.fill(left+navWidth-7,top+40,left+navWidth-4,top+40+track,0xffc1aa87);g.fill(left+navWidth-7,top+40+offset,left+navWidth-4,top+40+offset+thumb,0xff84362e);}
  int row=article-navOffset;if(row>=0&&row<navRows())g.fill(left+5,top+40+row*24,left+7,top+62+row*24,0xff84362e);
  super.render(g,x,y,partial);
 }
 @Override public boolean mouseClicked(double x,double y,int button){if(x>=left+bookWidth-14&&x<left+bookWidth-7&&y>=top+articleTop()&&y<top+bookHeight-12){lineOffset=(int)((y-top-articleTop())/(bookHeight-articleTop()-16)*Math.max(0,lines().size()-pageRows()));return true;}if(x>=left+navWidth-9&&x<left+navWidth-2&&y>=top+40&&y<top+bookHeight-12){navOffset=(int)((y-top-40)/(bookHeight-56)*Math.max(0,IDS.length-navRows()));init();return true;}return super.mouseClicked(x,y,button);}
 @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
 @Override public boolean isPauseScreen(){return false;}
}
