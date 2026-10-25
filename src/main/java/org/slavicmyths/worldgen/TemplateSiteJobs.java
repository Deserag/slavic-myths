package org.slavicmyths.worldgen;
import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
/** Small template sites use one selected footprint and bounded preparation/placement. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class TemplateSiteJobs {
 private static final Map<ServerLevel,Job> JOBS=new HashMap<>();
 public static int create(CommandSourceStack source,String id){return create(source,id,false);}
 public static int create(CommandSourceStack source,String id,boolean next){
  var world=source.getLevel();if(world.dimension()!=net.minecraft.world.level.Level.OVERWORLD){source.sendFailure(net.minecraft.network.chat.Component.literal("Создание доступно только в обычном мире."));return 0;}var existing=ManualStructureRecords.get(world).entries().stream().filter(e->e.family().equals("site/"+id)&&e.arrival().distSqr(BlockPos.containing(source.getPosition()))<=1024.*1024).min(Comparator.comparingDouble(e->e.arrival().distSqr(BlockPos.containing(source.getPosition()))));
  if(!next&&existing.isPresent()){source.sendSuccess(()->Component.literal(id+": "+existing.get().arrival().toShortString()),false);return 1;}
  if(JOBS.containsKey(world)){source.sendFailure(Component.literal("Создание небольшой постройки уже выполняется."));return 0;}
  String template=switch(id){case "barracks"->"military/barracks";case "swamp_hut"->"swamp/v097_abandoned_0";case "abandoned_settlement"->"swamp/v097_cluster_0";case "bog_causeway"->"swamp/v097_boardwalk_0";case "flooded_shrine"->"swamp/v097_shrine_0";case "fishing_camp"->"swamp/v097_fisher_0";case "underwater_ruins"->"swamp/ruin_0";case "swamp_watchtower"->"swamp/v097_watchtower_0";default->null;};
  if(template==null){source.sendFailure(Component.literal("Неизвестная постройка: "+id));return 0;}
  try{JOBS.put(world,new Job(source,id,template));return 1;}catch(RuntimeException failure){source.sendFailure(Component.literal(failure.getMessage()));return 0;}
 }
 private static final class Job {
  final CommandSourceStack source;final ServerLevel world;final String id,name;final UUID uuid=UUID.randomUUID();final SiteChunks chunks;final List<ChunkPos> footprint=new ArrayList<>();final List<BoundingBox> clips=new ArrayList<>();final java.util.concurrent.CompletableFuture<StructureTemplate> template;
  BlockPos origin;BoundingBox box;StructurePiece piece;int phase,chunk,index,columnY=Integer.MIN_VALUE,clip;
  Job(CommandSourceStack s,String id,String name){source=s;world=s.getLevel();this.id=id;this.name=name;chunks=new SiteChunks(world);origin=new ChunkPos(BlockPos.containing(s.getPosition()).offset(64,0,-96)).getMiddleBlockPosition(64);
   if(!id.equals("barracks")){var type=org.slavicmyths.swamp.SwampStructures.get(world,id);BlockPos selected=null;for(int i=0;i<24;i++){double a=i*2.399963229728653;int radius=96+i*32;var at=new ChunkPos(BlockPos.containing(s.getPosition()).offset((int)(Math.cos(a)*radius),0,(int)(Math.sin(a)*radius))).getMiddleBlockPosition(64);var biome=world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(at.getX()>>2,16,at.getZ()>>2,world.getChunkSource().randomState().sampler());if(type!=null&&type.biomes().contains(biome)&&ManualStructureRecords.get(world).overlapping(new BoundingBox(at.getX()-16,world.getMinBuildHeight(),at.getZ()-16,at.getX()+64,world.getMaxBuildHeight()-1,at.getZ()+64)).isEmpty()){selected=at;break;}}if(selected==null)throw new IllegalStateException("Нет подходящего биома в локальном радиусе; чанки не перебираются");origin=selected;}
var manager=world.getStructureManager();template=java.util.concurrent.CompletableFuture.supplyAsync(()->manager.getOrCreate(ResourceLocation.fromNamespaceAndPath("slavicmyths",name)));}
  void step(){try{
   if(phase==0){if(!chunks.ready(new ChunkPos(origin))||!template.isDone())return;var t=template.join();var size=t.getSize();if(size.getX()==0)throw new IllegalStateException("Нет шаблона "+name);int y=world.getHeight(id.equals("underwater_ruins")?Heightmap.Types.OCEAN_FLOOR:Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,origin.getX(),origin.getZ())-1;if(id.equals("underwater_ruins")&&world.getHeight(Heightmap.Types.WORLD_SURFACE,origin.getX(),origin.getZ())-y<4)throw new IllegalStateException("Недостаточная глубина воды");origin=origin.atY(y);box=new BoundingBox(origin.getX(),y-12,origin.getZ(),origin.getX()+size.getX()-1,y+size.getY()-1,origin.getZ()+size.getZ()-1);if(!world.getWorldBorder().isWithinBounds(new BlockPos(box.minX(),0,box.minZ()))||!world.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(),0,box.maxZ()))||box.minY()<world.getMinBuildHeight()||box.maxY()>=world.getMaxBuildHeight()||!ManualStructureRecords.get(world).overlapping(box).isEmpty())throw new IllegalStateException("Место занято или выходит за границы мира");for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++){footprint.add(new ChunkPos(x,z));for(int yy=y;yy<=box.maxY();yy+=2)clips.add(new BoundingBox(x*16,yy,z*16,x*16+15,Math.min(yy+1,box.maxY()),z*16+15));}if(!id.equals("barracks"))piece=new org.slavicmyths.swamp.SwampPiece(world.getStructureManager(),name.substring(6),origin,world.getSeed()^origin.asLong());phase=1;return;}
   if(phase==1){if(chunk<footprint.size()){if(chunks.ready(footprint.get(chunk)))chunk++;return;}phase=2;}
   if(phase==2||phase==3){int width=box.getXSpan(),depth=box.getZSpan(),ops=0;long until=System.nanoTime()+2_000_000;while(index<width*depth&&ops++<512&&System.nanoTime()<until){int x=box.minX()+index/depth,z=box.minZ()+index%depth;if(columnY==Integer.MIN_VALUE){int ground=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1;if(!id.equals("underwater_ruins")&&Math.abs(ground-origin.getY())>12)throw new IllegalStateException("Рельеф требует изменения более 12 блоков");columnY=id.equals("underwater_ruins")?origin.getY():Math.min(ground,origin.getY());}if(columnY>box.maxY()){index++;columnY=Integer.MIN_VALUE;continue;}var p=new BlockPos(x,columnY++,z);var old=world.getBlockState(p);if(world.getBlockEntity(p)!=null||!ManualStructureJobs.natural(old)||old.is(Blocks.BEDROCK)||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(p))throw new IllegalStateException("Защищённая постройка: "+p.toShortString());if(phase==3&&!id.equals("underwater_ruins"))world.setBlock(p,(p.getY()<=origin.getY()?Blocks.DIRT:Blocks.AIR).defaultBlockState(),2|16);}
    if(index<width*depth)return;if(phase==2){phase=3;index=0;columnY=Integer.MIN_VALUE;ManualStructureRecords.get(world).add(uuid,"construction",0,origin,box);}else phase=4;return;
   }
   if(phase==4){if(clip<clips.size()){var bounds=clips.get(clip++);var pos=new ChunkPos(bounds.minX()>>4,bounds.minZ()>>4);if(piece!=null)piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(world.getSeed()),bounds,pos,origin);else template.join().placeInWorld(world,origin,origin,new StructurePlaceSettings().setBoundingBox(bounds).addProcessor(JigsawReplacementProcessor.INSTANCE),world.random,2|16);return;}ManualStructureRecords.get(world).replace(uuid,"site/"+id,0,origin.above(),box);source.sendSuccess(()->Component.literal(id+" создано: "+origin.toShortString()),false);finish();}
  }catch(RuntimeException e){source.sendFailure(Component.literal("Создание остановлено: "+e.getMessage()));finish();}}
  void finish(){chunks.close();JOBS.remove(world);}
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.step();}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.finish();}
 private TemplateSiteJobs(){}
}
