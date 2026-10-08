package org.slavicmyths.village;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.slavicmyths.registry.*;

public final class SettlementStorage {
    public static final DeferredHolder<Block,SettlementChestBlock> BLOCK=ModBlocks.BLOCKS.register("settlement_chest",SettlementChestBlock::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SettlementChest>> TILE=ModTiles.TILES.register("settlement_chest",()->BlockEntityType.Builder.of(SettlementChest::new,BLOCK.get()).build(null));
    public static final DeferredHolder<PoiType,PoiType> POI=VillageRoles.POIS.register("settlement_storage",()->new PoiType(java.util.Set.copyOf(BLOCK.get().getStateDefinition().getPossibleStates()),0,1));
    public static void init(IEventBus bus){
        ModItems.ITEMS.register("settlement_chest",()->new BlockItem(BLOCK.get(),new Item.Properties()));
        SettlementMenu.register();
        bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,TILE.get(),(tile,side)->new InvWrapper(tile)));
    }
    private SettlementStorage(){}
}
