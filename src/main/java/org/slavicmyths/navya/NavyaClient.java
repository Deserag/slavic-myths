package org.slavicmyths.navya;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class NavyaClient {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(()-> {
            Sheets.addWoodType(NavyaBlocks.WOOD);
            for(String name:new String[]{"navya_door","navya_trapdoor","navya_dead_leaves","navya_dry_branches","navya_roots","navya_hanging_roots","navya_shrub","ghost_fungus","navya_growth","navya_sapling"})
                ItemBlockRenderTypes.setRenderLayer(NavyaBlocks.get(name),RenderType.cutoutMipped());
        });
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(NavyaBlocks.SIGN.get(),SignRenderer::new);
        event.registerBlockEntityRenderer(NavyaBlocks.HANGING_SIGN.get(),HangingSignRenderer::new);
        for(var entry:NavyaContent.MOBS.entrySet())event.registerEntityRenderer(entry.getValue().get(),context->new org.slavicmyths.navya.client.NavyaRenderer(context,entry.getKey()));
        event.registerEntityRenderer(NavyaContent.SHOT.get(),context->new org.slavicmyths.navya.client.NavyaEffectRenderer<>(context,false));
        event.registerEntityRenderer(NavyaContent.ROOTS.get(),context->new org.slavicmyths.navya.client.NavyaEffectRenderer<>(context,true));
    }
    private NavyaClient() { }
}
