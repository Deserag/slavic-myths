package org.slavicmyths.client;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.slavicmyths.kitchen.*;
/** Approved recipe/preparation/result zones, full wrapped names and explicit server blocker. */
public final class KitchenScreen extends AbstractContainerScreen<KitchenMenu>{
 private int selected,offset,recipeW,centerW,centerX,resultX,invY,rowGap,resultScroll;private Button cook;private final Map<Button,Integer> recipes=new LinkedHashMap<>();
 public KitchenScreen(KitchenMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=440;imageHeight=320;}
 private Component tr(String key,Object... a){return Component.translatable("kitchen.slavicmyths."+key,a);}
 private int rows(){return Math.max(1,(imageHeight-58)/32);}
 @Override protected void init(){imageWidth=Math.min(440,width-12);imageHeight=Math.min(320,height);super.init();clearWidgets();recipes.clear();recipeW=imageWidth*28/100;centerW=imageWidth*43/100;centerX=recipeW+9;resultX=centerX+centerW+4;invY=imageHeight-80;rowGap=Math.max(24,Math.min(32,(invY-87)/3));
  for(int i=0;i<3;i++)FolkUi.reposition(menu,i,centerX+9,46+i*rowGap);
  FolkUi.reposition(menu,3,centerX+9,46+3*rowGap+12);FolkUi.reposition(menu,4,resultX+9,49);
  int inventoryX=recipeW+10;for(int i=5;i<menu.slots.size();i++){int n=i-5;FolkUi.reposition(menu,i,inventoryX+n%9*18,invY+(n<27?n/9*18:58));}
  offset=Math.max(0,Math.min(offset,KitchenRecipes.COUNT-rows()));for(int row=0;row<rows()&&offset+row<KitchenRecipes.COUNT;row++){final int id=offset+row;var b=addRenderableWidget(FolkUi.button(leftPos+12,topPos+45+row*32,recipeW-12,30,Component.empty(),selected==id,button->{selected=id;resultScroll=0;init();}));recipes.put(b,id);}
  cook=addRenderableWidget(FolkUi.button(leftPos+resultX+6,topPos+invY-43,imageWidth-resultX-18,20,tr("cook"),true,b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,selected)));
 }
 @Override protected void renderBg(GuiGraphics g,float f,int mx,int my){FolkUi.frame(g,leftPos,topPos,imageWidth,imageHeight);g.drawCenteredString(font,title,leftPos+imageWidth/2,topPos+9,0xfff1dfb7);
  FolkUi.panel(g,leftPos+10,topPos+27,recipeW-7,imageHeight-37,0xffd5c29c);FolkUi.panel(g,leftPos+centerX,topPos+27,centerW,invY-32,0xffead9b5);FolkUi.panel(g,leftPos+resultX,topPos+27,imageWidth-resultX-10,invY-32,0xfff2e4c5);
  g.drawString(font,tr("recipes"),leftPos+16,topPos+32,FolkUi.INK,false);g.drawString(font,tr("preparation"),leftPos+centerX+7,topPos+32,FolkUi.INK,false);g.drawString(font,tr("result"),leftPos+resultX+7,topPos+32,FolkUi.INK,false);
  for(var s:menu.slots)FolkUi.slot(g,leftPos+s.x-1,topPos+s.y-1,18,false);
  var ing=KitchenRecipes.ingredients(selected);for(int i=0;i<3;i++){int y=46+i*rowGap;var owned=menu.input.getItem(i);boolean valid=owned.is(ing[i])&&owned.getCount()>=KitchenRecipes.count(selected,i);if(owned.isEmpty())g.renderItem(new ItemStack(ing[i]),leftPos+centerX+9,topPos+y);
   FolkUi.text(g,font,ing[i].getDescription(),leftPos+centerX+31,topPos+y,centerW-42,FolkUi.INK,2);g.drawString(font,(owned.is(ing[i])?owned.getCount():0)+" / "+KitchenRecipes.count(selected,i),leftPos+centerX+31,topPos+y+18,valid?FolkUi.GREEN:FolkUi.RED,false);}
  int toolY=46+3*rowGap+12;boolean tool=menu.input.getItem(3).is(KitchenRecipes.tool(selected));g.drawString(font,tr("tool"),leftPos+centerX+7,topPos+toolY-11,FolkUi.INK,false);if(menu.input.getItem(3).isEmpty())g.renderItem(new ItemStack(KitchenRecipes.tool(selected)),leftPos+centerX+9,topPos+toolY);FolkUi.text(g,font,KitchenRecipes.tool(selected).getDescription(),leftPos+centerX+31,topPos+toolY,centerW-42,tool?FolkUi.GREEN:FolkUi.RED,2);g.drawString(font,tool?"1 / 1":"0 / 1",leftPos+centerX+31,topPos+toolY+19,tool?FolkUi.GREEN:FolkUi.RED,false);
  g.renderItem(new ItemStack(KitchenRecipes.output(selected)),leftPos+resultX+34,topPos+49);g.drawString(font,"x"+KitchenRecipes.amount(selected),leftPos+resultX+54,topPos+54,FolkUi.INK,false);
  FolkUi.text(g,font,KitchenRecipes.output(selected).getDescription(),leftPos+resultX+7,topPos+71,imageWidth-resultX-24,FolkUi.INK,2);
  var details=new ArrayList<Component>();var food=KitchenRecipes.output(selected).components().get(net.minecraft.core.component.DataComponents.FOOD);if(food!=null){details.add(tr("nutrition",food.nutrition()));details.add(tr("saturation",food.saturation()));if(food.effects().isEmpty())details.add(tr("effects_none"));for(var possible:food.effects()){var e=possible.effect();details.add(tr("effect_detail",e.getEffect().value().getDisplayName(),e.getAmplifier()+1,e.getDuration()/20,Math.round(possible.probability()*100)));}}
  var lines=new ArrayList<net.minecraft.util.FormattedCharSequence>();for(var c:details)lines.addAll(font.split(c,imageWidth-resultX-25));int h=Math.max(10,invY-141),n=Math.max(1,h/10);resultScroll=Math.max(0,Math.min(resultScroll,Math.max(0,lines.size()-n)));g.enableScissor(leftPos+resultX+7,topPos+93,leftPos+imageWidth-17,topPos+invY-48);for(int i=0;i<n&&i+resultScroll<lines.size();i++)g.drawString(font,lines.get(i+resultScroll),leftPos+resultX+7,topPos+93+i*10,FolkUi.INK,false);g.disableScissor();if(lines.size()>n){int thumb=Math.max(4,h*n/lines.size()),dy=(h-thumb)*resultScroll/(lines.size()-n);g.fill(leftPos+imageWidth-15,topPos+93,leftPos+imageWidth-13,topPos+93+h,0xffc1aa87);g.fill(leftPos+imageWidth-15,topPos+93+dy,leftPos+imageWidth-13,topPos+93+dy+thumb,FolkUi.RED);}
  var blocked=menu.unavailable(selected);cook.active=blocked==null;cook.setTooltip(blocked==null?null:net.minecraft.client.gui.components.Tooltip.create(blocked));
  // Status is permanent and has its own band below the button, above inventory.
  Component status=blocked==null?tr("ready"):blocked.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t?tr(t.getKey().endsWith("requires")?"tool_missing_short":t.getKey().endsWith("output_full")?"output_full_short":"missing_short"):blocked;FolkUi.text(g,font,status,leftPos+resultX+6,topPos+invY-21,imageWidth-resultX-18,blocked==null?FolkUi.GREEN:FolkUi.RED,2);
  if(KitchenRecipes.COUNT>rows()){int track=imageHeight-61,thumb=Math.max(12,track*rows()/KitchenRecipes.COUNT),dy=(track-thumb)*offset/(KitchenRecipes.COUNT-rows());g.fill(leftPos+recipeW-3,topPos+45,leftPos+recipeW-1,topPos+45+track,0xffad9776);g.fill(leftPos+recipeW-3,topPos+45+dy,leftPos+recipeW-1,topPos+45+dy+thumb,FolkUi.RED);}
 }
 @Override protected void renderLabels(GuiGraphics g,int x,int y){}
 @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(x<leftPos+centerX){offset-=(int)Math.signum(vertical);init();}else if(x>=leftPos+resultX)resultScroll-=(int)Math.signum(vertical);return true;}
 @Override public boolean mouseClicked(double x,double y,int button){if(x>=leftPos+recipeW-5&&x<leftPos+recipeW+2&&y>=topPos+45&&y<topPos+imageHeight-15){offset=(int)((y-topPos-45)/(imageHeight-60)*(KitchenRecipes.COUNT-rows()));init();return true;}return super.mouseClicked(x,y,button);}
 @Override public void render(GuiGraphics g,int x,int y,float f){super.render(g,x,y,f);for(var e:recipes.entrySet()){var b=e.getKey();int id=e.getValue();g.renderItem(new ItemStack(KitchenRecipes.output(id)),b.getX()+4,b.getY()+7);FolkUi.text(g,font,KitchenRecipes.output(id).getDescription(),b.getX()+23,b.getY()+5,b.getWidth()-27,0xfff1dfb7,2);if(b.isHovered())g.renderTooltip(font,KitchenRecipes.output(id).getDescription(),x,y);}renderTooltip(g,x,y);}
}
