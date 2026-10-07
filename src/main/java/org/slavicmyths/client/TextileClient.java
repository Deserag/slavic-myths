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
    private static void install(net.minecraft.client.renderer.entity.player.PlayerRenderer r,boolean slim,net.minecraft.client.renderer.entity.EntityRendererProvider.Context ctx){
        r.layers.removeIf(layer->layer instanceof net.minecraft.client.renderer.entity.layers.CustomHeadLayer);
        r.addLayer(new ClothingHeadLayer(r,ctx));r.addLayer(new TextileClothingLayer(r,slim));
    }
    public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins()){if(e.getSkin(skin) instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer r)install(r,skin==net.minecraft.client.resources.PlayerSkin.Model.SLIM,e.getContext());}}
}
