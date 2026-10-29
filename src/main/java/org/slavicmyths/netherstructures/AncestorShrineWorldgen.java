package org.slavicmyths.netherstructures;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;

/** Native 2.1.5 structure: NO new items, blocks, mobs, dimensions or rituals. */
public final class AncestorShrineWorldgen {
    public static final DeferredHolder<StructureType<?>, StructureType<AncestorShrineStructure>> STRUCTURE =
        SwampStructures.STRUCTURES.register("ancestor_shrine",()->()->AncestorShrineStructure.CODEC);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE =
        SwampStructures.PIECES.register("ancestor_shrine",()->(context,tag)->new AncestorShrinePiece(tag));
    public static ResourceLocation id(String path){
        return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);
    }
    public static void init(){}
    private AncestorShrineWorldgen(){}
}
