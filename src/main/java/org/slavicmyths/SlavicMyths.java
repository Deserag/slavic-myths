package org.slavicmyths;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModLoot;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModSounds;
import org.slavicmyths.world.SpiritSpawns;
import org.slavicmyths.world.ModWorldGen;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(SlavicMyths.MOD_ID)
public final class SlavicMyths {
    public static final String MOD_ID = "slavicmyths";

    public SlavicMyths() {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT,org.slavicmyths.combat.CombatHudConfig.SPEC);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
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
        ModItems.ITEMS.register(bus);
        org.slavicmyths.armorer.ArmorerRecipe.SERIALIZERS.register(bus);
        org.slavicmyths.armorer.ArmorerMenu.register();
        ModLoot.SERIALIZERS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);
        org.slavicmyths.registry.ModTiles.TILES.register(bus);
        org.slavicmyths.registry.ModFeatures.FEATURES.register(bus);
        org.slavicmyths.network.LoreNetwork.register();
        org.slavicmyths.rpg.RpgNetwork.register();
        org.slavicmyths.hunt.PouchMenu.register();
        org.slavicmyths.flight.CargoMenu.register();
        org.slavicmyths.flight.FlightNetwork.register();
        org.slavicmyths.flight.Tailwind.ENCHANTMENTS.register(bus);
        org.slavicmyths.kitchen.KitchenMenu.register();
        org.slavicmyths.yaga.YagaMenu.register();
        org.slavicmyths.rpg.RpgMenu.MENUS.register(bus);
        bus.addListener(this::setup);
        bus.addListener(org.slavicmyths.item.FolkAccessoryItem::slots);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(org.slavicmyths.swamp.SwampStructures::setup);
        event.enqueueWork(org.slavicmyths.bandit.CampStructures::setup);
        event.enqueueWork(org.slavicmyths.kurgan.KurganStructures::setup);
        event.enqueueWork(ModWorldGen::registerFeatures);
        event.enqueueWork(org.slavicmyths.wood.Woodlands::setup);
        event.enqueueWork(org.slavicmyths.hunt.HuntSpawns::placements);
        event.enqueueWork(SpiritSpawns::registerPlacements);
        event.enqueueWork(org.slavicmyths.world.WildlifeSpawns::placements);
        event.enqueueWork(org.slavicmyths.water.WaterSpawns::placements);
        event.enqueueWork(org.slavicmyths.world.LandEncounters::placements);
    }
}
