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

public final class ClientSetup {

    public static void registerModListeners(net.neoforged.bus.api.IEventBus bus) {

        bus.addListener(org.slavicmyths.burningislands.client.IslandsClient::register);

        bus.addListener(GreatswordClient::extensions);

        bus.addListener(InfernalFurnaceClient::screens);

        bus.addListener(NetherTrophyKeys.Registration::register);

        bus.addListener(PosadBossRenderer::register);

        bus.addListener(org.slavicmyths.losthalls.client.HallsClient::register);

        bus.addListener(org.slavicmyths.navya.NavyaClient::setup);

        bus.addListener(org.slavicmyths.navya.NavyaClient::renderers);

        bus.addListener(org.slavicmyths.nethersmithing.client.NetherArmorClient::register);

        bus.addListener(org.slavicmyths.nethersmithing.client.NetherArmorGlow::layers);

        bus.addListener(KalinClient::renderers);

        bus.addListener(PolozClient::renderers);

        bus.addListener(PolozClient::extensions);

        bus.addListener(PolozClient::setup);

        bus.addListener(UndergroundClient::renderers);

        bus.addListener(TrophyHeadsClient::renderers);

        bus.addListener(TrophyHeadsClient::items);

        bus.addListener(NochnitsaClient::renderers);

        bus.addListener(DogheadClient::renderers);

        bus.addListener(EarthClient::renderers);

        bus.addListener(WeaponMotionClient::register);

        bus.addListener(org.slavicmyths.client.runeglow.RuneGlowRenderTypes::shaders);

        bus.addListener(org.slavicmyths.client.runeglow.RuneGlowRenderTypes::buffers);

        bus.addListener(NightClient.Registration::keys);

        bus.addListener(NightClient.Registration::renderers);

        bus.addListener(NightClient.Registration::layers);

        bus.addListener(NightClient.Registration::models);

        bus.addListener(NightClient.Registration::extension);

        bus.addListener(Quest185Client.Registration::particles);

        bus.addListener(Quest185Client.Registration::models);

        bus.addListener(Quest185Client.Registration::extensions);

        bus.addListener(Quest185Client.Registration::renderers);

        bus.addListener(BogatyrClient.Registration::keys);

        bus.addListener(BogatyrClient.Registration::renderers);

        bus.addListener(WintersClient.Setup::ready);

        bus.addListener(FrostClient.Registration::keys);

        bus.addListener(FrostClient.Registration::renderers);

        bus.addListener(FrostClient.Registration::models);

        bus.addListener(FrostClient.Registration::extension);

        bus.addListener(org.slavicmyths.wood.WoodlandClient::setup);

        bus.addListener(org.slavicmyths.wood.WoodlandClient::renderers);

        bus.addListener(SanctuaryClient::renderers);

        bus.addListener(org.slavicmyths.furniture.FurnitureClient::setup);

        bus.addListener(BayunClient::register);

        bus.addListener(MarinkaClient.Registration::renderers);

        bus.addListener(FlailClient::models);

        bus.addListener(FlailClient::bake);

        bus.addListener(FlailClient::extensions);

        bus.addListener(WhipClient::additional);

        bus.addListener(WhipClient::bake);

        bus.addListener(BrightFalconClient.Registration::renderers);

        bus.addListener(SivkaClient.Setup::keys);

        bus.addListener(SivkaClient.Setup::renderers);

        bus.addListener(GuardianClient.Registration::renderers);

        bus.addListener(IndustryClient::screens);

        bus.addListener(CuriosAccessories::setup);

        bus.addListener(MiningGogglesRenderer::setup);

        bus.addListener(BowDrawClient::extensions);

        bus.addListener(GorynychClient::renderers);

        bus.addListener(GorynychClient::extensions);

        bus.addListener(GorynychClient::setup);

        bus.addListener(ArmorStandClothingLayer::layers);

        bus.addListener(BasicArmorModels::register);

        bus.addListener(ChudPeopleClient::renderers);

        bus.addListener(KitchenDisplay::models);

        bus.addListener(ThrowingMotionClient::register);

        bus.addListener(FirebirdClient::renderers);

        bus.addListener(KuzyaClient::renderers);

        bus.addListener(SkimClient.Registration::renderers);

        bus.addListener(ThirdPersonWeaponMotion::additional);

        bus.addListener(ThirdPersonWeaponMotion::bake);

        bus.addListener(UniqueArmorModels::register);

        bus.addListener(ClientSetup::navigationKeys);

        bus.addListener(ClientSetup::tugarinModels);

        bus.addListener(ClientSetup::tugarinBake);

        bus.addListener(ClientSetup::tugarinLayers);

        bus.addListener(ClientSetup::setup);

        bus.addListener(ClientSetup::morParticles);

        bus.addListener(ClientSetup::renderers);

        bus.addListener(ClientSetup::screens);

        bus.addListener(ClientSetup::extensions);

        bus.addListener(ClientSetup::keys);

        bus.addListener(ClientSetup::modelLayers);

        bus.addListener(ClientSetup::layers);

    }

    @SubscribeEvent

    public static void navigationKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {

        org.slavicmyths.navigation.client.NavigationClient.keys(event);

    }

    @SubscribeEvent public static void tugarinModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event){TugarinPairClient.registerModels(event);}

    @SubscribeEvent public static void tugarinBake(net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult event){TugarinPairClient.bake(event);}

    @SubscribeEvent public static void tugarinLayers(EntityRenderersEvent.AddLayers event){TugarinPairClient.layers(event);}

    @SubscribeEvent

    public static void setup(FMLClientSetupEvent event) {

        net.neoforged.neoforge.client.gui.IConfigScreenFactory factory=(container,parent)->new ClassSettingsScreen(parent);

        net.neoforged.fml.ModList.get().getModContainerById(SlavicMyths.MOD_ID).orElseThrow().registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,factory);

        event.enqueueWork(() -> {

            MorJadeCompat.install();

            FieldBowClient.properties();FinistClient.properties();

            net.minecraft.client.renderer.Sheets.addWoodType(org.slavicmyths.garden.Gardens.APPLE_TYPE);

            for(String berry:org.slavicmyths.garden.Gardens.BERRIES)ItemBlockRenderTypes.setRenderLayer(org.slavicmyths.garden.Gardens.block(berry+"_bush"),RenderType.cutout());

            for(String name:new String[]{"apple_leaves","apple_sapling","apple_door","apple_trapdoor"})ItemBlockRenderTypes.setRenderLayer(org.slavicmyths.garden.Gardens.block(name),RenderType.cutoutMipped());

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

                    org.slavicmyths.registry.ModItems.PERUNITE_SHIELD.get(),

                    org.slavicmyths.equipment.UniqueEquipment.WARD_SHIELD.get(),org.slavicmyths.rewards.RewardContent.item("gift_ilya_shield") }) {

                net.minecraft.client.renderer.item.ItemProperties.register(shield, net.minecraft.resources.ResourceLocation.parse("blocking"),

                        (stack, world, entity, seed) -> entity != null && entity.isUsingItem()

                                && entity.getUseItem() == stack ? 1.0F : 0.0F);

            }

            org.slavicmyths.farming.Farming.crops().forEach(crop -> ItemBlockRenderTypes.setRenderLayer(crop, RenderType.cutout()));

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLAX.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WORMWOOD.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ST_JOHNS_WORT.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.NETTLE.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FIREWEED.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.JUNIPER_BERRIES.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RITUAL_CANDLE.get(), RenderType.cutout());

        });

    }

    @SubscribeEvent public static void morParticles(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event){event.registerSpriteSet(org.slavicmyths.mor.MorContent.SPARK.get(),s->MorParticle.provider(s,false));event.registerSpriteSet(org.slavicmyths.mor.MorContent.ASH.get(),s->MorParticle.provider(s,true));}

    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){

        event.registerEntityRenderer(ModEntities.POSAD_RESIDENT.get(), PosadResidentRenderer::new);

        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.GNETEYA.get(), ctx -> new SistersRenderer(ctx, false, new net.minecraft.client.model.geom.ModelLayerLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths", "gneteya"), "main")));
        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.LEDEYA.get(), LedeyaRenderer::new);
        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.FROST_BOLT.get(), SistersVisuals.Bolt::new);

        event.registerEntityRenderer(org.slavicmyths.viy.ViyContent.VIY.get(), ViyRenderer::new);
        event.registerEntityRenderer(org.slavicmyths.viy.ViyContent.VIY_UPYR.get(), ctx -> new ViyServantRenderer(ctx, org.slavicmyths.sisters.ViyServant.Kind.UPYR));
        event.registerEntityRenderer(org.slavicmyths.viy.ViyContent.VIY_NAV.get(), ctx -> new ViyServantRenderer(ctx, org.slavicmyths.sisters.ViyServant.Kind.NAV));
        event.registerEntityRenderer(org.slavicmyths.viy.ViyContent.VIY_KORNELAP.get(), ctx -> new ViyServantRenderer(ctx, org.slavicmyths.sisters.ViyServant.Kind.KORNELAP));
        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.NEVEYA.get(), NeveyaRenderer::new);
        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.NEVEYA_BOLT.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.LOMEYA.get(), LomeyaRenderer::new);
        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.AGNEYA.get(), ctx -> new AgneyaRenderer(ctx, new net.minecraft.client.model.geom.ModelLayerLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths", "agneya"), "main")));

        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.TRYASEYA.get(), ctx -> new SistersRenderer(ctx, true, new net.minecraft.client.model.geom.ModelLayerLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths", "tryaseya"), "main")));

        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.SPIRIT.get(),SistersVisuals.Spirit::new);

        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.BARRIER.get(),SistersVisuals.Wall::new);

        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.BOLT.get(),SistersVisuals.Bolt::new);

        event.registerEntityRenderer(org.slavicmyths.sisters.SistersContent.ZONE.get(),SistersVisuals.Zone::new);

        event.registerEntityRenderer(org.slavicmyths.morovoy.MorovoyRavenContent.BOSS.get(),MorovoyRavenRenderer::new);

        event.registerEntityRenderer(org.slavicmyths.morovoy.MorovoyRavenContent.MINION.get(),MorovoyRavenRenderer::new);

        event.registerEntityRenderer(org.slavicmyths.morovoy.MorovoyRavenContent.BOLT.get(),RavenBlightRenderer::new);

        event.registerEntityRenderer(org.slavicmyths.morovoy.MorovoyRavenContent.MIASMA.get(),RavenMiasmaRenderer::new);

        event.registerEntityRenderer(ModEntities.NETHER_RETAINER.get(),NetherRetainerRenderer::new);

        event.registerEntityRenderer(ModEntities.MOROVA_DEVA.get(),ctx->new NetherSummonBossRenderer(ctx,true));

        event.registerEntityRenderer(ModEntities.TRIZNA_PRINCE.get(),ctx->new NetherSummonBossRenderer(ctx,false));

        event.registerEntityRenderer(org.slavicmyths.mor.MorContent.ZONE.get(),MorZoneRenderer::new);

        event.registerEntityRenderer(org.slavicmyths.mor.MorContent.FLASK.get(),m->new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(m));

        event.registerEntityRenderer(org.slavicmyths.mor.MorContent.BOLT.get(),MorBoltRenderer::new);

        event.registerEntityRenderer(org.slavicmyths.rpg.RuneVisual.TYPE.get(),RuneVisualRenderer::new);

        org.slavicmyths.client.HusbandryRenderer.register(event);

        event.registerBlockEntityRenderer(org.slavicmyths.storage.Household.TYPE.get(),StorageDisplay::new);

        event.registerBlockEntityRenderer(org.slavicmyths.brewing.Brewing.TYPE.get(),BrewDisplay::new);

        event.registerBlockEntityRenderer(org.slavicmyths.kitchen.KitchenII.TABLE.get(),KitchenDisplay.Table::new);

        event.registerBlockEntityRenderer(org.slavicmyths.kitchen.KitchenII.TILE.get(),KitchenDisplay.Kitchen::new);

        event.registerEntityRenderer(org.slavicmyths.textile.Textiles.CARCASS.get(),CarcassRenderer::new);

        event.registerBlockEntityRenderer(org.slavicmyths.textile.Textiles.STATION.get(),SpinningWheelRenderer::new);

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

        event.registerEntityRenderer(ModEntities.WEAPON_PROJECTILE.get(),WeaponProjectileRenderer::new);

        event.registerEntityRenderer(ModEntities.BOLOTNIK.get(),BolotnikRenderer::new);

        event.registerEntityRenderer(ModEntities.VODYANOY.get(),m->new WaterSpiritRenderer(m,false));

        event.registerEntityRenderer(ModEntities.RUSALKA.get(),RusalkaRenderer::new);

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

        event.registerEntityRenderer(ModEntities.CURSED_VOLKOLAK.get(),CursedVolkolakRenderer::new);

        event.registerEntityRenderer(ModEntities.EMBER_CLUMP.get(),m->new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(m));

        event.registerEntityRenderer(ModEntities.HOT_STONE.get(), manager -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(manager));

        event.registerEntityRenderer(ModEntities.GREY_WOLF.get(),GreyWolfRenderer::new);

        event.registerEntityRenderer(ModEntities.FINIST_ARROW.get(),FinistArrowRenderer::new);

        event.registerEntityRenderer(ModEntities.BROWN_BEAR.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.BEAR,"brown_bear",.8F));

        event.registerEntityRenderer(ModEntities.BEAR_CUB.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.CUB,"bear_cub",.4F));

        event.registerEntityRenderer(ModEntities.FOREST_WOLF.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.WOLF,"forest_wolf",.5F));

        event.registerEntityRenderer(ModEntities.BOAR.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.BOAR,"boar",.6F));

        event.registerEntityRenderer(ModEntities.STAG.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.STAG,"stag",.6F));

        event.registerEntityRenderer(ModEntities.DOE.get(), m -> new WildlifeRenderer(m, org.slavicmyths.entity.WildlifeEntity.Kind.DOE,"doe",.5F));

        event.registerEntityRenderer(ModEntities.BABA_YAGA.get(),BabaYagaRenderer::new);

        event.registerBlockEntityRenderer(org.slavicmyths.registry.ModTiles.BURIAL_COFFIN.get(),BurialCoffinRenderer::new);

        event.registerBlockEntityRenderer(org.slavicmyths.garden.Gardens.SIGN.get(),net.minecraft.client.renderer.blockentity.SignRenderer::new);

        event.registerBlockEntityRenderer(org.slavicmyths.garden.Gardens.HANGING_SIGN.get(),net.minecraft.client.renderer.blockentity.HangingSignRenderer::new);

    }

    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){

        event.register(org.slavicmyths.military.MilitaryMenu.TYPE.get(),MilitaryScreen::new);

        event.register(org.slavicmyths.village.SettlementMenu.TYPE.get(),SettlementScreen::new);

        event.register(org.slavicmyths.storage.SackMenu.TYPE.get(),SackScreen::new);

            event.<org.slavicmyths.rpg.RpgMenu,net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<org.slavicmyths.rpg.RpgMenu>>register(org.slavicmyths.rpg.RpgMenu.TYPE.get(),(menu,inventory,title)->menu.anvil?new RuneAnvilScreen(menu,inventory,title):new RpgScreen(menu,inventory,title));

            event.register(org.slavicmyths.yaga.YagaMenu.TYPE.get(),YagaScreen::new);

            event.register(org.slavicmyths.hunt.PouchMenu.TYPE.get(),PouchScreen::new);

            event.register(org.slavicmyths.kurgan.BurialCoffinMenu.TYPE.get(),BurialCoffinScreen::new);

            event.register(org.slavicmyths.armorer.ArmorerMenu.TYPE.get(),ArmorerScreen::new);

            event.register(org.slavicmyths.kitchen.KitchenMenu.TYPE.get(),KitchenScreen::new);

            event.register(org.slavicmyths.brewing.VatMenu.TYPE.get(),VatScreen::new);

    }

    @SubscribeEvent public static void extensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent e){e.registerItem(new RewardArmor(0),org.slavicmyths.rewards.RewardContent.item("trophy_idol_chest"));e.registerItem(new RewardArmor(1),org.slavicmyths.rewards.RewardContent.item("trophy_marinka_cloak"));e.registerItem(new RewardArmor(2),org.slavicmyths.rewards.RewardContent.item("trophy_kartaus_mantle"));e.registerItem(new PeruniteArmor(),org.slavicmyths.registry.ModItems.PERUNITE_HELMET.get(),org.slavicmyths.registry.ModItems.PERUNITE_CHESTPLATE.get(),org.slavicmyths.registry.ModItems.PERUNITE_LEGGINGS.get(),org.slavicmyths.registry.ModItems.PERUNITE_BOOTS.get());}

    private ClientSetup() { }

    @SubscribeEvent public static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(WolfStoryClient.RING);RpgClient.keys(e);FlightClient.keys(e);}

    @SubscribeEvent public static void modelLayers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {

        event.registerLayerDefinition(new net.minecraft.client.model.geom.ModelLayerLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths", "gneteya"), "main"), () -> SistersModel.create(false));
        event.registerLayerDefinition(LedeyaRenderer.LAYER, LedeyaRenderer::layer);
        event.registerLayerDefinition(new net.minecraft.client.model.geom.ModelLayerLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths", "agneya"), "main"), AgneyaModelV7::create);

        event.registerLayerDefinition(new net.minecraft.client.model.geom.ModelLayerLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths", "tryaseya"), "main"), () -> SistersModel.create(true));

    }

    @SubscribeEvent public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e){FolkEquipmentLayer.layers(e);TextileClient.layers(e);FlightClient.renderers(e.getContext());GusliClient.renderers(e.getContext());}

}
