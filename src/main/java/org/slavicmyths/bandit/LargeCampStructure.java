package org.slavicmyths.bandit;
import java.util.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.*;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
public final class LargeCampStructure extends Structure<NoFeatureConfig> {
 public LargeCampStructure(){super(NoFeatureConfig.CODEC);}
 @Override public GenerationStage.Decoration step(){return GenerationStage.Decoration.SURFACE_STRUCTURES;}
 @Override public IStartFactory<NoFeatureConfig> getStartFactory(){return Start::new;}
 @Override protected boolean isFeatureChunk(ChunkGenerator g,BiomeProvider biomes,long seed,SharedSeedRandom r,int x,int z,Biome biome,ChunkPos chunk,NoFeatureConfig c){
  for(Map.Entry<Structure<?>,StructureSeparationSettings>e:g.getSettings().structureConfig().entrySet()){
   Structure<?>type=e.getKey();if(type==this||type instanceof CampStructure)continue;
   if(!(type instanceof org.slavicmyths.swamp.SwampStructure)&&type!=Structure.VILLAGE&&type!=Structure.WOODLAND_MANSION&&type!=Structure.PILLAGER_OUTPOST&&type!=Structure.RUINED_PORTAL)continue;
   int radius=type==Structure.VILLAGE||type==Structure.WOODLAND_MANSION?12:7;
   for(int dx=-radius;dx<=radius;dx+=radius)for(int dz=-radius;dz<=radius;dz+=radius){ChunkPos q=type.getPotentialFeatureChunk(e.getValue(),seed,new SharedSeedRandom(),x+dx,z+dz);if(Math.abs(q.x-x)<=radius&&Math.abs(q.z-z)<=radius&&biomes.getNoiseBiome((q.x<<2)+2,16,(q.z<<2)+2).getGenerationSettings().isValidStart(type))return false;}
  }return true;
 }
 private static final class Start extends StructureStart<NoFeatureConfig>{
  Start(Structure<NoFeatureConfig>type,int x,int z,MutableBoundingBox box,int refs,long seed){super(type,x,z,box,refs,seed);}
  @Override public BlockPos getLocatePos(){return new BlockPos((boundingBox.x0+boundingBox.x1)/2,boundingBox.y1+1,(boundingBox.z0+boundingBox.z1)/2);}
  @Override public void generatePieces(DynamicRegistries registries,ChunkGenerator g,TemplateManager templates,int cx,int cz,Biome biome,NoFeatureConfig cfg){
   int x=cx<<4,z=cz<<4;boolean hill=biome.getDepth()>.3F&&random.nextInt(3)==0;String family=hill?"hill":biome.getBiomeCategory()==Biome.Category.PLAINS?"plains":"forest";
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
   UUID id=new UUID(random.nextLong(),random.nextLong());int allMin=255,allMax=0;Map<String,Integer> floors=new HashMap<>();
   for(Object[]part:plan){String name=(String)part[0];int px=x+(Integer)part[1],pz=z+(Integer)part[2];BlockPos size=templates.getOrCreate(new ResourceLocation("slavicmyths","stronghold/"+name)).getSize();if(size.equals(BlockPos.ZERO)){pieces.clear();return;}
    int min=255,max=0;for(int a=0;a<size.getX();a+=Math.max(1,(size.getX()-1)/3))for(int b=0;b<size.getZ();b+=Math.max(1,(size.getZ()-1)/3)){
     int floor=g.getBaseHeight(px+a,pz+b,Heightmap.Type.OCEAN_FLOOR_WG),top=g.getBaseHeight(px+a,pz+b,Heightmap.Type.WORLD_SURFACE_WG);if(floor!=top||floor<61||floor>135){pieces.clear();return;}min=Math.min(min,floor);max=Math.max(max,floor);
    }
    boolean yard=name.startsWith("perimeter");if(max-min>(yard?(hill?12:8):3)){pieces.clear();return;}
    allMin=Math.min(allMin,min);allMax=Math.max(allMax,max);if(allMax-allMin>(hill?14:9)){pieces.clear();return;}
    int py=max-1;if(name.startsWith("cache"))py=floors.get(cacheType==0?"ataman_house":"warehouse")-5;
    else floors.put(name.startsWith("warehouse")?"warehouse":name,py);
    pieces.add(new LargeCampPiece(templates,name,new BlockPos(px,py,pz),id));
   }calculateBoundingBox();
  }
 }
}
