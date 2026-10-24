package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.industry.ChudTools;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class ChudCapture {
 static boolean opened,setup,ready,done;static int age,scene=-1,shot=-1;static volatile boolean failed;static final Path OUT=Path.of("D:/slavic-myths/docs/implementation/chud258/client");
 static Item[] items(){return new Item[]{ChudTools.LANTERN.get(),ChudTools.PICKAXE.get(),ChudTools.AXE.get(),ChudTools.MULTI_PICK.get(),ChudTools.SHOVEL.get(),ChudTools.LIFT.get()};}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
  if(done)return;var mc=Minecraft.getInstance();if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;Files.createDirectories(OUT);mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);mc.options.enableVsync().set(false);mc.options.framerateLimit().set(120);mc.options.guiScale().set(2);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
   mc.createWorldOpenFlows().createFreshLevel("Chud258-QA-"+System.currentTimeMillis(),new LevelSettings("Chud258 disposable",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(2582026L,true,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;
  }
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!setup){setup=true;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();var w=p.serverLevel();
   for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++){w.setBlock(new BlockPos(x,8,z),Blocks.STONE.defaultBlockState(),2);w.setBlock(new BlockPos(x,16,z),Blocks.STONE.defaultBlockState(),2);for(int y=9;y<16;y++)w.setBlock(new BlockPos(x,y,z),Math.abs(x)==10||Math.abs(z)==10?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);}
   p.teleportTo(w,.5,9,.5,0,15);p.getAbilities().flying=true;p.onUpdateAbilities();for(int i=0;i<6;i++)p.getInventory().setItem(i,new ItemStack(items()[i]));p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();ready=true;
  });return;}if(!ready)return;
  int next=age++/75;if(next>=22){done=true;mc.stop();return;}
  if(next!=scene){scene=next;mc.setScreen(null);mc.options.setCameraType(scene%3==1?CameraType.THIRD_PERSON_FRONT:CameraType.FIRST_PERSON);int item=Math.min(5,scene/3);boolean offhand=scene==19;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();if(scene==1)p.serverLevel().setBlock(new BlockPos(0,10,3),Blocks.GLOWSTONE.defaultBlockState(),3);if(scene==18)p.serverLevel().setBlock(new BlockPos(0,10,3),Blocks.AIR.defaultBlockState(),3);p.setItemInHand(InteractionHand.MAIN_HAND,scene==18||scene>=20||offhand?ItemStack.EMPTY:new ItemStack(items()[item]));p.setItemInHand(InteractionHand.OFF_HAND,offhand?new ItemStack(ChudTools.LANTERN.get()):ItemStack.EMPTY);p.inventoryMenu.broadcastChanges();});if(scene<18&&scene%3==2)mc.setScreen(new InventoryScreen(mc.player));System.out.println("CHUD_CLIENT_SCENE "+scene);}
  if(age%75==60&&shot!=scene){shot=scene;var source=BlockPos.containing(mc.player.getX(),mc.player.getY()+1,mc.player.getZ());int light=mc.level.getBrightness(LightLayer.BLOCK,source);if(scene==0||scene>=18){int current=scene;mc.getSingleplayerServer().execute(()->{var w=mc.getSingleplayerServer().overworld();int server=w.getBrightness(LightLayer.BLOCK,source);String row="scene="+current+" client_block="+light+" server_block="+server;System.out.println("CHUD_LIGHT "+row);try{Files.writeString(OUT.resolve("light.txt"),row+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception e){throw new RuntimeException(e);}if(server!=0||((current==0||current==19)?light<13:light!=0)){failed=true;System.out.println("CHUD_LIGHT_FAIL "+row);}});}
   try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(String.format("%02d-%s.png",scene,scene<18?items()[scene/3].toString().replace(':','_')+"-"+new String[]{"firstperson","thirdperson","inventory"}[scene%3]:scene==19?"offhand-light":"lamp-removed")));}
  }
 }
}
