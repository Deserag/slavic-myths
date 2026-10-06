package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.slavicmyths.kitchen.*;

/** Selection is presentation only; the menu validates and consumes on the server. */
public final class KitchenScreen extends AbstractContainerScreen<KitchenMenu>{
 private int selected;private Button cook;
 public KitchenScreen(KitchenMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=320;imageHeight=240;inventoryLabelX=79;inventoryLabelY=142;}
 private Component tr(String key,Object... args){return Component.translatable("kitchen.slavicmyths."+key,args);}
 @Override protected void init(){super.init();
  for(int i=0;i<KitchenRecipes.COUNT;i++){final int id=i;String name=KitchenRecipes.output(i).getDescription().getString();if(font.width(name)>66)name=font.plainSubstrByWidth(name,58)+"…";
   addRenderableWidget(Button.builder(Component.literal(name),b->selected=id).bounds(leftPos+7,topPos+30+i*14,72,13).build());}
  cook=addRenderableWidget(Button.builder(tr("cook"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,selected)).bounds(leftPos+224,topPos+128,88,20).build());
 }
 @Override protected void renderBg(GuiGraphics g,float f,int x,int y){
  g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff493322);
  g.fill(leftPos+4,topPos+4,leftPos+imageWidth-4,topPos+imageHeight-4,0xffeadcba);
  g.fill(leftPos+4,topPos+26,leftPos+82,topPos+201,0xffc4ab83);
  g.fill(leftPos+84,topPos+26,leftPos+218,topPos+138,0xffd8c59f);
  g.fill(leftPos+220,topPos+26,leftPos+316,topPos+150,0xfff3e7cb);
  g.fill(leftPos+4,topPos+30+selected*14,leftPos+6,topPos+43+selected*14,0xff8b3e2f);
  for(var s:menu.slots){g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xff6d5740);g.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xffbda886);}
 }
 @Override protected void renderLabels(GuiGraphics g,int x,int y){
  g.drawString(font,title,8,7,0xff30261d,false);g.drawString(font,tr("ingredients"),90,29,0xff30261d,false);g.drawString(font,tr("result"),226,29,0xff30261d,false);
  var ingredients=KitchenRecipes.ingredients(selected);
  for(int i=0;i<3;i++){boolean ok=menu.input.getItem(i).is(ingredients[i])&&menu.input.getItem(i).getCount()>=KitchenRecipes.count(selected,i);
   var lines=font.split(Component.literal(KitchenRecipes.count(selected,i)+" × ").append(ingredients[i].getDescription()),88);
   for(int line=0;line<Math.min(2,lines.size());line++)g.drawString(font,lines.get(line),125,44+i*22+line*9,ok?0xff315331:0xff85362c,false);}
  boolean tool=menu.input.getItem(3).is(KitchenRecipes.tool(selected));var toolLines=font.split(KitchenRecipes.tool(selected).getDescription(),88);for(int i=0;i<Math.min(2,toolLines.size());i++)g.drawString(font,toolLines.get(i),90,112+i*9,tool?0xff315331:0xff85362c,false);
  g.renderItem(new ItemStack(KitchenRecipes.output(selected)),236,55);
  var resultLines=font.split(KitchenRecipes.output(selected).getDescription(),84);for(int i=0;i<Math.min(2,resultLines.size());i++)g.drawString(font,resultLines.get(i),226,79+i*9,0xff30261d,false);
  var food=KitchenRecipes.output(selected).components().get(net.minecraft.core.component.DataComponents.FOOD);
  if(food!=null){var info=font.split(tr("food",food.nutrition(),food.saturation()),84);for(int i=0;i<Math.min(2,info.size());i++)g.drawString(font,info.get(i),226,98+i*9,0xff30261d,false);}
  g.drawString(font,tr(food==null||food.effects().isEmpty()?"effects_none":"effects_count",food==null?0:food.effects().size()),226,117,0xff30261d,false);
  g.drawString(font,Component.literal("×"+KitchenRecipes.amount(selected)),253,59,0xff30261d,false);
 }
 @Override public void render(GuiGraphics g,int x,int y,float f){cook.active=menu.unavailable(selected)==null;super.render(g,x,y,f);renderTooltip(g,x,y);
  if(x>=cook.getX()&&x<cook.getX()+cook.getWidth()&&y>=cook.getY()&&y<cook.getY()+20&&menu.unavailable(selected)!=null)g.renderTooltip(font,font.split(menu.unavailable(selected),Math.min(240,width-20)),x,y);
  if(x>=leftPos+220&&x<leftPos+316&&y>=topPos+26&&y<topPos+126){
   java.util.List<Component> details=new java.util.ArrayList<>();details.add(KitchenRecipes.output(selected).getDescription());
   var food=KitchenRecipes.output(selected).components().get(net.minecraft.core.component.DataComponents.FOOD);
   if(food!=null){details.add(tr("food",food.nutrition(),food.saturation()));if(food.effects().isEmpty())details.add(tr("effects_none"));
    for(var possible:food.effects()){var effect=possible.effect();details.add(tr("effect_detail",effect.getEffect().value().getDisplayName(),effect.getAmplifier()+1,effect.getDuration()/20,Math.round(possible.probability()*100)));}}
   java.util.List<net.minecraft.util.FormattedCharSequence> lines=new java.util.ArrayList<>();for(var detail:details)lines.addAll(font.split(detail,Math.min(250,width-20)));g.renderTooltip(font,lines,x,y);
  }
  if(x>=leftPos+7&&x<leftPos+79&&y>=topPos+30&&y<topPos+198){int id=(y-topPos-30)/14;if(id<KitchenRecipes.COUNT)g.renderTooltip(font,KitchenRecipes.output(id).getDescription(),x,y);}
 }
}
