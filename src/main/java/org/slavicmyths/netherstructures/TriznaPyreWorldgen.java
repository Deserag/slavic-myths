package org.slavicmyths.netherstructures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
public final class TriznaPyreWorldgen {
    public static final DeferredHolder<StructureType<?>,StructureType<TriznaPyreStructure>> STRUCTURE=SwampStructures.STRUCTURES.register("trizna_pyre",()->()->TriznaPyreStructure.CODEC);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("trizna_pyre",()->(context,tag)->new TriznaPyrePiece(tag));
    public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
    public static void init(){}
    private TriznaPyreWorldgen(){}
}
