package org.slavicmyths.hunt;

import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.slavicmyths.registry.ModEntities;

/** Opt-in in-engine render fixture. Never compiled into the production source set. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class BossSceneProbe {
 private static boolean opened,started,finished,diveShot,riseShot;
 private static int time,stage=-1,shot=-1;
 private static Entity actor,prey;
 private static final String[] names={"tugarin-earth","tugarin-serpent","nightingale-forest","nightingale-enraged","werewolf-four-legs","werewolf-upright","tugarin-spin","nightingale-airborne","werewolf-active-ai"};
 private static final Path OUT=Path.of("../docs/verification/combat-feedback-2026-10-09/client-frames");
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.createWorldOpenFlows().openWorld("BossReworkQA",()->{});}
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null||finished)return;
  if(!started){started=true;mc.getSingleplayerServer().execute(()->{
   var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayers().getFirst();var w=p.serverLevel();
   w.setDayTime(6000);w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
   for(var old:w.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(-16,75,-16,16,95,16),e->!(e instanceof net.minecraft.world.entity.player.Player)))old.discard();
   w.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false,server);w.setWeatherParameters(60000,0,false,false);
   p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(w,0,84,-13,0,6);
   // Bounded test-only platform in a copied disposable world.
   for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)w.setBlock(new BlockPos(x,80,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
  });}
  time++;
  int next=(time-100)/80;
  if(time>=740&&time<820)next=7;else if(time>=820)next=8;
  if(time>=100&&next<names.length&&next!=stage){stage=next;int selected=next;mc.getSingleplayerServer().execute(()->scene(mc,selected));}
  if(time>100+(names.length+1)*80){finished=true;System.out.println("BOSS_SCENE_CAPTURE_COMPLETE="+names.length+"; real Minecraft render; scripted scene, not manual survival acceptance");mc.stop();}
  mc.options.hideGui=stage!=1;mc.player.setYRot(0);mc.player.setXRot(6);
 }
 private static void scene(Minecraft mc,int selected){
  var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();ServerLevel w=p.serverLevel();
  if(actor!=null)actor.discard();
  if(prey!=null){prey.discard();prey=null;}
  if(selected==0||selected==1||selected==6){var b=ModEntities.TUGARIN_ZMEY.get().create(w);b.setNoAi(true);if(selected==1){b.setHealth(150);b.tugarinState(WorldBoss.TugarinState.PHASE_TWO);}if(selected==6)b.tugarinMove(BossRules.Move.SPIN);actor=b;}
  else if(selected==2||selected==3||selected==7){var b=ModEntities.NIGHTINGALE.get().create(w);b.setNoAi(true);if(selected==3||selected==7){b.setHealth(120);var n=new net.minecraft.nbt.CompoundTag();b.addAdditionalSaveData(n);n.putString("NightingalePhase","PHASE_TWO");b.readAdditionalSaveData(n);}actor=b;}
  else {var b=ModEntities.CURSED_VOLKOLAK.get().create(w);b.setNoAi(true);if(selected==5)try{var method=b.getClass().getDeclaredMethod("stance",boolean.class);method.setAccessible(true);method.invoke(b,true);}catch(Exception ex){throw new RuntimeException(ex);}actor=b;}
  float facing=215;
  actor.moveTo(0,81,0,facing,0);if(actor instanceof LivingEntity living){living.yBodyRot=facing;living.yHeadRot=facing;}w.addFreshEntity(actor);
  if(selected>=7){var target=EntityType.COW.create(w);target.setNoAi(true);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(400);target.setHealth(400);target.moveTo(0,81,-3);w.addFreshEntity(target);prey=target;((Mob)actor).setNoAi(false);((Mob)actor).setTarget(target);}
  boolean large=selected==0||selected==1||selected==6;
  p.teleportTo(w,0,large?83:81.8,large?-8:-5,0,6);
  if(selected==7)p.teleportTo(w,0,85,-8,0,6);
  // The player-height silhouette is a native armor stand beside each boss.
  if(selected==0){var stand=EntityType.ARMOR_STAND.create(w);stand.moveTo(4,81,0,180,0);w.addFreshEntity(stand);}
  System.out.println("BOSS_SCENE="+names[selected]);
 }
 @SubscribeEvent public static void render(RenderFrameEvent.Post event){
  if(stage==7&&time>=754&&!diveShot){save("nightingale-air-dive");diveShot=true;}
  if(stage==7&&time>=800&&!riseShot){save("nightingale-air-rising");riseShot=true;}
  if(stage<0||stage>=names.length||time<100+stage*80+(stage>=8?80:0)+35||shot==stage)return;
  var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  save(names[stage]);shot=stage;
 }
 private static void save(String name){
  var mc=Minecraft.getInstance();
  try{Files.createDirectories(OUT);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name+".png"));}System.out.println("BOSS_FRAME_SAVED="+name);}
  catch(Exception ex){throw new RuntimeException("Scene capture failed",ex);}
 }
 private BossSceneProbe(){}
}
