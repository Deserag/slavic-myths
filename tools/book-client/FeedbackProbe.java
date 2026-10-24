package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.yaga.*;
import org.slavicmyths.tales.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class FeedbackProbe {
 static boolean enabled,opened,working,done,reload,painted;static volatile boolean ready;static int phase,age,attempt,giver;static long started;static UUID yaga,cat;static Vec3 home;static final List<String> rows=new ArrayList<>();static final Path OUT=Path.of("D:/slavic-myths/docs/implementation/feedback19765/client");
 public static void begin(){enabled=true;}
 static void record(String line){rows.add(line);System.out.println("FEEDBACK65 "+line);}
 static void save()throws Exception{Files.writeString(OUT.resolve("research-results.txt"),String.join("\n",rows)+"\n");}
 @SuppressWarnings("unchecked") static boolean pending(ServerPlayer p)throws Exception{var f=org.slavicmyths.chud.ChudResearch.class.getDeclaredField("PENDING");f.setAccessible(true);return ((Set<UUID>)f.get(null)).contains(p.getUUID());}
 static String giver(){return giver==0?"yaga":"bayun";}
 static CompoundTag quest(ServerPlayer p){return TaleData.get(p.serverLevel()).player(p.getUUID()).getCompound("ChudResearch").getCompound(giver());}
 static void ask(ServerPlayer p){
  var w=p.serverLevel();if(giver==0){var npc=w.getEntity(yaga);if(npc==null)return;var menu=new YagaMenu(1,p.getInventory(),npc.getId());p.containerMenu=menu;record("Yaga menu703 accepted="+menu.clickMenuButton(p,703));p.closeContainer();}
  else {var npc=(org.slavicmyths.bayun.BayunEntity)w.getEntity(cat);if(npc==null)return;p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);npc.interact(p,InteractionHand.MAIN_HAND);org.slavicmyths.bayun.BayunNetwork.open(p,npc);var n=p.getPersistentData();if(!n.hasUUID("BayunMenu"))throw new AssertionError("Bayun SAFE session unavailable");org.slavicmyths.bayun.BayunNetwork.choose(p,new org.slavicmyths.bayun.BayunNetwork.Choice(npc.getId(),n.getUUID("BayunMenu"),1,6));record("Bayun authenticated option6 submitted");}
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
  if(!enabled||done||working)return;var mc=Minecraft.getInstance();if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;Files.createDirectories(OUT);mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.framerateLimit().set(120);mc.resizeDisplay();String name=Files.readString(Path.of("D:/slavic-myths/docs/implementation/guides236/normal-world/world.txt")).trim();mc.createWorldOpenFlows().openWorld(name,()->{});return;}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(phase>=50){visual(mc);return;}if(++age%20!=0)return;working=true;
  mc.getSingleplayerServer().execute(()->{try{
   var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayers().getFirst();var w=p.serverLevel();
   if(phase==0){p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();var pos=p.blockPosition();p.teleportTo(w,pos.getX()+.5,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos.getX(),pos.getZ()),pos.getZ()+.5,0,20);home=p.position();
    var npc=org.slavicmyths.registry.ModEntities.BABA_YAGA.get().create(w);npc.moveTo(home.add(1,0,0));npc.finalizeSpawn(w,w.getCurrentDifficultyAt(npc.blockPosition()),net.minecraft.world.entity.MobSpawnType.SPAWN_EGG,null);npc.setNoAi(true);npc.setNoGravity(true);w.addFreshEntity(npc);yaga=npc.getUUID();var progress=YagaData.get(w).progress(p.getUUID());progress.stage=Math.max(1,progress.stage);progress.intro=2;
    record("NORMAL seed="+w.getSeed()+" actual NPC research; world="+server.getWorldData().getLevelName());ask(p);started=System.nanoTime();phase=1;
   }else if(phase==1){
    var q=quest(p);if(q.contains("Target")&&!q.getBoolean("Claimed")){var target=BlockPos.of(q.getLong("Target"));var owner=new ChunkPos(q.getLong("Owner"));record(giver()+" assignment_seconds="+((System.nanoTime()-started)/1e9)+" average_mspt="+(server.getAverageTickTimeNanos()/1e6)+" owner_FULL_loaded="+(w.getChunkSource().getChunkNow(owner.x,owner.z)!=null));record(giver()+" persisted/native assignment="+target+" owner="+owner+" recordedPieces="+q.getInt("StartPieces")+" bounds="+java.util.Arrays.toString(q.getIntArray("Bounds")));p.teleportTo(w,target.getX()+.5,target.getY()+2,target.getZ()+.5,0,15);started=System.nanoTime();phase=2;}
    else if(!pending(p)){if(++attempt>=4){record("NOT_RUN "+giver()+" no validated native city after4 pages");next(p);}else{ask(p);started=System.nanoTime();}}
    else if(System.nanoTime()-started>120_000_000_000L)throw new AssertionError("Bounded city worker did not finish");
   }else if(phase==2){
    var q=quest(p);if(q.getBoolean("Visited")){record(giver()+" real travel/native exploration observer marked Visited=true");mc.execute(()->shot(mc,giver()+"-city.png"));p.teleportTo(w,home.x,home.y,home.z,0,20);age=0;phase=3;}
    else if(System.nanoTime()-started>90_000_000_000L){record("FAIL "+giver()+" travel did not mark native visit");next(p);}else if(age%40==0){var target=BlockPos.of(q.getLong("Target"));p.teleportTo(w,target.getX()+.5+(age/40%2),target.getY()+2,target.getZ()+.5,0,15);}
   }else if(phase==3&&age>=80){
    if(w.getEntity(giver==0?yaga:cat)==null)return;long before=org.slavicmyths.rpg.Runes.inventoryCount(p,org.slavicmyths.registry.ModItems.ANCIENT_SIGN.get());ask(p);var q=quest(p);long after=org.slavicmyths.rpg.Runes.inventoryCount(p,org.slavicmyths.registry.ModItems.ANCIENT_SIGN.get());record(giver()+" return Claimed="+q.getBoolean("Claimed")+" ancient_sign delta="+(after-before));if(!q.getBoolean("Claimed")||after-before!=1)throw new AssertionError("Native research return reward failed");next(p);
   }else if(phase==4&&age>=40){ask(p);started=System.nanoTime();phase=1;}
  }catch(Throwable failure){failure.printStackTrace();record("FAIL "+failure);try{save();}catch(Exception ignored){}phase=50;age=0;}finally{working=false;}});
 }
 static void next(ServerPlayer p)throws Exception{
  save();if(giver++==0){var cats=p.serverLevel().getEntitiesOfClass(org.slavicmyths.bayun.BayunEntity.class,p.getBoundingBox().inflate(16));var npc=cats.isEmpty()?org.slavicmyths.bayun.BayunContent.ENTITY.get().create(p.serverLevel()):cats.getFirst();if(cats.isEmpty()){npc.home=p.blockPosition().offset(3,0,0);npc.moveTo(p.position().add(3,0,0));npc.setNoAi(true);npc.setNoGravity(true);if(!p.serverLevel().addFreshEntity(npc))throw new AssertionError("Fixture Bayun reservation rejected");}cat=npc.getUUID();record("Bayun canonical fixture="+cat+" reused="+!cats.isEmpty());p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.MILK_BUCKET));npc.interact(p,InteractionHand.MAIN_HAND);npc.bond(p).putInt("Trust",85);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);attempt=0;age=0;phase=4;}
  else{phase=50;age=0;}
 }
 static void shot(Minecraft mc,String name){try{Files.createDirectories(OUT);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name));}}catch(Exception x){throw new RuntimeException(x);}}
 static void visual(Minecraft mc){
  if(reload)return;
  if(phase==50||phase==52){boolean before=phase==50;reload=true;var repo=mc.getResourcePackRepository();repo.reload();var packs=new ArrayList<>(mc.options.resourcePacks);packs.remove("file/feedback65-before");if(before)packs.add("file/feedback65-before");repo.setSelected(packs);mc.reloadResourcePacks().whenComplete((ok,error)->mc.execute(()->{if(error!=null)throw new RuntimeException(error);mc.setScreen(new ItemsScreen(before));age=0;phase++;reload=false;}));return;}
  if(phase==51||phase==53){if(mc.getOverlay()!=null)return;if(++age==40){shot(mc,phase==51?"items-before.png":"items-after.png");phase++;age=0;}return;}
  if(phase==54){mc.setScreen(null);working=true;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(org.slavicmyths.registry.ModItems.LORE_BOOK.get()));TaleService.open(p);working=false;phase=55;age=0;});return;}
  if(phase==55&&mc.screen instanceof org.slavicmyths.client.TaleScreen screen){if(!painted){painted=true;BookCapture.set(screen,"part","quests");BookCapture.set(screen,"selected","slavicmyths:chud_research_yaga");BookCapture.set(screen,"page",1);((Set<String>)BookCapture.get(screen,"expanded")).add("quests");screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());}if(++age==60){shot(mc,"research-book.png");phase=56;age=0;}}
  if(phase>=56&&phase<=59){int scale=phase-55;if(age++==0){mc.options.guiScale().set(scale);mc.resizeDisplay();}if(age==40){shot(mc,"book-gui-scale-"+scale+".png");phase++;age=0;}return;}
  if(phase==60){done=true;mc.stop();}
 }
 static final class ItemsScreen extends Screen {
  final boolean before;ItemsScreen(boolean b){super(Component.literal("Actual native item renderer"));before=b;}
  public void render(GuiGraphics g,int x,int y,float partial){g.fill(0,0,width,height,0xff302b25);g.drawString(font,"Perunite: same vanilla iron silhouettes / Poloz serpent trophy",20,16,0xffeee1ba,false);
   String[] names={"perunite_pickaxe","perunite_axe","perunite_shovel","perunite_hoe","poloz_head"};for(int i=0;i<5;i++){int px=30+i*115;g.drawString(font,names[i],px,65,0xffeee1ba,false);g.pose().pushPose();g.pose().translate(px,90,0);g.pose().scale(3,3,1);g.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",names[i]))),0,0);g.pose().popPose();if(i<4){g.pose().pushPose();g.pose().translate(px,185,0);g.pose().scale(3,3,1);g.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("iron_"+new String[]{"pickaxe","axe","shovel","hoe"}[i]))),0,0);g.pose().popPose();}}
   g.drawString(font,"Top: mod assets. Bottom: native iron tools for shape comparison.",20,265,0xffeee1ba,false);
  }public boolean isPauseScreen(){return false;}
 }
}
