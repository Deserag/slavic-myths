package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import org.slavicmyths.rpg.classes.*;
import org.slavicmyths.registry.ModItems;

/** Server-authoritative progression; one prerequisite canvas and the actual Minecraft key mappings. */
public final class ClassScreen extends Screen {
 private String selected="",choice="druzhinnik",confirmation="",layoutBase="";
 private int tab,binding=-1,infoOffset,left,top,panel,right,contentTop,contentBottom,treeWidth,panX,panY;
 private ClassTreeLayout.Tree tree;
 private final Map<String,int[]> positions=new LinkedHashMap<>();
 private static Component tr(String key,Object... args){return Component.translatable("classes.slavicmyths."+key,args);}
 public ClassScreen(){super(tr("title"));}
 private boolean creative(){return minecraft.player!=null&&minecraft.player.isCreative();}
 private ClassTheme.WoodButton button(int x,int y,int w,Component label,net.minecraft.client.gui.components.Button.OnPress press){return addRenderableWidget(new ClassTheme.WoodButton(x,y,w,22,label,press));}
 public void refresh(){if(minecraft!=null)init();}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){g.fill(0,0,width,height,0xCC100D0B);}
 @Override protected void renderBlurredBackground(float delta){}
 @Override protected void init(){
  clearWidgets();panel=Math.min(900,width-16);left=(width-panel)/2;top=8;right=left+panel-Math.min(292,Math.max(202,panel*2/5));contentTop=top+66;contentBottom=height-72;treeWidth=right-left-14;
  button(left+panel-26,top+5,20,Component.literal("×"),b->onClose());var d=ClassClient.data();String base=d.getString("Base");
  if(base.isEmpty()){initChoice();return;}
  for(int i=0;i<3;i++){int target=i;var b=button(left+8+i*105,top+37,101,tr(new String[]{"tree","branches","controls"}[i]),click->{tab=target;binding=-1;infoOffset=0;refresh();});b.selected=tab==i;}
  if(tab==0){
   if(!layoutBase.equals(base)){layoutBase=base;tree=ClassTreeLayout.create(base);var root=tree.nodes().stream().filter(n->n.id().equals(base)).findFirst().orElseThrow();panX=Math.max(0,root.x()+22-treeWidth/2);panY=0;}
   clampPan();if(!ClassDefinitions.SKILLS.containsKey(selected)||!ClassDefinitions.SKILLS.get(selected).base().equals(base))selected=ClassDefinitions.SKILLS.values().stream().filter(s->s.base().equals(base)).findFirst().orElseThrow().id();
   int rank=d.getCompound("Ranks").getInt(selected);var learn=button(right+8,contentBottom-24,left+panel-right-16,tr(rank==0?"study":"upgrade"),b->ClassNetwork.send(1,selected,0));learn.active=ClassState.blocker(d,selected,creative()).isEmpty();
  }else if(tab==1)initBranches(d);else initBindings();
  if(!confirmation.isEmpty())initConfirmation();
 }
 private int previewWidth(){return Math.max(88,panel/5);}
 private int choiceWidth(){return Math.max(142,panel/4);}
 private int cardHeight(){return Math.min(66,(height-112)/3);}
 private void initChoice(){
  int cx=left+previewWidth()+12,cw=choiceWidth(),h=cardHeight();
  for(int i=0;i<3;i++){String id=ClassDefinitions.BASES.get(i);var b=addRenderableWidget(new ClassTheme.WoodButton(cx,contentTop+i*(h+5),cw,h,Component.empty(),click->{choice=id;infoOffset=0;refresh();}));b.selected=choice.equals(id);}
  int dx=cx+cw+12;button(dx,height-40,Math.min(220,left+panel-dx-8),tr("choose",Component.translatable("class.slavicmyths."+choice)),b->{confirmation=choice;refresh();});if(!confirmation.isEmpty())initConfirmation();
 }
 private void initBindings(){int x=left+24,w=Math.min(350,panel-48);for(int i=0;i<3;i++){int slot=i;button(x,contentTop+25+i*53,w,binding==i?tr("press_key"):tr("binding_row",roman(i),ClassClient.ACTIVE[i].getTranslatedKeyMessage()),b->{binding=slot;refresh();});}}
 private void initBranches(CompoundTag d){int n=0;for(var branch:branches(d)){
  int x=left+12+n*(panel-32)/2,w=(panel-40)/2;var b=button(x,contentBottom-18,w,tr("choose",Component.translatable("class.slavicmyths."+branch.id())),click->{confirmation=branch.id();refresh();});b.active=d.getString(branch.level()==ClassDefinitions.FIRST_LEVEL?"First":"Second").isEmpty()&&(creative()||d.getInt("Level")>=branch.level());n++;
 }}
 private List<ClassDefinitions.Branch> branches(CompoundTag d){String parent=d.getString("First").isEmpty()?d.getString("Base"):d.getString("First");return ClassDefinitions.BRANCHES.values().stream().filter(b->b.parent().equals(parent)).toList();}
 private void initConfirmation(){int w=Math.min(340,panel-32),x=(width-w)/2,y=(height-122)/2;button(x+12,y+84,(w-32)/2,tr("back"),b->{confirmation="";refresh();});button(x+20+(w-32)/2,y+84,(w-32)/2,tr("confirm"),b->{ClassNetwork.send(ClassDefinitions.BASES.contains(confirmation)?0:3,confirmation,0);confirmation="";refresh();});}
 @Override public void render(GuiGraphics g,int mx,int my,float delta){
  ClassTheme.panel(g,left,top,panel,height-16);var d=ClassClient.data();String base=d.getString("Base");
  if(base.isEmpty())g.drawString(font,tr("choose_path"),left+10,top+10,ClassTheme.TEXT);
  else{g.drawString(font,tr("progress_top",Component.translatable("class.slavicmyths."+base),d.getInt("Level")),left+10,top+7,ClassTheme.TEXT);g.drawString(font,tr("progress_bottom",d.getLong("Xp"),ClassDefinitions.xpForLevel(d.getInt("Level")),d.getInt("Points")),left+10,top+21,ClassTheme.MUTED);}
  if(base.isEmpty())renderChoice(g,mx,my);else if(tab==0){renderTree(g,d);details(g,d);renderLoadout(g,d,mx,my);}else if(tab==1)renderBranches(g,d);else renderBindings(g);
  super.render(g,mx,my,delta);if(base.isEmpty())renderChoiceCards(g);
  if(!confirmation.isEmpty())renderConfirmation(g,mx,my,delta);else if(tab==0&&!base.isEmpty())nodeTooltip(g,d,mx,my);
 }
 private String state(CompoundTag d,String id){return d.getCompound("Ranks").getInt(id)>0?"learned":ClassState.blocker(d,id,creative()).isEmpty()?"available":"locked";}
 private void renderTree(GuiGraphics g,CompoundTag d){
  ClassTheme.panel(g,left+6,contentTop,treeWidth+2,contentBottom-contentTop);positions.clear();for(var node:tree.nodes())positions.put(node.id(),new int[]{left+9+node.x()-panX,contentTop+8+node.y()-panY});
  g.enableScissor(left+7,contentTop+1,right-5,contentBottom-1);
  for(var node:tree.nodes())if(!node.parent().isEmpty()){
   var from=positions.get(node.parent());var to=positions.get(node.id());int color=state(d,node.id()).equals("learned")?0xFFBEA16A:state(d,node.id()).equals("available")?0xFF8F7446:0xFF4F463B;
   int fx=from[0]+22,fy=from[1]+(node.parent().equals(d.getString("Base"))?48:44),tx=to[0]+22,ty=to[1],mid=(fy+ty)/2;g.fill(fx-1,fy,fx+1,mid+1,color);g.fill(Math.min(fx,tx)-1,mid,Math.max(fx,tx)+1,mid+2,color);g.fill(tx-1,mid,tx+1,ty,color);
  }
  for(var node:tree.nodes()){
   var pos=positions.get(node.id());int x=pos[0],y=pos[1];if(y+54<contentTop||y>contentBottom||x+44<left||x>right)continue;
   boolean root=node.id().equals(d.getString("Base"));String state=root?"learned":state(d,node.id());ClassTheme.image(g,"ui/node_"+state,x-(root?4:0),y-(root?4:0),root?52:44,root?52:44,44,44);ClassTheme.icon(g,node.id(),x+(root?2:6),y+(root?2:6),root?40:32);
   if(state.equals("locked"))g.fill(x+6,y+6,x+38,y+38,0x35000000);if(node.id().equals(selected))ClassTheme.image(g,"ui/node_selected",x-2,y-2,48,48,48,48);
   if(!root){var def=ClassDefinitions.SKILLS.get(node.id());int rank=d.getCompound("Ranks").getInt(node.id());g.drawCenteredString(font,Component.literal(rank+"/"+def.maxRank()),x+22,y+46,rank==def.maxRank()?ClassTheme.GOLD:ClassTheme.MUTED);if(rank==def.maxRank())g.fill(x+8,y+2,x+36,y+4,0xFFD8BC78);for(int i=0;i<def.maxRank();i++)g.fill(x+8+i*10,y+39,x+15+i*10,y+41,i<rank?0xFFD8BC78:0xFF554A3A);}
  }g.disableScissor();
  if(tree.width()>treeWidth){int bar=Math.max(16,treeWidth*treeWidth/tree.width()),x=left+7+(treeWidth-bar)*panX/Math.max(1,tree.width()-treeWidth+12);g.fill(x,contentBottom-4,x+bar,contentBottom-2,0xFF9C8152);}
  if(tree.height()+16>contentBottom-contentTop){int h=contentBottom-contentTop,bar=Math.max(12,h*h/(tree.height()+16)),y=contentTop+(h-bar)*panY/Math.max(1,tree.height()+16-h);g.fill(right-8,y,right-6,y+bar,0xFF9C8152);}
 }
 private void details(GuiGraphics g,CompoundTag d){
  var s=ClassDefinitions.SKILLS.get(selected);if(s==null)return;int x=right+8,w=left+panel-x-8,rank=d.getCompound("Ranks").getInt(selected);ClassTheme.panel(g,right+2,contentTop,left+panel-right-6,contentBottom-contentTop);
  ClassTheme.icon(g,selected,x+2,contentTop+4,48);g.drawWordWrap(font,ClassClient.label(selected),x+57,contentTop+7,w-57,ClassTheme.TEXT);
  var text=new ArrayList<Component>();text.add(tr("kind_"+s.kind().name().toLowerCase(Locale.ROOT)).copy().append(" · ").append(tr("rank",rank,s.maxRank())));text.add(Component.translatable(s.key()+".description"));
  if(s.kind()!=ClassDefinitions.Kind.EVOLUTION){text.add(tr(rank==0?"rank_one":"current_effect"));stats(text,d,s,Math.max(1,rank),Math.min(s.maxRank(),rank+1),rank>0&&rank<s.maxRank());}else text.add(tr("evolution_effect"));
  text.add(tr("requirements_heading"));if(creative())text.add(tr("creative_free"));else{
   text.add(tr("required_level",s.level()+rank*2,d.getInt("Level")));if(!s.prerequisite().isEmpty())text.add(tr("required_skill",ClassClient.label(s.prerequisite()),s.prerequisiteRank(),d.getCompound("Ranks").getInt(s.prerequisite())));
   if(!s.branch().isEmpty())text.add(tr("required_branch",Component.translatable("class.slavicmyths."+s.branch())));text.add(tr("point_cost",d.getInt("Points")));
  }
  String blocker=ClassState.blocker(d,selected,creative());if(!blocker.isEmpty()){var explanation=reason(d,s,blocker);text.add(explanation);var lines=font.split(explanation,w);for(int i=0;i<Math.min(2,lines.size());i++)g.drawString(font,lines.get(i),x,contentBottom-47+i*10,ClassTheme.ERROR,false);for(var widget:children())if(widget instanceof ClassTheme.WoodButton b&&b.getX()==x&&b.getY()==contentBottom-24)b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(explanation));}if(s.kind()==ClassDefinitions.Kind.TRIGGER||s.kind()==ClassDefinitions.Kind.EVOLUTION)text.add(tr("no_slot"));drawScrollText(g,text,x,contentTop+57,w,contentBottom-(blocker.isEmpty()?28:51));
 }
 private void stats(List<Component> out,CompoundTag d,ClassDefinitions.Skill s,int rank,int next,boolean compare){
  if(compare)out.add(tr("next_rank",rank,next));if(s.cooldown(rank)>0)out.add(statLine("cooldown",ClassBalance.cooldown(d,s.id(),rank)/20.0,ClassBalance.cooldown(d,s.id(),next)/20.0,compare));if(s.duration(rank)>0)out.add(statLine("duration",ClassBalance.duration(d,s.id(),rank)/20.0,ClassBalance.duration(d,s.id(),next)/20.0,compare));if(s.range(rank)>0)out.add(statLine("range",ClassBalance.range(d,s.id(),rank),ClassBalance.range(d,s.id(),next),compare));
  var percentages=Set.of("lunge","flurry","aerial_strike","steadfast","ward_power","dirty_strike","light_step","steady_aim","cold_blood","opportunity");
  if(net.minecraft.client.resources.language.I18n.exists("classes.slavicmyths.stat_"+s.id())){double factor=percentages.contains(s.id())?100:1;String value=number(ClassBalance.power(d,s.id(),rank)*factor);if(compare)value+=" → "+number(ClassBalance.power(d,s.id(),next)*factor);out.add(tr("stat_"+s.id(),value));}
 }
 private Component statLine(String key,double current,double next,boolean compare){return tr("value_"+key,number(current)+(compare?" → "+number(next):""));}
 private Component reason(CompoundTag d,ClassDefinitions.Skill s,String blocker){return switch(blocker){case "level_required"->tr("required_level",s.level()+d.getCompound("Ranks").getInt(s.id())*2,d.getInt("Level"));case "prerequisite"->tr("required_skill",ClassClient.label(s.prerequisite()),s.prerequisiteRank(),d.getCompound("Ranks").getInt(s.prerequisite()));case "branch_required"->tr("required_branch",Component.translatable("class.slavicmyths."+s.branch()));case "no_points"->tr("point_cost",d.getInt("Points"));default->tr(blocker);};}
 private void drawScrollText(GuiGraphics g,List<Component> text,int x,int y,int w,int bottom){
  var lines=new ArrayList<net.minecraft.util.FormattedCharSequence>();for(var c:text){lines.addAll(font.split(c,w-7));lines.add(Component.empty().getVisualOrderText());}int capacity=Math.max(1,(bottom-y)/10);infoOffset=Math.max(0,Math.min(infoOffset,Math.max(0,lines.size()-capacity)));g.enableScissor(x,y,x+w,bottom);for(int i=0;i<capacity&&i+infoOffset<lines.size();i++)g.drawString(font,lines.get(i+infoOffset),x,y+i*10,ClassTheme.TEXT,false);g.disableScissor();if(lines.size()>capacity){int bar=Math.max(8,(bottom-y)*capacity/lines.size()),sy=y+((bottom-y)-bar)*infoOffset/Math.max(1,lines.size()-capacity);g.fill(x+w-3,sy,x+w-1,sy+bar,0xFFAD925D);}
 }
 private void renderLoadout(GuiGraphics g,CompoundTag d,int mx,int my){int x=left+12,y=height-57;g.drawString(font,tr("active_loadout"),x,y-11,ClassTheme.MUTED);
  for(int i=0;i<4;i++){int sx=x+i*50+(i==3?18:0);String id=d.getString(i==3?"Passive":"Active"+i);ClassTheme.image(g,"ui/node_learned",sx,y,36,36,44,44);if(ClassDefinitions.SKILLS.containsKey(id))ClassTheme.icon(g,id,sx+2,y+2,32);g.drawCenteredString(font,i==3?tr("passive_short"):Component.literal(roman(i)),sx+18,y+38,ClassTheme.MUTED);if(mx>=sx&&mx<sx+36&&my>=y&&my<y+36)g.renderTooltip(font,List.of(id.isEmpty()?tr("empty"):ClassClient.label(id),tr("slot_hint")),Optional.empty(),mx,my);}
  g.drawWordWrap(font,tr("tree_hint"),x+225,y+3,Math.max(90,panel-245),ClassTheme.MUTED);
 }
 private void renderChoice(GuiGraphics g,int mx,int my){int pw=previewWidth(),cx=left+pw+12,dx=cx+choiceWidth()+12,w=left+panel-dx-10;
  if(minecraft.player!=null){var p=minecraft.player;ItemStack main=p.getMainHandItem(),off=p.getOffhandItem();try{
   p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(choice.equals("druzhinnik")?Items.IRON_SWORD:choice.equals("vedun")?ModItems.CARVED_STAFF.get():Items.BOW));p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(choice.equals("druzhinnik")?Items.SHIELD:choice.equals("vedun")?ModItems.WARDING_CHARM.get():ModItems.SILVER_DAGGER.get()));
   InventoryScreen.renderEntityInInventoryFollowsMouse(g,left+10,contentTop+4,left+pw,Math.min(height-42,contentTop+208),Math.min(70,(height-118)/2),.0625F,mx,my,p);
  }finally{p.setItemSlot(EquipmentSlot.MAINHAND,main);p.setItemSlot(EquipmentSlot.OFFHAND,off);}}
  var text=new ArrayList<Component>();text.add(Component.translatable("class.slavicmyths."+choice));text.add(Component.translatable("class.slavicmyths."+choice+".description"));text.add(tr("class_features_"+choice));text.add(tr("class_start",ClassClient.label(choice.equals("druzhinnik")?"lunge":choice.equals("vedun")?"binding_sign":"keen_eye")));drawScrollText(g,text,dx,contentTop+3,w,height-84);g.drawWordWrap(font,tr("selection_requirements"),dx,height-79,w,ClassTheme.GOLD);
 }
 private void renderChoiceCards(GuiGraphics g){int cx=left+previewWidth()+12,cw=choiceWidth(),h=cardHeight();for(int i=0;i<3;i++){String id=ClassDefinitions.BASES.get(i);int y=contentTop+i*(h+5),size=h>=60?48:32;ClassTheme.icon(g,id,cx+6,y+(h-size)/2,size);g.drawWordWrap(font,Component.translatable("class.slavicmyths."+id),cx+size+12,y+8,cw-size-18,ClassTheme.TEXT);g.drawWordWrap(font,tr("role_"+id),cx+size+12,y+(h>=60?30:25),cw-size-18,ClassTheme.MUTED);}}
 private void renderBindings(GuiGraphics g){int x=left+24;g.drawWordWrap(font,tr("binding_hint"),x,contentTop,panel-48,ClassTheme.MUTED);for(int i=0;i<3;i++){var names=ClassClient.conflicts(ClassClient.ACTIVE[i]);if(!names.isEmpty()){var joined=Component.empty();for(var name:names){if(!joined.getString().isEmpty())joined.append(", ");joined.append(name);}g.drawWordWrap(font,tr("conflict_named",joined),x,contentTop+49+i*53,panel-48,ClassTheme.ERROR);}}}
 private void renderBranches(GuiGraphics g,CompoundTag d){int n=0;for(var b:branches(d)){int x=left+12+n*(panel-32)/2,w=(panel-40)/2;g.drawWordWrap(font,Component.translatable("class.slavicmyths."+b.id()),x,contentTop+5,w,ClassTheme.TEXT);g.drawWordWrap(font,tr("branch_info",b.level(),ClassClient.label(b.enhancedSkill()),number(b.multiplier())),x,contentTop+31,w,ClassTheme.MUTED);g.drawWordWrap(font,creative()?tr("creative_free"):tr("required_level",b.level(),d.getInt("Level")),x,contentTop+70,w,ClassTheme.MUTED);n++;}
  String current=d.getString("Second").isEmpty()?d.getString("First").isEmpty()?d.getString("Base"):d.getString("First"):d.getString("Second");g.drawWordWrap(font,tr("branch_current",Component.translatable("class.slavicmyths."+current)),left+12,contentBottom+12,panel-24,ClassTheme.TEXT);
 }
 private void renderConfirmation(GuiGraphics g,int mx,int my,float delta){g.fill(0,0,width,height,0xCC100D0B);int w=Math.min(340,panel-32),x=(width-w)/2,y=(height-122)/2;ClassTheme.panel(g,x,y,w,122);g.drawWordWrap(font,tr("confirm_path",Component.translatable("class.slavicmyths."+confirmation)),x+12,y+14,w-24,ClassTheme.TEXT);for(var widget:renderables)if(widget instanceof ClassTheme.WoodButton b&&b.getY()==y+84)b.render(g,mx,my,delta);}
 private void nodeTooltip(GuiGraphics g,CompoundTag d,int mx,int my){if(mx>=right||my<contentTop||my>=contentBottom)return;for(var entry:positions.entrySet()){var pos=entry.getValue();var s=ClassDefinitions.SKILLS.get(entry.getKey());if(s!=null&&mx>=pos[0]&&mx<pos[0]+44&&my>=pos[1]&&my<pos[1]+44){g.renderTooltip(font,List.of(ClassClient.label(s.id()),tr("kind_"+s.kind().name().toLowerCase(Locale.ROOT)).copy().append(" · ").append(tr("rank",d.getCompound("Ranks").getInt(s.id()),s.maxRank())),Component.translatable(s.key()+".description")),Optional.empty(),mx,my);break;}}}
 @Override public boolean mouseClicked(double x,double y,int mouse){
  if(binding>=0){bind(InputConstants.Type.MOUSE.getOrCreate(mouse));return true;}if(!confirmation.isEmpty()){int cy=(height-122)/2+84;for(var widget:children())if(widget instanceof ClassTheme.WoodButton b&&b.getY()==cy&&b.isMouseOver(x,y))return b.mouseClicked(x,y,mouse);return true;}
  var d=ClassClient.data();if(!d.getString("Base").isEmpty()&&tab==0){
   if(x<right&&y>=contentTop&&y<contentBottom)for(var e:positions.entrySet()){var p=e.getValue();if(ClassDefinitions.SKILLS.containsKey(e.getKey())&&x>=p[0]&&x<p[0]+44&&y>=p[1]&&y<p[1]+44){selected=e.getKey();infoOffset=0;refresh();return true;}}
   int sy=height-57;for(int i=0;i<4;i++){int px=left+12+i*50+(i==3?18:0);if(x>=px&&x<px+36&&y>=sy&&y<sy+36){var s=ClassDefinitions.SKILLS.get(selected);if(mouse==1)ClassNetwork.send(2,"",i);else if(s!=null&&d.getCompound("Ranks").getInt(selected)>0&&(i==3?s.kind()==ClassDefinitions.Kind.PASSIVE:s.kind()==ClassDefinitions.Kind.ACTIVE))ClassNetwork.send(2,selected,i);return true;}}
  }return super.mouseClicked(x,y,mouse);
 }
 private void bind(InputConstants.Key key){ClassClient.ACTIVE[binding].setKey(key);net.minecraft.client.KeyMapping.resetMapping();minecraft.options.save();binding=-1;refresh();}
 @Override public boolean keyPressed(int key,int scan,int modifiers){if(binding>=0){if(key==256){binding=-1;refresh();}else bind(InputConstants.getKey(key,scan));return true;}if(!confirmation.isEmpty()){if(key==256){confirmation="";refresh();}else if(key==257||key==335){ClassNetwork.send(ClassDefinitions.BASES.contains(confirmation)?0:3,confirmation,0);confirmation="";refresh();}return true;}return super.keyPressed(key,scan,modifiers);}
 @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(tab==0&&!ClassClient.data().getString("Base").isEmpty()&&x<right){if(hasShiftDown())panX-=(int)(vertical*28);else panY-=(int)(vertical*28);panX-=(int)(horizontal*28);clampPan();}else infoOffset-=(int)(vertical*3);return true;}
 @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){if(confirmation.isEmpty()&&tab==0&&x<right&&y>=contentTop&&y<contentBottom){panX-=(int)dx;panY-=(int)dy;clampPan();return true;}return super.mouseDragged(x,y,button,dx,dy);}
 private void clampPan(){if(tree!=null){panX=Math.max(0,Math.min(panX,Math.max(0,tree.width()+12-treeWidth)));panY=Math.max(0,Math.min(panY,Math.max(0,tree.height()+16-(contentBottom-contentTop))));}}
 private static String roman(int slot){return new String[]{"I","II","III"}[slot];}
 private static String number(double value){return String.format(Locale.ROOT,"%.2f",value).replaceAll("0+$","").replaceAll("\\.$","");}
}
