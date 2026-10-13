package org.slavicmyths.swamp;
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
public final class SwampStructure extends Structure {
 public final String id;
 public SwampStructure(StructureSettings settings,String id){super(settings);this.id=id;}
 public static MapCodec<SwampStructure> codec(String id){return simpleCodec(settings->new SwampStructure(settings,id));}
 @Override public StructureType<?> type(){return SwampStructures.TYPES.get(id).get();}
 private boolean candidate(GenerationContext c,ChunkPos at,List<StructurePiece> pieces){List<String> priority=Arrays.asList("underwater_ruins","abandoned_settlement","flooded_shrine","fishing_camp","swamp_hut","underwater_ruins","bog_causeway","swamp_remnants","swamp_watchtower");
 return StructureCandidates.clearAccepted(c,at,other->{if(other==null)return 0;boolean ours=other.getNamespace().equals("slavicmyths")&&priority.contains(other.getPath());if(ours){if(other.getPath().equals(id)||priority.indexOf(other.getPath())>priority.indexOf(id))return 0;return 5;}if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 12;return StructureCandidates.vanilla(other,"pillager_outpost","swamp_hut","ruined_portal","monument","ocean_ruin","shipwreck")?6:0;},pieces);}
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

            int x=anchor.getX(),z=anchor.getZ();int variant=random.nextInt(3);
            List<Plan> plan=new ArrayList<>();
            switch(id) {
                case "swamp_hut":plan.add(new Plan("v097_abandoned_"+variant,0,0,false));break;
                case "fishing_camp":plan.add(new Plan("v097_fisher_"+variant,0,0,false));break;
                case "bog_causeway":plan.add(new Plan("v097_boardwalk_"+variant,0,0,false));break;
                case "swamp_remnants":plan.add(new Plan("v097_landing_"+variant,0,0,false));break;
                case "flooded_shrine":plan.add(new Plan("v097_shrine_"+variant,0,0,false));break;
                case "abandoned_settlement":plan.add(new Plan("v097_cluster_"+variant,0,0,false));break;
                case "swamp_watchtower":plan.add(new Plan("v097_watchtower_"+variant,0,0,false));break;
                case "underwater_ruins":plan.add(new Plan("ruin_"+variant,0,0,true));break;
                default:return;
            }
            // Validate every piece before accepting any part of the start. Independent house
            // foundations follow terrain; no 50x50 flattening and no chunk access here.
            for(Plan p:plan) {
                net.minecraft.core.Vec3i size=templates.getOrCreate(ResourceLocation.fromNamespaceAndPath("slavicmyths","swamp/"+p.name)).getSize();
                if(size.equals(BlockPos.ZERO)){StructureCoverageService.note(Rejection.UNIQUE_STRUCTURE_RULE,"missingTemplate",1);pieces.clear();return;}
                int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE,minSurface=Integer.MAX_VALUE,maxSurface=Integer.MIN_VALUE,maxWater=0;
                for(int a=0;a<=size.getX();a+=Math.max(1,(size.getX()-1)/3))for(int b=0;b<=size.getZ();b+=Math.max(1,(size.getZ()-1)/3)) {
                    int px=x+p.x+Math.min(a,size.getX()-1),pz=z+p.z+Math.min(b,size.getZ()-1);
                    int ground=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(generator,px,pz,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
                    int surface=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(generator,px,pz,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
                    maxWater=Math.max(maxWater,surface-ground);min=Math.min(min,ground);max=Math.max(max,ground);minSurface=Math.min(minSurface,surface);maxSurface=Math.max(maxSurface,surface);
                }
                var profile=org.slavicmyths.worldgen.StructureCoverageService.profile(id);
                var decision=org.slavicmyths.worldgen.StructureCoverageService.terrain(profile,min,max,maxWater,context.heightAccessor().getMinBuildHeight(),context.heightAccessor().getMaxBuildHeight(),size.getY(),(p.wet?max-min:maxSurface-min)*size.getX()*size.getZ());
                StructureCoverageService.note(decision);
                if(!decision.accepted()){pieces.clear();return;}
                // Wet templates retain their finite pier/support geometry; dry slopes are terraced below.
                if(maxWater>0&&maxSurface-minSurface>profile.slope()){StructureCoverageService.note(Rejection.DEEP_WATER,"mixedWaterBankRelief",maxSurface-minSurface);pieces.clear();return;}
                int y=maxSurface-1;

                if(p.name.startsWith("camp_")) {
                    int land=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(generator,x+p.x+4,z+p.z+3,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
                    int water=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(generator,x+p.x+6,z+p.z+14,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
                    int waterSurface=org.slavicmyths.worldgen.StructureCoverageService.baseHeight(generator,x+p.x+6,z+p.z+14,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
                    if(land<waterSurface-1||land>waterSurface+1||waterSurface-water<2){pieces.clear();return;}
                    y=waterSurface-1;
                }
                if(p.name.startsWith("poi_")&&!p.name.equals("poi_2")&&(max-min>1||min<maxSurface-1)){pieces.clear();return;}
                if(p.wet) {
                    int depth=minSurface-max;
                    if(id.equals("underwater_ruins")) {
                        if(depth<(variant==1?5:4)||maxSurface-min>profile.water()){StructureCoverageService.note(depth<(variant==1?5:4)?Rejection.UNIQUE_STRUCTURE_RULE:Rejection.DEEP_WATER,"ruinWaterDepth",depth);pieces.clear();return;}
                        y=max-1;
                    } else {
                        if(depth<1||maxSurface-min>7){pieces.clear();return;}
                        y=minSurface-5;
                    }
                }
                p.y=y;p.dry=!p.wet&&maxWater==0;
            }
            for(Plan p:plan){var piece=new SwampPiece(templates,p.name,new BlockPos(x+p.x,p.y,z+p.z),random.nextLong());if(p.dry)pieces.add(new org.slavicmyths.worldgen.LandFoundationPiece(piece.getBoundingBox(),p.y,8,context.heightAccessor().getMinBuildHeight(),context.heightAccessor().getMaxBuildHeight()-1));pieces.add(piece);}


 }
 private static final class Plan{final String name;final int x,z;final boolean wet;boolean dry;int y;Plan(String name,int x,int z,boolean wet){this.name=name;this.x=x;this.z=z;this.wet=wet;}}
}
