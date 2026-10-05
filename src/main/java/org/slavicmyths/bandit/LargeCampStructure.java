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
public final class LargeCampStructure extends Structure {
 public LargeCampStructure(StructureSettings settings){super(settings);}
 public static final MapCodec<LargeCampStructure> CODEC=simpleCodec(settings->new LargeCampStructure(settings));
 @Override public StructureType<?> type(){return CampStructures.LARGE.get();}
 private boolean candidate(GenerationContext c){return StructureCandidates.clear(c,other->{if(other==null)return 0;if(other.getNamespace().equals("slavicmyths"))return org.slavicmyths.swamp.SwampStructures.TYPES.containsKey(other.getPath())?7:0;if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 12;return StructureCandidates.vanilla(other,"pillager_outpost","ruined_portal")?7:0;});}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
 if(!candidate(context))return Optional.empty();List<StructurePiece> pieces=new ArrayList<>();plan(context,pieces);if(pieces.isEmpty())return Optional.empty();
 BlockPos locate=pieces.getFirst().getBoundingBox().getCenter();return Optional.of(new GenerationStub(locate,builder->pieces.forEach(builder::addPiece)));
 }
 private void plan(GenerationContext context,List<StructurePiece> pieces){
 ChunkGenerator g=context.chunkGenerator(),generator=g;StructureTemplateManager templates=context.structureTemplateManager(),t=templates;int cx=context.chunkPos().x,cz=context.chunkPos().z;var random=context.random();

   int x=cx<<4,z=cz<<4;boolean hill=context.biomeSource().getNoiseBiome((cx<<2)+2,16,(cz<<2)+2,context.randomState().sampler()).is(net.neoforged.neoforge.common.Tags.Biomes.IS_MOUNTAIN)&&random.nextInt(3)==0;String family=hill?"hill":context.biomeSource().getNoiseBiome((cx<<2)+2,16,(cz<<2)+2,context.randomState().sampler()).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","worldgen/core_plains")))?"plains":"forest";
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
   for(Object[]part:plan){String name=(String)part[0];int px=x+(Integer)part[1],pz=z+(Integer)part[2];net.minecraft.core.Vec3i size=templates.getOrCreate(ResourceLocation.fromNamespaceAndPath("slavicmyths","stronghold/"+name)).getSize();if(size.equals(BlockPos.ZERO)){pieces.clear();return;}
    int min=255,max=0;for(int a=0;a<size.getX();a+=Math.max(1,(size.getX()-1)/3))for(int b=0;b<size.getZ();b+=Math.max(1,(size.getZ()-1)/3)){
     int floor=g.getBaseHeight(px+a,pz+b,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState()),top=g.getBaseHeight(px+a,pz+b,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());if(floor!=top||floor<61||floor>135){pieces.clear();return;}min=Math.min(min,floor);max=Math.max(max,floor);
    }
    boolean yard=name.startsWith("perimeter");if(max-min>(yard?(hill?12:8):3)){pieces.clear();return;}
    allMin=Math.min(allMin,min);allMax=Math.max(allMax,max);if(allMax-allMin>(hill?14:9)){pieces.clear();return;}
    int py=max-1;if(name.startsWith("cache"))py=floors.get(cacheType==0?"ataman_house":"warehouse")-5;
    else floors.put(name.startsWith("warehouse")?"warehouse":name,py);
    pieces.add(new LargeCampPiece(templates,name,new BlockPos(px,py,pz),id));
   }

 }
}
