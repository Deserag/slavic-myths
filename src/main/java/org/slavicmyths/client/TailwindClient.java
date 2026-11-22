package org.slavicmyths.client;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.marine.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class TailwindClient {
 private static boolean held; private static int last=-100; private static Object world;
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
 var mc=Minecraft.getInstance();if(world!=mc.level){world=mc.level;last=-100;held=false;}
 if(mc.player==null||mc.screen!=null||!mc.player.isAlive()){last=-100;held=mc.options.keyJump.isDown();return;}
 boolean down=mc.options.keyJump.isDown();int now=mc.player.tickCount;
 if(down&&!held){if(now>=last&&now-last<=7&&MarineAccessories.wears(mc.player,"tailwind_feather")){PacketDistributor.sendToServer(new TailwindControls.Dash());last=-100;}else last=now;}held=down;
 }
 @SubscribeEvent public static void hud(RenderGuiEvent.Post event){
 var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||mc.options.hideGui||!MarineAccessories.wears(mc.player,"tailwind_feather"))return;
 var item=MarineAccessories.TAILWIND_FEATHER.get();float left=mc.player.getCooldowns().getCooldownPercent(item,0);var g=event.getGuiGraphics();int x=g.guiWidth()/2+100,y=g.guiHeight()-55;
 g.renderItem(item.getDefaultInstance(),x,y);g.drawString(mc.font,net.minecraft.network.chat.Component.translatable("tailwind.hud",left>0?Integer.toString((int)Math.ceil(left*12)):"OK"),x+20,y+4,0x99EEEE,true);
 }
}
