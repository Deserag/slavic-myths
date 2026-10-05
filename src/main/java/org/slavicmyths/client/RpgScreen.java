package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import java.util.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import org.slavicmyths.rpg.*;
public final class RpgScreen extends AbstractContainerScreen<RpgMenu> {
 private int path;private boolean confirm;private final Map<Button,Integer> skillButtons=new LinkedHashMap<>();
 public RpgScreen(RpgMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=280;imageHeight=240;}
 private MutableComponent tr(String s,Object... args){return Component.translatable("rpg.slavicmyths."+s,args);}
 private void send(int action){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
 public void refresh(){if(minecraft!=null)init();}
 @Override protected void init(){super.init();clearWidgets();skillButtons.clear();
  if(menu.anvil){
   addRenderableWidget(Button.builder(tr("forge"),b->send(0)).bounds(leftPos+12,topPos+80,82,20).build());
   addRenderableWidget(Button.builder(tr("install"),b->send(1)).bounds(leftPos+99,topPos+80,82,20).build());
   for(int i=0;i<3;i++){final int n=i;addRenderableWidget(Button.builder(tr("remove",i+1),b->send(2+n)).bounds(leftPos+12+i*88,topPos+110,84,20).build());}
   return;
  }
  for(int i=0;i<4;i++){final int n=i;addRenderableWidget(Button.builder(Component.translatable("path.slavicmyths."+PathData.PATHS[i]),b->{path=n;confirm=false;refresh();}).bounds(leftPos+8+i*67,topPos+44,65,20).build());}
  for(int row=0;row<6;row++){final int skill=path*6+row;String id=PathData.SKILLS[skill];boolean owned=RpgClient.data.getCompound("Skills").getInt(id)>0;
   Button button=addRenderableWidget(Button.builder(Component.literal((owned?"+ ":"")+Component.translatable("skill.slavicmyths."+id).getString()+(owned?"":" — "+PathData.COST[row]+" XP")),b->send(10+skill)).bounds(leftPos+12,topPos+72+row*22,212,20).build());button.active=!owned;skillButtons.put(button,skill);
   if(owned&&Abilities.active(skill))addRenderableWidget(Button.builder(Component.literal(RpgClient.data.getInt("Active")==skill+1?"[G]":"G"),b->send(50+skill)).bounds(leftPos+228,topPos+72+row*22,38,20).build());
  }
  addRenderableWidget(Button.builder(tr(confirm?"confirm_path":"choose_path"),b->{if(confirm){send(path);confirm=false;}else confirm=true;refresh();}).bounds(leftPos+12,topPos+210,254,20).build());
 }
 @Override protected void renderBg(GuiGraphics m,float partial,int x,int y){
  m.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff39332a);m.fill(leftPos+4,topPos+4,leftPos+imageWidth-4,topPos+imageHeight-4,0xff645640);
  if(menu.anvil){for(net.minecraft.world.inventory.Slot s:menu.slots){m.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xff25221d);m.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xff8d8270);}}
  else for(int i=0;i<4;i++){String id=PathData.PATHS[i];int color=id.equals(RpgClient.data.getString("Main"))?0xffd6b665:id.equals(RpgClient.data.getString("Secondary"))?0xffbec2c2:0xff39332a;m.fill(leftPos+7+i*67,topPos+43,leftPos+74+i*67,topPos+65,color);}
 }
 @Override protected void renderLabels(GuiGraphics m,int x,int y){m.drawString(font,title,10,8,0xf5e8ce,false);
  if(menu.anvil){m.drawString(font,tr("item"),48,34,0xf5e8ce,false);m.drawString(font,tr("material"),113,34,0xf5e8ce,false);m.drawString(font,tr("catalyst"),185,34,0xf5e8ce,false);m.drawString(font,tr("remove_warning"),12,136,0xf5e8ce,false);}
  else{String main=RpgClient.data.getString("Main"),secondary=RpgClient.data.getString("Secondary");m.drawString(font,tr("paths_info",name(main),name(secondary)),10,21,0xf5e8ce,false);m.drawString(font,tr("xp",RpgClient.xp),10,32,0xf5e8ce,false);}
 }
 private String name(String id){return id.isEmpty()?tr("none").getString():Component.translatable("path.slavicmyths."+id).getString();}
 @Override public void render(GuiGraphics m,int x,int y,float partial){super.render(m,x,y,partial);renderTooltip(m,x,y);
  if(!menu.anvil){for(Map.Entry<Button,Integer> entry:skillButtons.entrySet()){Button b=entry.getKey();if(x>=b.getX()&&x<b.getX()+b.getWidth()&&y>=b.getY()&&y<b.getY()+20){int i=entry.getValue();List<Component> tips=new ArrayList<>();tips.add(Component.translatable("skill.slavicmyths."+PathData.SKILLS[i]+".desc"));if(i%6>0)tips.add(tr("prerequisite",Component.translatable("skill.slavicmyths."+PathData.SKILLS[i-1])));if(!PathData.gate(i).isEmpty())tips.add(tr("knowledge",Component.translatable("advancements.slavicmyths."+PathData.gate(i)+".title")));if(i%6>=3)tips.add(tr("main_only"));java.util.List<net.minecraft.util.FormattedCharSequence> lines=new ArrayList<>();for(Component tip:tips)lines.addAll(font.split(tip,Math.min(260,width-30)));m.renderTooltip(font,lines,x,y);}}
   if(y>=topPos+210&&y<topPos+230)m.renderTooltip(font,tr("init_"+PathData.PATHS[path]),x,y);
  }else if(y>=topPos+80&&y<topPos+100){if(x<leftPos+98)m.renderTooltip(font,tr("forge_materials_"+Runes.slots(menu.input.getItem(0))),x,y);else if(x<leftPos+184)m.renderTooltip(font,tr("install_help"),x,y);}
 }
}
