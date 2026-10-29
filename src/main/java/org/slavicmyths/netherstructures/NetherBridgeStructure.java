package org.slavicmyths.netherstructures;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;
/** Deterministic native new-chunk structure; /locate cannot place or mutate terrain. */
public final class NetherBridgeStructure extends Structure {
 public static final MapCodec<NetherBridgeStructure> CODEC=simpleCodec(NetherBridgeStructure::new);
 public NetherBridgeStructure(StructureSettings s){super(s);}
 @Override public StructureType<?> type(){return NetherBridgeWorldgen.STRUCTURE.get();}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
  int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
  if(!biomes().contains(c.chunkGenerator().getBiomeSource().getNoiseBiome(x>>2,16,z>>2,c.randomState().sampler())))return Optional.empty();
  if(!StructureCandidates.clear(c,c.chunkPos(),id->
    id==null||id.equals(NetherBridgeWorldgen.id("broken_nether_bridge"))?0:
    id.getNamespace().equals("minecraft")?5:3))return Optional.empty();
  var plan=NetherBridgeTerrain.find(c);
  if(plan==null)return Optional.empty();
  var piece=new NetherBridgePiece(plan);
  if(!StructureCoverageService.referencesFit(c.chunkPos(),List.of(piece)))return Optional.empty();
  return Optional.of(new GenerationStub(plan.origin(),builder->builder.addPiece(piece)));
 }
}
