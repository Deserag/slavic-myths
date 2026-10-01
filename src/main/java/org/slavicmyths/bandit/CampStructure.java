package org.slavicmyths.bandit;
import java.util.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.*;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
public final class CampStructure extends Structure<NoFeatureConfig> {
    public final boolean medium;
    CampStructure(boolean medium){super(NoFeatureConfig.CODEC);this.medium=medium;}
    public GenerationStage.Decoration step(){return GenerationStage.Decoration.SURFACE_STRUCTURES;}
    public IStartFactory<NoFeatureConfig> getStartFactory(){return Start::new;}
    @Override protected boolean isFeatureChunk(ChunkGenerator g,BiomeProvider biomes,long seed,SharedSeedRandom random,int x,int z,Biome biome,ChunkPos pos,NoFeatureConfig cfg){
        for(Map.Entry<Structure<?>,StructureSeparationSettings> entry:g.getSettings().structureConfig().entrySet()){
            Structure<?> other=entry.getKey();if(other==this||other instanceof CampStructure&&medium)continue;
            if(!(other instanceof LargeCampStructure)&&!(other instanceof CampStructure)&&!(other instanceof org.slavicmyths.swamp.SwampStructure)&&other!=Structure.VILLAGE&&other!=Structure.PILLAGER_OUTPOST&&other!=Structure.WOODLAND_MANSION&&other!=Structure.RUINED_PORTAL)continue;
            int radius=other==Structure.VILLAGE||other==Structure.WOODLAND_MANSION?10:4;Set<Long> seen=new HashSet<>();
            for(int a=x-radius;a<=x+radius;a+=radius)for(int b=z-radius;b<=z+radius;b+=radius){ChunkPos p=other.getPotentialFeatureChunk(entry.getValue(),seed,new SharedSeedRandom(),a,b);if(!seen.add(p.toLong())||Math.abs(p.x-x)>radius||Math.abs(p.z-z)>radius)continue;if(biomes.getNoiseBiome((p.x<<2)+2,16,(p.z<<2)+2).getGenerationSettings().isValidStart(other))return false;}
        }return true;
    }
    private static final class Start extends StructureStart<NoFeatureConfig>{
        Start(Structure<NoFeatureConfig> type,int x,int z,MutableBoundingBox box,int refs,long seed){super(type,x,z,box,refs,seed);}
        public BlockPos getLocatePos(){return new BlockPos((boundingBox.x0+boundingBox.x1)/2,boundingBox.y1+1,(boundingBox.z0+boundingBox.z1)/2);}
        public void generatePieces(DynamicRegistries registries,ChunkGenerator g,TemplateManager templates,int cx,int cz,Biome biome,NoFeatureConfig cfg){
            boolean medium=((CampStructure)getFeature()).medium;int x=cx<<4,z=cz<<4,total=medium?9:5;UUID camp=new UUID(random.nextLong(),random.nextLong());
            List<Object[]> plan=new ArrayList<>();
            if(medium){
                plan.add(new Object[]{"leader",18,1,0});plan.add(new Object[]{"sleeping",2,5,1});plan.add(new Object[]{"sleeping",4,24,3});
                plan.add(new Object[]{"storage_tent",28,5,5});plan.add(new Object[]{"senior",26,26,6});plan.add(new Object[]{"warehouse",33,16,-1});
                plan.add(new Object[]{"watch",1,16,8});plan.add(new Object[]{"canopy",15,28,-1});plan.add(new Object[]{"yard_medium",0,0,-1});
            }else{
                plan.add(new Object[]{"sleeping",1,1,0});plan.add(new Object[]{"storage_tent",15,3,2});plan.add(new Object[]{"senior",8,15,3});plan.add(new Object[]{"yard_small",0,0,-1});
                if(random.nextBoolean())plan.add(new Object[]{"cart",0,12,-1});
            }
            for(Object[] p:plan){String name=(String)p[0];int px=x+(Integer)p[1],pz=z+(Integer)p[2];BlockPos size=templates.getOrCreate(new ResourceLocation("slavicmyths","bandit/"+name)).getSize();if(size.equals(BlockPos.ZERO)){pieces.clear();return;}
                int min=255,max=0;for(int a=0;a<size.getX();a+=Math.max(1,(size.getX()-1)/2))for(int b=0;b<size.getZ();b+=Math.max(1,(size.getZ()-1)/2)){
                    int ground=g.getBaseHeight(px+a,pz+b,Heightmap.Type.OCEAN_FLOOR_WG),surface=g.getBaseHeight(px+a,pz+b,Heightmap.Type.WORLD_SURFACE_WG);
                    if(ground<surface||surface<55||surface>110){pieces.clear();return;}min=Math.min(min,ground);max=Math.max(max,ground);
                }
                if(max-min>(name.startsWith("yard")?7:3)){pieces.clear();return;}
                pieces.add(new CampPiece(templates,name,new BlockPos(px,max-1,pz),camp,total,(Integer)p[3]));
            }calculateBoundingBox();
        }
    }
}
