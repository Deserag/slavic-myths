package org.slavicmyths.hunt;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slavicmyths.worldgen.*;
/** Explicit guide requests create one local arena footprint, never search FULL chunks. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BossSiteJobs {
 private static final Map<ServerLevel,Job> JOBS=new HashMap<>();
 public static boolean create(ServerPlayer p,BossKind kind,Consumer<HuntRecords.Boss> callback){
  if(p.level().dimension()!=net.minecraft.world.level.Level.OVERWORLD)return false;var old=JOBS.get(p.serverLevel());if(old!=null){if(old.kind!=kind)return false;old.callbacks.add(callback);return true;}
  var world=p.serverLevel();BlockPos selected=null;
  // Biome metadata only: no candidate chunk is requested during selection.
  for(int i=0;i<24;i++){double a=i*2.399963229728653;int radius=1200+i*32;var at=new ChunkPos(p.blockPosition().offset((int)(Math.cos(a)*radius),0,(int)(Math.sin(a)*radius))).getMiddleBlockPosition(64);int rx=Math.floorDiv(at.getX(),512),rz=Math.floorDiv(at.getZ(),512);
   if(kind==BossKind.TUGARIN&&!BossRules.eligibleRegion(world.getSeed(),rx,rz,kind))continue;
   var biome=world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(at.getX()>>2,16,at.getZ()>>2,world.getChunkSource().randomState().sampler());
   if(world.getWorldBorder().isWithinBounds(at.offset(-5,0,-5))&&world.getWorldBorder().isWithinBounds(at.offset(5,0,5))&&BossAnchors.biome(world,biome,kind)){selected=at;break;}
  }
  if(selected==null)return false;JOBS.put(world,new Job(world,kind,selected,callback));return true;
 }
 private static final class Job {
  final ServerLevel world;final BossKind kind;final SiteChunks chunks;final List<Consumer<HuntRecords.Boss>> callbacks=new ArrayList<>();final List<BlockPos> positions=new ArrayList<>();BlockPos anchor;int phase,index;
  Job(ServerLevel world,BossKind kind,BlockPos at,Consumer<HuntRecords.Boss> callback){this.world=world;this.kind=kind;anchor=at;chunks=new SiteChunks(world);callbacks.add(callback);}
  void step(){try{
   if(phase==0){var existing=HuntRecords.get(world).bosses.get(BossAnchors.key(kind,Math.floorDiv(anchor.getX(),512),Math.floorDiv(anchor.getZ(),512)));if(existing!=null&&(existing.safe||existing.active)){finish(existing);return;}if(!chunks.ready(new ChunkPos(anchor)))return;int y=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,anchor.getX(),anchor.getZ());anchor=anchor.atY(y);var box=new net.minecraft.world.level.levelgen.structure.BoundingBox(anchor.getX()-4,y-2,anchor.getZ()-4,anchor.getX()+4,y+5,anchor.getZ()+4);if(!ManualStructureRecords.get(world).overlapping(box).isEmpty())throw new IllegalStateException("Occupied site");for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int dy=-2;dy<=5;dy++)positions.add(anchor.offset(x,dy,z));phase=1;return;}
   long until=System.nanoTime()+2_000_000;int ops=0;while(index<positions.size()&&ops++<128&&System.nanoTime()<until){var pos=positions.get(index++);var old=world.getBlockState(pos);if(world.getBlockEntity(pos)!=null||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(pos)||!ManualStructureJobs.natural(old)||old.is(Blocks.BEDROCK))throw new IllegalStateException("Protected site");if(phase==2)world.setBlock(pos,pos.getY()<anchor.getY()?(pos.getY()==anchor.getY()-1?Blocks.GRASS_BLOCK:Blocks.DIRT).defaultBlockState():Blocks.AIR.defaultBlockState(),2|16);}
   if(index<positions.size())return;if(phase==1){index=0;phase=2;return;}
   String key=BossAnchors.key(kind,Math.floorDiv(anchor.getX(),512),Math.floorDiv(anchor.getZ(),512));var data=HuntRecords.get(world);var boss=data.bosses.get(key);if(boss==null){boss=new HuntRecords.Boss(key,kind,anchor,true);boss.safe=true;data.bosses.put(key,boss);data.bossRegions.add(key);data.setDirty();}if(!boss.active){boss.anchor=anchor;boss.safe=true;data.setDirty();}if(kind!=BossKind.LIKHO||BossAnchors.night(world))BossAnchors.spawn(world,boss,null);finish(boss);
  }catch(RuntimeException e){com.mojang.logging.LogUtils.getLogger().warn("Boss site stopped",e);finish(null);}}
  void finish(HuntRecords.Boss result){JOBS.remove(world);chunks.close();for(var callback:callbacks)callback.accept(result);}
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.step();}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.finish(null);}
 private BossSiteJobs(){}
}
