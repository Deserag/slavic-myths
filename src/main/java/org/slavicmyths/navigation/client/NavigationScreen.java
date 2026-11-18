package org.slavicmyths.navigation.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slavicmyths.navigation.*;

/** Standard paged Minecraft list/settings screen, never a map. */
public final class NavigationScreen extends Screen {
    private boolean filters;private int page;private List<SlavicMarker> rows=List.of();
    private int left,top,listWidth,perPage,detailOffset;private Component detailContent;private UUID detailMarker;private long detailTick=Long.MIN_VALUE;
    private final org.slavicmyths.client.UiText.Layout detailLayout=new org.slavicmyths.client.UiText.Layout();
    public NavigationScreen(){super(Component.translatable("navigation.slavicmyths.title"));}
    public void refresh(){if(minecraft!=null)init();}
    @Override protected void init(){
        clearWidgets();detailContent=null;left=Math.max(8,(width-520)/2);listWidth=Math.min(520,width-16);top=58;perPage=1;
        addRenderableWidget(Button.builder(Component.translatable("navigation.slavicmyths.markers"),b->{filters=false;page=0;detailOffset=0;init();}).bounds(left,27,listWidth/2-2,20).build());
        addRenderableWidget(Button.builder(Component.translatable("navigation.slavicmyths.filters"),b->{filters=true;init();}).bounds(left+listWidth/2+2,27,listWidth/2-2,20).build());
        if(filters){
            int options=MarkerCategory.values().length+NavigationState.Preference.values().length;
            int pageSize=Math.max(2,(height-112)/22*2);page=Math.max(0,Math.min(page,(options-1)/pageSize));
            int i=0;for(var category:MarkerCategory.values()){
                final var c=category;boolean shown=NavigationClient.state.categories.contains(c);
                int optionIndex=i++;if(optionIndex>=page*pageSize&&optionIndex<(page+1)*pageSize)
                    button(toggle(c.translation(),shown),optionIndex-page*pageSize,b->NavigationNetwork.request(NavigationNetwork.Action.CATEGORY,null,c.ordinal(),!shown));
            }
            for(var pref:NavigationState.Preference.values()){
                final var option=pref;boolean enabled=NavigationClient.state.preferences.contains(option);
                int optionIndex=i++;if(optionIndex>=page*pageSize&&optionIndex<(page+1)*pageSize)
                    button(toggle("navigation.slavicmyths.preference."+option.name().toLowerCase(Locale.ROOT),enabled),optionIndex-page*pageSize,b->NavigationNetwork.request(NavigationNetwork.Action.PREFERENCE,null,option.ordinal(),!enabled));
            }
            addRenderableWidget(Button.builder(Component.literal("<"),b->{page--;detailOffset=0;init();}).bounds(left,height-43,24,20).build());
            addRenderableWidget(Button.builder(Component.literal(">"),b->{page++;detailOffset=0;init();}).bounds(left+28,height-43,24,20).build());
        }else{
            var all=new ArrayList<>(NavigationClient.state.markers.values());
            if(!NavigationClient.state.preferences.contains(NavigationState.Preference.HIDE_COMPLETED))
                NavigationClient.state.history.stream().filter(m->!NavigationClient.state.markers.containsKey(m.id())).forEach(all::add);
            rows=all;int max=Math.max(0,(rows.size()-1)/perPage);page=Math.max(0,Math.min(page,max));
            for(int r=0;r<perPage&&page*perPage+r<rows.size();r++){
                var marker=rows.get(page*perPage+r);if(!marker.id().equals(detailMarker)){detailOffset=0;detailMarker=marker.id();}int y=height-68;boolean live=marker.equals(NavigationClient.state.markers.get(marker.id()));
                boolean tracked=marker.id().equals(NavigationClient.state.tracked);
                var track=Button.builder(Component.translatable(tracked?"navigation.slavicmyths.stop":"navigation.slavicmyths.track"),b->NavigationNetwork.request(tracked?NavigationNetwork.Action.STOP:NavigationNetwork.Action.TRACK,marker.id(),0,true)).bounds(left,y,Math.max(60,listWidth/3-4),20).build();track.active=live;addRenderableWidget(track);
                boolean hidden=NavigationClient.state.hidden.contains(marker.id());
                var hide=Button.builder(Component.translatable(hidden?"navigation.slavicmyths.show":"navigation.slavicmyths.hide"),b->NavigationNetwork.request(NavigationNetwork.Action.HIDE,marker.id(),0,!hidden)).bounds(left+listWidth/3,y,listWidth/3-4,20).build();hide.active=live;addRenderableWidget(hide);
                var forget=Button.builder(Component.translatable("navigation.slavicmyths.forget"),b->NavigationNetwork.request(NavigationNetwork.Action.FORGET,marker.id(),0,true)).bounds(left+listWidth*2/3,y,listWidth/3-4,20).build();forget.active=live&&marker.permanent();addRenderableWidget(forget);
            }
            addRenderableWidget(Button.builder(Component.literal("<"),b->{page--;detailOffset=0;init();}).bounds(left,height-43,24,20).build());
            addRenderableWidget(Button.builder(Component.literal(">"),b->{page++;detailOffset=0;init();}).bounds(left+28,height-43,24,20).build());
            addRenderableWidget(Button.builder(Component.translatable("navigation.slavicmyths.save_nearby"),b->NavigationNetwork.request(NavigationNetwork.Action.SAVE_NEARBY,null,0,true)).bounds(left+60,height-43,Math.min(200,listWidth-120),20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(left+listWidth-60,height-43,60,20).build());
    }
    private Component toggle(String key,boolean enabled){return Component.literal(enabled?"[✓] ":"[ ] ").append(Component.translatable(key));}
    private void button(Component name,int i,Button.OnPress press){int col=i%2,row=i/2;
        addRenderableWidget(Button.builder(name,press).bounds(left+col*(listWidth/2+2),top+row*22,listWidth/2-4,20).build());}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        super.render(g,mouseX,mouseY,partial);g.drawCenteredString(font,title,width/2,10,0xffffff);
        if(filters)return;
        if(!rows.isEmpty())g.drawCenteredString(font,Component.translatable("navigation.search.page",page+1,rows.size()),width/2,height-17,0xbbbbbb);
        if(rows.isEmpty())g.drawCenteredString(font,Component.translatable("navigation.slavicmyths.empty"),width/2,top+15,0xbbbbbb);
        for(int r=0;r<perPage&&page*perPage+r<rows.size();r++){
            var m=rows.get(page*perPage+r);var player=minecraft.player;long tick=minecraft.level==null?0:minecraft.level.getGameTime();
            if(detailContent==null||detailTick!=tick){
                detailContent=Component.translatable(m.name()).copy().append("\n").append(SearchGuidance.marker(m,player==null?"":player.level().dimension().location().toString(),player==null?Double.NaN:player.getX(),player==null?Double.NaN:player.getZ(),NavigationClient.state.markers.containsKey(m.id())));detailTick=tick;
            }
            var lines=detailLayout.split(font,detailContent,Math.max(8,listWidth-8));int bottom=height-78,count=SearchCoordinates.visibleRows(top,bottom);detailOffset=SearchCoordinates.scroll(detailOffset,lines.size(),count);
            g.enableScissor(left,top,left+listWidth,bottom);
            for(int i=0;i<count&&detailOffset+i<lines.size();i++)g.drawString(font,lines.get(detailOffset+i),left,top+i*12,0xffffff,false);
            g.disableScissor();
            if(lines.size()>count){int track=bottom-top,thumb=Math.max(6,track*count/lines.size()),at=(track-thumb)*detailOffset/Math.max(1,lines.size()-count);g.fill(left+listWidth-3,top+at,left+listWidth-1,top+at+thumb,0xffaaaaaa);}

        }
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(!filters){detailOffset=Math.max(0,detailOffset+(vertical>0?-3:3));return true;}return super.mouseScrolled(x,y,horizontal,vertical);}
    @Override public boolean isPauseScreen(){return false;}
}
