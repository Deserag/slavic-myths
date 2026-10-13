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
  private Site fail(String reason,BlockPos pos){failure=Result.fail(reason,pos);return this;}
 }
 private static final int NO_SURFACE=Integer.MIN_VALUE,WATER=Integer.MIN_VALUE+1;
 public static int ground(LevelAccessor w,int x,int z){int y=Math.min(w.getMaxBuildHeight()-1,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1);
  // The target may place valid terrain below Y=0; failure sentinels cannot overlap valid heights.
  for(int i=0;i<48&&y>=w.getMinBuildHeight();i++,y--){BlockPos p=new BlockPos(x,y,z);BlockState s=w.getBlockState(p);if(s.liquid())return WATER;if(s.isSolidRender(w,p)&&!s.is(BlockTags.LOGS)&&!(s.getBlock() instanceof LeavesBlock))return y;}return NO_SURFACE;
 }
 private static boolean soil(BlockState s){Block b=s.getBlock();return b==Blocks.GRASS_BLOCK||b==Blocks.DIRT||b==Blocks.COARSE_DIRT||b==Blocks.PODZOL;}
 private static String unsafe(LevelAccessor w,BlockPos p,BlockState s){if(w.getBlockEntity(p)!=null)return "container";if(s.liquid())return "water";
  if(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getNamespace().equals("slavicmyths")&&!s.canBeReplaced())return "protected";
  return s.isAir()||s.canBeReplaced()||s.is(BlockTags.LOGS)||s.getBlock() instanceof LeavesBlock||soil(s)?null:"obstruction";
 }
 public static Site prepare(LevelAccessor w,BlockPos candidate,YagaHutPlan plan,Function<String,BlockState> resolve){Site site=new Site();
  if(!w.hasChunksAt(candidate.offset(-10,0,-17),candidate.offset(10,0,10)))return site.fail("chunks",candidate);
  int min=w.getMaxBuildHeight(),max=w.getMinBuildHeight()-1;for(int x=-9;x<=9;x+=3)for(int z=-15;z<=9;z+=3){int y=ground(w,candidate.getX()+x,candidate.getZ()+z);if(y==NO_SURFACE||y==WATER)return site.fail(y==WATER?"water":"surface",candidate.offset(x,0,z));min=Math.min(min,y);max=Math.max(max,y);}
  if(max-min>3)return site.fail("slope",candidate);if(max+15>=w.getMaxBuildHeight())return site.fail("height",candidate);
  site.anchor=new BlockPos(candidate.getX(),max+1,candidate.getZ());
  for(Map.Entry<BlockPos,String> e:plan.blocks.entrySet()){BlockPos p=site.anchor.offset(e.getKey());BlockState before=w.getBlockState(p);String bad=unsafe(w,p,before);if(bad!=null)return site.fail(bad,p);site.desired.put(p,resolve.apply(e.getValue()));site.old.put(p,before);}
  for(Map.Entry<BlockPos,String> e:plan.blocks.entrySet()){if(e.getKey().getY()!=0)continue;BlockPos base=site.anchor.offset(e.getKey()).below();int terrain=ground(w,base.getX(),base.getZ());if(terrain==NO_SURFACE||terrain==WATER)return site.fail(terrain==WATER?"water":"surface",base);if(site.anchor.getY()-1-terrain>4)return site.fail("slope",base);
   for(int y=terrain+1;y<site.anchor.getY();y++){BlockPos p=new BlockPos(base.getX(),y,base.getZ());BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null)return site.fail("container",p);if(before.liquid())return site.fail("water",p);if(!before.isAir()&&!before.canBeReplaced())return site.fail("obstruction",p);site.old.putIfAbsent(p,before);site.desired.put(p,Blocks.COARSE_DIRT.defaultBlockState());}}
  for(Map.Entry<BlockPos,String> e:plan.blocks.entrySet())if(e.getValue().equals("minecraft:brown_mushroom")){BlockPos p=site.anchor.offset(e.getKey()).below();BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null)return site.fail("container",p);if(!before.isAir()&&!before.canBeReplaced()&&!soil(before))return site.fail("obstruction",p);site.old.putIfAbsent(p,before);site.desired.put(p,Blocks.PODZOL.defaultBlockState());}
  return site;
 }
 private static final class Refused extends RuntimeException {final String reason;final BlockPos pos;Refused(String reason,BlockPos pos){super(reason+" at "+pos);this.reason=reason;this.pos=pos;}}
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
