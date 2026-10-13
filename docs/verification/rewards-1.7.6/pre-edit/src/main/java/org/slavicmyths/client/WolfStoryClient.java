package org.slavicmyths.client;
import net.minecraft.client.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.wolf.WolfStoryNetwork;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class WolfStoryClient {
 public static final KeyMapping RING=new KeyMapping("key.slavicmyths.grey_pack",86,"key.categories.slavicmyths");
 @SubscribeEvent public static void view(WolfStoryNetwork.Viewed e){var mc=Minecraft.getInstance();if(e.view.data().isEmpty()){if(mc.screen instanceof WolfStoryScreen)mc.setScreen(null);return;}if(e.view.open())mc.setScreen(new WolfStoryScreen(e.view.data()));else if(mc.screen instanceof WolfStoryScreen s)s.update(e.view.data());}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){var mc=Minecraft.getInstance();while(RING.consumeClick())if(mc.player!=null&&mc.screen==null){if(ClassClient.conflict(RING)){mc.player.displayClientMessage(org.slavicmyths.military.GreyWolfStory.text("key_conflict"),true);continue;}net.neoforged.neoforge.network.PacketDistributor.sendToServer(new WolfStoryNetwork.Ring());}}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.options.hideGui||!mc.player.getMainHandItem().is(org.slavicmyths.finist.FinistItems.BOW.get())&&!mc.player.getOffhandItem().is(org.slavicmyths.finist.FinistItems.BOW.get()))return;var d=RpgClient.data.getCompound("FinistMark");if(!d.hasUUID("Target")||!d.getString("Dimension").equals(mc.level.dimension().location().toString())||d.getLong("Until")<=mc.level.getGameTime())return;var target=mc.level.getEntity(d.getInt("Entity"));if(target==null||!target.getUUID().equals(d.getUUID("Target"))||!target.isAlive()||!mc.player.hasLineOfSight(target)||mc.player.distanceToSqr(target)>48*48)return;var g=e.getGuiGraphics();int x=mc.getWindow().getGuiScaledWidth()/2,y=mc.getWindow().getGuiScaledHeight()/2;g.blit(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/item/finist_target_marker.png"),x+8,y-4,8,8,0,0,32,32,32,32);}
 private WolfStoryClient(){}
}
