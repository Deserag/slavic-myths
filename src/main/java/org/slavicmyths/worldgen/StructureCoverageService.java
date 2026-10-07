package org.slavicmyths.worldgen;

import java.util.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.placement.*;

/** Shared placement metadata and measured terrain decisions, with native locate-compatible cells. */
public final class StructureCoverageService {
 public enum Tier { SMALL, MEDIUM, LARGE }
 public enum Rejection { ACCEPTED, TERRAFORM_REQUIRED, INVALID_BIOME, DEEP_WATER, LAVA, WORLD_BOUNDS, PROTECTED_STRUCTURE, BLOCK_ENTITY_COLLISION, EXTREME_TERRAIN, TERRAFORM_BUDGET_EXCEEDED, UNDERGROUND_COLLISION, SPACING_CONFLICT, UNIQUE_STRUCTURE_RULE }
 public record Profile(String id,String family,Tier tier,int spacing,int jitter,int salt,int water,int slope,int foundationDepth,int budget){
  public RandomSpreadStructurePlacement placement(){return new RandomSpreadStructurePlacement(spacing,spacing-jitter-1,RandomSpreadType.LINEAR,salt);}
  public int hardDistance(){return switch(tier){case SMALL->1000;case MEDIUM->2000;case LARGE->4000;};}
 }
 public record TerrainDecision(Rejection reason,int minFloor,int maxFloor,int waterDepth,int fillCost){public boolean accepted(){return reason==Rejection.ACCEPTED||reason==Rejection.TERRAFORM_REQUIRED;}}
 private static final Map<String,Profile> PROFILES;
 static {
  var p=new LinkedHashMap<String,Profile>();
  add(p,"kurgan_small","kurgan",Tier.SMALL,20,4,841973,0,8,0,1000000);
  add(p,"kurgan_warrior","kurgan",Tier.MEDIUM,64,8,841974,0,10,0,4000000);
  add(p,"kurgan_great","kurgan",Tier.LARGE,128,8,841975,0,12,0,12000000);
  add(p,"bandit_camp_small","bandit",Tier.SMALL,20,4,803701,0,6,8,4096);
  add(p,"bandit_camp_medium","bandit",Tier.MEDIUM,64,8,803729,0,8,8,8192);
  add(p,"bandit_camp_large","bandit",Tier.LARGE,320,81,813797,0,10,10,16384);
  add(p,"bog_causeway","swamp",Tier.SMALL,16,4,731269,7,4,8,2048);
  add(p,"swamp_remnants","swamp",Tier.SMALL,20,4,733745,7,4,8,2048);
  add(p,"fishing_camp","swamp",Tier.SMALL,20,4,732507,7,4,8,4096);
  add(p,"swamp_hut","swamp",Tier.SMALL,20,4,730031,7,4,8,4096);
  add(p,"swamp_watchtower","swamp",Tier.MEDIUM,48,6,970041,7,4,8,4096);
  add(p,"flooded_shrine","swamp",Tier.MEDIUM,48,6,731888,7,4,8,4096);
  add(p,"abandoned_settlement","swamp",Tier.LARGE,96,8,730650,7,4,8,16384);
  add(p,"underwater_ruins","water",Tier.SMALL,20,4,733126,64,6,8,4096);
  PROFILES=Collections.unmodifiableMap(p);
 }
 private static void add(Map<String,Profile> profiles,String id,String family,Tier tier,int spacing,int jitter,int salt,int water,int slope,int foundationDepth,int budget){profiles.put(id,new Profile(id,family,tier,spacing,jitter,salt,water,slope,foundationDepth,budget));}
 public static Collection<Profile> profiles(){return PROFILES.values();}
 public static Profile profile(String id){return Objects.requireNonNull(PROFILES.get(id),"No coverage profile for "+id);}
 public static double distance(ChunkPos a,ChunkPos b){return Math.hypot(((long)a.x-b.x)*16.0,((long)a.z-b.z)*16.0);}
 /** Vanilla creates structure references only within eight chunks of the owning start. */
 public static boolean referencesFit(ChunkPos owner,Collection<net.minecraft.world.level.levelgen.structure.StructurePiece> pieces){
  for(var piece:pieces){var box=piece.getBoundingBox();if((box.minX()>>4)<owner.x-8||(box.maxX()>>4)>owner.x+8||(box.minZ()>>4)<owner.z-8||(box.maxZ()>>4)>owner.z+8)return false;}
  return true;
 }
 public record PlacementDiagnostics(Map<Rejection,Integer> decisions,Map<String,Long> lastMeasurements,Rejection lastReason){}
 private static final ThreadLocal<PlacementDiagnostics> LAST_PLACEMENT=new ThreadLocal<>();
 public static PlacementDiagnostics lastPlacement(){return LAST_PLACEMENT.get();}
 public static void note(Rejection reason,String measurement,long value){var active=ACTIVE_SAMPLES.get();if(active!=null){active.lastReason=reason;active.decisions.merge(reason,1,Integer::sum);active.measurements.clear();active.measurements.put(measurement,value);}}
 public static void note(Rejection reason,Map<String,Long> measurements){var active=ACTIVE_SAMPLES.get();if(active!=null){active.lastReason=reason;active.decisions.merge(reason,1,Integer::sum);active.measurements.clear();active.measurements.putAll(measurements);}}
 public static void note(TerrainDecision decision){var active=ACTIVE_SAMPLES.get();if(active!=null){if(!decision.accepted()){active.lastReason=decision.reason();active.decisions.merge(decision.reason(),1,Integer::sum);}active.measurements.clear();active.measurements.put("minFloor",(long)decision.minFloor());active.measurements.put("maxFloor",(long)decision.maxFloor());active.measurements.put("waterDepth",(long)decision.waterDepth());active.measurements.put("fillCost",(long)decision.fillCost());}}
 private static final ThreadLocal<TerrainSamples> ACTIVE_SAMPLES=new ThreadLocal<>();
 private record Neighbor(String id,long chunk){}
 private record FootprintKey(Object generator,Object random,Object templates,Object registries,long seed,String id,long chunk,int minY,int maxY){}
 private static final Map<FootprintKey,List<net.minecraft.world.level.levelgen.structure.BoundingBox>> FOOTPRINTS=new LinkedHashMap<>(256,.75f,true);
 private static int footprintBoxes;private static long footprintHits,footprintMisses;
 public record FootprintStats(int plans,int boxes,long hits,long misses){}
 public static FootprintStats footprintStats(){synchronized(FOOTPRINTS){return new FootprintStats(FOOTPRINTS.size(),footprintBoxes,footprintHits,footprintMisses);}}
 public static void clearFootprints(){synchronized(FOOTPRINTS){FOOTPRINTS.clear();footprintBoxes=0;footprintHits=0;footprintMisses=0;}}
 /** Only immutable-by-convention collision boxes are retained, never mutable placed pieces or world data.
  * Same input gives the same accepted plan; cache bounds are 2048 plans / 100000 boxes across all workers.
  * Generation happens outside the lock, so native worldgen workers never wait for one large plan.
  */
 public static List<net.minecraft.world.level.levelgen.structure.BoundingBox> neighborFootprints(net.minecraft.world.level.levelgen.structure.Structure.GenerationContext c,net.minecraft.world.level.levelgen.structure.Structure other,ChunkPos chunk){
  var id=c.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(other);
  var key=new FootprintKey(c.chunkGenerator(),c.randomState(),c.structureTemplateManager(),c.registryAccess(),c.seed(),id.toString(),chunk.toLong(),c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getMaxBuildHeight());
  synchronized(FOOTPRINTS){var cached=FOOTPRINTS.get(key);if(cached!=null){footprintHits++;return cached;}footprintMisses++;}
  var start=neighborPlan(c,other,chunk);var boxes=new ArrayList<net.minecraft.world.level.levelgen.structure.BoundingBox>();if(start.isValid())for(var piece:start.getPieces()){var b=piece.getBoundingBox();boxes.add(new net.minecraft.world.level.levelgen.structure.BoundingBox(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()));}
  var result=List.copyOf(boxes);synchronized(FOOTPRINTS){var previous=FOOTPRINTS.put(key,result);footprintBoxes+=result.size()-(previous==null?0:previous.size());while(FOOTPRINTS.size()>2048||footprintBoxes>100000)footprintBoxes-=FOOTPRINTS.remove(FOOTPRINTS.keySet().iterator().next()).size();}return result;
 }

 private record Column(int x,int z,net.minecraft.world.level.levelgen.Heightmap.Types type){}
 /** Cache belongs to one generation invocation, never a world or a global tick loop. */
 public static final class TerrainSamples implements AutoCloseable{
  private final net.minecraft.world.level.levelgen.structure.Structure.GenerationContext context;
  private final TerrainSamples previous;private final Map<Column,Integer> columns;
  private Rejection lastReason;
  private final Map<Neighbor,net.minecraft.world.level.levelgen.structure.StructureStart> neighbors;
  private final Map<Rejection,Integer> decisions=new EnumMap<>(Rejection.class);private final Map<String,Long> measurements=new LinkedHashMap<>();
  private TerrainSamples(net.minecraft.world.level.levelgen.structure.Structure.GenerationContext context){this.context=context;previous=ACTIVE_SAMPLES.get();boolean shared=previous!=null&&previous.context.chunkGenerator()==context.chunkGenerator()&&previous.context.randomState()==context.randomState()&&previous.context.seed()==context.seed();columns=shared?previous.columns:new HashMap<>();neighbors=shared?previous.neighbors:new HashMap<>();ACTIVE_SAMPLES.set(this);}
  @Override public void close(){LAST_PLACEMENT.set(new PlacementDiagnostics(Map.copyOf(decisions),Map.copyOf(measurements),lastReason));if(previous==null)ACTIVE_SAMPLES.remove();else ACTIVE_SAMPLES.set(previous);}
 }
 public static TerrainSamples sampleTerrain(net.minecraft.world.level.levelgen.structure.Structure.GenerationContext context){return new TerrainSamples(context);}
 public static int baseHeight(net.minecraft.world.level.chunk.ChunkGenerator generator,int x,int z,net.minecraft.world.level.levelgen.Heightmap.Types type,net.minecraft.world.level.LevelHeightAccessor height,net.minecraft.world.level.levelgen.RandomState random){
  var active=ACTIVE_SAMPLES.get();
  if(active==null||active.context.chunkGenerator()!=generator||active.context.randomState()!=random||active.context.heightAccessor().getMinBuildHeight()!=height.getMinBuildHeight()||active.context.heightAccessor().getMaxBuildHeight()!=height.getMaxBuildHeight())return generator.getBaseHeight(x,z,type,height,random);
  var key=new Column(x,z,type);var saved=active.columns.get(key);if(saved!=null)return saved;
  // Both raw heightmaps come from the same vanilla noise column. Reuse that column
  // instead of rebuilding NoiseChunk twice; never substitute approximate climate heights.
  if(generator instanceof net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator&&(type==net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG||type==net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG)){
   var column=generator.getBaseColumn(x,z,height,random);if(column==null)return generator.getBaseHeight(x,z,type,height,random);int requested=height.getMinBuildHeight();
   for(var map:List.of(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG)){
    int result=height.getMinBuildHeight();for(int y=height.getMaxBuildHeight()-1;y>=height.getMinBuildHeight();y--)if(map.isOpaque().test(column.getBlock(y))){result=y+1;break;}
    if(active.columns.size()<8192)active.columns.put(new Column(x,z,map),result);if(map==type)requested=result;
   }
   return requested;
  }
  int result=generator.getBaseHeight(x,z,type,height,random);if(active.columns.size()<8192)active.columns.put(key,result);return result;
 }
 /** Neighbor plans are cached only during this invocation; no loaded-world lookup or recursive kurgan call. */
 public static net.minecraft.world.level.levelgen.structure.StructureStart neighborPlan(net.minecraft.world.level.levelgen.structure.Structure.GenerationContext c,net.minecraft.world.level.levelgen.structure.Structure other,ChunkPos chunk){
  var id=c.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(other);var key=new Neighbor(id.toString(),chunk.toLong());var active=ACTIVE_SAMPLES.get();
  var cached=active==null?null:active.neighbors.get(key);if(cached!=null)return cached;
  var result=other.generate(c.registryAccess(),c.chunkGenerator(),c.biomeSource(),c.randomState(),c.structureTemplateManager(),c.seed(),chunk,0,c.heightAccessor(),other.biomes()::contains);
  if(active!=null&&active.neighbors.size()<256)active.neighbors.put(key,result);return result;
 }
 /** Ordered bounded local retries; offset zero always comes first. */
 public static Iterable<net.minecraft.core.BlockPos> localCandidates(net.minecraft.world.level.levelgen.structure.Structure.GenerationContext context,net.minecraft.world.level.levelgen.structure.Structure structure){
  int[] rings={0,16,32,48,64,96,128,160,192,256};
  int[][] directions={{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};
  int rotation=Math.floorMod(context.seed()^context.chunkPos().toLong(),8);
  var id=context.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(structure);
  var profile=id!=null?PROFILES.get(id.getPath()):null;
  int maxRadius=profile!=null&&profile.tier()==Tier.SMALL?128:256;
  // Do not sample all 73 noise columns when the first site succeeds.
  return ()->new Iterator<>(){
   private int index;private net.minecraft.core.BlockPos next;private boolean prepared;
   @Override public boolean hasNext(){
    if(prepared)return next!=null;prepared=true;
    while(index<73){
     int current=index++,ring=current==0?0:1+(current-1)/8,n=current==0?0:(current-1)%8;
     int radius=rings[ring];if(radius>maxRadius)continue;int[] direction=directions[(rotation+n)%8];
     int step=direction[0]!=0&&direction[1]!=0?(int)Math.floor(radius/Math.sqrt(2)):radius;
     int x=(context.chunkPos().x<<4)+step*direction[0],z=(context.chunkPos().z<<4)+step*direction[1];
     int y=baseHeight(context.chunkGenerator(),x,z,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
     var biome=context.biomeSource().getNoiseBiome(x>>2,y>>2,z>>2,context.randomState().sampler());
     if(!structure.biomes().contains(biome)){note(Rejection.INVALID_BIOME,"sampleY",y);continue;}
     if(structure.biomes().contains(biome)){next=new net.minecraft.core.BlockPos(x,y,z);return true;}
    }
    next=null;return false;
   }
   @Override public net.minecraft.core.BlockPos next(){if(!hasNext())throw new NoSuchElementException();var value=next;next=null;prepared=false;return value;}
  };
 }
 /** Numerical policy only. Collision/biome checks are separate, never disguised as this decision. */
 public static TerrainDecision terrain(Profile p,int minFloor,int maxFloor,int maxWaterDepth,int worldMin,int worldMax,int height,int fillCost){
  int water=Math.max(0,maxWaterDepth);Rejection reason=Rejection.ACCEPTED;
  if(minFloor<=worldMin||maxFloor+height>=worldMax)reason=Rejection.WORLD_BOUNDS;
  else if(water>p.water)reason=Rejection.DEEP_WATER;
  else if(maxFloor-minFloor>p.slope)reason=p.family.equals("water")?Rejection.EXTREME_TERRAIN:Rejection.TERRAFORM_REQUIRED;
  else if(fillCost>p.budget)reason=p.family.equals("water")?Rejection.TERRAFORM_BUDGET_EXCEEDED:Rejection.TERRAFORM_REQUIRED;
  return new TerrainDecision(reason,minFloor,maxFloor,water,fillCost);
 }
 private StructureCoverageService(){}
}
