package org.slavicmyths.netherstructures;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;

/** 2.1.3: Nether-only native shelter using existing vanilla blocks and items. */
public final class WayfarerWorldgen {
    public static final DeferredHolder<StructureType<?>, StructureType<WayfarerStructure>> STRUCTURE =
        SwampStructures.STRUCTURES.register("wayfarer_camp", () -> () -> WayfarerStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> PIECE =
        SwampStructures.PIECES.register("wayfarer_camp", () -> (context, tag) -> new WayfarerPiece(tag));
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("slavicmyths", path);
    }
    public static void init() {}
    private WayfarerWorldgen() {}
}
