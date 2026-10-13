package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.sanctuary.SanctuaryContent;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class SanctuaryClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(SanctuaryContent.VIAL.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new);e.registerBlockEntityRenderer(SanctuaryContent.CHEST.get(),net.minecraft.client.renderer.blockentity.ChestRenderer::new);}
 private SanctuaryClient(){}
}
