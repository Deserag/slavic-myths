package org.slavicmyths.client;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import org.slavicmyths.rpg.*;
/** Approved hierarchy; preview is a copy, all committed operations are server validated. */
public final class RpgScreen extends AbstractContainerScreen<RpgMenu>{
 private int path,skill,socket,listOffset,infoOffset,invY,infoX,infoY,infoW,infoH;private boolean confirm;private Button action,learn;private final Map<Button,Integer> rows=new LinkedHashMap<>();
 public RpgScreen(RpgMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=440;imageHeight=320;}
 private Component tr(String s,Object... a){return Component.translatable("rpg.slavicmyths."+s,a);}
 private void send(int n){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,n);}
 private boolean creative(){return minecraft.player.isCreative();}
 public void refresh(){if(minecraft!=null)init();}
 private Button button(int x,int y,int w,int h,Component c,boolean selected,Button.OnPress press){return addRenderableWidget(FolkUi.button(leftPos+x,topPos+y,w,h,c,selected,press));}
 private int operation(){return menu.mode==2?2+socket:menu.mode;}
 private boolean owned(int i){return RpgClient.data.getCompound("Skills").getInt(PathData.SKILLS[i])>0;}
 @Override protected void init(){imageWidth=Math.min(440,width-12);imageHeight=Math.min(menu.anvil?320:360,height);super.init();clearWidgets();rows.clear();invY=imageHeight-80;
  if(menu.anvil){
   FolkUi.reposition(menu,0,imageWidth/5-8,45);FolkUi.reposition(menu,1,imageWidth/2-8,45);
   for(int i=2;i<menu.slots.size();i++){int n=i-2;FolkUi.reposition(menu,i,(imageWidth-162)/2+n%9*18,invY+(n<27?n/9*18:58));}
   for(int i=0;i<3;i++){final int mode=i;button(12+i*(imageWidth-24)/3,74,(imageWidth-24)/3-3,22,tr(new String[]{"forge","apply_mode","remove_mode"}[i]),menu.mode==i,b->{menu.mode=mode;infoOffset=0;send(100+mode);refresh();});}
   for(int i=0;i<3;i++){final int n=i;button(20+i*48,116,42,invY<180?22:30,Component.literal(new String[]{"I","II","III"}[i]),socket==i,b->{socket=n;infoOffset=0;});}
   action=button(16,invY-20,146,18,tr(menu.mode==0?"forge":menu.mode==1?"apply_mode":"remove_mode"),true,b->send(operation()));
   infoX=180;infoY=107;infoW=imageWidth-198;infoH=Math.max(10,invY-infoY-30);
  }else{
   for(int i=0;i<4;i++){final int n=i;addRenderableWidget(FolkUi.iconButton(leftPos+12+i*(imageWidth-24)/4,topPos+67,(imageWidth-24)/4-3,24,Component.translatable("path.slavicmyths."+PathData.PATHS[i]),path==i,new ItemStack(pathIcon(i)),b->{path=n;skill=n*6;listOffset=0;infoOffset=0;confirm=false;refresh();}));}
   int count=Math.max(1,(imageHeight-143)/50),listW=(imageWidth-36)*3/5;listOffset=Math.max(0,Math.min(listOffset,6-count));
   for(int r=0;r<count&&r+listOffset<6;r++){final int i=path*6+r+listOffset;rows.put(button(23,99+r*50,listW-14,48,Component.translatable("skill.slavicmyths."+PathData.SKILLS[i]),skill==i,b->{skill=i;infoOffset=0;}),i);}
   infoX=listW+28;infoY=107;infoW=imageWidth-infoX-18;infoH=Math.max(20,imageHeight-infoY-70);
   learn=button(infoX,imageHeight-61,infoW,21,tr("learn_action"),true,b->{if(owned(skill)&&Abilities.active(skill))send(50+skill);else send(10+skill);});
   action=button(12,imageHeight-34,(imageWidth-40)/2,23,tr(confirm?"confirm_path":"choose_path"),true,b->{if(confirm){send(path);confirm=false;}else confirm=true;refresh();});
   Button reset=button(imageWidth/2+5,imageHeight-34,(imageWidth-40)/2,23,tr("reset"),false,b->{});reset.active=false;reset.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tr("reset_unavailable")));
  }
 }
 private Item pathIcon(int i){return new Item[]{org.slavicmyths.registry.ModItems.RETAINER_SHIELD.get(),org.slavicmyths.registry.ModItems.CARVED_STAFF.get(),org.slavicmyths.registry.ModItems.SILVER_DAGGER.get(),org.slavicmyths.registry.ModItems.ANCIENT_SIGN.get()}[i];}
 private String name(String id){return id.isEmpty()?tr("none").getString():Component.translatable("path.slavicmyths."+id).getString();}
 private Component skillBlocker(int i){var d=RpgClient.data;String id=PathData.PATHS[i/6];boolean main=id.equals(d.getString("Main")),secondary=id.equals(d.getString("Secondary"));
  if(owned(i))return tr("unlocked");if(!main&&!secondary||secondary&&i%6>=3||PathData.points(d,id)>=(main?30:10))return tr("skill_locked");
  if(i%6>0&&!owned(i-1))return tr("prerequisite",Component.translatable("skill.slavicmyths."+PathData.SKILLS[i-1]));
  if(!PathData.gate(i).isEmpty()&&!d.getCompound("KnownGates").getBoolean(PathData.gate(i)))return tr("knowledge",Component.translatable("advancements.slavicmyths."+PathData.gate(i)+".title"));
  if(!creative()&&RpgClient.xp<PathData.COST[i%6])return tr("need_xp",PathData.COST[i%6]);return null;}
 private Component chooseBlocker(){var d=RpgClient.data;if(!d.getString("Main").isEmpty()&&(!d.getString("Secondary").isEmpty()||PathData.PATHS[path].equals(d.getString("Main"))||PathData.points(d,d.getString("Main"))<3))return tr("secondary_locked");if(!creative()&&RpgClient.xp<10)return tr("need_xp",10);Item[][] offers={{Items.IRON_SWORD,Items.SHIELD},{org.slavicmyths.registry.ModItems.WARDING_CHARM.get(),org.slavicmyths.registry.ModItems.WORMWOOD.get()},{Items.BOW,org.slavicmyths.registry.ModItems.SILVER_DAGGER.get()},{org.slavicmyths.registry.ModItems.CARVED_STAFF.get(),org.slavicmyths.registry.ModItems.ANCIENT_SIGN.get()}};for(int n=0;n<2;n++){int count=path==1&&n==1?4:1;if(Runes.inventoryCount(minecraft.player,offers[path][n])<count)return tr("missing_material",offers[path][n].getDescription(),count);}return null;}
 @Override protected void renderBg(GuiGraphics g,float f,int mx,int my){FolkUi.frame(g,leftPos,topPos,imageWidth,imageHeight);g.drawCenteredString(font,title,leftPos+imageWidth/2,topPos+9,0xfff1dfb7);
  if(menu.anvil){
   for(var s:menu.slots)if(s.isActive())FolkUi.slot(g,leftPos+s.x-1,topPos+s.y-1,18,false);
   int[] centers={imageWidth/5,imageWidth/2,imageWidth*4/5};for(int i=0;i<3;i++)if(i!=1||menu.mode!=2)FolkUi.slot(g,leftPos+centers[i]-18,topPos+35,36,i==2&&Runes.blocker(minecraft.player,menu.input,operation())==null&&!Runes.preview(menu.input,operation()).isEmpty());
   g.drawCenteredString(font,tr("item"),leftPos+centers[0],topPos+25,FolkUi.INK);if(menu.mode!=2)g.drawCenteredString(font,tr(menu.mode==0?"material":"rune"),leftPos+centers[1],topPos+25,FolkUi.INK);g.drawCenteredString(font,tr("result"),leftPos+centers[2],topPos+25,FolkUi.INK);
   g.drawString(font,menu.mode==2?"":"+",leftPos+imageWidth*7/20,topPos+48,FolkUi.INK,false);g.drawString(font,">",leftPos+(menu.mode==2?imageWidth/2:imageWidth*13/20),topPos+48,FolkUi.INK,false);
   var preview=Runes.preview(menu.input,operation());if(!preview.isEmpty())g.renderItem(preview,leftPos+centers[2]-8,topPos+45);
   FolkUi.panel(g,leftPos+12,topPos+100,154,invY-101,0xff553b29);FolkUi.text(g,font,tr("installed"),leftPos+18,topPos+103,140,0xfff3e2bf,1);
   FolkUi.panel(g,leftPos+174,topPos+100,imageWidth-186,invY-101,0xfff2e3c2);
   var blocker=Runes.blocker(minecraft.player,menu.input,operation());action.active=blocker==null;action.setTooltip(blocker==null?null:net.minecraft.client.gui.components.Tooltip.create(blocker));
   FolkUi.text(g,font,creative()?tr("creative_xp"):tr("cost",Runes.operationCost(minecraft.player,menu.input.getItem(0),operation())),leftPos+180,topPos+invY-29,infoW,FolkUi.INK,2);
  }else{
   FolkUi.text(g,font,tr("primary",name(RpgClient.data.getString("Main"))),leftPos+15,topPos+30,(imageWidth-40)/2,FolkUi.INK,2);FolkUi.text(g,font,tr("secondary",name(RpgClient.data.getString("Secondary"))),leftPos+imageWidth/2,topPos+30,(imageWidth-40)/2,FolkUi.INK,2);
   FolkUi.text(g,font,creative()?tr("creative_xp"):tr("xp",RpgClient.xp),leftPos+15,topPos+53,imageWidth-30,FolkUi.INK,1);
   FolkUi.panel(g,leftPos+infoX-6,topPos+99,infoW+12,imageHeight-143,0xfff1e2bf);action.active=chooseBlocker()==null;action.setTooltip(net.minecraft.client.gui.components.Tooltip.create(chooseBlocker()==null?tr((creative()?"init_creative_":"init_")+PathData.PATHS[path]):chooseBlocker()));
   learn.active=skillBlocker(skill)==null||owned(skill)&&Abilities.active(skill);learn.setMessage(tr(owned(skill)&&Abilities.active(skill)?"activate_action":"learn_action"));learn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(skillBlocker(skill)==null?tr("available"):skillBlocker(skill)));
  }
  drawDetails(g,menu.anvil?anvilDetails():pathDetails());
 }
 private List<Component> anvilDetails(){var weapon=menu.input.getItem(0);var out=new ArrayList<Component>();
  out.add(tr("rune_count",Runes.occupied(weapon),Runes.max(weapon)));if(Runes.armor(weapon))out.add(tr("armor_runes_inactive"));
  if(menu.mode==0){out.add(tr("before",Runes.slots(weapon),Runes.max(weapon)));out.add(tr("after",Math.min(Runes.max(weapon),Runes.slots(weapon)+1)));out.add(tr("required_material",Runes.forgeMaterial(weapon).getDescription(),menu.input.getItem(1).is(Runes.forgeMaterial(weapon))?menu.input.getItem(1).getCount():0,Runes.forgeCount(weapon)));if(Runes.extra(weapon)!=null)out.add(tr("extra_material",Runes.extra(weapon).getDescription(),Runes.inventoryCount(minecraft.player,Runes.extra(weapon))));}
  else {var installed=Runes.list(weapon);String id=menu.mode==1?Runes.rune(menu.input.getItem(1)):socket<installed.size()?installed.get(socket):"";if(!id.isEmpty()){out.add(Component.translatable("item.slavicmyths.rune_"+id));if(!Runes.armor(weapon))out.add(RuneDefinition.ALL.get(id).effect());out.add(RuneDefinition.ALL.get(id).compatible());out.add(tr("conflicts_none"));}else out.add(tr(menu.mode==2?"no_rune":"need_rune"));if(menu.mode==2)out.add(tr("destroy_warning"));}
  var blocker=Runes.blocker(minecraft.player,menu.input,operation());out.add(blocker==null?tr("available"):blocker);return out;}
 private List<Component> pathDetails(){var out=new ArrayList<Component>();out.add(Component.translatable("skill.slavicmyths."+PathData.SKILLS[skill]));out.add(Component.translatable("skill.slavicmyths."+PathData.SKILLS[skill]+".desc"));out.add(creative()?tr("creative_xp"):tr("cost",PathData.COST[skill%6]));if(skill%6>0)out.add(tr("prerequisite",Component.translatable("skill.slavicmyths."+PathData.SKILLS[skill-1])));if(!PathData.gate(skill).isEmpty())out.add(tr("knowledge",Component.translatable("advancements.slavicmyths."+PathData.gate(skill)+".title")));if(skill%6>=3)out.add(tr("main_only"));out.add(skillBlocker(skill)==null?tr("available"):skillBlocker(skill));out.add(tr((creative()?"init_creative_":"init_")+PathData.PATHS[path]));return out;}
 private void drawDetails(GuiGraphics g,List<Component> details){var lines=new ArrayList<net.minecraft.util.FormattedCharSequence>();for(var c:details)lines.addAll(font.split(c,infoW-7));int count=Math.max(1,infoH/10);infoOffset=Math.max(0,Math.min(infoOffset,Math.max(0,lines.size()-count)));g.enableScissor(leftPos+infoX,topPos+infoY,leftPos+infoX+infoW,topPos+infoY+infoH);for(int i=0;i<count&&i+infoOffset<lines.size();i++)g.drawString(font,lines.get(i+infoOffset),leftPos+infoX,topPos+infoY+i*10,FolkUi.INK,false);g.disableScissor();if(lines.size()>count){int h=Math.max(5,infoH*count/lines.size()),dy=(infoH-h)*infoOffset/(lines.size()-count);g.fill(leftPos+infoX+infoW-3,topPos+infoY,leftPos+infoX+infoW-1,topPos+infoY+infoH,0xffc1aa87);g.fill(leftPos+infoX+infoW-3,topPos+infoY+dy,leftPos+infoX+infoW-1,topPos+infoY+dy+h,FolkUi.RED);}}
 @Override protected void renderLabels(GuiGraphics g,int x,int y){}
 @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(!menu.anvil&&x<leftPos+infoX){listOffset-=(int)Math.signum(vertical);refresh();}else infoOffset-=(int)Math.signum(vertical)*2;return true;}
 @Override public void render(GuiGraphics g,int mx,int my,float f){super.render(g,mx,my,f);
  if(menu.anvil){var installed=Runes.list(menu.input.getItem(0));for(int i=0;i<3;i++){int y=invY<180?120:121;if(i<installed.size())g.renderItem(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_"+installed.get(i)))),leftPos+45+i*48,topPos+y);else g.drawString(font,i<Runes.slots(menu.input.getItem(0))?"+":"x",leftPos+46+i*48,topPos+y+3,0xfff1dfb7,false);}
   if(mx>=leftPos+imageWidth*4/5-18&&mx<leftPos+imageWidth*4/5+18&&my>=topPos+35&&my<topPos+71){var preview=Runes.preview(menu.input,operation());if(!preview.isEmpty())g.renderTooltip(font,preview,mx,my);}
   if(my>=topPos+116&&my<topPos+146&&mx>=leftPos+20&&mx<leftPos+164){int n=(mx-leftPos-20)/48;if(n<3)g.renderTooltip(font,n<installed.size()?Component.translatable("item.slavicmyths.rune_"+installed.get(n)):tr(n<Runes.slots(menu.input.getItem(0))?"socket_empty":n<Runes.max(menu.input.getItem(0))?"socket_locked":"socket_unsupported",Runes.max(menu.input.getItem(0))),mx,my);}
  }else for(var e:rows.entrySet()){Button b=e.getKey();int i=e.getValue();int color=owned(i)?FolkUi.GREEN:skillBlocker(i)==null?FolkUi.GOLD:0xff62574a;g.drawString(font,owned(i)?"РІСљвЂњ":skillBlocker(i)==null?"+":"Р“вЂ”",b.getX()-9,b.getY()+15,color,false);
   g.fill(b.getX()+4,b.getY()+3,b.getX()+b.getWidth()-4,b.getY()+45,i==skill?FolkUi.RED:0xff443023);g.renderItem(new ItemStack(pathIcon(i/6)),b.getX()+5,b.getY()+4);FolkUi.text(g,font,Component.translatable("skill.slavicmyths."+PathData.SKILLS[i]),b.getX()+24,b.getY()+4,b.getWidth()-29,0xfff1dfb7,2);FolkUi.text(g,font,Component.translatable("skill.slavicmyths."+PathData.SKILLS[i]+".desc"),b.getX()+5,b.getY()+25,b.getWidth()-49,0xfff1dfb7,2);g.drawString(font,(creative()?0:PathData.COST[i%6])+" XP",b.getX()+b.getWidth()-41,b.getY()+34,0xfff1dfb7,false);
   if(b.isHovered())g.renderTooltip(font,skillBlocker(i)==null?tr("available"):skillBlocker(i),mx,my);
  }
  renderTooltip(g,mx,my);
 }
}
