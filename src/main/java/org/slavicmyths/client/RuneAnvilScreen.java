package org.slavicmyths.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slavicmyths.rpg.*;

/** Vanilla-sized container. Detailed requirements are tooltips, not a second text panel. */
public final class RuneAnvilScreen extends AbstractContainerScreen<RpgMenu> {
    private int socket;private boolean recipeList;private Button recipeButton,operationButton,removeButton;
    private final java.util.List<Button> socketButtons=new java.util.ArrayList<>();
    public RuneAnvilScreen(RpgMenu m,Inventory i,Component title){super(m,i,title);imageWidth=202;imageHeight=214;}
    private Component tr(String key,Object... args){return Component.translatable("rune_foundation.slavicmyths."+key,args);}
    private void send(int action){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
    private Button button(int x,int y,int w,Component label,Button.OnPress action){return addRenderableWidget(Button.builder(label,action).bounds(leftPos+x,topPos+y,w,18).build());}
    @Override protected void init(){super.init();clearWidgets();socketButtons.clear();
        button(8,21,60,tr("create"),b->send(103));button(71,21,60,tr("enhance"),b->send(100));button(134,21,60,tr("install"),b->send(101));
        recipeButton=button(8,79,186,tr("recipe"),b->recipeList=!recipeList);
        operationButton=button(8,79,116,tr("execute"),b->send(menu.mode==2?2+socket:menu.mode));
        removeButton=button(128,79,66,tr("remove"),b->send(menu.mode==2?101:102));
        for(int n=0;n<3;n++){final int index=n;socketButtons.add(button(110+n*27,105,24,Component.literal(new String[]{"I","II","III"}[n]),b->socket=index));}
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xffc6c6c6);
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+2,0xffeeeeee);g.fill(leftPos,topPos+imageHeight-2,leftPos+imageWidth,topPos+imageHeight,0xff555555);
        g.drawCenteredString(font,title,leftPos+imageWidth/2,topPos+7,0xff30251b);
        boolean create=menu.mode==3;recipeButton.visible=create;operationButton.visible=!create;removeButton.visible=!create;
        for(var widget:children())if(widget instanceof Button b&&b.getY()==topPos+105)b.visible=!create;
        for(var slot:menu.slots)if(slot.isActive()){int x=leftPos+slot.x-1,y=topPos+slot.y-1;g.fill(x,y,x+18,y+18,0xff373737);g.fill(x+1,y+1,x+17,y+17,0xff8b8b8b);}
        if(create){var r=menu.selectedRecipe();recipeButton.active=!menu.recipes().isEmpty();
            if(r!=null){if(menu.result.getItem(0).isEmpty())g.renderItem(r.getResultItem(minecraft.level.registryAccess()),leftPos+158,topPos+54);recipeButton.setMessage(r.getResultItem(minecraft.level.registryAccess()).getHoverName());
                var tips=new java.util.ArrayList<Component>();
                for(int n=0;n<r.inputs().size();n++){var c=r.inputs().get(n);var items=c.displayItems();Component name=items.length==0?tr("material"):items[0].getHoverName();
                    int have=c.test(menu.crafting.getItem(n))?menu.crafting.getItem(n).getCount():0;
                    g.drawCenteredString(font,have+"/"+c.count(),leftPos+28+n*32,topPos+43,have>=c.count()?0xff316334:0xff9b3030);tips.add(Component.translatable("rune_foundation.slavicmyths.requirement",name,have,c.count()));}
                if(r.chisel()){g.drawCenteredString(font,tr("tool"),leftPos+124,topPos+43,0xff333333);tips.add(tr("durability"));}
                String rune=Runes.rune(r.getResultItem(minecraft.level.registryAccess()));if(!rune.isEmpty()){tips.addAll(RuneDefinition.ALL.get(rune).tooltip());tips.add(RuneDefinition.ALL.get(rune).compatible());}
                var text=Component.empty();for(int n=0;n<tips.size();n++){if(n>0)text.append("\n");text.append(tips.get(n));}recipeButton.setTooltip(Tooltip.create(text));
                g.drawString(font,tr("recipe_hint"),leftPos+9,topPos+103,0xff444444,false);
            }
            g.drawString(font,">",leftPos+142,topPos+58,0xff333333,false);
        }else{
            int action=menu.mode==2?2+socket:menu.mode;var blocker=Runes.blocker(minecraft.player,menu.input,action);operationButton.active=blocker==null;
            operationButton.setTooltip(blocker==null?null:Tooltip.create(blocker));removeButton.setMessage(tr(menu.mode==2?"install":"remove"));
            var preview=Runes.preview(menu.input,action);if(!preview.isEmpty())g.renderItem(preview,leftPos+158,topPos+54);
            g.drawString(font,Component.translatable("rpg.slavicmyths.cost",Runes.operationCost(minecraft.player,menu.input.getItem(0),action)),leftPos+9,topPos+107,0xff333333,false);
        }
        g.drawString(font,playerInventoryTitle,leftPos+20,topPos+121,0xff404040,false);
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);
        if(recipeList&&menu.mode==3){var all=menu.recipes();int rows=(all.size()+4)/5;int x=leftPos+8,y=topPos+42;
            g.fill(x,y,x+124,y+rows*24+8,0xff333333);
            for(int n=0;n<all.size();n++){int sx=x+4+(n%5)*24,sy=y+4+(n/5)*24;g.fill(sx-1,sy-1,sx+19,sy+19,n==menu.recipeIndex?0xffa38e50:0xff777777);g.renderItem(all.get(n).value().getResultItem(minecraft.level.registryAccess()),sx+1,sy+1);}
            int index=(my-y-4)/24*5+(mx-x-4)/24;
            if(mx>=x+4&&mx<x+124&&my>=y+4&&my<y+4+rows*24&&index>=0&&index<all.size())g.renderTooltip(font,all.get(index).value().getResultItem(minecraft.level.registryAccess()),mx,my);
        }else{renderTooltip(g,mx,my);if(menu.mode!=3){var installed=Runes.list(menu.input.getItem(0));for(int n=0;n<socketButtons.size();n++){Button b=socketButtons.get(n);b.setMessage(Component.literal(n<installed.size()?"":n<Runes.slots(menu.input.getItem(0))?"+":"×"));
            if(n<installed.size()){String id=installed.get(n);var stack=new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_"+id)));g.renderItem(stack,b.getX()+4,b.getY()+1);if(b.isHovered())g.renderTooltip(font,stack,mx,my);}
            else if(b.isHovered())g.renderTooltip(font,Component.translatable("rpg.slavicmyths."+(n<Runes.slots(menu.input.getItem(0))?"socket_empty":"socket_locked")),mx,my);
        }
        if(mx>=leftPos+157&&mx<leftPos+175&&my>=topPos+53&&my<topPos+71){var preview=menu.mode==3&&menu.selectedRecipe()!=null?menu.selectedRecipe().getResultItem(minecraft.level.registryAccess()):Runes.preview(menu.input,menu.mode==2?2+socket:menu.mode);if(!preview.isEmpty())g.renderTooltip(font,preview,mx,my);}
        }}
    }
    @Override public boolean mouseClicked(double mx,double my,int button){if(recipeList){int x=leftPos+12,y=topPos+46;var all=menu.recipes();int rows=(all.size()+4)/5;
        if(button==0&&mx>=x&&mx<x+120&&my>=y&&my<y+rows*24){int index=(int)(my-y)/24*5+(int)(mx-x)/24;if(index<all.size())send(200+index);}recipeList=false;return true;}return super.mouseClicked(mx,my,button);}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(recipeList&&key==256){recipeList=false;return true;}return super.keyPressed(key,scan,modifiers);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(recipeList)return true;if(menu.mode==3&&x>=leftPos+8&&x<leftPos+194&&y>=topPos+79&&y<topPos+97){var all=menu.recipes();if(!all.isEmpty())send(200+Math.floorMod(menu.recipeIndex-(int)Math.signum(vertical),all.size()));return true;}return super.mouseScrolled(x,y,horizontal,vertical);}
}
