package org.slavicmyths.morovoy;

import java.util.List;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;

/** Rare native, seed-driven and /locate-compatible Nether arena. */
public final class MorovoyArenaStructure extends Structure {
 public static final MapCodec<MorovoyArenaStructure> CODEC=simpleCodec(MorovoyArenaStructure::new);
 public MorovoyArenaStructure(StructureSettings settings){super(settings);}
 @Override public StructureType<?> type(){return MorovoyArenaWorldgen.STRUCTURE.get();}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
  MorovoyArenaTerrain.Plan plan=MorovoyArenaTerrain.find(c);
  if(plan==null)return Optional.empty();
  MorovoyArenaPiece piece=new MorovoyArenaPiece(plan);
  List<StructurePiece> proposed=List.of(piece);
  if(!StructureCoverageService.referencesFit(c.chunkPos(),proposed))return Optional.empty();
  if(!StructureCandidates.clearAccepted(c,c.chunkPos(),
   id->id==null||id.equals(MorovoyArenaWorldgen.id("morovoy_raven_arena"))?0:1,proposed))return Optional.empty();
  return Optional.of(new GenerationStub(plan.center(),builder->builder.addPiece(piece)));
 }
}
