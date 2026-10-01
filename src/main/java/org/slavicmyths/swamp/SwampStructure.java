package org.slavicmyths.swamp;

import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import java.util.*;

public final class SwampStructure extends Structure<NoFeatureConfig> {
    final String id;
    SwampStructure(String id){super(NoFeatureConfig.CODEC);this.id=id;}
    @Override public GenerationStage.Decoration step(){return GenerationStage.Decoration.SURFACE_STRUCTURES;}
    @Override public IStartFactory<NoFeatureConfig> getStartFactory(){return Start::new;}
    @Override protected boolean isFeatureChunk(ChunkGenerator generator,BiomeProvider biomes,long seed,SharedSeedRandom random,int cx,int cz,Biome biome,ChunkPos pos,NoFeatureConfig config) {
        // Conservative candidate exclusion, bounded noise-only work, independent of generation
        // order. Lower-priority custom structures yield to higher-priority ones.
        List<String> priority=Arrays.asList("abandoned_settlement","flooded_shrine","fishing_camp","swamp_hut","underwater_ruins","bog_causeway","swamp_remnants");
        for(Map.Entry<Structure<?>,StructureSeparationSettings> e:generator.getSettings().structureConfig().entrySet()) {
            Structure<?> other=e.getKey();if(other==this)continue;
            boolean ours=other instanceof SwampStructure;
            if(ours&&priority.indexOf(((SwampStructure)other).id)>priority.indexOf(id))continue;
            if(!ours && other!=Structure.VILLAGE && other!=Structure.WOODLAND_MANSION && other!=Structure.PILLAGER_OUTPOST && other!=Structure.SWAMP_HUT && other!=Structure.RUINED_PORTAL && other!=Structure.OCEAN_MONUMENT && other!=Structure.OCEAN_RUIN && other!=Structure.SHIPWRECK)continue;
            int margin=other==Structure.VILLAGE||other==Structure.WOODLAND_MANSION?12:ours?5:6;
            Set<Long> seen=new HashSet<>();
            for(int x=cx-margin;x<=cx+margin;x+=margin)for(int z=cz-margin;z<=cz+margin;z+=margin) {
                ChunkPos candidate=other.getPotentialFeatureChunk(e.getValue(),seed,new SharedSeedRandom(),x,z);
                if(!seen.add(candidate.toLong())||Math.abs(candidate.x-cx)>margin||Math.abs(candidate.z-cz)>margin)continue;
                if(biomes.getNoiseBiome((candidate.x<<2)+2,16,(candidate.z<<2)+2).getGenerationSettings().isValidStart(other))return false;
            }
        }
        return true;
    }
    private static final class Start extends StructureStart<NoFeatureConfig> {
        Start(Structure<NoFeatureConfig> type,int x,int z,MutableBoundingBox box,int refs,long seed){super(type,x,z,box,refs,seed);}
        @Override public BlockPos getLocatePos() {
            // Vanilla's base implementation uses Y=0; report an actual landmark height.
            return new BlockPos((boundingBox.x0+boundingBox.x1)/2,boundingBox.y1+1,(boundingBox.z0+boundingBox.z1)/2);
        }
        @Override public void generatePieces(DynamicRegistries registries,ChunkGenerator generator,TemplateManager templates,int cx,int cz,Biome biome,NoFeatureConfig config) {
            String id=((SwampStructure)getFeature()).id;int x=(cx<<4),z=(cz<<4);int variant=random.nextInt(3);
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
                BlockPos size=templates.getOrCreate(new ResourceLocation("slavicmyths","swamp/"+p.name)).getSize();
                if(size.equals(BlockPos.ZERO)){pieces.clear();return;}
                int min=255,max=0,minSurface=255,maxSurface=0;
                for(int a=0;a<=size.getX();a+=Math.max(1,(size.getX()-1)/3))for(int b=0;b<=size.getZ();b+=Math.max(1,(size.getZ()-1)/3)) {
                    int px=x+p.x+Math.min(a,size.getX()-1),pz=z+p.z+Math.min(b,size.getZ()-1);
                    int ground=generator.getBaseHeight(px,pz,Heightmap.Type.OCEAN_FLOOR_WG);
                    int surface=generator.getBaseHeight(px,pz,Heightmap.Type.WORLD_SURFACE_WG);
                    min=Math.min(min,ground);max=Math.max(max,ground);minSurface=Math.min(minSurface,surface);maxSurface=Math.max(maxSurface,surface);
                }
                if(min<45||max>78||max-min>4||maxSurface-minSurface>4){pieces.clear();return;}
                int y=maxSurface-1;
                if((p.name.startsWith("hut_")||p.name.startsWith("settlement_"))&&!p.name.contains("paths")&&max-min>2){pieces.clear();return;}
                if(p.name.startsWith("camp_")) {
                    int land=generator.getBaseHeight(x+p.x+4,z+p.z+3,Heightmap.Type.OCEAN_FLOOR_WG);
                    int water=generator.getBaseHeight(x+p.x+6,z+p.z+14,Heightmap.Type.OCEAN_FLOOR_WG);
                    int waterSurface=generator.getBaseHeight(x+p.x+6,z+p.z+14,Heightmap.Type.WORLD_SURFACE_WG);
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
            calculateBoundingBox();
        }
    }
    private static final class Plan {
        final String name;final int x,z;final boolean wet;int y;
        Plan(String name,int x,int z,boolean wet){this.name=name;this.x=x;this.z=z;this.wet=wet;}
    }
}
