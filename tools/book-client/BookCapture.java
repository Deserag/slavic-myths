package org.slavicmyths.verify;
import java.nio.file.*;import java.lang.reflect.*;import java.util.*;
import net.minecraft.client.*;import net.minecraft.client.gui.screens.*;import net.minecraft.nbt.*;
import net.neoforged.api.distmarker.Dist;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.client.*;import org.slavicmyths.tales.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class BookCapture {
 static boolean opened,requested,ready,finished,reloading;static int finalScene=-1;static final boolean FINAL=Boolean.getBoolean("slavicmyths.bookFinal");static int ticks,scene=-1,shot=-1;static long previous;static CompoundTag book;
 static final ArrayList<Double> frames=new ArrayList<>();static final ArrayList<String> results=new ArrayList<>();
 static Path out=Path.of(System.getProperty("slavicmyths.bookOutput"));
 static void set(Object o,String key,Object value){try{var f=o.getClass().getDeclaredField(key);f.setAccessible(true);f.set(o,value);}catch(Exception e){throw new RuntimeException(e);}}
 static Object get(Object o,String key){try{var f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);}catch(Exception e){throw new RuntimeException(e);}}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
  if(finished)return;var mc=Minecraft.getInstance();
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.pauseOnLostFocus=false;mc.options.framerateLimit().set(260);mc.options.enableVsync().set(false);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.createWorldOpenFlows().openWorld("BookQA",()->{});}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;mc.getSingleplayerServer().execute(()->{
   var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
   book=TaleService.snapshot(p);var entries=book.getCompound("Entries");for(var raw:book.getList("Catalog",10)){String id=((CompoundTag)raw).getString("Id");var n=new CompoundTag();n.putInt("Stage",4);entries.put(id,n);}book.put("Entries",entries);
   mc.execute(()->{TaleScreenBefore.receive(new TaleNetwork.Snapshot(book,true,false));TaleScreen.receive(new TaleNetwork.Snapshot(book,true,false));ready=true;});
  });}
  if(!ready)return;
  if(FINAL){finalTick(mc);return;}
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
  if(finished||reloading)return;if(FINAL){finalFrame();return;}
  if(scene<0||ticks%65<20)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(previous!=0)frames.add((now-previous)/1e6);previous=now;
  if(shot!=scene&&ticks%65>45){Files.createDirectories(out);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(name()+".png"));}shot=scene;}
 }
 static int finalAge;static String finalName;static boolean finalShot;
 static void finalTick(Minecraft mc)throws Exception{
  if(reloading)return;
  if(finalScene>=0&&++finalAge<45)return;
  if(finalScene>=0){recordFinal();}
  if(++finalScene>=48){Files.createDirectories(out);Files.writeString(out.resolve("frames.csv"),"scene,frames,mean_fps,p95_ms,p99_ms\n"+String.join("\n",results)+"\n");System.out.println("BOOK_FINAL_CAPTURE_FINISHED scenes="+results.size());finished=true;mc.disconnect(new TitleScreen());GuideWorldProbe.begin();return;}
  String lang=finalScene<24?"en_us":"ru_ru";
  if(!mc.getLanguageManager().getSelected().equals(lang)){reloading=true;mc.getLanguageManager().setSelected(lang);mc.options.languageCode=lang;mc.reloadResourcePacks().thenRun(()->mc.execute(()->{reloading=false;setupFinal(mc);}));}else setupFinal(mc);
 }
 static void setupFinal(Minecraft mc){
  int group=finalScene%24,res=group/8,scale=new int[]{2,3,4,0}[group/2%4];boolean deeds=group%2==1;boolean tree=deeds&&group%8!=3;
  org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),new int[]{1280,1920,2560}[res],new int[]{720,1080,1440}[res]);mc.options.guiScale().set(scale);mc.resizeDisplay();
  var screen=new TaleScreen();mc.setScreen(screen);var entry=(CompoundTag)book.getList("Catalog",10).get(0);set(screen,"query","");set(screen,"navOffset",0);set(screen,"part",deeds?"deeds":entry.getString("Part"));set(screen,"selected",deeds?"":entry.getString("Id"));set(screen,"page",3);set(screen,"tree",tree);set(screen,"sidebar",group%4<2);set(screen,"fullscreen",group%4>=2);
  ((Set<String>)get(screen,"expanded")).add(deeds?"deeds":entry.getString("Part"));set(screen,"selectedDeed",((CompoundTag)book.getList("Advancements",10).get(0)).getString("Id"));screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
  if(finalScene==0)checkKeys(mc,screen);
  finalName=(finalScene<24?"en":"ru")+"-"+new int[]{1280,1920,2560}[res]+"-scale"+scale+"-"+(deeds?(tree?"tree":"list"):"page")+"-"+(group%4<2?"sidebar":"reader");finalName+="-actual"+mc.getWindow().getGuiScale();finalAge=0;finalShot=false;frames.clear();previous=0;System.out.println("BOOK_FINAL_SCENE "+finalName);
 }
 static void checkKeys(Minecraft mc,Screen back){
  for(int i=0;i<3;i++){if(ClassClient.ACTIVE[i].getDefaultKeyModifier()!=net.neoforged.neoforge.client.settings.KeyModifier.CONTROL||ClassClient.ACTIVE[i].getDefaultKey().getValue()!=49+i)throw new AssertionError("default combo");ClassClient.ACTIVE[i].setKeyModifierAndCode(net.neoforged.neoforge.client.settings.KeyModifier.CONTROL,com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(49+i));}
  var classes=new ClassScreen();mc.setScreen(classes);set(classes,"keyOnly",0);set(classes,"capturing",true);classes.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());classes.keyPressed(341,0,2);if(!(boolean)get(classes,"capturing"))throw new AssertionError("modifier prematurely captured");classes.keyPressed(49,0,2);if(get(classes,"draftModifier")!=net.neoforged.neoforge.client.settings.KeyModifier.CONTROL)throw new AssertionError("Ctrl lost");
  String save=net.minecraft.network.chat.Component.translatable("classes.slavicmyths.save").getString();for(var widget:classes.children())if(widget instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals(save)){b.onPress();break;}
  mc.options.save();ClassClient.ACTIVE[0].setKeyModifierAndCode(net.neoforged.neoforge.client.settings.KeyModifier.NONE,com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(88));mc.options.load();if(ClassClient.ACTIVE[0].getKeyModifier()!=net.neoforged.neoforge.client.settings.KeyModifier.CONTROL||ClassClient.ACTIVE[0].getKey().getValue()!=49)throw new AssertionError("native options combo reload");
  if(ClassClient.conflictsWith(ClassClient.ACTIVE[0],ClassClient.ACTIVE[0].getKey(),ClassClient.ACTIVE[0].getKeyModifier(),mc.options.keyHotbarSlots[0]))throw new AssertionError("false hotbar conflict");net.minecraft.client.KeyMapping.resetMapping();mc.setScreen(back);System.out.println("KEY_COMBO_CAPTURE_SAVE_RELOAD_PASS real client; physical key activation NOT_TESTED");
 }
 static void finalFrame()throws Exception{if(finalScene<0||finalAge<15||reloading)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(previous!=0)frames.add((now-previous)/1e6);previous=now;if(!finalShot&&finalAge>32){Files.createDirectories(out);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(finalName+".png"));}finalShot=true;}}
 static void recordFinal(){var sorted=frames.stream().sorted().toList();if(sorted.isEmpty())return;double sum=sorted.stream().mapToDouble(v->v).sum();String line=finalName+","+sorted.size()+","+(1000*sorted.size()/sum)+","+sorted.get(Math.min(sorted.size()-1,(int)(sorted.size()*.95)))+","+sorted.get(Math.min(sorted.size()-1,(int)(sorted.size()*.99)));results.add(line);System.out.println("BOOK_FINAL_METRIC "+line);}
 static void record(){var sorted=frames.stream().sorted().toList();if(sorted.isEmpty())return;double sum=sorted.stream().mapToDouble(v->v).sum();String result=name()+","+sorted.size()+","+(1000*sorted.size()/sum)+","+sorted.get(Math.min(sorted.size()-1,(int)(sorted.size()*.95)))+","+sorted.get(Math.min(sorted.size()-1,(int)(sorted.size()*.99)));results.add(result);System.out.println("BOOK_METRIC "+result);}
 static void finish()throws Exception{record();Files.createDirectories(out);Files.writeString(out.resolve("frames.csv"),"scene,frames,mean_fps,p95_ms,p99_ms\n"+String.join("\n",results)+"\n");System.out.println("BOOK_CAPTURE_FINISHED real_client_fixture scenes="+results.size());}
}
