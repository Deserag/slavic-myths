package org.slavicmyths.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModEntities;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DOMOVOY.get(), DomovoyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LESHY.get(), LeshyRenderer::new);
        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(ModBlocks.FLAX.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.WORMWOOD.get(), RenderType.cutout());
        });
    }

    private ClientSetup() { }
}
