package org.slavicmyths.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.slavicmyths.rpg.classes.ClassState;

/** Full-texture UVs and nine-slice plates independent of the vanilla button atlas. */
public final class ClassTheme {
 public static final int TEXT=0xF1E7D0,MUTED=0xC1B6A1,GOLD=0xD6B46D,ERROR=0xE49288;
 public static void image(GuiGraphics g,String file,int x,int y,int w,int h,int sw,int sh){g.blit(ClassState.id("textures/gui/classes/"+file+".png"),x,y,w,h,0,0,sw,sh,sw,sh);}
 public static void icon(GuiGraphics g,String id,int x,int y,int size){int source=org.slavicmyths.rpg.classes.ClassDefinitions.BASES.contains(id)?64:32;image(g,id,x,y,size,size,source,source);}
 public static void plate(GuiGraphics g,int x,int y,int w,int h,String state){
  var texture=ClassState.id("textures/gui/classes/ui/button_"+state+".png");
  int[] sx={0,4,92},sy={0,4,20},sw={4,88,4},sh={4,16,4},dx={x,x+4,x+w-4},dy={y,y+4,y+h-4},dw={4,w-8,4},dh={4,h-8,4};
  for(int a=0;a<3;a++)for(int b=0;b<3;b++)g.blit(texture,dx[a],dy[b],dw[a],dh[b],sx[a],sy[b],sw[a],sh[b],96,24);
 }
 public static void panel(GuiGraphics g,int x,int y,int w,int h){g.fill(x,y,x+w,y+h,0xF7211A14);g.fill(x,y,x+w,y+1,0xFF80623D);g.fill(x,y+h-1,x+w,y+h,0xFF59452E);}
 public static final class WoodButton extends Button {
  public boolean selected;private long pressedUntil;
  public WoodButton(int x,int y,int w,int h,Component label,OnPress press){super(x,y,w,h,label,press,DEFAULT_NARRATION);}
  @Override public void onPress(){pressedUntil=net.minecraft.Util.getMillis()+120;super.onPress();}
  @Override protected void renderWidget(GuiGraphics g,int mx,int my,float delta){String state=!active?"disabled":net.minecraft.Util.getMillis()<pressedUntil?"pressed":selected?"selected":isHoveredOrFocused()?"hover":"normal";plate(g,getX(),getY(),getWidth(),getHeight(),state);renderScrollingString(g,Minecraft.getInstance().font,6,active?TEXT:MUTED);}
 }
 private ClassTheme(){}
}
