package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import org.slavicmyths.rpg.classes.*;

/** Full canvas, shared render/hit transform and transient inspectors. Server owns progression. */
public final class ClassScreen extends Screen {
 private final ClassTreeViewport camera=new ClassTreeViewport();
 private final Map<ClassTheme.WoodButton,Component> tips=new IdentityHashMap<>();
 private final Map<String,Integer> branchOffsets=new HashMap<>();
 private String selected="",choice="druzhinnik",confirmation="",cameraBase="",draftSkill="";
 private int page,slot=-1,infoOffset,listOffset,keyOnly=-1;
 private boolean capturing,dragging;private InputConstants.Key draftKey;private KeyModifier draftModifier=KeyModifier.NONE;
 private ClassTreeLayout.Tree tree;private ClassTreeViewport.Rect card,popup;
 private final Map<String,ClassTreeLayout.Node> treeIndex=new HashMap<>();
 private static Component tr(String key,Object...args){return Component.translatable("classes.slavicmyths."+key,args);}
 public ClassScreen(){super(tr("title"));}
 private boolean creative(){return minecraft.player!=null&&minecraft.player.isCreative();}
 private ClassTheme.WoodButton button(int x,int y,int w,Component text,net.minecraft.client.gui.components.Button.OnPress action){var b=addRenderableWidget(new ClassTheme.WoodButton(x,y,w,20,text,action));b.setTooltip(null);return b;}
 private void tip(ClassTheme.WoodButton b,Component text){tips.put(b,text);}
 private void box(ClassTheme.WoodButton b,ClassTreeViewport.Rect rect){b.setWidth(rect.width());b.setHeight(rect.height());}
 private Component joined(List<Component> names){var c=Component.empty();for(var name:names){if(!c.getString().isEmpty())c.append(", ");c.append(name);}return c;}
 public void refresh(){if(minecraft!=null)init();}
 @Override public boolean isPauseScreen(){return false;}
 @Override protected void renderBlurredBackground(float delta){}
 @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){g.fill(0,0,width,height,0xF0100D0B);}
 @Override protected void init(){
  clearWidgets();tips.clear();var d=ClassClient.data();String base=d.getString("Base");
  if(!base.equals(cameraBase)){cameraBase=base;camera.reset();selected="";slot=-1;}
  tree=ClassTreeLayout.create(base);treeIndex.clear();for(var node:tree.nodes())treeIndex.put(node.id(),node);camera.resize(ClassTreeViewport.canvas(width,height));camera.open(tree);
  if(!selected.isEmpty()&&(!ClassDefinitions.SKILLS.containsKey(selected)||!ClassDefinitions.SKILLS.get(selected).base().equals(base)))selected="";
  if(!confirmation.isEmpty()){initConfirmation();return;}
  if(keyOnly>=0){initKeyOnly();return;}
  if(base.isEmpty()){initChoice();return;}
  if(slot>=0){initSlot(d);return;}
  navigation();
  if(page==0){if(!selected.isEmpty())initCard(d);}else if(page==1)initBranches(d);else initBindings();
 }
 private void navigation(){
  int x=8;for(int i=0;i<4;i++){int target=i;Component label=tr(switch(i){case 0->"tree";case 1->"show_directions";case 2->"mastery";default->"controls";});int w=font.width(label)+12;
   var b=button(x,25,w,label,v->{if(target==2){minecraft.setScreen(new ClassMasteryScreen(this));return;}page=target==3?2:target;selected="";infoOffset=0;refresh();});b.selected=i==(page==2?3:page);x+=w+3;
  }
  button(width-28,3,20,Component.literal("×"),b->onClose());
  if(page==0){var fit=button(width-56,46,48,tr("fit_short"),b->camera.fit(tree));tip(fit,tr("view_hint"));}
 }
 private void initChoice(){
  int gap=8,w=(width-32)/3,h=Math.min(100,height-99),y=35;
  for(int i=0;i<ClassDefinitions.BASES.size();i++){String id=ClassDefinitions.BASES.get(i);var r=new ClassTreeViewport.Rect(8+i*(w+gap),y,w,h);var b=button(r.x(),r.y(),w,Component.empty(),v->{choice=id;infoOffset=0;refresh();});box(b,r);b.selected=choice.equals(id);tip(b,Component.translatable("class.slavicmyths."+id+".description"));}
  button(8,height-27,width-44,tr("choose",Component.translatable("class.slavicmyths."+choice)),b->{confirmation=choice;refresh();});button(width-28,4,20,Component.literal("×"),b->onClose());
 }
 private List<Component> skillText(CompoundTag d,ClassDefinitions.Skill s){
  int rank=d.getCompound("Ranks").getInt(s.id());var out=new ArrayList<Component>();out.add(ClassClient.label(s.id()));out.add(tr("rank",rank,s.maxRank()).copy().append(" · ").append(tr("kind_"+s.kind().name().toLowerCase(Locale.ROOT))));out.add(Component.translatable(s.key()+".description"));
  stats(out,d,s,Math.max(1,rank),Math.min(s.maxRank(),rank+1),rank>0&&rank<s.maxRank());
  if(creative())out.add(tr("creative_free"));else if(rank<s.maxRank()){
   out.add(tr("required_level",s.level()+rank*2,d.getInt("Level")));out.add(tr("point_cost",d.getInt("Points")));
   if(!s.prerequisite().isEmpty())out.add(tr("required_skill",ClassClient.label(s.prerequisite()),s.prerequisiteRank(),d.getCompound("Ranks").getInt(s.prerequisite())));
   if(!s.branch().isEmpty())out.add(tr("required_branch",Component.translatable("class.slavicmyths."+s.branch())));
  }
  String blocker=ClassState.blocker(d,s.id(),creative());if(!blocker.isEmpty())out.add(reason(d,s,blocker));
  if(s.kind()==ClassDefinitions.Kind.TRIGGER||s.kind()==ClassDefinitions.Kind.EVOLUTION)out.add(tr("automatic_skill"));
  return out;
 }
 private void initCard(CompoundTag d){
  card=camera.card(tree,selected,Math.min(280,width-80),Math.min(220,height-24));var s=ClassDefinitions.SKILLS.get(selected);int x=card.x()+6,y=card.bottom()-26,w=card.width()-12;
  button(card.right()-23,card.y()+2,20,Component.literal("×"),b->{selected="";refresh();});
  var learn=button(x,y,w/2-2,tr(d.getCompound("Ranks").getInt(selected)==0?"study":"upgrade"),b->ClassNetwork.send(1,selected,0));learn.active=ClassState.blocker(d,selected,creative()).isEmpty();String blocker=ClassState.blocker(d,selected,creative());if(!blocker.isEmpty())tip(learn,reason(d,s,blocker));
  var assign=button(x+w/2+2,y,w/2-2,tr("quick_assign"),b->{int target=s.kind()==ClassDefinitions.Kind.ACTIVE?0:3;for(int i=0;i<5;i++)if(slotId(d,i).equals(selected))target=i;openSlot(target,selected);});assign.active=d.getCompound("Ranks").getInt(selected)>0&&(s.kind()==ClassDefinitions.Kind.ACTIVE||s.kind()==ClassDefinitions.Kind.PASSIVE);
 }

 private String slotId(CompoundTag d,int index){return d.getString(index==4?"Passive2":index==3?"Passive":"Active"+index);}
 private Component slotName(int index){return index<3?tr("binding_slot",index+1,ClassClient.ACTIVE[index].getTranslatedKeyMessage()):tr(index==3?"passive_short":"passive_second");}
 private void openSlot(int index,String skill){slot=index;draftSkill=skill;draftKey=null;capturing=false;listOffset=0;refresh();}
 private List<ClassDefinitions.Skill> candidates(){var d=ClassClient.data();return ClassDefinitions.SKILLS.values().stream().filter(s->s.base().equals(d.getString("Base"))&&s.kind()==(slot<3?ClassDefinitions.Kind.ACTIVE:ClassDefinitions.Kind.PASSIVE)&&d.getCompound("Ranks").getInt(s.id())>0&&(slot<3||!slotId(d,slot==3?4:3).equals(s.id()))).toList();}
 private boolean validDraft(CompoundTag d){var s=ClassDefinitions.SKILLS.get(draftSkill);return draftSkill.isEmpty()||s!=null&&(slot<3||!slotId(d,slot==3?4:3).equals(draftSkill))&&s.base().equals(d.getString("Base"))&&d.getCompound("Ranks").getInt(s.id())>0&&s.kind()==(slot<3?ClassDefinitions.Kind.ACTIVE:ClassDefinitions.Kind.PASSIVE);}
 private int popupRows(){return Math.max(1,(popup.height()-140)/22);}
 private void initSlot(CompoundTag d){
  int w=Math.min(286,width-24),h=Math.min(250,height-16);popup=new ClassTreeViewport.Rect((width-w)/2,(height-h)/2,w,h);int x=popup.x()+8,y=popup.y()+48;
  if(capturing){button(x,popup.bottom()-28,w-16,tr("cancel"),b->{capturing=false;refresh();});return;}
  int first=slot<3?0:3,count=slot<3?3:2,cell=(w-16-(count-1)*4)/count;
  for(int i=0;i<count;i++){int target=first+i;var switcher=button(x+i*(cell+4),popup.y()+24,cell,target<3?Component.literal((target+1)+" · ").append(ClassClient.ACTIVE[target].getTranslatedKeyMessage()):tr(target==3?"passive_short":"passive_second"),b->openSlot(target,draftSkill));switcher.selected=target==slot;switcher.active=target!=4||ClassState.secondPassive(d);tip(switcher,target==4&&!ClassState.secondPassive(d)?tr("passive_locked"):slotName(target));}
  var all=candidates();listOffset=Math.clamp(listOffset,0,Math.max(0,all.size()-popupRows()));
  for(int i=0;i<popupRows()&&i+listOffset<all.size();i++){var s=all.get(i+listOffset);var b=button(x,y+i*22,w-16,ClassClient.label(s.id()),v->{draftSkill=s.id();refresh();});b.selected=draftSkill.equals(s.id());tip(b,ClassClient.label(s.id()));}
  int by=popup.bottom()-76;
  if(slot<3){var kb=button(x,by,w-16,tr("quick_key",draftKey==null?ClassClient.ACTIVE[slot].getTranslatedKeyMessage():draftLabel()),b->{capturing=true;refresh();});tip(kb,tr("conflict_named",joined(keyConflicts(ClassClient.ACTIVE[slot],draftKey==null?ClassClient.ACTIVE[slot].getKey():draftKey))));}
  button(x,popup.bottom()-53,(w-20)/2,tr("quick_clear"),b->{draftSkill="";refresh();});
  if(slot<3)button(x+(w-20)/2+4,popup.bottom()-53,(w-20)/2,tr("quick_unbind"),b->{draftKey=InputConstants.UNKNOWN;draftModifier=KeyModifier.NONE;refresh();});
  var save=button(x,popup.bottom()-28,(w-20)/2,tr("save"),b->{if(slot<3&&draftKey!=null){ClassClient.ACTIVE[slot].setKeyModifierAndCode(draftModifier,draftKey);net.minecraft.client.KeyMapping.resetMapping();minecraft.options.save();}if(!slotId(ClassClient.data(),slot).equals(draftSkill))ClassNetwork.send(2,draftSkill,slot);closeSlot();});save.active=validDraft(d)&&(slot!=4||ClassState.secondPassive(d));
  button(x+(w-20)/2+4,popup.bottom()-28,(w-20)/2,tr("cancel"),b->closeSlot());
 }
 private void closeSlot(){slot=-1;capturing=false;draftKey=null;refresh();}
 private net.minecraft.client.KeyMapping mapping(int index){return index<3?ClassClient.ACTIVE[index]:index==3?ClassClient.ULTIMATE:ClassClient.OPEN;}
 private Component mappingName(int index){return index<3?slotName(index):tr(index==3?"key_ultimate":"key_menu");}
 private void initBindings(){
  for(int i=0;i<5;i++){int target=i;int y=61+i*Math.max(21,(height-72)/5);var b=button(width-134,y,126,mapping(i).getTranslatedKeyMessage(),v->{keyOnly=target;draftKey=null;capturing=false;infoOffset=0;refresh();});tip(b,Component.translatable(mapping(i).getName()).copy().append("\n").append(tr("quick_slot_hint")));}
 }
 private void initKeyOnly(){int w=Math.min(286,width-24),h=Math.min(164,height-16);popup=new ClassTreeViewport.Rect((width-w)/2,(height-h)/2,w,h);int x=popup.x()+8;
  if(capturing){button(x,popup.bottom()-28,w-16,tr("cancel"),b->{capturing=false;refresh();});return;}
  var key=button(x,popup.y()+36,w-16,tr("quick_key",draftKey==null?mapping(keyOnly).getTranslatedKeyMessage():draftLabel()),b->{capturing=true;refresh();});tip(key,tr("conflict_named",joined(keyConflicts(mapping(keyOnly),draftKey==null?mapping(keyOnly).getKey():draftKey))));
  button(x,popup.y()+60,w-16,tr("quick_unbind"),b->{draftKey=InputConstants.UNKNOWN;draftModifier=KeyModifier.NONE;refresh();});
  button(x,popup.bottom()-28,(w-20)/2,tr("save"),b->{if(draftKey!=null){mapping(keyOnly).setKeyModifierAndCode(draftModifier,draftKey);net.minecraft.client.KeyMapping.resetMapping();minecraft.options.save();}keyOnly=-1;refresh();});
  button(x+(w-20)/2+4,popup.bottom()-28,(w-20)/2,tr("cancel"),b->{keyOnly=-1;draftKey=null;refresh();});
 }
 private Component draftLabel(){return draftModifier.getCombinedName(draftKey,draftKey::getDisplayName);}
 private static KeyModifier modifier(int flags){return (flags&org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL)!=0?KeyModifier.CONTROL:(flags&org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)!=0?KeyModifier.SHIFT:(flags&org.lwjgl.glfw.GLFW.GLFW_MOD_ALT)!=0?KeyModifier.ALT:KeyModifier.NONE;}
 private List<Component> keyConflicts(net.minecraft.client.KeyMapping current,InputConstants.Key key){var result=new ArrayList<Component>();for(var other:minecraft.options.keyMappings)if(other!=current&&ClassClient.conflictsWith(current,key,draftKey==null?current.getKeyModifier():draftModifier,other))result.add(Component.translatable(other.getName()));return result;}
 private List<ClassDefinitions.Branch> branches(CompoundTag d){String parent=d.getString("First").isEmpty()?d.getString("Base"):d.getString("First");return ClassDefinitions.BRANCHES.values().stream().filter(b->b.parent().equals(parent)).toList();}
 private void initBranches(CompoundTag d){int n=0;for(var branch:branches(d)){int x=12+n*(width-24)/2,w=(width-32)/2;var b=button(x,height-32,w,tr("choose",Component.translatable("class.slavicmyths."+branch.id())),v->{confirmation=branch.id();refresh();});String field=branch.level()==ClassDefinitions.FIRST_LEVEL?"First":"Second";b.active=d.getString(field).isEmpty()&&(creative()||d.getInt("Level")>=branch.level());n++;}}
 private void initConfirmation(){int w=Math.min(330,width-24),x=(width-w)/2,y=(height-116)/2;button(x+8,y+86,(w-20)/2,tr("cancel"),b->{confirmation="";refresh();});button(x+12+(w-20)/2,y+86,(w-20)/2,tr("confirm"),b->{ClassNetwork.send(ClassDefinitions.BASES.contains(confirmation)?0:3,confirmation,0);confirmation="";refresh();});}
 @Override public void render(GuiGraphics g,int mx,int my,float delta){
  renderBackground(g,mx,my,delta);var d=ClassClient.data();String base=d.getString("Base");
  if(base.isEmpty())renderChoice(g);else{
   g.drawString(font,UiText.fit(font,Component.translatable("class.slavicmyths."+base).getString()+" · "+tr("progress_compact",d.getInt("Level"),d.getLong("Xp"),ClassDefinitions.xpForLevel(d.getInt("Level")),d.getInt("Points")).getString(),width-42),8,5,ClassTheme.GOLD);
   if(page==0){renderTree(g,d,mx,my);renderQuickbar(g,d,mx,my);if(!selected.isEmpty()&&slot<0&&keyOnly<0&&confirmation.isEmpty())renderCard(g,d);}else if(page==1)renderBranches(g,d);else renderBindings(g,d);
  }
  if(!confirmation.isEmpty())renderConfirmation(g);else if(keyOnly>=0)renderKeyOnly(g);else if(slot>=0)renderSlot(g,d);
  for(var widget:renderables)widget.render(g,mx,my,delta);
  if(base.isEmpty()&&confirmation.isEmpty())renderChoiceLabels(g);
  if(confirmation.isEmpty()&&!capturing){for(var entry:tips.entrySet())if(entry.getKey().isMouseOver(mx,my)){tooltip(g,entry.getValue(),mx,my);return;}}
  if(!base.isEmpty()&&page==0&&slot<0&&keyOnly<0&&confirmation.isEmpty()&&(selected.isEmpty()||!card.contains(mx,my))){String id=camera.hit(tree,mx,my);if(!id.isEmpty())tooltip(g,ClassClient.label(id),mx,my);}

 }
 private void renderTree(GuiGraphics g,CompoundTag d,int mx,int my){
  var a=camera.area();g.fill(a.x(),a.y(),a.right(),a.bottom(),0xFF192126);g.enableScissor(a.x(),a.y(),a.right(),a.bottom());
  for(var node:tree.nodes()){var parent=treeIndex.get(node.parent());if(parent==null)continue;var from=camera.node(parent);var to=camera.node(node);int ax=from.x()+from.width()/2,ay=from.bottom(),bx=to.x()+to.width()/2,by=to.y(),mid=(ay+by)/2,color=d.getCompound("Ranks").getInt(node.id())>0?0xFF86BA83:0xFF826746;
   g.fill(ax,ay,ax+2,mid+2,color);g.fill(Math.min(ax,bx),mid,Math.max(ax,bx)+2,mid+2,color);g.fill(bx,mid,bx+2,by,color);
  }
  String hovered=camera.hit(tree,mx,my);
  for(var node:tree.nodes()){var rect=camera.node(node);if(!a.overlaps(rect))continue;var s=ClassDefinitions.SKILLS.get(node.id());int rank=d.getCompound("Ranks").getInt(node.id());boolean available=s!=null&&ClassState.blocker(d,node.id(),creative()).isEmpty();
   String state=selected.equals(node.id())?"selected":hovered.equals(node.id())?"hover":s==null||rank>0||available?"normal":"disabled";
   ClassTheme.image(g,"ui/node_"+(s==null||rank>0?"learned":available?"available":"locked"),rect.x(),rect.y(),rect.width(),rect.height(),44,44);
   if(selected.equals(node.id())||hovered.equals(node.id()))ClassTheme.image(g,"ui/node_selected",rect.x()-2,rect.y()-2,rect.width()+4,rect.height()+4,48,48);
   ClassTheme.icon(g,node.id(),rect.x()+2,rect.y()+2,Math.max(1,rect.width()-4));
   if(s!=null&&rect.width()>=23){int color=rank>0?0xFF9FE09B:available?ClassTheme.GOLD:ClassTheme.MUTED;String text=rank+"/"+s.maxRank();g.fill(rect.x()+1,rect.bottom()-11,rect.right()-1,rect.bottom()-1,0xDD100D0B);g.drawCenteredString(font,Component.literal(text),rect.x()+rect.width()/2,rect.bottom()-10,color);}
  }
  g.disableScissor();

  if(camera.scale()<.5)g.drawString(font,tr("zoom_hint"),10,a.bottom()-11,ClassTheme.MUTED);
 }
 private void renderQuickbar(GuiGraphics g,CompoundTag d,int mx,int my){
  g.fill(0,height-44,width,height,0xF731271E);
  for(int i=0;i<5;i++){var r=ClassTreeViewport.slot(width,height,i);boolean locked=i==4&&!ClassState.secondPassive(d);String id=slotId(d,i);ClassTheme.plate(g,r.x(),r.y(),r.width(),r.height(),locked?"disabled":slot==i?"selected":"normal");
   if(!id.isEmpty())ClassTheme.icon(g,id,r.x()+3,r.y()+2,24);
   Component label=i<3?ClassClient.ACTIVE[i].getTranslatedKeyMessage():Component.literal(i==3?"P":"P2");g.fill(r.x()+1,r.bottom()-10,r.right()-1,r.bottom()-1,0xDD100D0B);g.drawCenteredString(font,Component.literal(UiText.fit(font,label.getString(),28)),r.x()+15,r.bottom()-9,i<3&&ClassClient.conflict(ClassClient.ACTIVE[i])?ClassTheme.ERROR:ClassTheme.TEXT);
   if(r.contains(mx,my)&&slot<0&&confirmation.isEmpty())tooltip(g,locked?tr("passive_locked"):slotName(i).copy().append("\n").append(id.isEmpty()?tr("empty"):ClassClient.label(id)).append("\n").append(tr("quick_slot_hint")),mx,my);
  }
 }
 private void renderCard(GuiGraphics g,CompoundTag d){ClassTheme.panel(g,card.x(),card.y(),card.width(),card.height());drawText(g,skillText(d,ClassDefinitions.SKILLS.get(selected)),card.x()+7,card.y()+24,card.width()-14,card.bottom()-32);g.drawString(font,tr("skill_card"),card.x()+7,card.y()+7,ClassTheme.GOLD);}
 private void renderSlot(GuiGraphics g,CompoundTag d){
  g.fill(0,0,width,height,0x88100D0B);ClassTheme.panel(g,popup.x(),popup.y(),popup.width(),popup.height());g.drawString(font,UiText.fit(font,slotName(slot).getString(),popup.width()-16),popup.x()+8,popup.y()+7,ClassTheme.GOLD);
  if(capturing){g.drawWordWrap(font,tr("press_key"),popup.x()+8,popup.y()+34,popup.width()-16,ClassTheme.TEXT);g.drawWordWrap(font,tr("capture_hint"),popup.x()+8,popup.y()+53,popup.width()-16,ClassTheme.MUTED);return;}
  if(candidates().isEmpty())g.drawWordWrap(font,tr("no_learned"),popup.x()+8,popup.y()+49,popup.width()-16,ClassTheme.MUTED);
  if(slot<3){var key=draftKey==null?ClassClient.ACTIVE[slot].getKey():draftKey;var conflicts=keyConflicts(ClassClient.ACTIVE[slot],key);
   if(!conflicts.isEmpty())g.drawString(font,UiText.fit(font,tr("conflict_named",joined(conflicts)).getString(),popup.width()-16),popup.x()+8,popup.bottom()-88,ClassTheme.ERROR);
  }

 }
 private void renderChoice(GuiGraphics g){g.drawString(font,tr("choose_path"),8,9,ClassTheme.GOLD);int y=35+Math.min(100,height-99)+5;drawText(g,List.of(Component.translatable("class.slavicmyths."+choice+".description"),tr("class_features_"+choice)),10,y,width-20,height-33);}
 private void renderChoiceLabels(GuiGraphics g){int w=(width-32)/3,h=Math.min(100,height-99);for(int i=0;i<3;i++){String id=ClassDefinitions.BASES.get(i);int x=8+i*(w+8);ClassTheme.icon(g,id,x+w/2-16,40,32);g.drawWordWrap(font,Component.translatable("class.slavicmyths."+id),x+5,76,w-10,ClassTheme.TEXT);if(h>70)g.drawWordWrap(font,tr("role_"+id),x+5,94,w-10,ClassTheme.MUTED);}}
 private void renderBindings(GuiGraphics g,CompoundTag d){for(int i=0;i<5;i++){int y=61+i*Math.max(21,(height-72)/5);g.drawString(font,UiText.fit(font,mappingName(i).getString(),width-152),8,y+5,ClassTheme.TEXT);var conflicts=keyConflicts(mapping(i),mapping(i).getKey());if(!conflicts.isEmpty())g.drawString(font,Component.literal("!"),width-145,y+5,ClassTheme.ERROR);}g.drawString(font,tr("keys_hint"),8,49,ClassTheme.MUTED);}
 private void renderKeyOnly(GuiGraphics g){g.fill(0,0,width,height,0x88100D0B);ClassTheme.panel(g,popup.x(),popup.y(),popup.width(),popup.height());g.drawString(font,mappingName(keyOnly),popup.x()+8,popup.y()+10,ClassTheme.GOLD);if(capturing){g.drawWordWrap(font,tr("press_key"),popup.x()+8,popup.y()+38,popup.width()-16,ClassTheme.TEXT);g.drawWordWrap(font,tr("capture_hint"),popup.x()+8,popup.y()+60,popup.width()-16,ClassTheme.MUTED);}else {var conflicts=keyConflicts(mapping(keyOnly),draftKey==null?mapping(keyOnly).getKey():draftKey);if(!conflicts.isEmpty())g.drawString(font,UiText.fit(font,tr("conflict_named",joined(conflicts)).getString(),popup.width()-16),popup.x()+8,popup.y()+85,ClassTheme.ERROR);}}
 private List<Component> branchText(CompoundTag d,ClassDefinitions.Branch branch){
  var lines=new ArrayList<Component>();String percent=number((branch.multiplier()-1)*100);
  lines.add(Component.translatable("classbranch.slavicmyths."+branch.id()+".effect",percent));
  lines.add(Component.translatable("classbranch.slavicmyths."+branch.id()+".role"));
  lines.add(tr("branch_skill",ClassClient.label(branch.enhancedSkill())));
  for(var skill:ClassDefinitions.SKILLS.values())if(skill.branch().equals(branch.id())){
   lines.add(tr("branch_unlock",ClassClient.label(skill.id())));lines.add(Component.translatable(skill.key()+".description"));
   lines.add(tr("branch_unlock_requirement",ClassClient.label(skill.prerequisite()),skill.prerequisiteRank(),skill.level()));
  }
  if(branch.level()==ClassDefinitions.FIRST_LEVEL){var next=ClassDefinitions.BRANCHES.values().stream().filter(b->b.parent().equals(branch.id())).map(b->Component.translatable("class.slavicmyths."+b.id())).toList();lines.add(tr("branch_future",joined(new ArrayList<Component>(next))));}
  else {lines.add(tr("branch_second_passive"));for(var u:ClassUltimates.ALL.values())if(u.parent().equals(branch.id()))lines.add(tr("branch_mastery",Component.translatable("class.slavicmyths."+u.id())));}
  if(creative())lines.add(tr("creative_free"));
  String chosen=d.getString(branch.level()==ClassDefinitions.FIRST_LEVEL?"First":"Second");if(!chosen.isEmpty())lines.add(tr("branch_current",Component.translatable("class.slavicmyths."+chosen)));
  return lines;
 }
 private void renderBranches(GuiGraphics g,CompoundTag d){int n=0;for(var branch:branches(d)){
  int x=12+n*(width-24)/2,w=(width-32)/2;ClassTheme.panel(g,x,65,w,height-104);
  ClassTheme.icon(g,branch.enhancedSkill(),x+w-36,69,28);g.drawString(font,UiText.fit(font,Component.translatable("class.slavicmyths."+branch.id()).getString(),w-52),x+8,76,ClassTheme.GOLD);
  g.drawString(font,UiText.fit(font,tr("branch_level_short",branch.level(),d.getInt("Level")).getString(),w-48),x+8,90,!creative()&&d.getInt("Level")<branch.level()?ClassTheme.ERROR:ClassTheme.MUTED);
  int saved=infoOffset;infoOffset=branchOffsets.getOrDefault(branch.id(),0);drawText(g,branchText(d,branch),x+8,104,w-16,height-44);branchOffsets.put(branch.id(),infoOffset);infoOffset=saved;n++;
 }}
 private void tooltip(GuiGraphics g,Component text,int x,int y){g.renderTooltip(font,font.split(text,Math.max(80,Math.min(280,width-20))),x,y);}
 private void renderConfirmation(GuiGraphics g){g.fill(0,0,width,height,0xCC100D0B);int w=Math.min(330,width-24),x=(width-w)/2,y=(height-116)/2;ClassTheme.panel(g,x,y,w,116);g.drawWordWrap(font,tr("confirm_path",Component.translatable("class.slavicmyths."+confirmation)),x+8,y+10,w-16,ClassTheme.TEXT);}
 private void drawText(GuiGraphics g,List<Component> text,int x,int y,int w,int bottom){var lines=new ArrayList<net.minecraft.util.FormattedCharSequence>();for(var line:text){lines.addAll(font.split(line,Math.max(20,w-5)));lines.add(Component.empty().getVisualOrderText());}int capacity=Math.max(1,(bottom-y)/10);infoOffset=Math.clamp(infoOffset,0,Math.max(0,lines.size()-capacity));if(bottom<=y)return;g.enableScissor(x,y,x+w,bottom);for(int i=0;i<capacity&&i+infoOffset<lines.size();i++)g.drawString(font,lines.get(i+infoOffset),x,y+i*10,ClassTheme.TEXT,false);g.disableScissor();if(lines.size()>capacity)g.drawString(font,Component.literal("↕"),x+w-7,bottom-9,ClassTheme.GOLD);}
 @Override public boolean mouseClicked(double x,double y,int mouse){
  if(capturing){if(popup.contains(x,y)&&y>=popup.bottom()-30)return super.mouseClicked(x,y,mouse);draftKey=InputConstants.Type.MOUSE.getOrCreate(mouse);draftModifier=hasControlDown()?KeyModifier.CONTROL:hasShiftDown()?KeyModifier.SHIFT:hasAltDown()?KeyModifier.ALT:KeyModifier.NONE;capturing=false;refresh();return true;}
  if(!confirmation.isEmpty()||slot>=0||keyOnly>=0){if(super.mouseClicked(x,y,mouse))return true;if(slot>=0&&!popup.contains(x,y))closeSlot();else if(keyOnly>=0&&!popup.contains(x,y)){keyOnly=-1;draftKey=null;refresh();}return true;}
  if(super.mouseClicked(x,y,mouse))return true;
  if(page==0&&!ClassClient.data().getString("Base").isEmpty()){
   for(int i=0;i<5;i++)if(ClassTreeViewport.slot(width,height,i).contains(x,y)){if(i!=4||ClassState.secondPassive(ClassClient.data()))openSlot(i,slotId(ClassClient.data(),i));return true;}
   if(!selected.isEmpty()&&card.contains(x,y))return true;
   if(camera.area().contains(x,y)){String id=camera.hit(tree,x,y);if(mouse==2||mouse==0&&id.isEmpty()){selected="";dragging=true;refresh();dragging=true;}else if(mouse==0){selected=id;infoOffset=0;refresh();}return true;}
  }return false;
 }
 @Override public boolean mouseDragged(double x,double y,int mouse,double dx,double dy){if(dragging){camera.pan(dx,dy);return true;}return super.mouseDragged(x,y,mouse,dx,dy);}
 @Override public boolean mouseReleased(double x,double y,int mouse){dragging=false;return super.mouseReleased(x,y,mouse);}
 @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
  if(keyOnly>=0||!confirmation.isEmpty())return true;
  if(slot>=0){listOffset-=(int)vertical;refresh();return true;}
  if(page==1){int n=0;for(var branch:branches(ClassClient.data())){int bx=12+n*(width-24)/2,bw=(width-32)/2;if(x>=bx&&x<bx+bw&&y>=65&&y<height-40){branchOffsets.merge(branch.id(),-(int)(vertical*3),Integer::sum);return true;}n++;}return true;}
  if(!selected.isEmpty()&&card.contains(x,y)||page!=0){infoOffset-=(int)(vertical*3);return true;}
  if(camera.area().contains(x,y)){camera.zoom(x,y,vertical);return true;}return false;
 }
 @Override public boolean keyPressed(int key,int scan,int modifiers){
  if(capturing){if(key==256){capturing=false;refresh();}else {var code=InputConstants.getKey(key,scan);if(KeyModifier.isKeyCodeModifier(code))return true;draftKey=code;draftModifier=modifier(modifiers);capturing=false;refresh();}return true;}
  if(key==256){if(!confirmation.isEmpty()){confirmation="";refresh();return true;}if(keyOnly>=0){keyOnly=-1;draftKey=null;refresh();return true;}if(slot>=0){closeSlot();return true;}if(!selected.isEmpty()){selected="";refresh();return true;}if(page!=0){page=0;infoOffset=0;refresh();return true;}}
  return super.keyPressed(key,scan,modifiers);
 }
 private static String number(double value){return String.format(Locale.ROOT,"%.2f",value).replaceAll("0+$","").replaceAll("\\.$","");} private void stats(List<Component> out,CompoundTag d,ClassDefinitions.Skill s,int rank,int next,boolean compare){
  if(compare)out.add(tr("next_rank",rank,next));if(s.cooldown(rank)>0)out.add(statLine("cooldown",ClassBalance.cooldown(d,s.id(),rank)/20.0,ClassBalance.cooldown(d,s.id(),next)/20.0,compare));if(s.duration(rank)>0)out.add(statLine("duration",ClassBalance.duration(d,s.id(),rank)/20.0,ClassBalance.duration(d,s.id(),next)/20.0,compare));if(s.range(rank)>0)out.add(statLine("range",ClassBalance.range(d,s.id(),rank),ClassBalance.range(d,s.id(),next),compare));
  var percentages=Set.of("lunge","flurry","aerial_strike","steadfast","ward_power","dirty_strike","light_step","steady_aim","cold_blood","opportunity");
  if(net.minecraft.client.resources.language.I18n.exists("classes.slavicmyths.stat_"+s.id())){double factor=percentages.contains(s.id())?100:1;String value=number(ClassBalance.power(d,s.id(),rank)*factor);if(compare)value+=" → "+number(ClassBalance.power(d,s.id(),next)*factor);out.add(tr("stat_"+s.id(),value));}
 }
 private Component statLine(String key,double current,double next,boolean compare){return tr("value_"+key,number(current)+(compare?" → "+number(next):""));}
 private Component reason(CompoundTag d,ClassDefinitions.Skill s,String blocker){return switch(blocker){case "level_required"->tr("required_level",s.level()+d.getCompound("Ranks").getInt(s.id())*2,d.getInt("Level"));case "prerequisite"->tr("required_skill",ClassClient.label(s.prerequisite()),s.prerequisiteRank(),d.getCompound("Ranks").getInt(s.prerequisite()));case "branch_required"->tr("required_branch",Component.translatable("class.slavicmyths."+s.branch()));case "no_points"->tr("point_cost",d.getInt("Points"));default->tr(blocker);};}

}
