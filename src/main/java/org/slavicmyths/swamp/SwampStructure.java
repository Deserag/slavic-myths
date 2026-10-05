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
public final class SwampStructure extends Structure {
 public final String id;
 public SwampStructure(StructureSettings settings,String id){super(settings);this.id=id;}
 public static MapCodec<SwampStructure> codec(String id){return simpleCodec(settings->new SwampStructure(settings,id));}
 @Override public StructureType<?> type(){return SwampStructures.TYPES.get(id).get();}
 private boolean candidate(GenerationContext c){List<String> priority=Arrays.asList("abandoned_settlement","flooded_shrine","fishing_camp","swamp_hut","underwater_ruins","bog_causeway","swamp_remnants");
 return StructureCandidates.clear(c,other->{if(other==null)return 0;boolean ours=other.getNamespace().equals("slavicmyths")&&priority.contains(other.getPath());if(ours){if(other.getPath().equals(id)||priority.indexOf(other.getPath())>priority.indexOf(id))return 0;return 5;}if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 12;return StructureCandidates.vanilla(other,"pillager_outpost","swamp_hut","ruined_portal","monument","ocean_ruin","shipwreck")?6:0;});}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
 if(!candidate(context))return Optional.empty();List<StructurePiece> pieces=new ArrayList<>();plan(context,pieces);if(pieces.isEmpty())return Optional.empty();
 BlockPos locate=pieces.getFirst().getBoundingBox().getCenter();return Optional.of(new GenerationStub(locate,builder->pieces.forEach(builder::addPiece)));
 }
 private void plan(GenerationContext context,List<StructurePiece> pieces){
 ChunkGenerator g=context.chunkGenerator(),generator=g;StructureTemplateManager templates=context.structureTemplateManager(),t=templates;int cx=context.chunkPos().x,cz=context.chunkPos().z;var random=context.random();

            int x=(cx<<4),z=(cz<<4);int variant=random.nextInt(3);
            List<Plan> plan=new ArrayList<>();
            switch(id) {
                case "swamp_hut": int decay=random.nextInt(10);plan.add(new Plan("hut_"+(decay<3?0:decay<9?1:2),0,0,false));break;
                case "bog_causeway": plan.add(new Plan("causeway_"+(random.nextInt(5)==0?1:0),0,0,false));break;
                case "abandoned_settlement":
                    boolean second=random.nextBoolean();
                    plan.add(new Plan("settlement_well",17,17,false));
                    plan.add(new Plan("settlement_small",11,0,false));
                    plan.add(new Plan("settlement_main",30,13,false));
                    if(!second)plan.add(new Plan("settlement_flooded",17,32,false));
                    plan.add(new Plan("settlement_barn",0,18,false));
                    plan.add(new Plan("settlement_paths_"+(second?1:0),0,0,false));break;
                case "flooded_shrine":plan.add(new Plan("shrine_"+(variant%2),0,0,true));break;
                case "fishing_camp":plan.add(new Plan("camp_"+(variant%2),0,0,false));break;
                case "underwater_ruins":plan.add(new Plan("ruin_"+variant,0,0,true));break;
                default:plan.add(new Plan("poi_"+random.nextInt(4),0,0,false));
            }
            // Validate every piece before accepting any part of the start. Independent house
            // foundations follow terrain; no 50x50 flattening and no chunk access here.
            for(Plan p:plan) {
                net.minecraft.core.Vec3i size=templates.getOrCreate(ResourceLocation.fromNamespaceAndPath("slavicmyths","swamp/"+p.name)).getSize();
                if(size.equals(BlockPos.ZERO)){pieces.clear();return;}
                int min=255,max=0,minSurface=255,maxSurface=0;
                for(int a=0;a<=size.getX();a+=Math.max(1,(size.getX()-1)/3))for(int b=0;b<=size.getZ();b+=Math.max(1,(size.getZ()-1)/3)) {
                    int px=x+p.x+Math.min(a,size.getX()-1),pz=z+p.z+Math.min(b,size.getZ()-1);
                    int ground=generator.getBaseHeight(px,pz,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
                    int surface=generator.getBaseHeight(px,pz,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
                    min=Math.min(min,ground);max=Math.max(max,ground);minSurface=Math.min(minSurface,surface);maxSurface=Math.max(maxSurface,surface);
                }
                if(min<45||max>78||max-min>4||maxSurface-minSurface>4){pieces.clear();return;}
                int y=maxSurface-1;
                if((p.name.startsWith("hut_")||p.name.startsWith("settlement_"))&&!p.name.contains("paths")&&max-min>2){pieces.clear();return;}
                if(p.name.startsWith("camp_")) {
                    int land=generator.getBaseHeight(x+p.x+4,z+p.z+3,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
                    int water=generator.getBaseHeight(x+p.x+6,z+p.z+14,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
                    int waterSurface=generator.getBaseHeight(x+p.x+6,z+p.z+14,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
                    if(land<waterSurface-1||land>waterSurface+1||waterSurface-water<2){pieces.clear();return;}
                    y=waterSurface-1;
                }
                if(p.name.startsWith("poi_")&&!p.name.equals("poi_2")&&(max-min>1||min<maxSurface-1)){pieces.clear();return;}
                if(p.wet) {
                    int depth=minSurface-max;
                    if(id.equals("underwater_ruins")) {
                        if(depth<(variant==1?5:4)||maxSurface-min>10){pieces.clear();return;}
                        y=max-1;
                    } else {
                        if(depth<1||maxSurface-min>7){pieces.clear();return;}
                        y=minSurface-5;
                    }
                }
                p.y=y;
            }
            for(Plan p:plan)pieces.add(new SwampPiece(templates,p.name,new BlockPos(x+p.x,p.y,z+p.z),random.nextLong()));


 }
 private static final class Plan{final String name;final int x,z;final boolean wet;int y;Plan(String name,int x,int z,boolean wet){this.name=name;this.x=x;this.z=z;this.wet=wet;}}
}
