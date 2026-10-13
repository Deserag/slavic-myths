package org.slavicmyths.verify;

import java.nio.file.*;import java.lang.reflect.*;import java.util.*;
import net.minecraft.client.*;import net.minecraft.client.gui.screens.*;import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.*;import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.client.*;import org.slavicmyths.rpg.classes.*;import org.slavicmyths.tales.*;

/** Opt-in real-client screen capture in a copied disposable dev world. Not shipped. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class UiScreenProbe {
 static boolean opened,ready,requested;static int ticks,stage=-1,shot=-1;static CompoundTag book;static String base="druzhinnik";
 static final String[] NAMES={"class-tree-scale2","class-hover-scale2","class-controls-conflict-scale2","book-page-scale2","book-search-scale2","book-deeds-scale2","book-drag-scale2","class-tree-scale3","class-hover-scale3","class-controls-conflict-scale3","book-page-scale3","book-search-scale3","book-deeds-scale3","class-tree-vedun-scale3","class-tree-razboinik-scale3","class-tree-scale4","book-page-scale4","book-search-scale4","book-deeds-scale4","class-controls-scale4"};
 static Object field(Object obj,String name){try{var f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj);}catch(Exception e){throw new RuntimeException(e);}}
 static void set(Object obj,String name,Object value){try{var f=obj.getClass().getDeclaredField(name);f.setAccessible(true);f.set(obj,value);}catch(Exception e){throw new RuntimeException(e);}}
 static CompoundTag classes(){var d=new CompoundTag();d.putString("Base",base);d.putInt("Level",30);d.putInt("Points",20);d.putLong("Xp",120);var ranks=new CompoundTag();var actives=new ArrayList<String>();for(var s:ClassDefinitions.SKILLS.values())if(s.base().equals(base)){ranks.putInt(s.id(),s.maxRank());if(s.kind()==ClassDefinitions.Kind.ACTIVE)actives.add(s.id());}d.put("Ranks",ranks);for(int i=0;i<Math.min(3,actives.size());i++)d.putString("Active"+i,actives.get(i));return d;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.createWorldOpenFlows().openWorld("UiQA",()->{});}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.setGameMode(GameType.CREATIVE);var d=ClassState.data(p);d.merge(classes());book=TaleService.snapshot(p);var known=book.getCompound("Entries");for(var raw:book.getList("Catalog",10)){String id=((CompoundTag)raw).getString("Id");var n=new CompoundTag();n.putInt("Stage",4);known.put(id,n);}book.put("Entries",known);mc.execute(()->{TaleScreen.receive(new TaleNetwork.Snapshot(book,true,false));ready=true;});});}
  if(!ready)return;ticks++;int next=(ticks-30)/55;if(ticks>=30&&next<NAMES.length&&stage!=next){stage=next;scene(mc,next);}
  if(ticks>30+NAMES.length*55){System.out.println("UI_CLIENT_CAPTURE_PASS scenes="+NAMES.length+" REAL_RENDER_FIXTURE_NOT_MANUAL_GAMEPLAY");mc.stop();}
 }
 static void pointer(Minecraft mc,int x,int y){org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),x*mc.getWindow().getGuiScale(),y*mc.getWindow().getGuiScale());}
 static void scene(Minecraft mc,int n){int kind=n>=15?new int[]{0,3,4,5,2}[n-15]:n>=13?0:n>=7?n-7:n;mc.options.guiScale().set(n>=15?4:n>=7?3:2);mc.resizeDisplay();ClassClient.ACTIVE[0].setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(49));KeyMapping.resetMapping();if(n==13)base="vedun";if(n==14)base="razboinik";RpgClient.data.put(ClassState.KEY,classes());for(String text:List.of("Финист Ясный Сокол","Добрыня Никитич","Древние глубины и забытые сказания"))for(int width:new int[]{24,50,90,130}){if(mc.font.width(UiText.fit(mc.font,text,width))>width)throw new AssertionError("Text overflow");for(var line:UiText.lines(mc.font,net.minecraft.network.chat.Component.literal(text),width,2))if(mc.font.width(line)>width)throw new AssertionError("Wrapped overflow");}
  if(kind<=2){var screen=new ClassScreen();mc.setScreen(screen);if(kind==2){set(screen,"tab",2);screen.refresh();int x=(int)field(screen,"left")+(int)field(screen,"panel")-30,y=(int)field(screen,"contentTop")+42;screen.mouseClicked(x,y,0);screen.keyPressed(49,0,0);if(!ClassClient.conflict(ClassClient.ACTIVE[0]))throw new AssertionError("Key conflict absent");System.out.println("UI_KEY_CAPTURE_PASS hotbar_conflict="+ClassClient.conflicts(ClassClient.ACTIVE[0]));}pointer(mc,5,5);}
  else {var screen=new TaleScreen();mc.setScreen(screen);if(kind==3){var entry=(CompoundTag)book.getList("Catalog",10).get(0);set(screen,"part",entry.getString("Part"));set(screen,"selected",entry.getString("Id"));set(screen,"page",3);screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());}
   if(kind==4){((EditBox)field(screen,"search")).setValue("ЯГА");if(((List<?>)field(screen,"navigation")).isEmpty())throw new AssertionError("Cross-category yo search failed");System.out.println("UI_SEARCH_PASS query=ЯГА rows="+((List<?>)field(screen,"navigation")).size());}
   if(kind==5||kind==6){set(screen,"part","deeds");set(screen,"deedGroup","legends");screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());}
   if(kind==6){int x=(int)field(screen,"left")+(int)field(screen,"navWidth")+90,y=(int)field(screen,"top")+130;screen.mouseClicked(x,y,0);screen.mouseDragged(x+60,y-30,0,60,-30);screen.mouseReleased(x+60,y-30,0);}
   int count=screen.children().size();screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());if(screen.children().size()!=count)throw new AssertionError("Duplicate widgets after refresh");pointer(mc,5,5);
  }
  System.out.println("UI_SCENE="+NAMES[n]);
 }
 @SubscribeEvent public static void render(RenderFrameEvent.Post e){if(stage<0||shot==stage||ticks<30+stage*55+25)return;var mc=Minecraft.getInstance();if((stage==1||stage==8)&&ticks<30+stage*55+35){var map=(Map<String,int[]>)field(mc.screen,"positions");var p=map.entrySet().stream().filter(v->ClassDefinitions.SKILLS.containsKey(v.getKey())&&v.getValue()[1]>=(int)field(mc.screen,"contentTop")&&v.getValue()[1]+32<(int)field(mc.screen,"contentBottom")).findFirst().orElseThrow().getValue();pointer(mc,p[0]+12,p[1]+12);return;}
  try{var out=Path.of("../../docs/verification/ui-1.7.8/client-frames");Files.createDirectories(out);try(var picture=Screenshot.takeScreenshot(mc.getMainRenderTarget())){picture.writeToFile(out.resolve(NAMES[stage]+".png"));}shot=stage;System.out.println("UI_FRAME="+NAMES[stage]);}catch(Exception ex){throw new RuntimeException(ex);}
 }
}
