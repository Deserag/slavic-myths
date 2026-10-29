package org.slavicmyths.netherstructures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
/** 2.1.2: decorative only. No items, entities or loot. */
public final class NetherBridgeWorldgen {
 public static final DeferredHolder<StructureType<?>,StructureType<NetherBridgeStructure>> STRUCTURE=
    SwampStructures.STRUCTURES.register("broken_nether_bridge",()->()->NetherBridgeStructure.CODEC);
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=
    SwampStructures.PIECES.register("broken_nether_bridge",()->(ctx,nbt)->new NetherBridgePiece(nbt));
 public static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths",name);}
 public static void init(){}
 private NetherBridgeWorldgen(){}
}
