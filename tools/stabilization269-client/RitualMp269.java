package org.slavicmyths.verify.client;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import org.slavicmyths.sanctuary.*;

/** Real TCP clients and vanilla interaction/container packets, controlled fixture and supplied reagents. */
public final class RitualMp269 {
 static final Gson G=new GsonBuilder().setPrettyPrinting().create();
 static final boolean RESTART=Boolean.getBoolean("slavicmyths.qa269.ritualRestart");
 static final Path OUT=Path.of("../../docs/implementation/stabilization269-mp-ritual-20261026/"+(RESTART?"restart":"initial"));
 static final String WHO=Client269.REMOTE?"guest":"host";
 static final BlockPos FIRE=new BlockPos(1000,201,4),FC=new BlockPos(1002,201,4),P0=new BlockPos(1000,201,8),P1=new BlockPos(1001,201,8),SC=new BlockPos(1002,201,8);
 static final UUID FI=UUID.nameUUIDFromBytes("qa269-real-network-fire".getBytes()),SI=UUID.nameUUIDFromBytes("qa269-real-network-spring".getBytes());
 static final List<Map<String,Object>> checks=new ArrayList<>();static final Map<String,Object> out=new LinkedHashMap<>();
 static final AtomicBoolean pending=new AtomicBoolean();static final Set<String> shots=new HashSet<>();
 static boolean done,setup,connected,ending;static long born=System.nanoTime(),target,endAt;static int phase=-1,seen=-1;static Map<String,Integer> expected;static JsonObject instruction;
 record Step(String action,BlockPos at,String validation){}
 static final List<Step> INITIAL=List.of(new Step("use",FC,"sealedFire"),new Step("use",FIRE,"wrongFire"),new Step("use",FIRE,"fire"),new Step("use",FC,"openFire"),new Step("take",FC,"lootFire"),new Step("use",SC,"sealedSpring"),new Step("use",P0,"pool0"),new Step("use",SC,"halfSpring"),new Step("use",P1,"pool1"),new Step("use",SC,"openSpring"),new Step("take",SC,"lootSpring"));
 static final List<Step> RESUMED=List.of(new Step("use",FC,"openEmptyFire"),new Step("take",FC,"emptyFire"),new Step("use",SC,"openEmptySpring"),new Step("take",SC,"emptySpring"),new Step("use",FIRE,"repeatFire"),new Step("use",P0,"repeatPool0"),new Step("use",P1,"repeatPool1"));
 static List<Step> steps(){return RESTART?RESUMED:INITIAL;}
 static void check(boolean ok,String label){checks.add(Map.of("test",label,"status",ok?"PASS":"FAIL"));System.out.println("QA269_RITUAL_MP "+WHO+" "+(ok?"PASS ":"FAIL ")+label);if(!ok)throw new IllegalStateException(label);}
 static void finish(Throwable error){if(done)return;done=true;try{Files.createDirectories(OUT);out.put("checks",checks);out.put("status",error==null?"PASS_NETWORK_RITUAL_SCOPE":"FAIL");out.put("error",error==null?null:error.toString());out.put("scope","two real clients over loopback TCP; actual useItemOn/container packets; supplied fixture/reagents, not natural Survival acquisition; server receipt timing not instrumented");Files.writeString(OUT.resolve(WHO+"-receipt.json"),G.toJson(out));if(error!=null)error.printStackTrace();}catch(Exception e){e.printStackTrace();}Minecraft.getInstance().stop();}
 static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.parse("slavicmyths:"+id));}
 static SanctuaryChest cache(ServerLevel w,BlockPos p){return (SanctuaryChest)w.getBlockEntity(p);}
 static List<ServerPlayer> players(net.minecraft.server.MinecraftServer s){return s.getPlayerList().getPlayers();}
 static int count(net.minecraft.server.MinecraftServer s,Item item){return players(s).stream().mapToInt(p->p.getInventory().countItem(item)).sum();}
 static Map<String,Integer> stock(net.minecraft.server.MinecraftServer s,SanctuaryChest cache){var result=new TreeMap<String,Integer>();var w=s.overworld();java.util.function.Consumer<ItemStack> add=stack->{if(!stack.isEmpty())result.merge(stack.copyWithCount(1).saveOptional(w.registryAccess()).toString(),stack.getCount(),Integer::sum);};for(int i=0;i<cache.getContainerSize();i++)add.accept(cache.getItem(i));for(var p:players(s)){for(int i=0;i<p.getInventory().getContainerSize();i++)add.accept(p.getInventory().getItem(i));add.accept(p.containerMenu.getCarried());}return result;}
 static void give(net.minecraft.server.MinecraftServer server,Item item,int n){for(var p:players(server)){p.closeContainer();p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(item,n));p.inventoryMenu.broadcastChanges();}}
 static void prepare(Minecraft mc)throws Exception{var s=mc.getSingleplayerServer();var w=s.overworld();s.setDifficulty(Difficulty.PEACEFUL,true);
  if(!RESTART){for(int x=997;x<=1005;x++)for(int z=1;z<=11;z++)w.setBlock(new BlockPos(x,200,z),Blocks.STONE.defaultBlockState(),2);
   w.setBlock(FIRE,SanctuaryContent.block("unfading_brazier").defaultBlockState(),3);((SanctuaryHeart)w.getBlockEntity(FIRE)).configure(FI,FIRE,Rotation.NONE,false,-1);
   for(var p:List.of(P0,P1)){w.setBlock(p,SanctuaryContent.block("tainted_spring_node").defaultBlockState(),3);((SanctuaryHeart)w.getBlockEntity(p)).configure(SI,P0,Rotation.NONE,true,p.equals(P0)?0:1);}
   for(var p:List.of(FC,SC)){w.setBlock(p,SanctuaryContent.CACHE.get().defaultBlockState(),3);var c=cache(w,p);c.configure(p.equals(SC)?SI:FI,p.equals(SC));c.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,SanctuaryContent.id("rewards276/overworld/"+(p.equals(SC)?"spring_main":"fire_main"))),269);}
  }
  for(var p:players(s)){p.stopRiding();p.setGameMode(GameType.SURVIVAL);p.setNoGravity(true);p.getInventory().clearContent();p.teleportTo(w,1001.5,201,6.5,0,0);}
  s.setUsesAuthentication(false);s.getConnection().startTcpServerListener(java.net.InetAddress.getLoopbackAddress(),25770);setup=true;Files.createDirectories(OUT);Files.writeString(OUT.resolve("listening.txt"),"[::1]:25770");System.out.println("QA269_RITUAL_MP_LISTENING");
 }
 static void next(Minecraft mc)throws Exception{var s=mc.getSingleplayerServer();phase++;if(phase>=steps().size()){ending=true;endAt=s.overworld().getGameTime()+60;for(var p:players(s))if(p.getGameProfile().getName().equals("QA269Guest"))p.connection.disconnect(Component.literal("QA ritual tests complete"));return;}
  var step=steps().get(phase);String v=step.validation();
  if(v.equals("sealedFire")||v.equals("wrongFire")||v.equals("sealedSpring"))give(s,Items.DIRT,1);
  if(v.equals("fire")||v.equals("repeatFire"))give(s,item("sacred_ember"),1);
  if(v.equals("pool0")||v.equals("repeatPool0"))give(s,item("cleansing_powder"),2);
  if(v.startsWith("open"))give(s,Items.AIR,0);
  target=s.overworld().getGameTime()+40;var cmd=new JsonObject();cmd.addProperty("phase",phase);cmd.addProperty("action",step.action());cmd.addProperty("at",step.at().asLong());cmd.addProperty("game_time",target);Files.writeString(OUT.resolve("command.json"),G.toJson(cmd));
 }
 static void validate(Minecraft mc)throws Exception{var s=mc.getSingleplayerServer();var w=s.overworld();String v=steps().get(phase).validation();var f=cache(w,FC);var c=cache(w,SC);out.put("validated_phase_"+phase,v);
  switch(v){
   case "sealedFire","wrongFire" -> {check(!f.canOpen(players(s).getFirst())&&f.getLootTable()!=null,"fire remains sealed after "+v);check(players(s).stream().noneMatch(p->p.containerMenu instanceof ChestMenu),"neither network player opens sealed fire menu "+v);}
   case "fire" -> {check(SanctuaryRecords.get(w).restored(FI),"real duplicate client interactions restore fire");check(count(s,item("sacred_ember"))==1,"two concurrent fire requests consume exactly one of two embers");}
   case "sealedSpring","halfSpring" -> {check(!c.canOpen(players(s).getFirst())&&c.getLootTable()!=null,"spring sealed after "+v);check(players(s).stream().noneMatch(p->p.containerMenu instanceof ChestMenu),"both clients denied spring menu "+v);}
   case "pool0" -> {check(!SanctuaryRecords.get(w).cleansed(SI),"one pool does not complete network ritual");check(count(s,item("cleansing_powder"))==3,"duplicate pool0 requests consume exactly one powder");}
   case "pool1" -> {check(SanctuaryRecords.get(w).cleansed(SI),"second pool completes network ritual");check(count(s,item("cleansing_powder"))==2,"two pools consume two of four supplied powders");}
   case "openFire","openSpring","openEmptyFire","openEmptySpring" -> {var active=v.contains("Spring")?c:f;check(players(s).size()==2&&players(s).stream().allMatch(p->p.containerMenu instanceof ChestMenu),"two real network players open unlocked menu "+v);check(active.getLootTable()==null,"pending table resolves exactly once "+v);expected=stock(s,active);out.put(v+"_expected_stock",expected);check(RESTART?expected.isEmpty():!expected.isEmpty(),"expected actual reward state "+v);}
   case "lootFire","lootSpring","emptyFire","emptySpring" -> {var active=v.endsWith("Spring")?c:f;check(stock(s,active).equals(expected),"total component-aware stock preserved after two clients quick-move "+v);check(active.isEmpty()&&active.getLootTable()==null,"cache empty and no reroll "+v);}
   case "repeatFire" -> {check(count(s,item("sacred_ember"))==2,"already restored fire consumes no extra embers after restart");check(f.isEmpty()&&f.getLootTable()==null,"repeat fire interaction does not refill cache");}
   case "repeatPool0","repeatPool1" -> {check(count(s,item("cleansing_powder"))==4,"already clean pools consume no extra powder "+v);check(c.isEmpty()&&c.getLootTable()==null,"repeat pool interaction does not refill cache "+v);}
  }
 }
 static void serverTick(Minecraft mc){try{var s=mc.getSingleplayerServer();if(!setup){prepare(mc);return;}if(ending){if(s.overworld().getGameTime()>=endAt&&Files.exists(OUT.resolve("guest-receipt.json"))){s.saveEverything(false,true,true);mc.execute(()->finish(null));}return;}if(players(s).size()!=2)return;
  if(phase<0){check(players(s).stream().map(p->p.getGameProfile().getName()).collect(java.util.stream.Collectors.toSet()).equals(Set.of("Dev","QA269Guest")),"two distinct actual client profiles connected");for(var p:players(s)){p.setNoGravity(true);p.setGameMode(GameType.SURVIVAL);p.teleportTo(s.overworld(),1001.5,201,6.5,0,0);}next(mc);return;}
  if(s.overworld().getGameTime()<target+30)return;
  for(String who:List.of("host","guest")){var file=OUT.resolve(who+"-ack.json");if(!Files.exists(file)||JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("phase").getAsInt()!=phase)return;}
  validate(mc);next(mc);
 }catch(Throwable e){mc.execute(()->finish(e));}finally{pending.set(false);}}
 public static void tick(Minecraft mc){if(done)return;try{if(System.nanoTime()-born>240_000_000_000L)throw new IllegalStateException("network ritual timeout phase="+phase+" seen="+seen);
  if(mc.player==null||mc.level==null){if(connected&&Client269.REMOTE)finish(null);return;}if(!connected){connected=true;out.put("master_volume",mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).get());out.put("GLFW_visible",org.lwjgl.glfw.GLFW.glfwGetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_VISIBLE));check(Client269.REMOTE?mc.getSingleplayerServer()==null:mc.getSingleplayerServer()!=null,"correct actual client connection role");}
  Client269.ready=true;if(!Client269.REMOTE&&pending.compareAndSet(false,true))mc.getSingleplayerServer().execute(()->serverTick(mc));
  var file=OUT.resolve("command.json");if(!Files.exists(file))return;JsonObject cmd;try{cmd=JsonParser.parseString(Files.readString(file)).getAsJsonObject();}catch(Exception incomplete){return;}int n=cmd.get("phase").getAsInt();if(n<=seen||mc.level.getGameTime()<cmd.get("game_time").getAsLong())return;
  mc.player.getInventory().selected=0;String action=cmd.get("action").getAsString();var at=BlockPos.of(cmd.get("at").getAsLong());
  if(action.equals("use"))mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false));
  else{check(mc.player.containerMenu instanceof ChestMenu,"real client has synchronized ChestMenu for quick-move phase"+n);for(int i=0;i<27;i++)mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,i,0,ClickType.QUICK_MOVE,mc.player);}
  seen=n;instruction=cmd;out.put("sent_phase_"+n+"_game_time",mc.level.getGameTime());var ack=new JsonObject();ack.addProperty("phase",n);ack.addProperty("game_time",mc.level.getGameTime());Files.writeString(OUT.resolve(WHO+"-ack.json"),G.toJson(ack));System.out.println("QA269_RITUAL_MP_SENT "+WHO+" phase="+n);
 }catch(Throwable e){finish(e);}}
 public static void capture(Minecraft mc){if(done||instruction==null||mc.level==null||!shots.add("phase"+seen))return;try{Files.createDirectories(OUT);try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(WHO+"-phase"+seen+".png"));}}catch(Exception e){finish(e);}}
}
