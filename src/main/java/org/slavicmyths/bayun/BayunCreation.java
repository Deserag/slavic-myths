package org.slavicmyths.bayun;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slavicmyths.worldgen.*;
/** One shared, small site per dimension. Explicit requests only. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BayunCreation {
 private static final Map<ServerLevel,Job> JOBS=new HashMap<>();
 public static int create(net.minecraft.commands.CommandSourceStack source){
  var world=source.getLevel();if(world.dimension()!=net.minecraft.world.level.Level.OVERWORLD){source.sendFailure(net.minecraft.network.chat.Component.literal("Создание доступно только в обычном мире."));return 0;}if(JOBS.containsKey(world))return 1;var known=BayunHomes.get(world).nearest(BlockPos.containing(source.getPosition()));if(known!=null){source.sendSuccess(()->net.minecraft.network.chat.Component.literal("Баюн: "+known.position().toShortString()),false);return 1;}
  BlockPos selected=null;var origin=BlockPos.containing(source.getPosition());for(int i=0;i<24;i++){double angle=i*2.399963229728653;int radius=64+i*24;var at=new ChunkPos(origin.offset((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius))).getMiddleBlockPosition(64);var biome=world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(at.getX()>>2,16,at.getZ()>>2,world.getChunkSource().randomState().sampler());if(world.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null&&biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST)){selected=at;break;}}if(selected==null)selected=new ChunkPos(origin.offset(48,0,0)).getMiddleBlockPosition(64);if(selected==null){source.sendFailure(net.minecraft.network.chat.Component.literal("Поблизости нет подходящего лесного биома; поисковые чанки не загружаются."));return 0;}JOBS.put(world,new Job(source,selected));return 1;
 }
 private static final class Job {
  final net.minecraft.commands.CommandSourceStack source;final ServerLevel world;SiteChunks chunks;BlockPos anchor;final List<BlockPos> positions=new ArrayList<>();int phase,index,attempts;
  Job(net.minecraft.commands.CommandSourceStack source,BlockPos at){this.source=source;world=source.getLevel();chunks=new SiteChunks(world);anchor=at;}
  void step(){try{
   if(phase==0){if(!chunks.ready(new ChunkPos(anchor)))return;int y=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,anchor.getX(),anchor.getZ());anchor=anchor.atY(y);var box=new net.minecraft.world.level.levelgen.structure.BoundingBox(anchor.getX()-6,y-3,anchor.getZ()-6,anchor.getX()+6,y+4,anchor.getZ()+6);if(!world.getWorldBorder().isWithinBounds(anchor.offset(-6,0,-6))||!world.getWorldBorder().isWithinBounds(anchor.offset(6,0,6))||!ManualStructureRecords.get(world).overlapping(box).isEmpty())throw new IllegalStateException("Место занято/за границей мира");for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)for(int dy=-3;dy<=4;dy++)positions.add(anchor.offset(x,dy,z));phase=1;return;}
   long until=System.nanoTime()+2_000_000;int ops=0;while(index<positions.size()&&ops++<128&&System.nanoTime()<until){var pos=positions.get(index++);var old=world.getBlockState(pos);if(world.getBlockEntity(pos)!=null||!ManualStructureJobs.natural(old)||old.is(Blocks.BEDROCK)||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(pos))throw new IllegalStateException("Защищённая постройка");if(phase==2)world.setBlock(pos,(pos.getY()<anchor.getY()?pos.getY()==anchor.getY()-1?Blocks.GRASS_BLOCK:Blocks.DIRT:Blocks.AIR).defaultBlockState(),2|16);}
   if(index<positions.size())return;if(phase==1){phase=2;index=0;return;}boolean ok=BayunFeature.place(world,anchor);source.sendSuccess(()->net.minecraft.network.chat.Component.translatable(ok?"bayun.site.created":"bayun.site.blocked"),false);finish();
  }catch(RuntimeException failure){if(phase<2&&attempts++<8){chunks.close();chunks=new SiteChunks(world);anchor=new ChunkPos(BlockPos.containing(source.getPosition()).offset(48+(attempts%3)*32,0,(attempts/3)*32)).getMiddleBlockPosition(64);positions.clear();phase=index=0;return;}source.sendFailure(net.minecraft.network.chat.Component.literal("Место Баюна не создано: "+failure.getMessage()));finish();}}
  void finish(){chunks.close();JOBS.remove(world);}
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.step();}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.finish();}
 private BayunCreation(){}
}
