package org.slavicmyths.kurgan;
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
public final class KurganStructure extends Structure<NoFeatureConfig>{
 public final int kind;public KurganStructure(int k){super(NoFeatureConfig.CODEC);kind=k;}
 @Override public GenerationStage.Decoration step(){return GenerationStage.Decoration.SURFACE_STRUCTURES;}
 @Override public IStartFactory<NoFeatureConfig> getStartFactory(){return (t,x,z,b,r,s)->new Start(t,x,z,b,r,s,kind);}
 @Override protected boolean isFeatureChunk(ChunkGenerator g,BiomeProvider b,long seed,SharedSeedRandom random,int x,int z,Biome biome,ChunkPos chunk,NoFeatureConfig c){
  if(KurganShape.kind(seed,x,z)!=kind)return false;
  // Nearby major-structure candidates take precedence; no chunk loads for exclusions.
  for(Map.Entry<Structure<?>,StructureSeparationSettings>e:g.getSettings().structureConfig().entrySet()){
   Structure<?> s=e.getKey();if(s instanceof KurganStructure||s==Structure.MINESHAFT||s==Structure.STRONGHOLD||s==Structure.BURIED_TREASURE)continue;
   int clearance=(KurganShape.RADIUS[kind]+96+15)/16;
   for(int dx:new int[]{-clearance,0,clearance})for(int dz:new int[]{-clearance,0,clearance}){
    ChunkPos q=s.getPotentialFeatureChunk(e.getValue(),seed,new SharedSeedRandom(),x+dx,z+dz);
    if(Math.abs(q.x-x)<=clearance&&Math.abs(q.z-z)<=clearance&&b.getNoiseBiome((q.x<<2)+2,16,(q.z<<2)+2).getGenerationSettings().isValidStart(s))return false;
   }
  }
  // Great barrows cannot occupy immediately adjacent candidate regions.
  if(kind==2){StructureSeparationSettings cfg=g.getSettings().getConfig(this);for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){if(dx==0&&dz>=0||dx>0)continue;ChunkPos q=getPotentialFeatureChunk(cfg,seed,new SharedSeedRandom(),x+dx*64,z+dz*64);if(KurganShape.kind(seed,q.x,q.z)==2)return false;}}
  return true;
 }
 public static boolean terrainValid(ChunkGenerator g,KurganPlan plan,BlockPos origin){
  KurganPlan.Box bounds=plan.bounds();if(origin.getY()+bounds.y0<2||origin.getY()+bounds.y1>=250)return false;
  for(KurganPlan.Room room:plan.rooms){KurganPlan.Box b=room.box();for(int x=b.x0;x<=b.x1;x+=2)for(int z=b.z0;z<=b.z1;z+=2){int ground=g.getBaseHeight(origin.getX()+x,origin.getZ()+z,Heightmap.Type.OCEAN_FLOOR_WG);if(ground<origin.getY()+b.y1+3)return false;}}
  return true;
 }
 private static final class Start extends StructureStart<NoFeatureConfig>{
  private final int kind;Start(Structure<NoFeatureConfig> t,int x,int z,MutableBoundingBox b,int refs,long seed,int k){super(t,x,z,b,refs,seed);kind=k;}
  @Override public BlockPos getLocatePos(){for(StructurePiece piece:pieces)if(piece instanceof KurganDungeonPiece)return ((KurganDungeonPiece)piece).arrival();return new BlockPos((boundingBox.x0+boundingBox.x1)/2,boundingBox.y0+2,boundingBox.z1-3);}
  @Override public void generatePieces(DynamicRegistries r,ChunkGenerator g,TemplateManager t,int cx,int cz,Biome biome,NoFeatureConfig cfg){
   int x=(cx<<4)+8,z=(cz<<4)+8,radius=new int[]{8,12,18}[kind],low=255,high=0;
   for(int dx=-radius;dx<=radius;dx+=2)for(int dz=-radius;dz<=radius;dz+=2){if(dx*dx+dz*dz>radius*radius)continue;int floor=g.getBaseHeight(x+dx,z+dz,Heightmap.Type.OCEAN_FLOOR_WG),top=g.getBaseHeight(x+dx,z+dz,Heightmap.Type.WORLD_SURFACE_WG);if(floor!=top||floor<63||floor>140)return;low=Math.min(low,floor);high=Math.max(high,floor);if(high-low>(kind==0?4:6))return;}
   try {
    long layoutSeed=random.nextLong();KurganPlan plan=KurganPlan.create(kind,layoutSeed);BlockPos origin=new BlockPos(x,high-1,z);
    if(!terrainValid(g,plan,origin))return;
    pieces.add(new KurganDungeonPiece(plan,origin,new UUID(random.nextLong(),random.nextLong())));calculateBoundingBox();
   }catch(IllegalArgumentException invalid){/* bounded planner failed: do not add any piece */}
  }
 }
}
