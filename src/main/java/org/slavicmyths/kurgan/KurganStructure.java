package org.slavicmyths.kurgan;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;
import org.slavicmyths.worldgen.StructureCoverageService.Rejection;
public final class KurganStructure extends Structure {
 public final int kind;
 public KurganStructure(StructureSettings settings,int kind){super(settings);this.kind=kind;}
 public static MapCodec<KurganStructure> codec(int kind){return simpleCodec(settings->new KurganStructure(settings,kind));}
 @Override public StructureType<?> type(){return KurganStructures.type(kind);}
 private boolean candidate(GenerationContext c,ChunkPos at,List<StructurePiece> pieces){
 if(!StructureCandidates.clearAccepted(c,at,other->{if(other==null||other.getNamespace().equals("slavicmyths")&&other.getPath().startsWith("kurgan_")||StructureCandidates.vanilla(other,"mineshaft","stronghold","buried_treasure"))return 0;return (KurganShape.RADIUS[kind]+96+15)/16;},pieces))return false;
 return true;}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
 try(var samples=org.slavicmyths.worldgen.StructureCoverageService.sampleTerrain(context)){
 KurganPlan layout;try{layout=KurganPlan.create(kind,context.random().nextLong());}catch(IllegalArgumentException invalid){StructureCoverageService.note(Rejection.UNIQUE_STRUCTURE_RULE,"layoutAttemptLimit",KurganPlan.ATTEMPTS);return Optional.empty();}
 for(BlockPos anchor:org.slavicmyths.worldgen.StructureCoverageService.localCandidates(context,this)){
  List<StructurePiece> pieces=new ArrayList<>();plan(context,pieces,anchor,layout);if(pieces.isEmpty())continue;
  if(!StructureCoverageService.referencesFit(context.chunkPos(),pieces)){StructureCoverageService.note(Rejection.UNIQUE_STRUCTURE_RULE,"nativeReferenceReachChunks",8);continue;}
  if(!candidate(context,new ChunkPos(anchor),pieces)){StructureCoverageService.note(Rejection.SPACING_CONFLICT,"acceptedNeighborOverlap",1);continue;}
  StructureCoverageService.note(Rejection.ACCEPTED,"pieces",pieces.size());
  BlockPos locate=anchor;return Optional.of(new GenerationStub(locate,builder->pieces.forEach(builder::addPiece)));
 }
 return Optional.empty();
 }
 }
 private void plan(GenerationContext context,List<StructurePiece> pieces,BlockPos anchor,KurganPlan plan){
 ChunkGenerator g=context.chunkGenerator(),generator=g;StructureTemplateManager templates=context.structureTemplateManager(),t=templates;int cx=anchor.getX()>>4,cz=anchor.getZ()>>4;var random=context.random();

   int x=anchor.getX()+8,z=anchor.getZ()+8,radius=new int[]{8,12,18}[kind],low=Integer.MAX_VALUE,high=Integer.MIN_VALUE;int wetSamples=0,totalSamples=0;java.util.List<Integer> sampledHeights=new java.util.ArrayList<>();
   for(int dx=-radius;dx<=radius;dx+=2)for(int dz=-radius;dz<=radius;dz+=2){if(dx*dx+dz*dz>radius*radius)continue;int floor=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(g,x+dx,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState()),top=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(g,x+dx,z+dz,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());totalSamples++;if(floor!=top)wetSamples++;if(floor<=context.heightAccessor().getMinBuildHeight()||floor>=context.heightAccessor().getMaxBuildHeight()-32){StructureCoverageService.note(Rejection.WORLD_BOUNDS,"floor",floor);return;}low=Math.min(low,floor);high=Math.max(high,floor);sampledHeights.add(floor);}
   if(wetSamples>totalSamples/8){StructureCoverageService.note(Rejection.DEEP_WATER,"wetSamples",wetSamples);return;}
   try {
    java.util.Collections.sort(sampledHeights);BlockPos origin=new BlockPos(x,sampledHeights.get(sampledHeights.size()/2)-1,z);
    KurganEarthwork.Site site=KurganEarthwork.plan(g,context.randomState(),context.heightAccessor(),plan,origin);
    if(site==null)return;
    pieces.add(new KurganDungeonPiece(plan,site.origin(),new UUID(random.nextLong(),random.nextLong()),site.earthwork()));
   }catch(IllegalArgumentException invalid){StructureCoverageService.note(Rejection.UNIQUE_STRUCTURE_RULE,"layoutAttemptLimit",KurganPlan.ATTEMPTS);}

 }
 public static boolean terrainValid(ChunkGenerator g,KurganPlan plan,BlockPos origin,net.minecraft.world.level.LevelHeightAccessor height,net.minecraft.world.level.levelgen.RandomState randomState){
  KurganPlan.Box bounds=plan.bounds();if(origin.getY()+bounds.y0<height.getMinBuildHeight()+2||origin.getY()+bounds.y1>=height.getMaxBuildHeight()-6)return false;
  for(KurganPlan.Room room:plan.rooms){if(plan.formatVersion==2&&room.archetype==KurganPlan.Archetype.VESTIBULE)continue;KurganPlan.Box b=room.box();for(int x=b.x0;x<=b.x1;x+=2)for(int z=b.z0;z<=b.z1;z+=2){int ground=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(g,origin.getX()+x,origin.getZ()+z,Heightmap.Types.OCEAN_FLOOR_WG,height,randomState);if(ground<origin.getY()+b.y1+3)return false;}}
  return true;
 }
}
