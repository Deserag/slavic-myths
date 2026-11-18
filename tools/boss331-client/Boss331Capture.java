package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.navya.*;
import org.slavicmyths.blackcinder.*;
import org.slavicmyths.burningislands.*;
import org.slavicmyths.trophy.*;

/** Real client framebuffer captures of production renderers, in a disposable world only. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class Boss331Capture {
 static final Path OUT=Path.of("D:/slavic-myths/docs/implementation/boss331-rework/client");
 static boolean opened,setup,done,finishing;static volatile boolean finishReady;static volatile boolean ready;static int age,scene=-1,shot=-1;
 record View(String name,double x,double y,double z,double tx,double ty,double tz){}
 static final List<View> VIEWS=new ArrayList<>();
 static {for(int i=0;i<3;i++){String n=new String[]{"guardian","zharenitsa","magma_lord"}[i];double x=i*40,ty=i==0?84:83.7;
  VIEWS.add(new View(n+"-front",x,83.6,9,x,ty,0));VIEWS.add(new View(n+"-three-quarter",x+6,83.6,7,x,ty,0));
  VIEWS.add(new View(n+"-side",x+9,83.6,0,x,ty,0));VIEWS.add(new View(n+"-back",x,83.6,-9,x,ty,0));}
  VIEWS.add(new View("four-trophy-heads",.5,81.2,108,.5,81.6,100));
  VIEWS.add(new View("guardian-head-three-quarter",-2.5,80.8,102,-4,81.5,100));
  VIEWS.add(new View("guardian-head-back",-4,80.8,97.5,-4,81.5,100));
  VIEWS.add(new View("posten-head-awake",3.5,80.8,102.3,2,81.5,100));
  VIEWS.add(new View("zharenitsa-head",.5,80.8,102.3,-1,81.5,100));
  VIEWS.add(new View("magma-lord-head",6.5,80.8,102.3,5,81.5,100));
  VIEWS.add(new View("magma-lord-phase2",86,83.6,7,80,83.7,0));
  VIEWS.add(new View("heads-inventory",.5,81.2,108,.5,81.6,100));
  if(Boolean.getBoolean("boss331.inventoryOnly")){var inventory=VIEWS.getLast();VIEWS.clear();VIEWS.add(inventory);}
 }
 static void pose(ServerPlayer p,View v){double dx=v.tx-v.x,dz=v.tz-v.z,dy=v.ty-(v.y+p.getEyeHeight());p.teleportTo(p.serverLevel(),v.x,v.y,v.z,(float)Math.toDegrees(Math.atan2(-dx,dz)),(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz))));}
 static void spawn(ServerLevel w,Mob e,double x){e.setPos(x,81,0);e.setNoAi(true);e.setPersistenceRequired();e.setInvulnerable(true);e.setYRot(0);e.setYBodyRot(0);e.setYHeadRot(0);w.addFreshEntity(e);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
  if(done)return;var mc=Minecraft.getInstance();
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;Files.createDirectories(OUT);mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(5);mc.options.simulationDistance().set(5);mc.options.enableVsync().set(false);mc.options.framerateLimit().set(60);mc.options.guiScale().set(2);mc.options.fov().set(55);mc.options.hideGui=true;mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
   mc.createWorldOpenFlows().createFreshLevel("Boss331-"+System.currentTimeMillis(),new LevelSettings("Boss331 disposable visual QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(3312026L,true,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;}
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!setup){setup=true;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();var w=p.serverLevel();
   w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,w.getServer());w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,w.getServer());w.setDayTime(6000);
   for(int center:new int[]{0,40,80})for(int x=center-18;x<=center+18;x++)for(int z=-18;z<=18;z++)w.setBlock(new BlockPos(x,80,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
   for(int x=-10;x<=11;x++)for(int z=94;z<=116;z++)w.setBlock(new BlockPos(x,80,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
   spawn(w,NavyaContent.MOBS.get(NavyaContent.Kind.GUARDIAN).get().create(w),0);
   spawn(w,CinderContent.TYPES.get(CinderContent.Kind.ZHARENITSA).get().create(w),40);
   spawn(w,IslandsContent.TYPES.get(IslandsContent.Kind.MAGMA_LORD).get().create(w),80);
   var kinds=new TrophyHeads.Kind[]{TrophyHeads.Kind.GROVE_GUARDIAN,TrophyHeads.Kind.ZHARENITSA,TrophyHeads.Kind.POSTEN,TrophyHeads.Kind.MAGMA_LORD};
   p.getInventory().selected=8;
   for(int i=0;i<4;i++){var pos=new BlockPos(-4+3*i,81,100);p.setYRot(180);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(TrophyHeads.ITEMS.get(kinds[i]).get()));
    var context=new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos.below()).add(0,.5,0),Direction.UP,pos.below(),false));
    var result=p.getMainHandItem().useOn(context);if(!result.consumesAction())throw new AssertionError("Head BlockItem placement failed: "+kinds[i]);
    if(!(w.getBlockEntity(pos) instanceof TrophyHeadBlock.Tile))throw new AssertionError("Missing live head tile: "+kinds[i]);p.getInventory().setItem(i,new ItemStack(TrophyHeads.ITEMS.get(kinds[i]).get()));}
   p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);
   // Deliberately remove a legacy-compatible tile, then invoke the production repair event.
   BlockPos legacy=new BlockPos(-1,81,100);w.removeBlockEntity(legacy);
   TrophyHeadMigration.loaded(new net.neoforged.neoforge.event.level.ChunkEvent.Load(w.getChunkAt(legacy),false));
   p.setGameMode(GameType.SPECTATOR);pose(p,VIEWS.getFirst());ready=true;
  });return;}
  if(!ready)return;
  if(age==20)mc.getSingleplayerServer().execute(()->{var w=mc.getSingleplayerServer().overworld();if(!(w.getChunkAt(new BlockPos(-1,81,100)).getBlockEntities().get(new BlockPos(-1,81,100)) instanceof TrophyHeadBlock.Tile))throw new AssertionError("Legacy Zharenitsa tile not repaired");System.out.println("BOSS331_LIVE_HEADS BlockItem placement x4 + missing legacy tile repair PASS");});
  int next=age++/100;
  if(next>=VIEWS.size()){
   if(!finishing){finishing=true;mc.getSingleplayerServer().execute(()->{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();var w=p.serverLevel();var kinds=new TrophyHeads.Kind[]{TrophyHeads.Kind.GROVE_GUARDIAN,TrophyHeads.Kind.ZHARENITSA,TrophyHeads.Kind.POSTEN,TrophyHeads.Kind.MAGMA_LORD};var counts=new LinkedHashMap<String,Integer>();
    for(int i=0;i<4;i++){BlockPos pos=new BlockPos(-4+3*i,81,100);var item=TrophyHeads.ITEMS.get(kinds[i]).get();w.destroyBlock(pos,true,p);int count=w.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2),e->e.getItem().is(item)).stream().mapToInt(e->e.getItem().getCount()).sum();if(count!=1)throw new AssertionError("Head break/drop: "+kinds[i]+" count="+count);counts.put(kinds[i].id,count);}
    try{Files.writeString(OUT.resolve("live-head-regression.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(counts));}catch(Exception e){throw new RuntimeException(e);}System.out.println("BOSS331_HEAD_BREAK_DROP actual world: four heads x1 PASS");finishReady=true;
   });return;}
   if(!finishReady)return;done=true;Files.writeString(OUT.resolve("capture-finished.txt"),"Real Minecraft client screenshots; disposable creative visual fixture; not survival/battle/MP acceptance.\n");mc.stop();return;
  }
  if(next!=scene){scene=next;mc.setScreen(null);var v=VIEWS.get(scene);mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();pose(p,v);if(v.name.equals("magma-lord-phase2"))for(var mob:p.serverLevel().getEntitiesOfClass(IslandsMob.class,new net.minecraft.world.phys.AABB(70,75,-10,90,95,10)))if(mob.kind==IslandsContent.Kind.MAGMA_LORD)mob.setHealth(400);});}
  if(VIEWS.get(scene).name.equals("heads-inventory")&&mc.screen==null){mc.options.hideGui=false;mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));}
  if(age%100==85&&shot!=scene){shot=scene;var v=VIEWS.get(scene);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(String.format("%02d-%s.png",scene,v.name)));}System.out.println("BOSS331_SCREENSHOT "+v.name);}
 }
}
