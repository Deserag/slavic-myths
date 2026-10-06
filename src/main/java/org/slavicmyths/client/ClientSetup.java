package org.slavicmyths.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@net.neoforged.fml.common.EventBusSubscriber(modid = SlavicMyths.MOD_ID, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void navigationKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        org.slavicmyths.navigation.client.NavigationClient.keys(event);
    }
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.YAGA_DRIED_HERBS.get(),RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.YAGA_BONE_CHARM.get(),RenderType.cutout());
            for(String kind:new String[]{"door","trapdoor"})ItemBlockRenderTypes.setRenderLayer(org.slavicmyths.kurgan.DarkenedWood.get(kind),RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.REED.get(),RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WATER_GRASS.get(),RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WHITE_LILY.get(),RenderType.cutout());
            net.minecraft.client.Minecraft.getInstance().getBlockColors().register((state,world,pos,tint)->state.getBlock()==ModBlocks.RASPBERRY_BUSH.get()?0x668d41:0x355b43,ModBlocks.RASPBERRY_BUSH.get(),ModBlocks.BLUEBERRY_BUSH.get());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RASPBERRY_BUSH.get(),RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLUEBERRY_BUSH.get(),RenderType.cutout());
            for (net.minecraft.world.item.Item shield : new net.minecraft.world.item.Item[] {
                    org.slavicmyths.registry.ModItems.RETAINER_SHIELD.get(),
                    org.slavicmyths.registry.ModItems.PERUNITE_SHIELD.get() }) {
                net.minecraft.client.renderer.item.ItemProperties.register(shield, net.minecraft.resources.ResourceLocation.parse("blocking"),
                        (stack, world, entity, seed) -> entity != null && entity.isUsingItem()
                                && entity.getUseItem() == stack ? 1.0F : 0.0F);
            }
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLAX.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WORMWOOD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ST_JOHNS_WORT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.NETTLE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FIREWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.JUNIPER_BERRIES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RITUAL_CANDLE.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(ModEntities.UPYR.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.UPYR));
        event.registerEntityRenderer(ModEntities.NAV.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.NAV));
        event.registerEntityRenderer(ModEntities.DRUZHINNIK.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.DRUZHINNIK));
        event.registerEntityRenderer(ModEntities.VOEVODA.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.VOEVODA));
        event.registerEntityRenderer(ModEntities.VOLKHV.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.VOLKHV));
        event.registerEntityRenderer(ModEntities.PRINCE.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.PRINCE));
        event.registerEntityRenderer(ModEntities.KURGAN_BOLT.get(),m->new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(m));
        event.registerEntityRenderer(ModEntities.NIGHTINGALE.get(),NightingaleRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDER_VODYANOY.get(),ElderVodyanoyRenderer::new);
        event.registerEntityRenderer(ModEntities.BANDIT_FIGHTER.get(),m->new BanditRenderer(m,0));
        event.registerEntityRenderer(ModEntities.BANDIT_ARCHER.get(),m->new BanditRenderer(m,1));
        event.registerEntityRenderer(ModEntities.BANDIT_HEAVY.get(),m->new BanditRenderer(m,2));
        event.registerEntityRenderer(ModEntities.BANDIT_SENIOR.get(),m->new BanditRenderer(m,3));
        event.registerEntityRenderer(ModEntities.ATAMAN.get(),m->new BanditRenderer(m,4));

        event.registerEntityRenderer(ModEntities.THROWN_NET.get(),m->new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(m));
        event.registerEntityRenderer(ModEntities.BOLOTNIK.get(),BolotnikRenderer::new);
        event.registerEntityRenderer(ModEntities.VODYANOY.get(),m->new WaterSpiritRenderer(m,false));
        event.registerEntityRenderer(ModEntities.RUSALKA.get(),m->new WaterSpiritRenderer(m,true));
        event.registerEntityRenderer(ModEntities.PIKE.get(),m->new RiverFishRenderer(m,0));
        event.registerEntityRenderer(ModEntities.CARP.get(),m->new RiverFishRenderer(m,1));
        event.registerEntityRenderer(ModEntities.CRAYFISH.get(),m->new RiverFishRenderer(m,2));
        event.registerEntityRenderer(ModEntities.FLYING_BROOM.get(),FlightRenderer::new);
        event.registerEntityRenderer(ModEntities.FLYING_MORTAR.get(),FlightRenderer::new);
        event.registerEntityRenderer(ModEntities.DOMOVOY.get(), DomovoyRenderer::new);
        event.registerEntityRenderer(ModEntities.LESHY.get(), LeshyRenderer::new);
        event.registerEntityRenderer(ModEntities.KIKIMORA.get(), KikimoraRenderer::new);
        event.registerEntityRenderer(ModEntities.POLUDNITSA.get(), PoludnitsaRenderer::new);
        event.registerEntityRenderer(ModEntities.POLEVIK.get(), PolevikRenderer::new);
        event.registerEntityRenderer(ModEntities.BANNIK.get(), BannikRenderer::new);
        event.registerEntityRenderer(ModEntities.IGOSHA.get(), IgoshaRenderer::new);
        event.registerEntityRenderer(ModEntities.OVINNIK.get(), OvinnikRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_SERPENT.get(),m->new ElementRenderer<org.slavicmyths.hunt.ElementHuntMob>(m,true));
        event.registerEntityRenderer(ModEntities.LIKHO_ONE_EYED.get(),m->new WorldBossRenderer(m,true));
        event.registerEntityRenderer(ModEntities.TUGARIN_ZMEY.get(),m->new WorldBossRenderer(m,false));
        event.registerEntityRenderer(ModEntities.TUGARIN_STONE.get(),m->new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(m));
        event.registerEntityRenderer(ModEntities.PODVEY.get(),m->new ElementRenderer<org.slavicmyths.hunt.ElementHuntMob>(m,false));
        event.registerEntityRenderer(ModEntities.SERPENT_PROJECTION.get(),m->new ElementRenderer<org.slavicmyths.hunt.SerpentProjection>(m,true));
        event.registerEntityRenderer(ModEntities.VOLKOLAK.get(), m->new HuntRenderer<org.slavicmyths.entity.VolkolakEntity>(m,false));
        event.registerEntityRenderer(ModEntities.EMBER_CLUMP.get(),m->new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(m));
        event.registerEntityRenderer(ModEntities.HOT_STONE.get(), manager -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(manager));
        event.registerEntityRenderer(ModEntities.BROWN_BEAR.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.BEAR,"brown_bear",.8F));
        event.registerEntityRenderer(ModEntities.BEAR_CUB.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.CUB,"bear_cub",.4F));
        event.registerEntityRenderer(ModEntities.FOREST_WOLF.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.WOLF,"forest_wolf",.5F));
        event.registerEntityRenderer(ModEntities.BOAR.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.BOAR,"boar",.6F));
        event.registerEntityRenderer(ModEntities.STAG.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.STAG,"stag",.6F));
        event.registerEntityRenderer(ModEntities.DOE.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.DOE,"doe",.5F));
        event.registerEntityRenderer(ModEntities.BABA_YAGA.get(),BabaYagaRenderer::new);
        event.registerBlockEntityRenderer(org.slavicmyths.registry.ModTiles.BURIAL_COFFIN.get(),BurialCoffinRenderer::new);
    }
    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){
            event.register(org.slavicmyths.rpg.RpgMenu.TYPE.get(),RpgScreen::new);
            event.register(org.slavicmyths.yaga.YagaMenu.TYPE.get(),YagaScreen::new);
            event.register(org.slavicmyths.hunt.PouchMenu.TYPE.get(),PouchScreen::new);
            event.register(org.slavicmyths.kurgan.BurialCoffinMenu.TYPE.get(),BurialCoffinScreen::new);
            event.register(org.slavicmyths.armorer.ArmorerMenu.TYPE.get(),ArmorerScreen::new);
            event.register(org.slavicmyths.kitchen.KitchenMenu.TYPE.get(),KitchenScreen::new);
    }
    private ClientSetup() { }
    @SubscribeEvent public static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){RpgClient.keys(e);FlightClient.keys(e);}
    @SubscribeEvent public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e){FolkEquipmentLayer.layers(e);FlightClient.renderers(e.getContext());GusliClient.renderers(e.getContext());}
}
