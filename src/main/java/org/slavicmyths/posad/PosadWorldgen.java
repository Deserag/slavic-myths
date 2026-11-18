package org.slavicmyths.posad;

import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;

public final class PosadWorldgen {
    public static final DeferredHolder<StructureType<?>, StructureType<PosadStructure>> STRUCTURE =
        SwampStructures.STRUCTURES.register("nether_posad", () -> () -> PosadStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> PIECE =
        SwampStructures.PIECES.register("nether_posad", () -> (context, tag) -> new PosadPiece(tag));
    public static void init() {}
    private PosadWorldgen() {}
}
