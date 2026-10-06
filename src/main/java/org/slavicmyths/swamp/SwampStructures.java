package org.slavicmyths.swamp;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
public final class SwampStructures {
 public static final DeferredRegister<StructureType<?>> STRUCTURES=DeferredRegister.create(Registries.STRUCTURE_TYPE,"slavicmyths");
 public static final DeferredRegister<StructurePieceType> PIECES=DeferredRegister.create(Registries.STRUCTURE_PIECE,"slavicmyths");
 public static final String[] IDS={"swamp_hut","abandoned_settlement","bog_causeway","flooded_shrine","fishing_camp","underwater_ruins","swamp_remnants","swamp_watchtower"};
 public static final Map<String,DeferredHolder<StructureType<?>,StructureType<SwampStructure>>> TYPES=new LinkedHashMap<>();
 public static final DeferredHolder<StructurePieceType,StructurePieceType> LAND_FOUNDATION=PIECES.register("land_foundation",()->(context,tag)->new org.slavicmyths.worldgen.LandFoundationPiece(tag));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=PIECES.register("swamp_piece",()->(context,tag)->new SwampPiece(context.structureTemplateManager(),tag));
 static{for(String id:IDS)TYPES.put(id,STRUCTURES.register(id,()->()->SwampStructure.codec(id)));}
 public static Structure get(net.minecraft.server.level.ServerLevel world,String id){return world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
 public static boolean occupied(net.minecraft.world.level.WorldGenLevel world,net.minecraft.core.BlockPos pos){
 if(!(world instanceof net.minecraft.server.level.WorldGenRegion region))return false;
 var manager=world.getLevel().structureManager().forWorldGenRegion(region);
 for(String id:IDS){Structure structure=get(world.getLevel(),id);if(structure!=null&&manager.startsForStructure(net.minecraft.core.SectionPos.of(pos),structure).stream().anyMatch(net.minecraft.world.level.levelgen.structure.StructureStart::isValid))return true;}return false;
 }
 private SwampStructures(){}
}
