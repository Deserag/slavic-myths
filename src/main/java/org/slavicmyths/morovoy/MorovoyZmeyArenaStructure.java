package org.slavicmyths.morovoy;

import java.util.List;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;

/** Uses the existing #393 terrain/geometry while keeping the two boss anchors exclusive. */
public final class MorovoyZmeyArenaStructure extends Structure {
 public static final MapCodec<MorovoyZmeyArenaStructure> CODEC=simpleCodec(MorovoyZmeyArenaStructure::new);
 public MorovoyZmeyArenaStructure(StructureSettings settings){super(settings);}
 @Override public StructureType<?> type(){return MorovoyArenaWorldgen.ZMEY_STRUCTURE.get();}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
  MorovoyArenaTerrain.Plan plan=MorovoyArenaTerrain.find(context);
  if(plan==null)return Optional.empty();
  MorovoyArenaPiece piece=new MorovoyArenaPiece(plan,true);
  List<StructurePiece> parts=List.of(piece);
  if(!StructureCoverageService.referencesFit(context.chunkPos(),parts))return Optional.empty();
  if(!StructureCandidates.clearAccepted(context,context.chunkPos(),id->id==null||id.equals(MorovoyArenaWorldgen.id("morovoy_fire_zmey_arena"))?0:1,parts))return Optional.empty();
  return Optional.of(new GenerationStub(plan.center(),builder->builder.addPiece(piece)));
 }
}
