package org.slavicmyths.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import org.slavicmyths.ritual.AltarTileEntity;

public final class ModTiles {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "slavicmyths");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarTileEntity>> ALTAR = TILES.register("altar",
            () -> BlockEntityType.Builder.of(AltarTileEntity::new, ModBlocks.ALTAR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.slavicmyths.artifact.SkatertTile>> SKATERT = TILES.register("skatert",()->BlockEntityType.Builder.of(org.slavicmyths.artifact.SkatertTile::new,ModBlocks.SKATERT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.slavicmyths.water.FishingNetTile>> FISHING_NET=TILES.register("fishing_net",()->BlockEntityType.Builder.of(org.slavicmyths.water.FishingNetTile::new,ModBlocks.FISHING_NET.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.slavicmyths.depth.PoolStoneTile>> POOL_STONE=TILES.register("pool_stone",()->BlockEntityType.Builder.of(org.slavicmyths.depth.PoolStoneTile::new,ModBlocks.POOL_STONE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.slavicmyths.kurgan.BurialCoffinTile>> BURIAL_COFFIN=TILES.register("burial_coffin",()->BlockEntityType.Builder.of(org.slavicmyths.kurgan.BurialCoffinTile::new,ModBlocks.BURIAL_COFFIN.get()).build(null));
    private ModTiles() { }
}
