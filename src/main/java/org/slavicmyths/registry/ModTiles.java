package org.slavicmyths.registry;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.ritual.AltarTileEntity;

public final class ModTiles {
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, "slavicmyths");
    public static final RegistryObject<TileEntityType<AltarTileEntity>> ALTAR = TILES.register("altar",
            () -> TileEntityType.Builder.of(AltarTileEntity::new, ModBlocks.ALTAR.get()).build(null));
    private ModTiles() { }
}
