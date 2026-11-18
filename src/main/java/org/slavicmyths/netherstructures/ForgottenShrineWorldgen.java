package org.slavicmyths.netherstructures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
public final class ForgottenShrineWorldgen {
    public static final DeferredHolder<StructureType<?>,StructureType<ForgottenShrineStructure>> STRUCTURE=SwampStructures.STRUCTURES.register("forgotten_shrine",()->()->ForgottenShrineStructure.CODEC);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("forgotten_shrine",()->(context,tag)->new ForgottenShrinePiece(tag));
    public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
    public static void init(){}
    private ForgottenShrineWorldgen(){}
}
