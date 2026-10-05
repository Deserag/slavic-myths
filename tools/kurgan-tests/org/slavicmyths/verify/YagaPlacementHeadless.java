package org.slavicmyths.verify;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.slavicmyths.yaga.*;
/** No client/server launch. Uses real vanilla survival/shape code and the production placement pipeline. */
public final class YagaPlacementHeadless {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 private static final Map<String,Block> custom=new HashMap<>();
 private static BlockState state(String id){try{return BlockStateParser.parseForBlock(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),id,false).blockState();}catch(Exception e){throw new IllegalStateException(id,e);}}
 private static final class WorldFixture implements InvocationHandler {
  final Map<BlockPos,BlockState> blocks=new HashMap<>();final LevelAccessor world;final int surface;boolean loaded=true,slope,legacyNeighbours;int reads,writes,failWrite=-1;
  WorldFixture(int surface){this.surface=surface;world=(LevelAccessor)Proxy.newProxyInstance(LevelAccessor.class.getClassLoader(),new Class<?>[]{LevelAccessor.class},this);}
  int surface(int x){return surface+(slope&&x>0?6:0);}
  BlockState get(BlockPos p){if(blocks.containsKey(p))return blocks.get(p);int top=surface(p.getX());return p.getY()< -64||p.getY()>top?Blocks.AIR.defaultBlockState():p.getY()==top?Blocks.GRASS_BLOCK.defaultBlockState():p.getY()== -64?Blocks.BEDROCK.defaultBlockState():Blocks.DIRT.defaultBlockState();}
  public Object invoke(Object proxy,Method m,Object[] a){switch(m.getName()){
   case "getBlockState":reads++;return get((BlockPos)a[0]);
   case "getFluidState":return get((BlockPos)a[0]).getFluidState();
   case "getBlockEntity":return get((BlockPos)a[0]).getBlock()==Blocks.CHEST?new net.minecraft.world.level.block.entity.ChestBlockEntity((BlockPos)a[0],get((BlockPos)a[0])):null;
   case "getHeight":if(a==null||a.length==0)return 256;int x=(Integer)a[1],z=(Integer)a[2],top=surface(x);for(Map.Entry<BlockPos,BlockState> e:blocks.entrySet())if(e.getKey().getX()==x&&e.getKey().getZ()==z&&!e.getValue().isAir())top=Math.max(top,e.getKey().getY());return top+1;
   case "getMinBuildHeight":return -64;
   case "getMaxBuildHeight":return 256;
   case "hasChunksAt":case "hasChunkAt":case "hasChunk":return loaded;
   case "setBlock":writes++;if(writes==failWrite)return false;BlockPos pos=((BlockPos)a[0]).immutable();BlockState s=(BlockState)a[1];int flags=(Integer)a[2];check(legacyNeighbours||flags==YagaPlacement.BUILD_FLAGS,"placement allowed neighbour destruction before supports");blocks.put(pos,s);
    if(legacyNeighbours&&(flags&16)==0)for(Direction direction:Direction.values()){BlockPos p=pos.relative(direction);BlockState old=get(p),next=Block.updateFromNeighbourShapes(old,world,p);if(!old.equals(next))blocks.put(p,next);}return true;
   case "getBrightness":case "getMaxLocalRawBrightness":case "getRawBrightness":return 15;
   case "getRandom":return net.minecraft.util.RandomSource.create(42);
   case "isEmptyBlock":return get((BlockPos)a[0]).isAir();
   case "isClientSide":return false;
   case "getShade":return 1F;
   case "getBlockTicks":case "getFluidTicks":return net.minecraft.world.ticks.BlackholeTickAccess.emptyLevelList();
   case "toString":return "isolated Yaga placement fixture";
   case "hashCode":return System.identityHashCode(proxy);
   case "equals":return proxy==a[0];
   default:if(m.getReturnType()==boolean.class)return false;if(m.getReturnType()==int.class)return 0;if(m.getReturnType()==long.class)return 0L;if(m.getReturnType()==void.class)return null;throw new UnsupportedOperationException(m.toString());
  }}
 }
 private static YagaPlacement.Site prepare(WorldFixture f,YagaHutPlan plan){return YagaPlacement.prepare(f.world,BlockPos.ZERO,plan,YagaPlacementHeadless::state);}
 private static void original(WorldFixture f,YagaPlacement.Site site){for(Map.Entry<BlockPos,BlockState> e:site.old.entrySet())check(f.get(e.getKey()).equals(e.getValue()),"partial house after rollback at "+e.getKey());}
 public static void main(String[] args)throws Exception{net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  for(String id:new String[]{"yaga_cauldron","yaga_chicken_leg","yaga_dried_herbs","yaga_bone_charm","ritual_candle","wormwood"})custom.put("slavicmyths:"+id,net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));
  YagaHutPlan plan=new YagaHutPlan();
  // Reproduce the former Y>4 bug and enforce valid surfaces all the way down to Y=0.
  for(int surface:new int[]{-60,-1,0,3,4,63}){WorldFixture f=new WorldFixture(surface);check(YagaPlacement.ground(f.world,0,0)==surface,"valid low surface rejected at Y="+surface);}
  for(int surface:new int[]{-60,-1,3,4,63}){WorldFixture f=new WorldFixture(surface);YagaPlacement.Site site=prepare(f,plan);check(site.failure==null,"preflight rejected flatland at "+surface);int[] spawns={0};YagaPlacement.Result r=YagaPlacement.commit(f.world,site,()->{spawns[0]++;check(f.get(site.anchor.offset(YagaHutPlan.HOME)).isAir(),"NPC spawned in solid block");return true;},()->{});check(r.ok,"flatland commit failed: "+r.reason+" at "+r.pos);check(spawns[0]==1,"NPC duplicated");
   check(f.get(site.anchor.offset(0,4,-6)).getBlock()==Blocks.SPRUCE_DOOR&&f.get(site.anchor.offset(0,5,-6)).getBlock()==Blocks.SPRUCE_DOOR,"door lost to neighbour updates");check(f.get(site.anchor.offset(3,6,-2)).getBlock()==Blocks.LANTERN,"lantern lost");check(f.get(site.anchor.offset(-7,0,1)).getBlock()==Blocks.BROWN_MUSHROOM,"mushroom lost");check(f.get(site.anchor.offset(-7,-1,1)).getBlock()==Blocks.PODZOL,"mushroom support lost");
   for(Map.Entry<BlockPos,BlockState> e:site.desired.entrySet())if(!e.getValue().isAir())check(f.get(e.getKey()).getBlock()==e.getValue().getBlock(),"silently missing hut block at "+e.getKey());}
  WorldFixture unloaded=new WorldFixture(3);unloaded.loaded=false;check(prepare(unloaded,plan).failure.reason.equals("chunks")&&unloaded.reads==0&&unloaded.writes==0,"forced unloaded terrain reads");
  WorldFixture slope=new WorldFixture(3);slope.slope=true;check(prepare(slope,plan).failure.reason.equals("slope")&&slope.writes==0,"steep site partially placed");
  WorldFixture water=new WorldFixture(3);water.blocks.put(new BlockPos(-9,5,-15),Blocks.WATER.defaultBlockState());check(prepare(water,plan).failure.reason.equals("water")&&water.writes==0,"water was overwritten");
  WorldFixture chest=new WorldFixture(3);chest.blocks.put(new BlockPos(0,8,0),Blocks.CHEST.defaultBlockState());check(prepare(chest,plan).failure.reason.equals("container")&&chest.writes==0,"container overwritten");
  WorldFixture obstruction=new WorldFixture(3);obstruction.blocks.put(new BlockPos(1,8,1),Blocks.STONE_BRICKS.defaultBlockState());check(prepare(obstruction,plan).failure.reason.equals("obstruction")&&obstruction.writes==0,"building overwritten");
  WorldFixture protectedSite=new WorldFixture(3);protectedSite.blocks.put(new BlockPos(1,8,1),custom.get("slavicmyths:yaga_chicken_leg").defaultBlockState());check(prepare(protectedSite,plan).failure.reason.equals("protected")&&protectedSite.writes==0,"protected mod block overwritten");
  WorldFixture legacy=new WorldFixture(3);legacy.legacyNeighbours=true;YagaPlacement.Site legacySite=prepare(legacy,plan);for(Map.Entry<BlockPos,BlockState> e:legacySite.desired.entrySet())legacy.world.setBlock(e.getKey(),e.getValue(),2);int lost=0;for(Map.Entry<BlockPos,BlockState> e:legacySite.desired.entrySet())if(!e.getValue().isAir()&&legacy.get(e.getKey()).isAir())lost++;check(lost>0,"legacy neighbour-update regression was not reproduced");
  for(boolean cancelled:new boolean[]{false,true}){WorldFixture f=new WorldFixture(3);YagaPlacement.Site site=prepare(f,plan);if(!cancelled){int index=0;for(BlockState expected:site.desired.values()){index++;if(!expected.isAir()){f.failWrite=index;break;}}}int[] spawns={0},undo={0};YagaPlacement.Result r=YagaPlacement.commit(f.world,site,()->{spawns[0]++;return false;},()->undo[0]++);check(!r.ok&&r.reason.equals(cancelled?"npc":"write"),"unexpected failure category: "+r.reason+" at "+r.pos);check(spawns[0]==(cancelled?1:0)&&undo[0]==1,"NPC spawned before preflight completion");original(f,site);}
  WorldFixture unsupported=new WorldFixture(3);YagaPlacement.Site bad=prepare(unsupported,plan);bad.desired.put(bad.anchor.offset(3,5,-2),Blocks.AIR.defaultBlockState());YagaPlacement.Result r=YagaPlacement.commit(unsupported.world,bad,()->{throw new AssertionError("NPC added to unsupported house");},()->{});check(!r.ok&&r.reason.equals("support"),"unsupported lantern accepted");original(unsupported,bad);
  System.out.println("PASS: real vanilla block-state placement on Y=3/4/63 flatland; Y=0 surface; complete doors/lantern/plants; no reads of unloaded chunks; slope/water/container/building rejection; write/spawn/support rollback. Executed against initialized target registries; no Minecraft client launched.");
 }
}
