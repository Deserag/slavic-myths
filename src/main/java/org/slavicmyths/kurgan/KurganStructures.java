package org.slavicmyths.kurgan;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
public final class KurganStructures {
 public static final DeferredHolder<StructureType<?>,StructureType<KurganStructure>> SMALL=SwampStructures.STRUCTURES.register("kurgan_small",()->()->KurganStructure.codec(0));
 public static final DeferredHolder<StructureType<?>,StructureType<KurganStructure>> WARRIOR=SwampStructures.STRUCTURES.register("kurgan_warrior",()->()->KurganStructure.codec(1));
 public static final DeferredHolder<StructureType<?>,StructureType<KurganStructure>> GREAT=SwampStructures.STRUCTURES.register("kurgan_great",()->()->KurganStructure.codec(2));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("kurgan_shell",()->(context,tag)->new KurganPiece(context.structureTemplateManager(),tag));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> DUNGEON=SwampStructures.PIECES.register("kurgan_dungeon",()->(context,tag)->new KurganDungeonPiece(context.structureTemplateManager(),tag));
 public static void register(){}
 public static StructureType<KurganStructure> type(int i){return i==0?SMALL.get():i==1?WARRIOR.get():GREAT.get();}
 public static String id(int i){return i==0?"kurgan_small":i==1?"kurgan_warrior":"kurgan_great";}
 private KurganStructures(){}
}
