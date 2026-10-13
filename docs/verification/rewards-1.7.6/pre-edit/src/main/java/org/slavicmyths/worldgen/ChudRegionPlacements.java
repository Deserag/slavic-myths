package org.slavicmyths.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ChudRegionPlacements {
    public static final DeferredRegister<StructurePlacementType<?>> TYPES=DeferredRegister.create(Registries.STRUCTURE_PLACEMENT,"slavicmyths");
    public static final DeferredHolder<StructurePlacementType<?>,StructurePlacementType<ChudRegionPlacement>> TYPE=
        TYPES.register("chud_region",()->()->ChudRegionPlacement.CODEC);
    private ChudRegionPlacements() {}
}
