package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Unattended captures in a fresh isolated world; never reads the user's saves. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class SteppeVisualProbe {
 private static final String[] IDS=System.getProperty("steppe.visual.ids", "polkan_spear,polkan_bow,polkan_axe,skarapeya_blade,skarapeya_gusli,skarapeya_whip,skarapeya_needle,steppe_sword,steppe_axe,steppe_mace,steppe_halberd,steppe_scythe,steppe_spear,steppe_bow,steppe_crossbow").split(",");
 private static boolean opened,ready,requested;private static int ticks=-40,scene=-1;private static final Set<String> frames=new HashSet<>();
 private static final Path OUT=Path.of("docs/technical-audit/steppe-visual-captures");
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();try{
  mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.1D);
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){opened=true;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.createWorldOpenFlows().createFreshLevel("SteppeVisualQA-"+System.currentTimeMillis(),new LevelSettings("Steppe visual QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(27112026L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();p.teleportTo(p.serverLevel(),0,-60,0,180,0);mc.execute(()->ready=true);});}
  if(!ready||mc.getOverlay()!=null)return;ticks++;if(ticks<0)return;
  int next=ticks/45;if(next!=scene){scene=next;if(scene>=IDS.length*4){Files.createDirectories(OUT);Files.writeString(OUT.resolve("complete.txt"),"Captured "+frames.size()+" client frames in fresh isolated world; master volume 10%.\n");System.out.println("STEPPE_VISUAL_COMPLETE frames="+frames.size());mc.stop();return;}
   int index=scene/4,view=scene%4;String id=IDS[index];mc.setScreen(null);mc.setCameraEntity(mc.player);mc.options.setCameraType(view==0?CameraType.FIRST_PERSON:view==1?CameraType.THIRD_PERSON_FRONT:CameraType.THIRD_PERSON_BACK);mc.player.setMainArm(net.minecraft.world.entity.HumanoidArm.RIGHT); if(view==2){var camera=new net.minecraft.world.entity.decoration.ArmorStand(mc.level,mc.player.getX()+4,mc.player.getY(),mc.player.getZ());camera.setYRot(90);camera.yRotO=90;camera.xo=camera.getX();camera.yo=camera.getY();camera.zo=camera.getZ();mc.setCameraEntity(camera);mc.options.setCameraType(CameraType.FIRST_PERSON);}
   mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.getInventory().setItem(0,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id))));p.getInventory().selected=0;p.inventoryMenu.broadcastChanges();});System.out.println("STEPPE_VISUAL_SCENE "+id+" view="+view);
  }
 if(scene%4==3&&ticks%45==15)mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
 }catch(Throwable t){t.printStackTrace();System.out.println("STEPPE_VISUAL_FAILURE "+t);mc.stop();}}
 @SubscribeEvent public static void frame(RenderFrameEvent.Post e){var mc=Minecraft.getInstance();if(!ready||scene<0||scene>=IDS.length*4||ticks%45!=30)return;String name=IDS[scene/4]+"-"+scene%4;if(frames.add(name))Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),c->{});}
}



