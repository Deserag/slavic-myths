package org.slavicmyths.bandit;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
public final class CampStructures {
 public static final DeferredHolder<StructureType<?>,StructureType<CampStructure>> SMALL=SwampStructures.STRUCTURES.register("bandit_camp_small",()->()->CampStructure.codec(false));
 public static final DeferredHolder<StructureType<?>,StructureType<CampStructure>> MEDIUM=SwampStructures.STRUCTURES.register("bandit_camp_medium",()->()->CampStructure.codec(true));
 public static final DeferredHolder<StructureType<?>,StructureType<LargeCampStructure>> LARGE=SwampStructures.STRUCTURES.register("bandit_camp_large",()->()->LargeCampStructure.CODEC);
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("bandit_camp_piece",()->(context,tag)->new CampPiece(context.structureTemplateManager(),tag));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> LARGE_PIECE=SwampStructures.PIECES.register("large_camp_piece",()->(context,tag)->new LargeCampPiece(context.structureTemplateManager(),tag));
 public static void register(){}
 private CampStructures(){}
}
