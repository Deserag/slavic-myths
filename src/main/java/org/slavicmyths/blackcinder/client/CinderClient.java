package org.slavicmyths.blackcinder.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.blackcinder.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class CinderClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){CinderContent.TYPES.forEach((k,t)->e.registerEntityRenderer(t.get(),c->new CinderRenderer(c,k)));e.registerEntityRenderer(CinderWorldgen.COAL.get(),c->new CinderEffectRenderer<>(c,false));e.registerEntityRenderer(CinderWorldgen.SPIKE.get(),c->new CinderEffectRenderer<>(c,true));}
 @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{for(String id:new String[]{"blackwood_door","blackwood_trapdoor","charred_blackwood_leaves"})net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(CinderBlocks.get(id),net.minecraft.client.renderer.RenderType.cutoutMipped());});}
 private CinderClient(){}
}
