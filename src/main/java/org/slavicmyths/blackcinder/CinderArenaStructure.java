package org.slavicmyths.blackcinder;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.*;
public final class CinderArenaStructure extends Structure {
 public static final MapCodec<CinderArenaStructure> CODEC=simpleCodec(CinderArenaStructure::new);
 public CinderArenaStructure(StructureSettings s){super(s);}
 @Override public StructureType<?> type(){return CinderWorldgen.ARENA.get();}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
  int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();int y=CinderTerrain.height(c.seed(),x,z);var p=new BlockPos(x,y,z);
  // A full candidate is a 33x33 core inside the same biome; no random skip.
  for(int dx:new int[]{-16,0,16})for(int dz:new int[]{-16,0,16})if(!c.chunkGenerator().getBiomeSource().getNoiseBiome((x+dx)>>2,y>>2,(z+dz)>>2,c.randomState().sampler()).is(CinderWorldgen.BIOME))return Optional.empty();
  if(!c.chunkGenerator().getBaseColumn(x,z,c.heightAccessor(),c.randomState()).getBlock(31).getFluidState().isEmpty())return Optional.empty();
  var piece=new CinderArenaPiece(p,c.seed());
  if(!org.slavicmyths.worldgen.StructureCandidates.clearAccepted(c,c.chunkPos(),other->other!=null&&other.getNamespace().equals("minecraft")?8:0,java.util.List.of(piece)))return Optional.empty();
  return Optional.of(new GenerationStub(p,b->b.addPiece(piece)));
 }
}
