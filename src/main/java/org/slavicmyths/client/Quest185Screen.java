package org.slavicmyths.client;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.quest185.*;
/** Minimal dialogue/journal extension of the existing book and NPC interfaces. */
public final class Quest185Screen extends Screen {
 private long updatedAt;private CompoundTag data;private int sequence,selected;private boolean pending;private long sentAt;private int x,y,w,h;
 public Quest185Screen(CompoundTag n){super(Quest185Story.text("chapter"));update(n);}
 public void update(CompoundTag n){data=n.copy();updatedAt=net.minecraft.Util.getMillis();sequence=n.getInt("Sequence");pending=false;if(minecraft!=null)init();}
 private String earthStatus(){return data.getBoolean("EarthClaimed")?"COMPLETED":!data.getBoolean("Cleansing")?"LOCKED":data.getInt("EarthCount")>=20?"READY":"ACTIVE";}
 private Component t(String key,Object...args){return Quest185Story.text(key,args);}
 private String kind(){return data.getString("Kind");}
 private void button(int bx,int by,int width,Component text,int action,int stage){var b=Button.builder(text,v->send(action,stage)).bounds(bx,by,width,20).build();b.active=!pending;addRenderableWidget(b);}
 private void send(int action,int stage){if(pending||!data.hasUUID("Token"))return;pending=true;sentAt=net.minecraft.Util.getMillis();PacketDistributor.sendToServer(new Quest185Network.Action(data.getUUID("Token"),++sequence,action,stage,-1));for(var child:children())if(child instanceof Button b)b.active=false;}
 public static void request(){PacketDistributor.sendToServer(new Quest185Network.Action(Quest185Sessions.NONE,0,0,0,-1));}
 @Override protected void init(){clearWidgets();w=Math.min(width-16,550);h=Math.min(height-16,330);x=(width-w)/2;y=kind().equals("journal")?(height-h)/2:height-Math.min(145,height-12)-6;
  if(kind().equals("invite")){button(x+8,y+105,w/2-12,t("teleport"),1,0);button(x+w/2+4,y+105,w/2-12,t("walk"),2,0);}
  else if(kind().equals("journal")){int row=Math.max(10,Math.min(21,(h-120)/10));for(int i=0;i<10;i++){final int index=i<6?i:i==6?9:i-1;int top=y+28+i*row+(i>=6?3:0);String status=index==9?earthStatus():data.getList("Stages",10).getCompound(index).getString("Status");var label=t(index==9?"earth_title":"title."+index).copy().append(" · ").append(t("state."+status));var b=Button.builder(label,v->{selected=index;init();}).bounds(x+8,top,w-16,row-1).build();addRenderableWidget(b);}int bottom=y+h-54;String status=selected==9?earthStatus():data.getList("Stages",10).getCompound(selected).getString("Status");boolean can=status.equals("READY")||status.equals("AVAILABLE")||selected==4&&status.equals("ACTIVE");button(x+8,bottom,90,t(status.equals("AVAILABLE")?"accept":"submit"),selected==9?15:6,selected);((Button)children().get(children().size()-1)).active=can&&!pending;button(x+102,bottom,104,t("pending"),7,0);button(x+210,bottom,Math.max(70,w-218),t(data.getBoolean("IntroDone")?"allies":"continue_intro"),data.getBoolean("IntroDone")?11:4,0);
   int third=(w-24)/3;if(!data.getBoolean("IntroDone")&&data.getBoolean("Invitation"))button(x+8,y+h-28,third,t("teleport"),1,0);else if(data.getBoolean("EyeReady"))button(x+8,y+h-28,third,t("study_eye"),12,0);else if(selected==6)button(x+8,y+h-28,third,t("sample"),8,0);if(data.getBoolean("Cleansing")){if(!data.getBoolean("EarthClaimed")&&data.getInt("EarthCount")<20)button(x+12+third,y+h-28,third,t(data.getBoolean("EarthVirtual")?"virtual_clean":"earth_training"),data.getBoolean("EarthVirtual")?14:13,0);addRenderableWidget(Button.builder(t("ask_recipe"),v->{onClose();minecraft.setScreen(TaleScreen.cleansing());}).bounds(x+w-third-8,y+h-28,third,20).build());}}
  else if(kind().equals("arrival")){button(x+8,y+105,w-16,t("waiting_arrival"),3,0);((Button)children().get(children().size()-1)).active=false;}
  else if(kind().equals("lesson_offer")){button(x+8,y+105,w/2-12,t("virtual_clean"),14,0);button(x+w/2+4,y+105,w/2-12,t("try_myself"),5,0);}
  else {button(x+w-130,y+5,122,t("ask_mor"),10,0);button(x+8,y+105,w/2-12,t(kind().equals("warning")&&data.getBoolean("Last")?"acknowledge":kind().equals("intro")&&data.getString("Line").endsWith("choice")?"begin":"next"),5,0);if(data.getLong("Wait")>0&&(kind().equals("ritual")||kind().equals("lesson")||kind().equals("eye")&&data.getInt("Page")==0))((Button)children().get(children().size()-1)).active=false;button(x+w/2+4,y+105,w/2-12,t("later"),9,0);}
 }
 @Override public void tick(){if(data.getLong("Wait")>0&&net.minecraft.Util.getMillis()-updatedAt>=data.getLong("Wait")*50){data.putLong("Wait",0);init();}if(minecraft.player==null||minecraft.level==null){minecraft.setScreen(null);return;}if(kind().equals("lesson")&&!pending&&net.minecraft.Util.getMillis()-updatedAt>3100)send(5,0);if(pending&&net.minecraft.Util.getMillis()-sentAt>3000){pending=false;init();}if(kind().equals("arrival")&&data.contains("Door")&&minecraft.level.hasChunkAt(net.minecraft.core.BlockPos.of(data.getLong("Door")))&&minecraft.player.blockPosition().distSqr(net.minecraft.core.BlockPos.of(data.getLong("Door")))<256&&!pending)send(3,0);}
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){if(kind().equals("journal"))g.fill(0,0,width,height,0x50100C08);}
 @Override protected void renderBlurredBackground(float partial){}
 private void lines(GuiGraphics g,Component text,int bx,int by,int width,int limit,int color){var rows=font.split(text,width);for(int i=0;i<Math.min(limit,rows.size());i++)g.drawString(font,rows.get(i),bx,by+i*10,color,false);}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){int ph=kind().equals("journal")?h:135;g.fill(x,y,x+w,y+ph,0xF3D8C5A1);g.renderOutline(x,y,w,ph,0xFF765238);g.drawString(font,title,x+8,y+8,0x3D3023,false);
  if(kind().equals("journal")){var q=data.getList("Stages",10).getCompound(selected);int row=Math.max(10,Math.min(21,(h-120)/10));int top=y+28+10*row+8;lines(g,selected==9?t("earth_progress",data.getInt("EarthCount")):t("objective."+selected,q.getInt("Progress"),data.getBoolean("Likho"),data.getBoolean("Ovinnik")),x+9,top,w-70,2,0x3D3023);var reward=selected==9?new ItemStack(Quest185Content.POWDER.get(),8):Quest185Story.reward(selected);if(!reward.isEmpty())g.renderItem(reward,x+w-30,top);if(data.getBoolean("HutKnown")){var pos=net.minecraft.core.BlockPos.of(data.getLong("Hut"));g.drawString(font,t("hut_coordinates",pos.getX(),pos.getZ()),x+130,y+h-23,0x665338,false);}}
  else{Component line=kind().equals("invite")?t("invite."+data.getString("Inviter")):kind().equals("arrival")?t("waiting_arrival"):Component.translatable(data.getString("Line"));if(!kind().equals("invite")&&!kind().equals("arrival"))g.drawString(font,t("speaker."+data.getString("Speaker")),x+9,y+26,0x80551E,false);lines(g,line,x+9,y+44,w-18,kind().equals("lesson")?3:6,0x3D3023);if(kind().equals("lesson")){long age=net.minecraft.Util.getMillis()-updatedAt;for(int i=0;i<9;i++)g.renderItem(new ItemStack(age>=3000L*(i+1)/9?net.minecraft.world.level.block.Blocks.STONE.asItem():org.slavicmyths.mor.MorBlocks.BLOCKS.get("stone").get().asItem()),x+12+i*18,y+80);g.drawString(font,t("earth_progress",data.getInt("EarthCount")),x+180,y+84,0x3D3023,false);}}
  super.render(g,mx,my,partial);
 }
 @Override public boolean keyPressed(int key,int scan,int mods){if((key==257||key==32)&&!kind().equals("journal")&&!kind().equals("arrival")){if(data.getLong("Wait")<=0)send(5,0);return true;}return super.keyPressed(key,scan,mods);}
 @Override public void onClose(){if(data.hasUUID("Token"))PacketDistributor.sendToServer(new Quest185Network.Action(data.getUUID("Token"),sequence+1,9,0,-1));Quest185Client.clearActors();minecraft.setScreen(null);}
 @Override public boolean isPauseScreen(){return false;}
}
