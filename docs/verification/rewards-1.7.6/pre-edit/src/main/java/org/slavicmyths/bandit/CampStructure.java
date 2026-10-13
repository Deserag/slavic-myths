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
public final class CampStructure extends Structure {
 public final boolean medium;
 public CampStructure(StructureSettings settings,boolean medium){super(settings);this.medium=medium;}
 public static MapCodec<CampStructure> codec(boolean medium){return simpleCodec(settings->new CampStructure(settings,medium));}
 @Override public StructureType<?> type(){return medium?CampStructures.MEDIUM.get():CampStructures.SMALL.get();}
 private boolean candidate(GenerationContext c,ChunkPos at,List<StructurePiece> pieces){return StructureCandidates.clearAccepted(c,at,other->{if(other==null)return 0;if(other.getNamespace().equals("slavicmyths")){String id=other.getPath();if(id.equals(medium?"bandit_camp_medium":"bandit_camp_small")||medium&&id.equals("bandit_camp_small"))return 0;if(id.startsWith("bandit_camp_")||org.slavicmyths.swamp.SwampStructures.TYPES.containsKey(id))return 4;return 0;}if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 10;return StructureCandidates.vanilla(other,"pillager_outpost","ruined_portal")?4:0;},pieces);}
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

            int x=anchor.getX(),z=anchor.getZ(),total=medium?9:5;UUID camp=new UUID(random.nextLong(),random.nextLong());
            List<Object[]> plan=new ArrayList<>();
            if(medium){
                plan.add(new Object[]{"leader",18,1,0});plan.add(new Object[]{"sleeping",2,5,1});plan.add(new Object[]{"sleeping",4,24,3});
                plan.add(new Object[]{"storage_tent",28,5,5});plan.add(new Object[]{"senior",26,26,6});plan.add(new Object[]{"warehouse",33,16,-1});
                plan.add(new Object[]{"watch",1,16,8});plan.add(new Object[]{"canopy",15,28,-1});plan.add(new Object[]{"yard_medium",0,0,-1});
            }else{
                plan.add(new Object[]{"sleeping",1,1,0});plan.add(new Object[]{"storage_tent",15,3,2});plan.add(new Object[]{"senior",8,15,3});plan.add(new Object[]{"yard_small",0,0,-1});
                if(random.nextBoolean())plan.add(new Object[]{"cart",0,12,-1});
            }
   java.util.List<Integer> terraceHeights=new java.util.ArrayList<>();int extent=medium?44:26;
   for(int a=0;a<=extent;a+=8)for(int b=0;b<=extent;b+=8)terraceHeights.add(StructureCoverageService.baseHeight(g,x+a,z+b,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState())-1);
   java.util.Collections.sort(terraceHeights);int constructionY=Math.max(terraceHeights.get(terraceHeights.size()/2),context.heightAccessor().getMinBuildHeight()+7);
            for(Object[] p:plan){String name=(String)p[0];int px=x+(Integer)p[1],pz=z+(Integer)p[2];net.minecraft.core.Vec3i size=templates.getOrCreate(ResourceLocation.fromNamespaceAndPath("slavicmyths","bandit/"+name)).getSize();if(size.equals(BlockPos.ZERO)){StructureCoverageService.note(Rejection.UNIQUE_STRUCTURE_RULE,"missingTemplate",1);pieces.clear();return;}
                int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;for(int a=0;a<size.getX();a+=Math.max(1,(size.getX()-1)/2))for(int b=0;b<size.getZ();b+=Math.max(1,(size.getZ()-1)/2)){
                    int ground=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(g,px+a,pz+b,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState()),surface=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(g,px+a,pz+b,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
                    if(ground<surface||surface<=context.heightAccessor().getMinBuildHeight()||surface+size.getY()>=context.heightAccessor().getMaxBuildHeight()){StructureCoverageService.note(ground<surface?Rejection.DEEP_WATER:Rejection.WORLD_BOUNDS,"surface",surface);pieces.clear();return;}min=Math.min(min,ground);max=Math.max(max,ground);
                }
                var piece=new CampPiece(templates,name,new BlockPos(px,constructionY,pz),camp,total,(Integer)p[3]);piece.preparedTerrain=true;pieces.add(piece);
            }

   org.slavicmyths.worldgen.LandFoundationPiece.prepend(pieces,constructionY,context.heightAccessor().getMinBuildHeight(),context.heightAccessor().getMaxBuildHeight()-1);

 }
}
