package org.slavicmyths.burningislands;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
/** Shared deterministic base-terrain mask; never reads or generates a world/chunk.
 * Sea queries use the SAME native generator restricted to one vertical noise cell.
 * LRU budgets are per generator/random state, not an ever-growing world map. */
public final class OceanTerrain {
 private static final DensityFunctions.BeardifierOrMarker NO_STRUCTURES=(DensityFunctions.BeardifierOrMarker)DensityFunctions.BeardifierOrMarker.CODEC.codec().codec().parse(com.mojang.serialization.JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow();
 public static final int CELL=512,RADIUS=168;
 public record Plan(int x,int z,int sea){public BlockPos home(){return new BlockPos(x,sea+27,z);}}
 private final NoiseBasedChunkGenerator generator;private final RandomState state;private final long seed;private final NoiseGeneratorSettings settings;
 private final Map<Long,Boolean> regions=lru(128),quart=lru(8192);
 private static <K,V> Map<K,V> lru(int max){return new LinkedHashMap<>(32,.75F,true){@Override protected boolean removeEldestEntry(Map.Entry<K,V> e){return size()>max;}};}
 public OceanTerrain(NoiseBasedChunkGenerator generator,RandomState state,long seed,NoiseGeneratorSettings settings){this.generator=generator;this.state=state;this.seed=seed;this.settings=settings;}
 public Plan plan(int x,int z){int rx=Math.floorDiv(x,CELL),rz=Math.floorDiv(z,CELL);long h=net.minecraft.util.Mth.getSeed(rx,(int)seed,rz)^seed;return new Plan(rx*CELL+256+(int)Math.floorMod(h,33)-16,rz*CELL+256+(int)Math.floorMod(h>>17,33)-16,generator.getSeaLevel());}
 private boolean lava(int x,int z){int sea=generator.getSeaLevel(),base=Math.floorDiv(sea-1,8)*8;return generator.getBaseColumn(x,z,LevelHeightAccessor.create(base,8),state).getBlock(sea-1).is(Blocks.LAVA);}
 /** Nine early probes reject puddles; all quart footprints of the entire connected disk are then certified over native lava. */
 public synchronized boolean region(Plan p){long key=ChunkPos.asLong(p.x(),p.z());return regions.computeIfAbsent(key,k->{for(int dx:new int[]{-112,0,112})for(int dz:new int[]{-112,0,112})if(!lava(p.x()+dx,p.z()+dz))return false;for(int qx=Math.floorDiv(p.x()-RADIUS,4);qx<=Math.floorDiv(p.x()+RADIUS,4);qx++)for(int qz=Math.floorDiv(p.z()-RADIUS,4);qz<=Math.floorDiv(p.z()+RADIUS,4);qz++)if(Math.hypot(qx*4+1.5-p.x(),qz*4+1.5-p.z())<=RADIUS&&!quartOcean(qx,qz))return false;return true;});}
 public synchronized boolean contains(int qx,int qy,int qz){int x=qx*4,z=qz*4;Plan p=plan(x,z);int y=qy*4;if(y<p.sea()-4||y>p.sea()+80||Math.hypot(x+1.5-p.x(),z+1.5-p.z())>RADIUS||!region(p))return false;long key=ChunkPos.asLong(qx,qz);return quartOcean(qx,qz);}
 private boolean quartOcean(int qx,int qz){return quart.computeIfAbsent(ChunkPos.asLong(qx,qz),k->{if(settings.noiseSettings().getCellWidth()!=4){for(int dx=0;dx<4;dx++)for(int dz=0;dz<4;dz++)if(!lava(qx*4+dx,qz*4+dz))return false;return true;}return new SeaCell(qx*4,qz*4).allLava(qx*4,qz*4);});}
 private final class SeaCell extends NoiseChunk {
  SeaCell(int x,int z){super(1,state,x,z,settings.noiseSettings().clampToHeightAccessor(LevelHeightAccessor.create(Math.floorDiv(generator.getSeaLevel()-1,settings.noiseSettings().getCellHeight())*settings.noiseSettings().getCellHeight(),settings.noiseSettings().getCellHeight())),NO_STRUCTURES,settings,(a,y,b)->new Aquifer.FluidStatus(generator.getSeaLevel(),settings.defaultFluid()),net.minecraft.world.level.levelgen.blending.Blender.empty());}
  boolean allLava(int x,int z){initializeForFirstCellX();advanceCellX(0);selectCellYZ(0,0);int y=generator.getSeaLevel()-1;updateForY(y,Math.floorMod(y,settings.noiseSettings().getCellHeight())/(double)settings.noiseSettings().getCellHeight());boolean lava=true;for(int a=0;a<4&&lava;a++)for(int b=0;b<4;b++){updateForX(x+a,a/4.0);updateForZ(z+b,b/4.0);var block=getInterpolatedState();if(block==null||!block.is(Blocks.LAVA)){lava=false;break;}}stopInterpolation();return lava;}
 }
 public static int floor(Plan p,int x,int z){double dx=x-p.x(),dz=z-p.z(),d=Math.hypot(dx,dz);if(d<26)return p.sea()+26;if(d<34)return Math.abs(dz)<=2?p.sea()+26:p.sea()+22;if(d<40)return p.sea()+26;if(d<98)return p.sea()+3+(int)Math.floor((98-d)*23/58);if(d<126)return p.sea()+3;
  for(int side:new int[]{-1,1})if(Math.hypot(dx-side*143,dz)<24||Math.abs(dz)<=2&&Math.abs(dx)<148)return p.sea()+3;return Integer.MIN_VALUE;}
 public static boolean craterLava(Plan p,int x,int z){double d=Math.hypot(x-p.x(),z-p.z());return d>=26&&d<34&&Math.abs(z-p.z())>2;}
 public int cachedRegions(){return regions.size();}public int cachedQuarts(){return quart.size();}
 public interface SamplerAccess {OceanTerrain slavicmyths$ocean();void slavicmyths$ocean(OceanTerrain value);}
 public interface GeneratorAccess {OceanTerrain slavicmyths$ocean(RandomState state);}
 public interface SeedAccess {long slavicmyths$seed();}
 
}
