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
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModLoot.SERIALIZERS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);
        org.slavicmyths.registry.ModTiles.TILES.register(bus);
        org.slavicmyths.registry.ModFeatures.FEATURES.register(bus);
        org.slavicmyths.network.LoreNetwork.register();
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModWorldGen::registerFeatures);
        event.enqueueWork(SpiritSpawns::registerPlacements);
        event.enqueueWork(org.slavicmyths.world.LandEncounters::placements);
    }
}
