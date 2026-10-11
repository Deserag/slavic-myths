package org.slavicmyths.chud;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
public final class AbandonedStructures {
    public static final DeferredHolder<StructureType<?>,StructureType<AbandonedStructure>> TYPE=SwampStructures.STRUCTURES.register("chud_abandoned",()->()->AbandonedStructure.CODEC);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("chud_abandoned_v2",()->(context,tag)->new AbandonedPiece(tag));
    public static void init(){}
    private AbandonedStructures(){}
}
