package org.slavicmyths.verify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Two real TCP clients; no test classes in the production jar. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class NetworkClient {
 static boolean joined;static int ticks;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
  var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.framerateLimit().set(30);mc.options.renderDistance().set(2);mc.options.guiScale().set(2);
  if(mc.player!=null){joined=true;if(++ticks%100==0){var dir=java.nio.file.Path.of("D:/slavic-myths/docs/implementation/heroes-coop/client");java.nio.file.Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(mc.getUser().getName()+".png"));}}}
  if(mc.screen instanceof DisconnectedScreen)mc.stop();
 }
}
