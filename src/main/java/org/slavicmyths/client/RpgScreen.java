package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.*;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.*;
import org.slavicmyths.rpg.*;
public final class RpgScreen extends ContainerScreen<RpgMenu> {
 private int path;private boolean confirm;private final Map<Button,Integer> skillButtons=new LinkedHashMap<>();
 public RpgScreen(RpgMenu m,PlayerInventory inv,ITextComponent title){super(m,inv,title);imageWidth=280;imageHeight=240;}
 private TranslationTextComponent tr(String s,Object... args){return new TranslationTextComponent("rpg.slavicmyths."+s,args);}
 private void send(int action){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
 public void refresh(){if(minecraft!=null)init();}
 @Override protected void init(){super.init();buttons.clear();children.clear();skillButtons.clear();
  if(menu.anvil){
   addButton(new Button(leftPos+12,topPos+80,82,20,tr("forge"),b->send(0)));
   addButton(new Button(leftPos+99,topPos+80,82,20,tr("install"),b->send(1)));
   for(int i=0;i<3;i++){final int n=i;addButton(new Button(leftPos+12+i*88,topPos+110,84,20,tr("remove",i+1),b->send(2+n)));}
   return;
  }
  for(int i=0;i<4;i++){final int n=i;addButton(new Button(leftPos+8+i*67,topPos+44,65,20,new TranslationTextComponent("path.slavicmyths."+PathData.PATHS[i]),b->{path=n;confirm=false;refresh();}));}
  for(int row=0;row<6;row++){final int skill=path*6+row;String id=PathData.SKILLS[skill];boolean owned=RpgClient.data.getCompound("Skills").getInt(id)>0;
   Button button=addButton(new Button(leftPos+12,topPos+72+row*22,212,20,new StringTextComponent((owned?"+ ":"")+new TranslationTextComponent("skill.slavicmyths."+id).getString()+(owned?"":" — "+PathData.COST[row]+" XP")),b->send(10+skill)));button.active=!owned;skillButtons.put(button,skill);
   if(owned&&Abilities.active(skill))addButton(new Button(leftPos+228,topPos+72+row*22,38,20,new StringTextComponent(RpgClient.data.getInt("Active")==skill+1?"[G]":"G"),b->send(50+skill)));
  }
  addButton(new Button(leftPos+12,topPos+210,254,20,tr(confirm?"confirm_path":"choose_path"),b->{if(confirm){send(path);confirm=false;}else confirm=true;refresh();}));
 }
 @Override protected void renderBg(MatrixStack m,float partial,int x,int y){
  fill(m,leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff39332a);fill(m,leftPos+4,topPos+4,leftPos+imageWidth-4,topPos+imageHeight-4,0xff645640);
  if(menu.anvil){for(net.minecraft.inventory.container.Slot s:menu.slots){fill(m,leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xff25221d);fill(m,leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xff8d8270);}}
  else for(int i=0;i<4;i++){String id=PathData.PATHS[i];int color=id.equals(RpgClient.data.getString("Main"))?0xffd6b665:id.equals(RpgClient.data.getString("Secondary"))?0xffbec2c2:0xff39332a;fill(m,leftPos+7+i*67,topPos+43,leftPos+74+i*67,topPos+65,color);}
 }
 @Override protected void renderLabels(MatrixStack m,int x,int y){font.draw(m,title,10,8,0xf5e8ce);
  if(menu.anvil){font.draw(m,tr("item"),48,34,0xf5e8ce);font.draw(m,tr("material"),113,34,0xf5e8ce);font.draw(m,tr("catalyst"),185,34,0xf5e8ce);font.draw(m,tr("remove_warning"),12,136,0xf5e8ce);}
  else{String main=RpgClient.data.getString("Main"),secondary=RpgClient.data.getString("Secondary");font.draw(m,tr("paths_info",name(main),name(secondary)),10,21,0xf5e8ce);font.draw(m,tr("xp",RpgClient.xp),10,32,0xf5e8ce);}
 }
 private String name(String id){return id.isEmpty()?tr("none").getString():new TranslationTextComponent("path.slavicmyths."+id).getString();}
 @Override public void render(MatrixStack m,int x,int y,float partial){renderBackground(m);super.render(m,x,y,partial);renderTooltip(m,x,y);
  if(!menu.anvil){for(Map.Entry<Button,Integer> entry:skillButtons.entrySet()){Button b=entry.getKey();if(x>=b.x&&x<b.x+b.getWidth()&&y>=b.y&&y<b.y+20){int i=entry.getValue();List<ITextComponent> tips=new ArrayList<>();tips.add(new TranslationTextComponent("skill.slavicmyths."+PathData.SKILLS[i]+".desc"));if(i%6>0)tips.add(tr("prerequisite",new TranslationTextComponent("skill.slavicmyths."+PathData.SKILLS[i-1])));if(!PathData.gate(i).isEmpty())tips.add(tr("knowledge",new TranslationTextComponent("advancements.slavicmyths."+PathData.gate(i)+".title")));if(i%6>=3)tips.add(tr("main_only"));java.util.List<net.minecraft.util.IReorderingProcessor> lines=new ArrayList<>();for(ITextComponent tip:tips)lines.addAll(font.split(tip,Math.min(260,width-30)));renderTooltip(m,lines,x,y);}}
   if(y>=topPos+210&&y<topPos+230)renderTooltip(m,tr("init_"+PathData.PATHS[path]),x,y);
  }else if(y>=topPos+80&&y<topPos+100){if(x<leftPos+98)renderTooltip(m,tr("forge_materials_"+Runes.slots(menu.input.getItem(0))),x,y);else if(x<leftPos+184)renderTooltip(m,tr("install_help"),x,y);}
 }
}
