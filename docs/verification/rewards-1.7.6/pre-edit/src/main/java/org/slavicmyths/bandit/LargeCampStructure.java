package org.slavicmyths.bandit;
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
public final class LargeCampStructure extends Structure {
 public LargeCampStructure(StructureSettings settings){super(settings);}
 public static final MapCodec<LargeCampStructure> CODEC=simpleCodec(settings->new LargeCampStructure(settings));
 @Override public StructureType<?> type(){return CampStructures.LARGE.get();}
 private boolean candidate(GenerationContext c,ChunkPos at,List<StructurePiece> pieces){return StructureCandidates.clearAccepted(c,at,other->{if(other==null)return 0;if(other.getNamespace().equals("slavicmyths"))return org.slavicmyths.swamp.SwampStructures.TYPES.containsKey(other.getPath())?7:0;if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 12;return StructureCandidates.vanilla(other,"pillager_outpost","ruined_portal")?7:0;},pieces);}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
 try(var samples=org.slavicmyths.worldgen.StructureCoverageService.sampleTerrain(context)){
 for(BlockPos anchor:org.slavicmyths.worldgen.StructureCoverageService.localCandidates(context,this)){
  List<StructurePiece> pieces=new ArrayList<>();plan(context,pieces,anchor);if(pieces.isEmpty())continue;
  if(!StructureCoverageService.referencesFit(context.chunkPos(),pieces)){StructureCoverageService.note(Rejection.UNIQUE_STRUCTURE_RULE,"nativeReferenceReachChunks",8);continue;}
  if(!candidate(context,new ChunkPos(anchor),pieces)){StructureCoverageService.note(Rejection.SPACING_CONFLICT,"acceptedNeighborOverlap",1);continue;}
  StructureCoverageService.note(Rejection.ACCEPTED,"pieces",pieces.size());
  BlockPos locate=anchor;return Optional.of(new GenerationStub(locate,builder->pieces.forEach(builder::addPiece)));
 }
 return Optional.empty();
 }
 }
 private void plan(GenerationContext context,List<StructurePiece> pieces,BlockPos anchor){
 ChunkGenerator g=context.chunkGenerator(),generator=g;StructureTemplateManager templates=context.structureTemplateManager(),t=templates;int cx=anchor.getX()>>4,cz=anchor.getZ()>>4;var random=context.random();

   int x=anchor.getX(),z=anchor.getZ();boolean hill=context.biomeSource().getNoiseBiome((cx<<2)+2,16,(cz<<2)+2,context.randomState().sampler()).is(net.neoforged.neoforge.common.Tags.Biomes.IS_MOUNTAIN)&&random.nextInt(3)==0;String family=hill?"hill":context.biomeSource().getNoiseBiome((cx<<2)+2,16,(cz<<2)+2,context.randomState().sampler()).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","worldgen/core_plains")))?"plains":"forest";
   int shift=family.equals("plains")?3:family.equals("hill")?-2:0;
   List<Object[]>plan=new ArrayList<>();
   plan.add(new Object[]{"gate",34,2});plan.add(new Object[]{random.nextBoolean()?"barracks_porch":"barracks_side",8+shift,16});
   plan.add(new Object[]{random.nextBoolean()?"forge_open":"forge_closed",25+shift,16});
   plan.add(new Object[]{random.nextBoolean()?"warehouse_long":"warehouse_square",8+shift,33});
   plan.add(new Object[]{"kitchen",25+shift,30});plan.add(new Object[]{"ataman_house",40,15});
   plan.add(new Object[]{"stable",59,15});plan.add(new Object[]{"prison",60,29});plan.add(new Object[]{"commander",23,49});
   plan.add(new Object[]{"utility",9,50});plan.add(new Object[]{"central_yard",38,29});plan.add(new Object[]{"nightingale_yard",40,43});
   plan.add(new Object[]{"tower_roof",4,5});plan.add(new Object[]{"tower_open",69,5});plan.add(new Object[]{"tower_tall",8,66});
   plan.add(new Object[]{"perimeter_"+family,0,0});
   boolean cache=random.nextFloat()<.35F;int cacheType=random.nextInt(2);
   if(cache)plan.add(new Object[]{"cache_"+cacheType,cacheType==0?43:11+shift,cacheType==0?18:36});
   UUID id=new UUID(random.nextLong(),random.nextLong());int allMin=Integer.MAX_VALUE,allMax=Integer.MIN_VALUE;Map<String,Integer> floors=new HashMap<>();
   java.util.List<Integer> terraceHeights=new java.util.ArrayList<>();int extent=84;
   for(int a=0;a<=extent;a+=8)for(int b=0;b<=extent;b+=8)terraceHeights.add(StructureCoverageService.baseHeight(g,x+a,z+b,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState())-1);
   java.util.Collections.sort(terraceHeights);int constructionY=Math.max(terraceHeights.get(terraceHeights.size()/2),context.heightAccessor().getMinBuildHeight()+7);
   for(Object[]part:plan){String name=(String)part[0];int px=x+(Integer)part[1],pz=z+(Integer)part[2];net.minecraft.core.Vec3i size=templates.getOrCreate(ResourceLocation.fromNamespaceAndPath("slavicmyths","stronghold/"+name)).getSize();if(size.equals(BlockPos.ZERO)){StructureCoverageService.note(Rejection.UNIQUE_STRUCTURE_RULE,"missingTemplate",1);pieces.clear();return;}
    int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;for(int a=0;a<size.getX();a+=Math.max(1,(size.getX()-1)/3))for(int b=0;b<size.getZ();b+=Math.max(1,(size.getZ()-1)/3)){
     int floor=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(g,px+a,pz+b,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState()),top=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(g,px+a,pz+b,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());if(floor!=top||floor<=context.heightAccessor().getMinBuildHeight()||floor+size.getY()>=context.heightAccessor().getMaxBuildHeight()){StructureCoverageService.note(floor!=top?Rejection.DEEP_WATER:Rejection.WORLD_BOUNDS,"surface",top);pieces.clear();return;}min=Math.min(min,floor);max=Math.max(max,floor);
    }
    int py=constructionY;
    if(name.startsWith("cache"))py=floors.get(cacheType==0?"ataman_house":"warehouse")-5;
    else floors.put(name.startsWith("warehouse")?"warehouse":name,py);
    if(py<=context.heightAccessor().getMinBuildHeight()||py+size.getY()>=context.heightAccessor().getMaxBuildHeight()){StructureCoverageService.note(Rejection.WORLD_BOUNDS,"pieceFloorY",py);pieces.clear();return;}
    var piece=new LargeCampPiece(templates,name,new BlockPos(px,py,pz),id);piece.preparedTerrain=true;pieces.add(piece);
   }

   org.slavicmyths.worldgen.LandFoundationPiece.prepend(pieces,constructionY,context.heightAccessor().getMinBuildHeight(),context.heightAccessor().getMaxBuildHeight()-1);

 }
}
