package org.slavicmyths.chud;

import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;

public final class ChudStructures {
    public static final DeferredHolder<StructureType<?>,StructureType<ChudStructure>> TYPE=SwampStructures.STRUCTURES.register("chud_city",()->()->ChudStructure.CODEC);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("chud_city_v1",()->(context,tag)->new ChudPiece(tag));
    public static final DeferredHolder<StructurePieceType,StructurePieceType> ACCESS=SwampStructures.PIECES.register("chud_surface_access",()->(context,tag)->new ChudAccessPiece(tag));
    public static void init() {AbandonedStructures.init();}
    private ChudStructures() {}
}
