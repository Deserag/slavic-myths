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

/** Server-authoritative progression; explicit skill choices and the actual Minecraft key mappings. */
public final class ClassScreen extends Screen {
 private String selected="",choice="druzhinnik",confirmation="";
 private int tab,binding=-1,infoOffset,left,top,panel,right,contentTop,contentBottom,skillOffset;
 private boolean directions;
 private int bindingY(int slot){return contentTop+24+slot*Math.max(45,(contentBottom-contentTop-24)/3);}
 private Component joined(List<Component> names){var out=Component.empty();for(var name:names){if(!out.getString().isEmpty())out.append(", ");out.append(name);}return out;}
 private List<ClassDefinitions.Skill> skills(){String base=ClassClient.data().getString("Base");return ClassDefinitions.SKILLS.values().stream().filter(s->s.base().equals(base)).toList();}
 private int listRows(){return Math.max(1,(contentBottom-contentTop)/28);}
 private void clampSkills(){skillOffset=Math.clamp(skillOffset,0,Math.max(0,skills().size()-listRows()));}
 private static Component tr(String key,Object... args){return Component.translatable("classes.slavicmyths."+key,args);}
 public ClassScreen(){super(tr("title"));}
 private boolean creative(){return minecraft.player!=null&&minecraft.player.isCreative();}
 private ClassTheme.WoodButton button(int x,int y,int w,Component label,net.minecraft.client.gui.components.Button.OnPress press){return addRenderableWidget(new ClassTheme.WoodButton(x,y,w,22,label,press));}
 public void refresh(){if(minecraft!=null)init();}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){g.fill(0,0,width,height,0xCC100D0B);}
 @Override protected void renderBlurredBackground(float delta){}
 @Override protected void init(){
  clearWidgets();panel=Math.min(900,width-16);left=(width-panel)/2;top=8;contentTop=top+81;contentBottom=height-14;right=left+Math.max(132,panel*2/5);
  button(left+panel-26,top+5,20,Component.literal("×"),b->onClose());var d=ClassClient.data();String base=d.getString("Base");
  if(base.isEmpty()){contentTop=top+66;initChoice();return;}
  for(int i=0;i<2;i++){int target=i;var b=button(left+8+i*154,top+37,150,tr(i==0?"paths":"controls"),click->{tab=target;binding=-1;infoOffset=0;refresh();});b.selected=tab==i;}
  button(left+Math.min(318,panel-88),top+62,80,tr("mastery"),b->minecraft.setScreen(new ClassMasteryScreen(this)));
  if(tab==0){
   var choices=skills();if(choices.stream().noneMatch(v->v.id().equals(selected)))selected=choices.getFirst().id();
   button(left+8,top+62,Math.min(130,panel/3),tr(directions?"show_skills":"show_directions"),b->{directions=!directions;infoOffset=0;refresh();});
   if(directions)initBranches(d);else initSkills(d);
  }else initBindings();
  if(!confirmation.isEmpty())initConfirmation();
 }
 private void initSkills(CompoundTag d){
  clampSkills();var all=skills();for(int row=0;row<listRows()&&row+skillOffset<all.size();row++){
   var skill=all.get(row+skillOffset);int rank=d.getCompound("Ranks").getInt(skill.id());
   var b=addRenderableWidget(new ClassTheme.WoodButton(left+10,contentTop+row*28,right-left-18,26,ClassClient.label(skill.id()).copy().append(" · "+rank+"/"+skill.maxRank()),v->{selected=skill.id();infoOffset=0;refresh();}));b.selected=selected.equals(skill.id());
   b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(skillDescription(d,skill)));
  }
  var skill=ClassDefinitions.SKILLS.get(selected);int x=right+10,w=left+panel-x-10,rank=d.getCompound("Ranks").getInt(selected),count=skill.kind()==ClassDefinitions.Kind.ACTIVE?3:skill.kind()==ClassDefinitions.Kind.PASSIVE?2:0;
  int assignTop=contentBottom-count*32,learnY=assignTop-28;
  var learn=button(x,learnY,w,tr(rank==0?"study":"upgrade"),b->ClassNetwork.send(1,selected,0));String blocker=ClassState.blocker(d,selected,creative());learn.active=blocker.isEmpty();if(!blocker.isEmpty())learn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(reason(d,skill,blocker)));
  for(int i=0;i<count;i++){
   int slot=skill.kind()==ClassDefinitions.Kind.PASSIVE?3+i:i;String occupied=d.getString(slot==4?"Passive2":slot==3?"Passive":"Active"+slot);Component key=slot>=3?tr(slot==4?"passive_second":"passive_short"):ClassClient.ACTIVE[slot].getTranslatedKeyMessage();
   var label=tr("assign_to",key).copy().append("\n").append(occupied.isEmpty()?tr("empty"):ClassClient.label(occupied));var b=addRenderableWidget(new ClassTheme.WoodButton(x,assignTop+i*32,w-26,30,label,v->ClassNetwork.send(2,selected,slot)));b.active=rank>0&&(slot!=4||ClassState.secondPassive(d));b.selected=occupied.equals(selected);if(slot==4&&!ClassState.secondPassive(d))b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tr("passive_locked")));
   var tip=tr("slot_occupied",key,occupied.isEmpty()?tr("empty"):ClassClient.label(occupied));if(slot<3&&!ClassClient.conflicts(ClassClient.ACTIVE[slot]).isEmpty())tip=tip.copy().append("\n").append(tr("conflict_named",joined(ClassClient.conflicts(ClassClient.ACTIVE[slot]))));b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tip));
   var clear=button(x+w-22,assignTop+i*32+4,22,Component.literal("×"),v->ClassNetwork.send(2,"",slot));clear.active=!occupied.isEmpty();clear.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tr("clear_slot",key)));
  }
 }
 private Component skillDescription(CompoundTag d,ClassDefinitions.Skill skill){var out=ClassClient.label(skill.id()).copy().append("\n").append(Component.translatable(skill.key()+".description"));String blocker=ClassState.blocker(d,skill.id(),creative());if(!blocker.isEmpty())out.append("\n").append(reason(d,skill,blocker));return out;}
 private void renderSkills(GuiGraphics g,CompoundTag d){
  ClassTheme.panel(g,left+6,contentTop-1,right-left-8,contentBottom-contentTop+2);var skill=ClassDefinitions.SKILLS.get(selected);int x=right+10,w=left+panel-x-10,rank=d.getCompound("Ranks").getInt(selected),count=skill.kind()==ClassDefinitions.Kind.ACTIVE?3:skill.kind()==ClassDefinitions.Kind.PASSIVE?2:0;
  var lines=new ArrayList<Component>();lines.add(ClassClient.label(selected));lines.add(tr("kind_"+skill.kind().name().toLowerCase(Locale.ROOT)).copy().append(" · ").append(tr("rank",rank,skill.maxRank())));lines.add(Component.translatable(skill.key()+".description"));stats(lines,d,skill,Math.max(1,rank),Math.min(skill.maxRank(),rank+1),rank>0&&rank<skill.maxRank());
  String blocker=ClassState.blocker(d,selected,creative());if(!blocker.isEmpty())lines.add(reason(d,skill,blocker));
  if(skill.kind()==ClassDefinitions.Kind.TRIGGER||skill.kind()==ClassDefinitions.Kind.EVOLUTION)lines.add(tr("automatic_skill"));
  else lines.add(tr(rank==0?"learn_then_assign":"assign_instruction"));
  for(int i=0;i<3;i++)if(d.getString("Active"+i).equals(selected))lines.add(tr("assigned_key",i+1,ClassClient.ACTIVE[i].getTranslatedKeyMessage()));
  drawScrollText(g,lines,x,contentTop,w,contentBottom-count*32-33);
  if(skills().size()>listRows()){int h=contentBottom-contentTop,bar=Math.max(10,h*listRows()/skills().size()),y=contentTop+(h-bar)*skillOffset/Math.max(1,skills().size()-listRows());g.fill(right-6,y,right-4,y+bar,0xFFAD925D);}
 }
 private int previewWidth(){return Math.max(88,panel/5);}
 private int choiceWidth(){return Math.max(142,panel/4);}
 private int cardHeight(){return Math.min(66,(height-112)/3);}
 private void initChoice(){
  int cx=left+previewWidth()+12,cw=choiceWidth(),h=cardHeight();
  for(int i=0;i<3;i++){String id=ClassDefinitions.BASES.get(i);var b=addRenderableWidget(new ClassTheme.WoodButton(cx,contentTop+i*(h+5),cw,h,Component.empty(),click->{choice=id;infoOffset=0;refresh();}));b.selected=choice.equals(id);}
  int dx=cx+cw+12;button(dx,height-40,Math.min(220,left+panel-dx-8),tr("choose",Component.translatable("class.slavicmyths."+choice)),b->{confirmation=choice;refresh();});if(!confirmation.isEmpty())initConfirmation();
 }
 private void initBindings(){int w=Math.min(140,panel/3);for(int i=0;i<3;i++){int slot=i;var b=button(left+panel-w-18,bindingY(i),w,binding==i?tr("press_key"):ClassClient.ACTIVE[i].getTranslatedKeyMessage(),v->{binding=slot;refresh();});var names=ClassClient.conflicts(ClassClient.ACTIVE[i]);if(!names.isEmpty())b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tr("conflict_named",joined(names))));}}
 private void initBranches(CompoundTag d){int n=0;for(var branch:branches(d)){
  int x=left+12+n*(panel-24)/2,w=(panel-36)/2;var b=button(x,contentBottom-26,w,tr("choose",Component.translatable("class.slavicmyths."+branch.id())),click->{confirmation=branch.id();refresh();});b.active=d.getString(branch.level()==ClassDefinitions.FIRST_LEVEL?"First":"Second").isEmpty()&&(creative()||d.getInt("Level")>=branch.level());b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(creative()?tr("creative_free"):tr("required_level",branch.level(),d.getInt("Level"))));n++;
 }}
 private List<ClassDefinitions.Branch> branches(CompoundTag d){String parent=d.getString("First").isEmpty()?d.getString("Base"):d.getString("First");return ClassDefinitions.BRANCHES.values().stream().filter(b->b.parent().equals(parent)).toList();}
 private void initConfirmation(){int w=Math.min(340,panel-32),x=(width-w)/2,y=(height-122)/2;button(x+12,y+84,(w-32)/2,tr("back"),b->{confirmation="";refresh();});button(x+20+(w-32)/2,y+84,(w-32)/2,tr("confirm"),b->{ClassNetwork.send(ClassDefinitions.BASES.contains(confirmation)?0:3,confirmation,0);confirmation="";refresh();});}
 @Override public void render(GuiGraphics g,int mx,int my,float delta){
  renderBackground(g,mx,my,delta);ClassTheme.panel(g,left,top,panel,height-16);var d=ClassClient.data();String base=d.getString("Base");
  if(base.isEmpty())g.drawString(font,tr("choose_path"),left+10,top+10,ClassTheme.TEXT);
  else{g.drawString(font,UiText.fit(font,tr("progress_top",Component.translatable("class.slavicmyths."+base),d.getInt("Level")).getString(),panel-42),left+10,top+7,ClassTheme.TEXT);g.drawString(font,UiText.fit(font,tr("progress_bottom",d.getLong("Xp"),ClassDefinitions.xpForLevel(d.getInt("Level")),d.getInt("Points")).getString(),panel-24),left+10,top+21,ClassTheme.MUTED);}
  if(base.isEmpty())renderChoice(g,mx,my);else if(tab==0){if(directions)renderBranches(g,d);else renderSkills(g,d);}else renderBindings(g);
  // Screen.render paints the background again in 1.21.1. Render widgets only, after content.
  for(var widget:renderables)widget.render(g,mx,my,delta);if(base.isEmpty())renderChoiceCards(g);
  if(!confirmation.isEmpty())renderConfirmation(g,mx,my,delta);
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

 private void renderChoice(GuiGraphics g,int mx,int my){int pw=previewWidth(),cx=left+pw+12,dx=cx+choiceWidth()+12,w=left+panel-dx-10;
  if(minecraft.player!=null){var p=minecraft.player;ItemStack main=p.getMainHandItem(),off=p.getOffhandItem();try{
   p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(choice.equals("druzhinnik")?Items.IRON_SWORD:choice.equals("vedun")?ModItems.CARVED_STAFF.get():Items.BOW));p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(choice.equals("druzhinnik")?Items.SHIELD:choice.equals("vedun")?ModItems.WARDING_CHARM.get():ModItems.SILVER_DAGGER.get()));
   InventoryScreen.renderEntityInInventoryFollowsMouse(g,left+10,contentTop+4,left+pw,Math.min(height-42,contentTop+208),Math.min(70,(height-118)/2),.0625F,mx,my,p);
  }finally{p.setItemSlot(EquipmentSlot.MAINHAND,main);p.setItemSlot(EquipmentSlot.OFFHAND,off);}}
  var text=new ArrayList<Component>();text.add(Component.translatable("class.slavicmyths."+choice));text.add(Component.translatable("class.slavicmyths."+choice+".description"));text.add(tr("class_features_"+choice));text.add(tr("class_start",ClassClient.label(choice.equals("druzhinnik")?"lunge":choice.equals("vedun")?"binding_sign":"keen_eye")));drawScrollText(g,text,dx,contentTop+3,w,height-84);g.drawWordWrap(font,tr("selection_requirements"),dx,height-79,w,ClassTheme.GOLD);
 }
 private void renderChoiceCards(GuiGraphics g){int cx=left+previewWidth()+12,cw=choiceWidth(),h=cardHeight();for(int i=0;i<3;i++){String id=ClassDefinitions.BASES.get(i);int y=contentTop+i*(h+5),size=h>=60?48:32;ClassTheme.icon(g,id,cx+6,y+(h-size)/2,size);g.drawWordWrap(font,Component.translatable("class.slavicmyths."+id),cx+size+12,y+8,cw-size-18,ClassTheme.TEXT);g.drawWordWrap(font,tr("role_"+id),cx+size+12,y+(h>=60?30:25),cw-size-18,ClassTheme.MUTED);}}
 private void renderBindings(GuiGraphics g){int x=left+18;g.drawWordWrap(font,tr("binding_hint"),x,contentTop-4,panel-36,ClassTheme.MUTED);var d=ClassClient.data();for(int i=0;i<3;i++){int y=bindingY(i);String id=d.getString("Active"+i);g.drawString(font,UiText.fit(font,tr("binding_slot",i+1,id.isEmpty()?tr("empty"):ClassClient.label(id)).getString(),panel-Math.min(140,panel/3)-44),x,y+5,ClassTheme.TEXT);var names=ClassClient.conflicts(ClassClient.ACTIVE[i]);if(!names.isEmpty()){var text=tr("conflict_named",joined(names));g.drawString(font,UiText.fit(font,text.getString(),panel-36),x,y+27,ClassTheme.ERROR);}}}
 private void renderBranches(GuiGraphics g,CompoundTag d){int n=0;for(var b:branches(d)){int x=left+12+n*(panel-24)/2,w=(panel-36)/2;var text=new ArrayList<Component>();text.add(Component.translatable("class.slavicmyths."+b.id()));text.add(tr("branch_info",b.level(),ClassClient.label(b.enhancedSkill()),number(b.multiplier())));text.add(creative()?tr("creative_free"):tr("required_level",b.level(),d.getInt("Level")));int saved=infoOffset;drawScrollText(g,text,x,contentTop+4,w,contentBottom-32);infoOffset=saved;n++;}
  String current=d.getString("Second").isEmpty()?d.getString("First").isEmpty()?d.getString("Base"):d.getString("First"):d.getString("Second");g.drawString(font,UiText.fit(font,tr("branch_current",Component.translatable("class.slavicmyths."+current)).getString(),panel-160),left+152,top+69,ClassTheme.TEXT);
 }
 private void renderConfirmation(GuiGraphics g,int mx,int my,float delta){g.fill(0,0,width,height,0xCC100D0B);int w=Math.min(340,panel-32),x=(width-w)/2,y=(height-122)/2;ClassTheme.panel(g,x,y,w,122);g.drawWordWrap(font,tr("confirm_path",Component.translatable("class.slavicmyths."+confirmation)),x+12,y+14,w-24,ClassTheme.TEXT);for(var widget:renderables)if(widget instanceof ClassTheme.WoodButton b&&b.getY()==y+84)b.render(g,mx,my,delta);}
 @Override public boolean mouseClicked(double x,double y,int mouse){
  if(binding>=0){bind(InputConstants.Type.MOUSE.getOrCreate(mouse));return true;}if(!confirmation.isEmpty()){int cy=(height-122)/2+84;for(var widget:children())if(widget instanceof ClassTheme.WoodButton b&&b.getY()==cy&&b.isMouseOver(x,y))return b.mouseClicked(x,y,mouse);return true;}
  return super.mouseClicked(x,y,mouse);
 }
 private void bind(InputConstants.Key key){ClassClient.ACTIVE[binding].setKey(key);net.minecraft.client.KeyMapping.resetMapping();minecraft.options.save();binding=-1;refresh();}
 @Override public boolean keyPressed(int key,int scan,int modifiers){if(binding>=0){if(key==256){binding=-1;refresh();}else bind(InputConstants.getKey(key,scan));return true;}if(!confirmation.isEmpty()){if(key==256){confirmation="";refresh();}else if(key==257||key==335){ClassNetwork.send(ClassDefinitions.BASES.contains(confirmation)?0:3,confirmation,0);confirmation="";refresh();}return true;}return super.keyPressed(key,scan,modifiers);}
 @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(tab==0&&!directions&&!ClassClient.data().getString("Base").isEmpty()&&x<right){skillOffset-=(int)vertical;clampSkills();refresh();}else infoOffset-=(int)(vertical*3);return true;}
 private static String number(double value){return String.format(Locale.ROOT,"%.2f",value).replaceAll("0+$","").replaceAll("\\.$","");}
}
