package org.slavicmyths.client;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.textile.BeltData;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class TextileClient {
    @SubscribeEvent public static void empty(PlayerInteractEvent.RightClickEmpty e){if(e.getEntity().isShiftKeyDown()&&!e.getEntity().getData(BeltData.BELT).isEmpty())PacketDistributor.sendToServer(new BeltData.Unequip(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND));}
    public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins()){if(e.getSkin(skin) instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer r)r.addLayer(new TextileClothingLayer(r,skin==net.minecraft.client.resources.PlayerSkin.Model.SLIM));}}
}
