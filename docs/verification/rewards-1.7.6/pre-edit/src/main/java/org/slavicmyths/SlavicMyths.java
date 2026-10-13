package org.slavicmyths;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModLoot;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModSounds;
import org.slavicmyths.world.SpiritSpawns;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(SlavicMyths.MOD_ID)
public final class SlavicMyths {
    public static final String MOD_ID = "slavicmyths";

    public SlavicMyths(IEventBus bus, ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,org.slavicmyths.rpg.classes.ClassHudConfig.SPEC);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,org.slavicmyths.earth.EarthVisualConfig.SPEC,"slavicmyths-earth-visuals.toml");
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,org.slavicmyths.chud.AbandonedConfig.SPEC,"slavicmyths-chud-worldgen.toml");
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,org.slavicmyths.earth.EarthConfig.SPEC,"slavicmyths-earth-spirit.toml");
        org.slavicmyths.item.ItemState.COMPONENTS.register(bus);
        org.slavicmyths.rpg.Runes.init();
        org.slavicmyths.garden.Gardens.init();
        org.slavicmyths.husbandry.Husbandry.init(bus);
        org.slavicmyths.textile.Textiles.init(bus);
        org.slavicmyths.kitchen.KitchenII.init(bus);
        org.slavicmyths.storage.Household.init();
        org.slavicmyths.brewing.Brewing.init(bus);
        org.slavicmyths.village.VillageRoles.init(bus);
        org.slavicmyths.village.VillageFoundation.init(bus);
        org.slavicmyths.kitchen.KitchenRecipe.SERIALIZER.getId();
        org.slavicmyths.wood.Woodlands.init();
        org.slavicmyths.furniture.Furniture.init();
        org.slavicmyths.kurgan.DarkenedWood.init();
        org.slavicmyths.kurgan.KurganBlocks.init();
        org.slavicmyths.hunt.BossEffects.init();
        org.slavicmyths.kurgan.KurganCurse.EFFECTS.register(bus);
        org.slavicmyths.kurgan.BurialCoffinMenu.register();
        org.slavicmyths.kurgan.KurganStructures.register();
        org.slavicmyths.earth.EarthContent.init();
        org.slavicmyths.sivka.SivkaContent.init();
        org.slavicmyths.firebird.FirebirdContent.init();
        org.slavicmyths.nochnitsa.NochnitsaContent.init(bus);
        org.slavicmyths.pesigolovtsy.DogheadContent.init(bus);
        org.slavicmyths.kuzya.KuzyaContent.init();
        org.slavicmyths.bayun.BayunContent.init();
        org.slavicmyths.bogatyr.BogatyrContent.init();
        org.slavicmyths.marinka.MarinkaContent.init();
        org.slavicmyths.skim.SkimContent.init();
        org.slavicmyths.finist.FinistContent.init();
        org.slavicmyths.kalin.KalinContent.init();
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,org.slavicmyths.bogatyr.BogatyrConfig.SPEC,"slavicmyths-bogatyr.toml");
        bus.addListener(org.slavicmyths.bogatyr.BogatyrNetwork::register);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,org.slavicmyths.pesigolovtsy.DogheadConfig.SPEC,"slavicmyths-pesigolovtsy.toml");
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,org.slavicmyths.nochnitsa.NochnitsaConfig.SPEC,"slavicmyths-nochnitsa.toml");
        bus.addListener(org.slavicmyths.sivka.SivkaNetwork::register);
        ModBlocks.BLOCKS.register(bus);
        org.slavicmyths.bandit.CampStructures.register();
        org.slavicmyths.gorodishche.GorodishcheStructures.init();
        org.slavicmyths.chud.ChudStructures.init();
        org.slavicmyths.worldgen.ChudRegionPlacements.TYPES.register(bus);
        org.slavicmyths.swamp.SwampStructures.STRUCTURES.register(bus);
        org.slavicmyths.swamp.SwampStructures.PIECES.register(bus);
        org.slavicmyths.equipment.UniqueEquipment.init();
        org.slavicmyths.item.ModGear.ARMOR_MATERIALS.register(bus);
        org.slavicmyths.registry.ModItemGroup.TABS.register(bus);
        org.slavicmyths.combat.WeaponCatalog.register();
        org.slavicmyths.combat.RareWeapons.init();
        org.slavicmyths.rpg.RuneRepair.init();
        org.slavicmyths.rpg.RuneFoundation.init();
        org.slavicmyths.rpg.RuneRework.init();
        org.slavicmyths.rpg.RareRunes.init();
        org.slavicmyths.rpg.RuneVisual.init();
        org.slavicmyths.rpg.RuneCraftRecipe.TYPES.register(bus);
        org.slavicmyths.rpg.RuneCraftRecipe.SERIALIZERS.register(bus);
        org.slavicmyths.industry.Industry.register(bus);
        org.slavicmyths.underground.UndergroundMobs.init(bus);
        org.slavicmyths.underground.UndergroundSounds.init();
        ModItems.ITEMS.register(bus);
        org.slavicmyths.armorer.ArmorerRecipe.TYPES.register(bus);
        org.slavicmyths.armorer.ArmorerRecipe.SERIALIZERS.register(bus);
        org.slavicmyths.armorer.ArmorerMenu.register();
        org.slavicmyths.farming.Farming.FUNCTIONS.register(bus);
        ModLoot.SERIALIZERS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);
        org.slavicmyths.registry.ModTiles.TILES.register(bus);
        org.slavicmyths.registry.ModFeatures.FEATURES.register(bus);
        bus.addListener(org.slavicmyths.network.LoreNetwork::register);
        bus.addListener(org.slavicmyths.tales.TaleNetwork::register);
        bus.addListener(org.slavicmyths.bayun.BayunNetwork::register);
        bus.addListener(org.slavicmyths.navigation.NavigationNetwork::register);
        bus.addListener(org.slavicmyths.rpg.RpgNetwork::register);
        bus.addListener(org.slavicmyths.rpg.RareRuneNetwork::register);
        bus.addListener(org.slavicmyths.equipment.ClearSightNetwork::register);
        org.slavicmyths.hunt.PouchMenu.register();
        org.slavicmyths.flight.CargoMenu.register();
        bus.addListener(org.slavicmyths.flight.FlightNetwork::register);
        org.slavicmyths.kitchen.KitchenMenu.register();
        org.slavicmyths.yaga.YagaMenu.register();
        org.slavicmyths.rpg.RpgMenu.MENUS.register(bus);
        bus.addListener(org.slavicmyths.hunt.HuntSpawns::placements);
        bus.addListener(org.slavicmyths.world.SpiritSpawns::registerPlacements);
        bus.addListener(org.slavicmyths.world.WildlifeSpawns::placements);
        bus.addListener(org.slavicmyths.water.WaterSpawns::placements);
        bus.addListener(org.slavicmyths.world.LandEncounters::placements);
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(org.slavicmyths.wood.Woodlands::setup);
    }
}
