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
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.KIKIMORA.get(), KikimoraRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.POLUDNITSA.get(), PoludnitsaRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.POLEVIK.get(), PolevikRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BANNIK.get(), BannikRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.IGOSHA.get(), IgoshaRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.OVINNIK.get(), OvinnikRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.HOT_STONE.get(), manager -> new net.minecraft.client.renderer.entity.SpriteRenderer<>(manager, net.minecraft.client.Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BROWN_BEAR.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.BEAR,"brown_bear",.8F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BEAR_CUB.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.CUB,"bear_cub",.4F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.FOREST_WOLF.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.WOLF,"forest_wolf",.5F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BOAR.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.BOAR,"boar",.6F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.STAG.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.STAG,"stag",.6F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DOE.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.DOE,"doe",.5F));
        event.enqueueWork(() -> {
            RpgClient.setup();
            FolkEquipmentLayer.setup();
            for (net.minecraft.item.Item shield : new net.minecraft.item.Item[] {
                    org.slavicmyths.registry.ModItems.RETAINER_SHIELD.get(),
                    org.slavicmyths.registry.ModItems.PERUNITE_SHIELD.get() }) {
                net.minecraft.item.ItemModelsProperties.register(shield, new net.minecraft.util.ResourceLocation("blocking"),
                        (stack, world, entity) -> entity != null && entity.isUsingItem()
                                && entity.getUseItem() == stack ? 1.0F : 0.0F);
            }
            RenderTypeLookup.setRenderLayer(ModBlocks.FLAX.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.WORMWOOD.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.ST_JOHNS_WORT.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.NETTLE.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.FIREWEED.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.JUNIPER_BERRIES.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.RITUAL_CANDLE.get(), RenderType.cutout());
        });
    }

    private ClientSetup() { }
}
