package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Actual native client/server charge, fixed GUI and first/third-person captures. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class Issue217CrossbowProbe {
 static boolean opened,ready;static int tick,selected=-1;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
  if(!Boolean.getBoolean("slavicmyths.issue217.crossbows"))return;var mc=Minecraft.getInstance();
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.2D);mc.createWorldOpenFlows().openWorld("VisualQA",()->{});}
  if(mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!ready){ready=true;Issue217Probe.server(p->{p.setGameMode(GameType.SURVIVAL);p.serverLevel().setDayTime(6000);p.teleportTo(p.serverLevel(),0,100,0,180,0);});}
  int scene=tick/125,phase=tick%125;tick++;if(scene>=8){mc.options.keyUse.setDown(false);System.out.println("ISSUE217_CROSSBOW_CAPTURE_DONE eight real charge/release cycles");mc.stop();return;}
  String id=scene/4==0?"serpent_crossbow":"three_breath_crossbow";var hand=scene%2==0?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
  if(scene!=selected){selected=scene;mc.setScreen(null);mc.options.setCameraType(scene%4<2?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_FRONT);Issue217Probe.server(p->{p.getInventory().clearContent();p.getInventory().selected=0;p.setItemInHand(hand,Issue217Probe.item(id));p.getInventory().setItem(9,new ItemStack(Items.ARROW,64));p.inventoryMenu.broadcastChanges();p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));});}
  if(phase==20){mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,hand);}
  if(phase==90){mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);}
  if(List.of(15,25,35,45,60,80,100).contains(phase)){
   String state=id+"-view"+(scene%4)+"-tick"+phase;Issue217Probe.capture(mc,state);
   System.out.println("ISSUE217_CROSSBOW_STATE="+state+" using="+mc.player.isUsingItem()+" ticks="+mc.player.getTicksUsingItem()+" charged="+CrossbowItem.isCharged(mc.player.getItemInHand(hand)));
  }
 }
}
