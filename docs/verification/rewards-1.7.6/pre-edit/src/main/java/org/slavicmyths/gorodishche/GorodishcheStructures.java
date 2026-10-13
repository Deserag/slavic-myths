package org.slavicmyths.gorodishche;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
public final class GorodishcheStructures {
 public static final DeferredHolder<StructureType<?>,StructureType<GorodishcheStructure>> TYPE=SwampStructures.STRUCTURES.register("gorodishche",()->()->GorodishcheStructure.CODEC);
 public static final DeferredHolder<StructurePieceType,StructurePieceType> BUILDING=SwampStructures.PIECES.register("gorodishche_building",()->(context,tag)->new CityBuildingPiece(context.structureTemplateManager(),tag));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> ROADS=SwampStructures.PIECES.register("gorodishche_roads",()->(context,tag)->new CityRoadPiece(tag));
 public static void init(){}
 private GorodishcheStructures(){}
}
