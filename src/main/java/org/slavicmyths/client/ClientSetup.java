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
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.UPYR.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.UPYR));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.NAV.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.NAV));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DRUZHINNIK.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.DRUZHINNIK));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.VOEVODA.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.VOEVODA));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.VOLKHV.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.VOLKHV));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PRINCE.get(),m->new KurganCreatureRenderer(m,org.slavicmyths.kurgan.KurganFighter.Kind.PRINCE));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.KURGAN_BOLT.get(),m->new net.minecraft.client.renderer.entity.SpriteRenderer<>(m,net.minecraft.client.Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.NIGHTINGALE.get(),NightingaleRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ELDER_VODYANOY.get(),ElderVodyanoyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BANDIT_FIGHTER.get(),m->new BanditRenderer(m,0));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BANDIT_ARCHER.get(),m->new BanditRenderer(m,1));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BANDIT_HEAVY.get(),m->new BanditRenderer(m,2));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BANDIT_SENIOR.get(),m->new BanditRenderer(m,3));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ATAMAN.get(),m->new BanditRenderer(m,4));

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.THROWN_NET.get(),m->new net.minecraft.client.renderer.entity.SpriteRenderer<>(m,net.minecraft.client.Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.VODYANOY.get(),m->new WaterSpiritRenderer(m,false));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.RUSALKA.get(),m->new WaterSpiritRenderer(m,true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIKE.get(),m->new RiverFishRenderer(m,0));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.CARP.get(),m->new RiverFishRenderer(m,1));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.CRAYFISH.get(),m->new RiverFishRenderer(m,2));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.FLYING_BROOM.get(),FlightRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.FLYING_MORTAR.get(),FlightRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DOMOVOY.get(), DomovoyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LESHY.get(), LeshyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.KIKIMORA.get(), KikimoraRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.POLUDNITSA.get(), PoludnitsaRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.POLEVIK.get(), PolevikRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BANNIK.get(), BannikRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.IGOSHA.get(), IgoshaRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.OVINNIK.get(), OvinnikRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.FIRE_SERPENT.get(),m->new ElementRenderer<org.slavicmyths.hunt.ElementHuntMob>(m,true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LIKHO_ONE_EYED.get(),m->new WorldBossRenderer(m,true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.TUGARIN_ZMEY.get(),m->new WorldBossRenderer(m,false));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.TUGARIN_STONE.get(),m->new net.minecraft.client.renderer.entity.SpriteRenderer<>(m,net.minecraft.client.Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PODVEY.get(),m->new ElementRenderer<org.slavicmyths.hunt.ElementHuntMob>(m,false));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SERPENT_PROJECTION.get(),m->new ElementRenderer<org.slavicmyths.hunt.SerpentProjection>(m,true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.VOLKOLAK.get(), m->new HuntRenderer<org.slavicmyths.entity.VolkolakEntity>(m,false));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.EMBER_CLUMP.get(),m->new net.minecraft.client.renderer.entity.SpriteRenderer<>(m,net.minecraft.client.Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.HOT_STONE.get(), manager -> new net.minecraft.client.renderer.entity.SpriteRenderer<>(manager, net.minecraft.client.Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BROWN_BEAR.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.BEAR,"brown_bear",.8F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BEAR_CUB.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.CUB,"bear_cub",.4F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.FOREST_WOLF.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.WOLF,"forest_wolf",.5F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BOAR.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.BOAR,"boar",.6F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.STAG.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.STAG,"stag",.6F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DOE.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.DOE,"doe",.5F));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BABA_YAGA.get(),BabaYagaRenderer::new);
        event.enqueueWork(() -> {
            net.minecraft.client.gui.ScreenManager.register(org.slavicmyths.yaga.YagaMenu.TYPE.get(),YagaScreen::new);
            RenderTypeLookup.setRenderLayer(ModBlocks.YAGA_DRIED_HERBS.get(),RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.YAGA_BONE_CHARM.get(),RenderType.cutout());
            net.minecraft.client.gui.ScreenManager.register(org.slavicmyths.hunt.PouchMenu.TYPE.get(),PouchScreen::new);
            net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntityRenderer(org.slavicmyths.registry.ModTiles.BURIAL_COFFIN.get(),BurialCoffinRenderer::new);
            net.minecraft.client.gui.ScreenManager.register(org.slavicmyths.kurgan.BurialCoffinMenu.TYPE.get(),BurialCoffinScreen::new);
            for(String kind:new String[]{"door","trapdoor"})RenderTypeLookup.setRenderLayer(org.slavicmyths.kurgan.DarkenedWood.get(kind),RenderType.cutout());
            net.minecraft.client.gui.ScreenManager.register(org.slavicmyths.armorer.ArmorerMenu.TYPE.get(),ArmorerScreen::new);
            RpgClient.setup();
            FlightClient.setup();
            RenderTypeLookup.setRenderLayer(ModBlocks.REED.get(),RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.WATER_GRASS.get(),RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.WHITE_LILY.get(),RenderType.cutout());
            net.minecraft.client.Minecraft.getInstance().getBlockColors().register((state,world,pos,tint)->state.getBlock()==ModBlocks.RASPBERRY_BUSH.get()?0x668d41:0x355b43,ModBlocks.RASPBERRY_BUSH.get(),ModBlocks.BLUEBERRY_BUSH.get());
            net.minecraft.client.gui.ScreenManager.register(org.slavicmyths.kitchen.KitchenMenu.TYPE.get(),KitchenScreen::new);
            RenderTypeLookup.setRenderLayer(ModBlocks.RASPBERRY_BUSH.get(),RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.BLUEBERRY_BUSH.get(),RenderType.cutout());
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
