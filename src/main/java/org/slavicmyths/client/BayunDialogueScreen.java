package org.slavicmyths.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.bayun.BayunNetwork;

public final class BayunDialogueScreen extends Screen {
    private boolean opponents;
    private BayunNetwork.Dialogue state;private int sequence,left,top,w,h,lineOffset,row;
    public BayunDialogueScreen(BayunNetwork.Dialogue state){super(Component.translatable("entity.slavicmyths.kot_bayun"));this.state=state;}
    public void answer(BayunNetwork.Dialogue p){boolean changed=state.trust()!=p.trust();state=p;lineOffset=0;if(changed&&minecraft!=null)init();}
    @Override protected void init(){
        clearWidgets();w=Math.min(420,width-20);h=Math.min(310,height-20);left=(width-w)/2;top=(height-h)/2;
        if(opponents){row=Math.max(18,Math.min(28,(h-90)/4));for(int i=0;i<3;i++){
            final int target=i;int required=new int[]{25,60,85}[i];
            var label=Component.translatable("entity.slavicmyths."+org.slavicmyths.guides.GuideQuests.TARGETS[i]);
            var button=FolkUi.button(left+16,top+h-12-(4-i)*row,w-32,row-2,label,false,v->send(7+target));
            button.active=state.trust()>=required;button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("guides.trust",required)));addRenderableWidget(button);
        }addRenderableWidget(FolkUi.button(left+16,top+h-12-row,w-32,row-2,Component.translatable("gui.back"),false,v->{opponents=false;init();}));return;}
        String[] keys={"lore","who","where","morozko","karachun","research","opponents","bye"};row=Math.max(16,Math.min(28,(h-90)/8));
        for(int i=0;i<8;i++){final int index=i;int option=i==3?4:i==4?5:i==5?6:i==7?3:i;
            addRenderableWidget(FolkUi.button(left+16,top+h-12-(8-i)*row,w-32,row-2,Component.translatable(i==6?"guides.opponents":"bayun.dialogue."+keys[i]),false,b->{if(index==6){opponents=true;init();}else if(option==3){send(option);onClose();}else send(option);}));}
    }
    private void send(int option){PacketDistributor.sendToServer(new BayunNetwork.Choice(state.entity(),state.token(),++sequence,option));}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){renderBackground(g,mx,my,delta);FolkUi.frame(g,left,top,w,h);g.drawString(font,title,left+16,top+10,FolkUi.GOLD,false);int grade=state.trust()<25?0:state.trust()<60?1:state.trust()<85?2:3;FolkUi.text(g,font,Component.translatable("bayun.trust",state.trust(),Component.translatable("bayun.bond."+grade)),left+16,top+30,w-32,FolkUi.GREEN,2);var lines=font.split(Component.translatable(state.answer(),minecraft.player==null?Component.empty():minecraft.player.getName()),w-36);int bottom=Math.max(top+67,top+h-16-(opponents?4:8)*row);int count=Math.max(1,(bottom-top-55)/12);lineOffset=Math.clamp(lineOffset,0,Math.max(0,lines.size()-count));g.enableScissor(left+16,top+55,left+w-16,bottom);for(int i=0;i<count&&lineOffset+i<lines.size();i++)g.drawString(font,lines.get(lineOffset+i),left+18,top+55+i*12,FolkUi.INK,false);g.disableScissor();super.render(g,mx,my,delta);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){lineOffset=Math.max(0,lineOffset+(vertical>0?-2:2));return true;}
    @Override public boolean isPauseScreen(){return false;}
}
