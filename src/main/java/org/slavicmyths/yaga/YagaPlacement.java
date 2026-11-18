package org.slavicmyths.yaga;
import java.util.*;
import java.util.function.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
/** The production placement pipeline, also executable against an isolated JVM world fixture. */
public final class YagaPlacement {
 // SEND_TO_CLIENTS | KNOWN_SHAPE: do not update neighbours while the house is incomplete.
 public static final int BUILD_FLAGS=2|16;
 public static final class Result {
  public final boolean ok;public final String reason;public final BlockPos pos;public final RuntimeException error;
  Result(boolean ok,String reason,BlockPos pos,RuntimeException error){this.ok=ok;this.reason=reason;this.pos=pos;this.error=error;}
  public static Result fail(String reason,BlockPos pos){return new Result(false,reason,pos,null);}
 }
 public static final class Site {
  public final LinkedHashMap<BlockPos,BlockState> desired=new LinkedHashMap<>(),old=new LinkedHashMap<>();
  public BlockPos anchor;public Result failure;
  Site fail(String reason,BlockPos pos){failure=Result.fail(reason,pos);return this;}
 }
 private static final int NO_SURFACE=Integer.MIN_VALUE,WATER=Integer.MIN_VALUE+1;
 public static int ground(LevelAccessor w,int x,int z){return ground(w,x,z,false);}
 private static int ground(LevelAccessor w,int x,int z,boolean reshape){int y=Math.min(w.getMaxBuildHeight()-1,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1);
  // The target may place valid terrain below Y=0; failure sentinels cannot overlap valid heights.
  for(int i=0;i<48&&y>=w.getMinBuildHeight();i++,y--){BlockPos p=new BlockPos(x,y,z);BlockState s=w.getBlockState(p);if(s.liquid()){if(reshape)continue;return WATER;}if(s.isSolidRender(w,p)&&!s.is(BlockTags.LOGS)&&!(s.getBlock() instanceof LeavesBlock))return y;}return NO_SURFACE;
 }
 private static boolean soil(BlockState s){Block b=s.getBlock();return b==Blocks.GRASS_BLOCK||b==Blocks.DIRT||b==Blocks.COARSE_DIRT||b==Blocks.PODZOL;}
 private static String unsafe(LevelAccessor w,BlockPos p,BlockState s){if(w.getBlockEntity(p)!=null)return "container";if(w instanceof net.minecraft.server.level.ServerLevel level&&org.slavicmyths.rpg.tools.ToolLogOrigins.get(level).blocked(p))return "protected";if(s.liquid())return "water";
  if(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getNamespace().equals("slavicmyths")&&!s.canBeReplaced())return "protected";
  return s.isAir()||s.canBeReplaced()||s.is(BlockTags.LOGS)||s.getBlock() instanceof LeavesBlock||soil(s)?null:"obstruction";
 }
 public static Site prepare(LevelAccessor w,BlockPos candidate,YagaHutPlan plan,Function<String,BlockState> resolve){return prepare(w,candidate,plan,resolve,false);}
 public static Site prepareForCreation(LevelAccessor w,BlockPos candidate,YagaHutPlan plan,Function<String,BlockState> resolve){return prepare(w,candidate,plan,resolve,true);}
 private static Site prepare(LevelAccessor w,BlockPos candidate,YagaHutPlan plan,Function<String,BlockState> resolve,boolean reshape){Site site=new Site();
  if(reshape)return terrace(w,candidate,plan,resolve);
  if(!w.hasChunksAt(candidate.offset(-10,0,-17),candidate.offset(10,0,10)))return site.fail("chunks",candidate);
  var heights=new ArrayList<Integer>();int min=w.getMaxBuildHeight(),max=w.getMinBuildHeight()-1;for(int x=-9;x<=9;x+=3)for(int z=-15;z<=9;z+=3){int y=ground(w,candidate.getX()+x,candidate.getZ()+z,reshape);if(y==NO_SURFACE||y==WATER)return site.fail(y==WATER?"water":"surface",candidate.offset(x,0,z));min=Math.min(min,y);max=Math.max(max,y);heights.add(y);}
  if(max-min>3)return site.fail("slope",candidate);if(max+15>=w.getMaxBuildHeight())return site.fail("height",candidate);
  heights.sort(Integer::compare);site.anchor=new BlockPos(candidate.getX(),max+1,candidate.getZ());
  for(Map.Entry<BlockPos,String> e:plan.blocks.entrySet()){BlockPos p=site.anchor.offset(e.getKey());BlockState before=w.getBlockState(p);String bad=unsafe(w,p,before);if(bad!=null&&!(reshape&&(bad.equals("water")||bad.equals("obstruction")&&org.slavicmyths.worldgen.ManualStructureJobs.natural(before)&&!before.is(Blocks.BEDROCK))))return site.fail(bad,p);site.desired.put(p,resolve.apply(e.getValue()));site.old.put(p,before);}
  for(Map.Entry<BlockPos,String> e:plan.blocks.entrySet()){if(e.getKey().getY()!=0)continue;BlockPos base=site.anchor.offset(e.getKey()).below();int terrain=ground(w,base.getX(),base.getZ(),reshape);if(terrain==NO_SURFACE||terrain==WATER)return site.fail(terrain==WATER?"water":"surface",base);if(site.anchor.getY()-1-terrain>4)return site.fail("slope",base);
   for(int y=terrain+1;y<site.anchor.getY();y++){BlockPos p=new BlockPos(base.getX(),y,base.getZ());BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null)return site.fail("container",p);String bad=unsafe(w,p,before);if(bad!=null&&!(reshape&&(bad.equals("water")||bad.equals("obstruction")&&org.slavicmyths.worldgen.ManualStructureJobs.natural(before)&&!before.is(Blocks.BEDROCK))))return site.fail(bad,p);site.old.putIfAbsent(p,before);site.desired.put(p,Blocks.COARSE_DIRT.defaultBlockState());}}
  // Ornamental herbs also need real soil. A rocky accepted surface must not
  // make every cooperative transaction roll back at the first wormwood plant.
  for(Map.Entry<BlockPos,String> e:plan.blocks.entrySet())if(e.getValue().equals("minecraft:brown_mushroom")||e.getValue().equals("slavicmyths:wormwood")){BlockPos p=site.anchor.offset(e.getKey()).below();BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null)return site.fail("container",p);String bad=unsafe(w,p,before);if(bad!=null&&!(reshape&&(bad.equals("water")||bad.equals("obstruction")&&org.slavicmyths.worldgen.ManualStructureJobs.natural(before)&&!before.is(Blocks.BEDROCK))))return site.fail(bad,p);site.old.putIfAbsent(p,before);site.desired.put(p,e.getValue().equals("minecraft:brown_mushroom")?Blocks.PODZOL.defaultBlockState():before.is(BlockTags.DIRT)?before:Blocks.DIRT.defaultBlockState());}
  return site;
 }
 /** Full yard + six-block transition, precomputed before reservation or the first write. */
 public static final int TERRACE_MIN_X=-15,TERRACE_MAX_X=15,TERRACE_MIN_Z=-22,TERRACE_MAX_Z=15;
 public static BlockPos minCorner(BlockPos p){return p.offset(TERRACE_MIN_X,0,TERRACE_MIN_Z-1);}
 public static BlockPos maxCorner(BlockPos p){return p.offset(TERRACE_MAX_X,0,TERRACE_MAX_Z);}
 private static boolean terrainAllowed(LevelAccessor w,BlockPos p,BlockState old){
  if(w.getBlockEntity(p)!=null||old.is(Blocks.BEDROCK)||old.is(Blocks.NETHER_PORTAL)||old.is(Blocks.END_PORTAL)||old.is(Blocks.END_PORTAL_FRAME)||old.is(Blocks.END_GATEWAY))return false;
  if(w instanceof net.minecraft.server.level.ServerLevel level&&org.slavicmyths.rpg.tools.ToolLogOrigins.get(level).blocked(p))return false;
  return !old.liquid()&&org.slavicmyths.worldgen.ManualStructureJobs.natural(old);
 }
 private static boolean desired(Site site,LevelAccessor w,BlockPos p,BlockState next){
  if(w.isOutsideBuildHeight(p)){site.fail("height",p);return false;}var old=w.getBlockState(p);
  if(!terrainAllowed(w,p,old)){site.fail(w.getBlockEntity(p)!=null?"container":"protected",p);return false;}
  site.old.putIfAbsent(p,old);site.desired.put(p,next);return true;
 }
 private static Site terrace(LevelAccessor w,BlockPos candidate,YagaHutPlan plan,Function<String,BlockState> resolve){
  var site=new Site();if(!w.hasChunksAt(minCorner(candidate),maxCorner(candidate)))return site.fail("chunks",candidate);
  int width=TERRACE_MAX_X-TERRACE_MIN_X+1,depth=TERRACE_MAX_Z-TERRACE_MIN_Z+1;int[] ground=new int[width*depth];var levels=new ArrayList<Integer>();
  for(int x=TERRACE_MIN_X;x<=TERRACE_MAX_X;x++)for(int z=TERRACE_MIN_Z;z<=TERRACE_MAX_Z;z++){
   int g=ground(w,candidate.getX()+x,candidate.getZ()+z,false);if(g==WATER||g==NO_SURFACE)return site.fail(g==WATER?"water":"surface",candidate.offset(x,0,z));
   ground[(x-TERRACE_MIN_X)*depth+z-TERRACE_MIN_Z]=g;if(x>=-9&&x<=9&&z>=-16&&z<=9)levels.add(g);
  }
  levels.sort(Integer::compare);int target=levels.get(levels.size()/2);if(target+15>=w.getMaxBuildHeight()||target-6<=w.getMinBuildHeight()+5)return site.fail("height",candidate);
  site.anchor=candidate.atY(target+1);long volume=0;
  // Same principle as KurganEarthwork: grade the complete footprint, taper back to actual terrain.
  for(int x=TERRACE_MIN_X;x<=TERRACE_MAX_X;x++)for(int z=TERRACE_MIN_Z;z<=TERRACE_MAX_Z;z++){
   int g=ground[(x-TERRACE_MIN_X)*depth+z-TERRACE_MIN_Z],distance=Math.max(0,Math.max(Math.max(-9-x,x-9),Math.max(-16-z,z-9)));
   if(distance==0&&Math.abs(g-target)>6)return site.fail("terrace_budget",candidate.offset(x,g-candidate.getY(),z));
   int top=g+Integer.signum(target-g)*Math.max(0,Math.abs(target-g)-distance);
   if(Math.abs(top-g)>6)return site.fail("terrace_budget",candidate.offset(x,0,z));
   volume+=Math.abs(top-g);if(volume>6000)return site.fail("terrace_budget",candidate);
   // Continuous soil under the entire yard, including all leg toes, fence and approach.
   for(int y=Math.min(g,top)-1;y<=Math.max(g,top);y++){
    BlockState next=y>top?Blocks.AIR.defaultBlockState():y==top?Blocks.GRASS_BLOCK.defaultBlockState():y<top-3?Blocks.STONE.defaultBlockState():Blocks.DIRT.defaultBlockState();
    if(!desired(site,w,new BlockPos(candidate.getX()+x,y,candidate.getZ()+z),next))return site;
   }
   if(distance==0){
    int clear=Math.max(top+3,Math.min(top+18,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,candidate.getX()+x,candidate.getZ()+z)));
    for(int y=top+1;y<=clear;y++)if(!desired(site,w,new BlockPos(candidate.getX()+x,y,candidate.getZ()+z),Blocks.AIR.defaultBlockState()))return site;
   }
  }
  // Actual central approach must meet natural ground in one-block grades, in both directions.
  int previous=target;
  for(int z=-17;z>=TERRACE_MIN_Z;z--){int g=ground[(0-TERRACE_MIN_X)*depth+z-TERRACE_MIN_Z];int top=Math.max(previous-1,Math.min(previous+1,g));
   for(int x=-1;x<=1;x++){int oldGround=ground[(x-TERRACE_MIN_X)*depth+z-TERRACE_MIN_Z];if(Math.abs(top-oldGround)>6)return site.fail("approach",candidate.offset(x,0,z));
    for(int y=Math.min(oldGround,top)-1;y<=Math.max(oldGround+3,top+3);y++)if(!desired(site,w,new BlockPos(candidate.getX()+x,y,candidate.getZ()+z),y>top?Blocks.AIR.defaultBlockState():y==top?Blocks.DIRT_PATH.defaultBlockState():y<top-3?Blocks.STONE.defaultBlockState():Blocks.DIRT.defaultBlockState()))return site;
   }previous=top;
  }
  int outside=ground(w,candidate.getX(),candidate.getZ()+TERRACE_MIN_Z-1,false);if(outside==NO_SURFACE||outside==WATER||Math.abs(outside-previous)>1)return site.fail("approach",candidate);
  // The final exterior and unchanged artistic house blueprint take precedence over grading.
  for(var e:plan.blocks.entrySet())if(!desired(site,w,site.anchor.offset(e.getKey()),resolve.apply(e.getValue())))return site;
  for(var e:plan.blocks.entrySet())if(e.getValue().equals("minecraft:brown_mushroom")||e.getValue().equals("slavicmyths:wormwood")){
   if(!desired(site,w,site.anchor.offset(e.getKey()).below(),e.getValue().equals("minecraft:brown_mushroom")?Blocks.PODZOL.defaultBlockState():Blocks.DIRT.defaultBlockState()))return site;
  }
  return site;
 }
 private static final class Refused extends RuntimeException {final String reason;final BlockPos pos;Refused(String reason,BlockPos pos){super(reason+" at "+pos);this.reason=reason;this.pos=pos;}}
 /** Cooperative version of the same transaction; no house-sized write burst in a book click. */
 public static final class Cursor {
  private final LevelAccessor world;private final Site site;private final BooleanSupplier spawn;private final Runnable undo;
  private final java.util.function.Consumer<Result> completion;
  private List<BlockPos> positions;private final LinkedHashMap<BlockPos,BlockState> written=new LinkedHashMap<>();
  private int phase,index;private RuntimeException failure;public Result result;
  public Cursor(LevelAccessor world,Site site,BooleanSupplier spawn,Runnable undo,java.util.function.Consumer<Result> completion){this.world=world;this.site=site;this.spawn=spawn;this.undo=undo;this.completion=completion;positions=List.copyOf(site.desired.keySet());if(site.failure!=null)finish(site.failure);}
  public boolean done(){return result!=null;}
  public void abort(){if(done())return;failure=new IllegalStateException("Site construction cancelled");beginRollback();}
  private void beginRollback(){phase=3;index=0;positions=List.copyOf(written.keySet());}
  private void finish(Result r){result=r;completion.accept(r);}
  public void step(int limit,long nanos){
   if(done())return;long deadline=System.nanoTime()+nanos;int operations=0;
   while(!done()&&operations++<limit&&System.nanoTime()<deadline)try{
    if(index==positions.size()){
     if(phase==0){phase=1;index=0;continue;}
     if(phase==1){if(!spawn.getAsBoolean())throw new Refused("npc",site.anchor);finish(new Result(true,"ok",site.anchor,null));return;}
     try{undo.run();}catch(RuntimeException e){failure.addSuppressed(e);}finish(new Result(false,failure.getSuppressed().length>0?"rollback":failure instanceof Refused?((Refused)failure).reason:"cancelled",site.anchor,failure));return;
    }
    BlockPos p=positions.get(index++);
    if(phase==0){if(!world.getBlockState(p).equals(site.old.get(p)))throw new Refused("changed",p);written.put(p,site.desired.get(p));write(world,p,site.desired.get(p));}
    else if(phase==1){var current=world.getBlockState(p);if(!current.equals(written.get(p)))throw new Refused("changed",p);var next=Block.updateFromNeighbourShapes(current,world,p);if(!current.isAir()&&next.isAir())throw new Refused("support",p);if(!current.equals(next)){written.put(p,next);write(world,p,next);}}
    else {var own=written.get(p);if(own!=null&&world.getBlockState(p).equals(own))write(world,p,site.old.get(p));}
   }catch(RuntimeException e){if(phase==3){failure.addSuppressed(e);}else{failure=e;beginRollback();}}
  }
 }
 private static void write(LevelAccessor w,BlockPos p,BlockState state){w.setBlock(p,state,BUILD_FLAGS);if(!w.getBlockState(p).equals(state))throw new Refused("write",p);}
 public static Result commit(LevelAccessor w,Site site,BooleanSupplier spawn,Runnable undoSpawn){if(site.failure!=null)return site.failure;
  try{
   for(Map.Entry<BlockPos,BlockState> e:site.desired.entrySet())write(w,e.getKey(),e.getValue());
   // Resolve all shapes after supports and both door halves exist; never accept silently lost blocks.
   LinkedHashMap<BlockPos,BlockState> connected=new LinkedHashMap<>();
   for(Map.Entry<BlockPos,BlockState> e:site.desired.entrySet()){BlockPos p=e.getKey();BlockState current=w.getBlockState(p);if(!current.equals(e.getValue()))throw new Refused("write",p);BlockState next=Block.updateFromNeighbourShapes(current,w,p);if(!current.isAir()&&next.isAir())throw new Refused("support",p);connected.put(p,next);}
   for(Map.Entry<BlockPos,BlockState> e:connected.entrySet())if(!w.getBlockState(e.getKey()).equals(e.getValue()))write(w,e.getKey(),e.getValue());
   if(!spawn.getAsBoolean())throw new Refused("npc",site.anchor.offset(YagaHutPlan.HOME));return new Result(true,"ok",site.anchor,null);
  }catch(RuntimeException failure){try{undoSpawn.run();}catch(RuntimeException undo){failure.addSuppressed(undo);}for(Map.Entry<BlockPos,BlockState> e:site.old.entrySet())try{write(w,e.getKey(),e.getValue());}catch(RuntimeException rollback){failure.addSuppressed(rollback);}
   if(failure.getSuppressed().length>0)return new Result(false,"rollback",site.anchor,failure);
   return new Result(false,failure instanceof Refused?((Refused)failure).reason:"internal",failure instanceof Refused?((Refused)failure).pos:site.anchor,failure);
  }
 }
 private YagaPlacement(){}
}
