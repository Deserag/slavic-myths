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
        org.slavicmyths.item.ItemState.COMPONENTS.register(bus);
        org.slavicmyths.rpg.Runes.init();
        org.slavicmyths.wood.Woodlands.init();
        org.slavicmyths.furniture.Furniture.init();
        org.slavicmyths.kurgan.DarkenedWood.init();
        org.slavicmyths.kurgan.KurganBlocks.init();
        org.slavicmyths.hunt.BossEffects.init();
        org.slavicmyths.kurgan.KurganCurse.EFFECTS.register(bus);
        org.slavicmyths.kurgan.BurialCoffinMenu.register();
        org.slavicmyths.kurgan.KurganStructures.register();
        ModBlocks.BLOCKS.register(bus);
        org.slavicmyths.bandit.CampStructures.register();
        org.slavicmyths.swamp.SwampStructures.STRUCTURES.register(bus);
        org.slavicmyths.swamp.SwampStructures.PIECES.register(bus);
        org.slavicmyths.item.ModGear.ARMOR_MATERIALS.register(bus);
        org.slavicmyths.registry.ModItemGroup.TABS.register(bus);
        ModItems.ITEMS.register(bus);
        org.slavicmyths.armorer.ArmorerRecipe.TYPES.register(bus);
        org.slavicmyths.armorer.ArmorerRecipe.SERIALIZERS.register(bus);
        org.slavicmyths.armorer.ArmorerMenu.register();
        ModLoot.SERIALIZERS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);
        org.slavicmyths.registry.ModTiles.TILES.register(bus);
        org.slavicmyths.registry.ModFeatures.FEATURES.register(bus);
        bus.addListener(org.slavicmyths.network.LoreNetwork::register);
        bus.addListener(org.slavicmyths.navigation.NavigationNetwork::register);
        bus.addListener(org.slavicmyths.rpg.RpgNetwork::register);
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
