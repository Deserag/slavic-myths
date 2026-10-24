package org.slavicmyths.verify;
import java.nio.file.*;import java.lang.reflect.*;import java.util.*;
import net.minecraft.client.*;import net.minecraft.client.gui.screens.*;import net.minecraft.nbt.*;
import net.neoforged.api.distmarker.Dist;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.client.*;import org.slavicmyths.tales.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class BookCapture {
 static boolean opened,requested,ready;static int ticks,scene=-1,shot=-1;static long previous;static CompoundTag book;
 static final ArrayList<Double> frames=new ArrayList<>();static final ArrayList<String> results=new ArrayList<>();
 static Path out=Path.of(System.getProperty("slavicmyths.bookOutput"));
 static void set(Object o,String key,Object value){try{var f=o.getClass().getDeclaredField(key);f.setAccessible(true);f.set(o,value);}catch(Exception e){throw new RuntimeException(e);}}
 static Object get(Object o,String key){try{var f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);}catch(Exception e){throw new RuntimeException(e);}}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
  var mc=Minecraft.getInstance();
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.pauseOnLostFocus=false;mc.options.framerateLimit().set(260);mc.options.enableVsync().set(false);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.createWorldOpenFlows().openWorld("BookQA",()->{});}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;mc.getSingleplayerServer().execute(()->{
   var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
   book=TaleService.snapshot(p);var entries=book.getCompound("Entries");for(var raw:book.getList("Catalog",10)){String id=((CompoundTag)raw).getString("Id");var n=new CompoundTag();n.putInt("Stage",4);entries.put(id,n);}book.put("Entries",entries);
   mc.execute(()->{TaleScreenBefore.receive(new TaleNetwork.Snapshot(book,true,false));TaleScreen.receive(new TaleNetwork.Snapshot(book,true,false));ready=true;});
  });}
  if(!ready)return;
  int next=ticks++/65;
  if(next>=24){finish();mc.stop();return;}
  if(next!=scene){if(scene>=0)record();scene=next;frames.clear();previous=0;setup(mc);}
 }
 static String name(){return (scene%2==0?"before":"after")+"-"+(scene/2%2==0?"page":"deeds")+"-scale"+(2+scene/4%3)+"-"+(scene<12?"1280x720":"1920x1080");}
 static void setup(Minecraft mc){
  int scale=2+scene/4%3;org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),scene<12?1280:1920,scene<12?720:1080);
  mc.options.guiScale().set(scale);mc.resizeDisplay();Screen screen=scene%2==0?new TaleScreenBefore():new TaleScreen();mc.setScreen(screen);
  var entry=(CompoundTag)book.getList("Catalog",10).get(0);set(screen,"part",scene/2%2==0?entry.getString("Part"):"deeds");set(screen,"selected",entry.getString("Id"));set(screen,"page",3);
  if(screen instanceof TaleScreen){((Set<String>)get(screen,"expanded")).add(scene/2%2==0?entry.getString("Part"):"deeds");if(scene/2%2==1){set(screen,"tree",true);set(screen,"selectedDeed",((CompoundTag)book.getList("Advancements",10).get(0)).getString("Id"));}}
  screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
  System.out.println("BOOK_SCENE "+name());
 }
 @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
  if(scene<0||ticks%65<20)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(previous!=0)frames.add((now-previous)/1e6);previous=now;
  if(shot!=scene&&ticks%65>45){Files.createDirectories(out);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(name()+".png"));}shot=scene;}
 }
 static void record(){var sorted=frames.stream().sorted().toList();if(sorted.isEmpty())return;double sum=sorted.stream().mapToDouble(v->v).sum();String result=name()+","+sorted.size()+","+(1000*sorted.size()/sum)+","+sorted.get(Math.min(sorted.size()-1,(int)(sorted.size()*.95)))+","+sorted.get(Math.min(sorted.size()-1,(int)(sorted.size()*.99)));results.add(result);System.out.println("BOOK_METRIC "+result);}
 static void finish()throws Exception{record();Files.createDirectories(out);Files.writeString(out.resolve("frames.csv"),"scene,frames,mean_fps,p95_ms,p99_ms\n"+String.join("\n",results)+"\n");System.out.println("BOOK_CAPTURE_FINISHED real_client_fixture scenes="+results.size());}
}
