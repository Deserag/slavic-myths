package org.slavicmyths.client;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.network.MythPayloads;

/** Optional Controls bindings; all equipment and cooldown checks remain server-side. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class NetherTrophyKeys {
 private static final KeyMapping PENDANT=new KeyMapping("key.slavicmyths.morova_pendant",InputConstants.UNKNOWN.getValue(),"key.categories.slavicmyths");
 private static final KeyMapping HORN=new KeyMapping("key.slavicmyths.trizna_horn",InputConstants.UNKNOWN.getValue(),"key.categories.slavicmyths");
 @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {
  @SubscribeEvent public static void register(RegisterKeyMappingsEvent e){e.register(PENDANT);e.register(HORN);}
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;
  while(PENDANT.consumeClick())PacketDistributor.sendToServer(new MythPayloads.NetherTrophy(true));
  while(HORN.consumeClick())PacketDistributor.sendToServer(new MythPayloads.NetherTrophy(false));
 }
 private NetherTrophyKeys(){}
}
