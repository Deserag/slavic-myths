package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slavicmyths.rpg.classes.*;
import com.mojang.blaze3d.platform.InputConstants;

/** Scrollable fourth-stage catalogue and trial card, accessible from the existing K screen. */
public final class ClassMasteryScreen extends Screen {
    private final Screen parent;private int scroll;private boolean binding,catalogue;
    private static Component tr(String key,Object...args){return Component.translatable("classes.slavicmyths."+key,args);}
    public ClassMasteryScreen(Screen parent){super(tr("mastery"));this.parent=parent;}
    public void refresh(){if(minecraft!=null)init();}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){clearWidgets();int x=Math.max(8,(width-Math.min(620,width-16))/2),w=Math.min(620,width-16);
        addRenderableWidget(new ClassTheme.WoodButton(x,height-31,72,22,tr("back"),b->onClose()));
        addRenderableWidget(new ClassTheme.WoodButton(x+78,height-31,Math.min(185,w/3),22,binding?tr("press_key"):tr("ultimate_key",ClassClient.ULTIMATE.getTranslatedKeyMessage()),b->{binding=true;refresh();}));
        addRenderableWidget(new ClassTheme.WoodButton(x+w-142,4,142,20,tr("mastery_related"),b->{catalogue=!catalogue;scroll=0;}));
        var d=ClassClient.data();var next=ClassUltimates.next(d);if(next!=null){var choose=addRenderableWidget(new ClassTheme.WoodButton(x+w-170,height-(w<470?57:31),170,22,tr("claim_mastery"),b->ClassNetwork.send(3,next.id(),0)));choose.active=d.getString("Third").isEmpty()&&d.getInt("Level")>=40&&ClassTrialService.completed(d,next.parent());}
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override protected void renderBlurredBackground(float delta){}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float delta){g.fill(0,0,width,height,0xF0100D0B);}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){renderBackground(g,mx,my,delta);int w=Math.min(620,width-16),x=(width-w)/2;var d=ClassClient.data();g.drawString(font,tr("mastery"),x+4,8,ClassTheme.GOLD);
        var lines=new ArrayList<Component>();var icons=new IdentityHashMap<Component,String>();for(String stage:List.of("Base","First","Second","Third")){String id=d.getString(stage);lines.add(tr("stage_"+stage.toLowerCase(Locale.ROOT),id.isEmpty()?tr("empty"):Component.translatable("class.slavicmyths."+id)));}
        lines.add(tr(ClassState.secondPassive(d)?"passive_open":"passive_locked"));
        var next=ClassUltimates.next(d);if(next==null||d.getInt("Level")<40)lines.add(tr("ultimate_locked"));if(next!=null){lines.add(tr("required_level",40,d.getInt("Level")));lines.add(Component.translatable("classtrial.slavicmyths."+next.parent()));lines.add(tr("trial_progress",String.format(Locale.ROOT,"%.1f",d.getCompound("Trials").getDouble(next.parent())),ClassTrialService.required(next.parent())));lines.add(tr("trial_encounters",d.getCompound("Trials").getInt(next.parent()+"Encounters")));}
        var selected=ClassUltimates.selected(d);if(selected!=null)lines.add(tr("ultimate_status",ClassClient.label(selected.skill()),Math.max(0,(d.getCompound("Cooldowns").getLong(selected.skill())-ClassClient.now()+19)/20)));
        if(ClassClient.conflict(ClassClient.ULTIMATE))lines.add(tr("conflict_named",String.join(", ",ClassClient.conflicts(ClassClient.ULTIMATE).stream().map(Component::getString).toList())));
        if(catalogue)lines.add(tr("ultimate_catalogue"));for(var u:ClassUltimates.ALL.values()){if(!u.base().equals(d.getString("Base"))||!catalogue&&(next==null||!u.id().equals(next.id())))continue;var header=Component.translatable("class.slavicmyths."+u.id()).copy().append(" · ").append(ClassClient.label(u.skill()));icons.put(header,u.id());lines.add(header);lines.add(Component.translatable("classskill.slavicmyths."+u.skill()+".description"));lines.add(tr("ultimate_numbers",u.cooldown()/20,u.duration()/20,u.range(),u.power()));}
        var rowIcons=new HashMap<Integer,String>();var wrapped=new ArrayList<net.minecraft.util.FormattedCharSequence>();for(var line:lines){if(icons.containsKey(line))rowIcons.put(wrapped.size(),icons.get(line));wrapped.addAll(font.split(line,w-48));wrapped.add(Component.empty().getVisualOrderText());}
        int footer=w<470?66:36;int capacity=Math.max(1,(height-26-footer)/11);scroll=Math.clamp(scroll,0,Math.max(0,wrapped.size()-capacity));g.enableScissor(x,25,x+w,height-footer);for(var row:rowIcons.entrySet()){int sy=25+(row.getKey()-scroll)*11;if(sy+24>=25&&sy<height-footer){g.fill(x,sy-2,x+w,sy+20,0xFF3A3023);ClassTheme.icon(g,row.getValue(),x+4,sy,24);}}for(int i=0;i<capacity&&i+scroll<wrapped.size();i++)g.drawString(font,wrapped.get(i+scroll),x+40,25+i*11,ClassTheme.TEXT,false);g.disableScissor();
        if(next!=null)ClassTheme.icon(g,next.id(),x+2,30,32);for(var widget:renderables)widget.render(g,mx,my,delta);
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){scroll-=(int)(vertical*3);return true;}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(binding){if(key!=256){ClassClient.ULTIMATE.setKey(InputConstants.getKey(key,scan));net.minecraft.client.KeyMapping.resetMapping();minecraft.options.save();}binding=false;refresh();return true;}return super.keyPressed(key,scan,modifiers);}
    @Override public boolean mouseClicked(double x,double y,int button){if(binding){ClassClient.ULTIMATE.setKey(InputConstants.Type.MOUSE.getOrCreate(button));net.minecraft.client.KeyMapping.resetMapping();minecraft.options.save();binding=false;refresh();return true;}return super.mouseClicked(x,y,button);}
}
