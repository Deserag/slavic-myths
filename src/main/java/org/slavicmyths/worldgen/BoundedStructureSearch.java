package org.slavicmyths.worldgen;
import java.util.*;
import java.util.function.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
/** Command searches never synchronously generate remote chunks. One ticket, 32 candidates, 30 seconds. */
@EventBusSubscriber(modid="slavicmyths")
public final class BoundedStructureSearch {
 private static final TicketType<Long> TICKET=TicketType.create("slavicmyths_search",Long::compare);
 private static final Map<ServerLevel,Job> ACTIVE=new HashMap<>();
 private record Candidate(Structure type,ChunkPos chunk){}
 public static boolean active(ServerLevel world){return ACTIVE.containsKey(world);}
 public static int start(CommandSourceStack source,List<Structure> types,Function<StructureStart,BlockPos> position,Predicate<BlockPos> allowed,Consumer<BlockPos> found){
  var world=source.getLevel();if(active(world)||ManualStructureJobs.active(world)){source.sendFailure(Component.literal("Уже выполняется поиск или генерация. Дождитесь окончания либо отмените задание."));return 0;}
  if(!world.getServer().getWorldData().worldGenOptions().generateStructures()){source.sendFailure(Component.literal("Естественная генерация структур отключена настройками мира. Ручное создание: /slavicmyths generate."));return 0;}
  var origin=BlockPos.containing(source.getPosition());var candidates=new ArrayList<Candidate>();var seen=new HashSet<String>();
  for(var type:types){if(type==null)continue;var placement=StructureCandidates.placement(world,type);if(placement==null)continue;
   for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){var chunk=placement.getPotentialStructureChunk(world.getSeed(),(origin.getX()>>4)+dx*placement.spacing(),(origin.getZ()>>4)+dz*placement.spacing());String key=System.identityHashCode(type)+":"+chunk.toLong();if(seen.add(key))candidates.add(new Candidate(type,chunk));}}
  if(candidates.isEmpty()){source.sendFailure(Component.literal("Природная генерация этой структуры выключена настройками мира. Для создания используйте /slavicmyths generate."));return 0;}
  candidates.sort(Comparator.comparingDouble(c->distance(origin,c.chunk.getMiddleBlockPosition(origin.getY()))));
  var job=new Job(source,List.copyOf(candidates.subList(0,Math.min(32,candidates.size()))),position,allowed,found);ACTIVE.put(world,job);source.sendSuccess(()->Component.literal("Поиск начат: до 32 кандидатов, максимум 30 секунд. /slavicmyths search status или cancel"),false);return 1;
 }
 public static int deliver(CommandSourceStack source,BlockPos pos,boolean load,Consumer<BlockPos> found){
  if(!load){found.accept(pos);return 1;}var world=source.getLevel();if(active(world)||ManualStructureJobs.active(world)){source.sendFailure(Component.literal("Уже выполняется поиск или генерация."));return 0;}
  var job=new Job(source,List.of(new Candidate(null,new ChunkPos(pos))),start->pos,p->true,found);job.known=pos;ACTIVE.put(world,job);source.sendSuccess(()->Component.literal("Загружается найденная структура для телепортации..."),false);return 1;
 }
 public static int status(CommandSourceStack source){var job=ACTIVE.get(source.getLevel());source.sendSuccess(()->Component.literal(job==null?"Активного поиска нет.":"Поиск: "+job.index+" / "+job.candidates.size()),false);return job==null?0:1;}
 public static int cancel(CommandSourceStack source){var job=ACTIVE.get(source.getLevel());if(job==null)return status(source);job.close();source.sendSuccess(()->Component.literal("Поиск отменён."),false);return 1;}
 @SubscribeEvent public static void tick(ServerTickEvent.Post event){for(var job:List.copyOf(ACTIVE.values()))if(job.world.getServer()==event.getServer())job.tick();}
 @SubscribeEvent public static void unload(LevelEvent.Unload event){if(event.getLevel() instanceof ServerLevel world){var job=ACTIVE.get(world);if(job!=null)job.close();}}
 private static double distance(BlockPos a,BlockPos b){double x=(double)a.getX()-b.getX(),z=(double)a.getZ()-b.getZ();return x*x+z*z;}
 private static final class Job {
  final CommandSourceStack source;final ServerLevel world;final List<Candidate> candidates;final Function<StructureStart,BlockPos> position;final Predicate<BlockPos> allowed;final Consumer<BlockPos> found;final long deadline=System.nanoTime()+30_000_000_000L;int index;ChunkPos ticket;BlockPos known;
  Job(CommandSourceStack source,List<Candidate> candidates,Function<StructureStart,BlockPos> position,Predicate<BlockPos> allowed,Consumer<BlockPos> found){this.source=source;world=source.getLevel();this.candidates=candidates;this.position=position;this.allowed=allowed;this.found=found;}
  void release(){if(ticket!=null){world.getChunkSource().removeRegionTicket(TICKET,ticket,0,ticket.toLong());ticket=null;}}
  void close(){release();ACTIVE.remove(world);}
  void tick(){try{
   if(index==candidates.size()||System.nanoTime()>=deadline){close();source.sendFailure(Component.literal("Ограниченный поиск завершён: структура не найдена. Мир не сканируется дальше. Для теста: /slavicmyths generate."));return;}
   var candidate=candidates.get(index);if(ticket==null){ticket=candidate.chunk;world.getChunkSource().addRegionTicket(TICKET,ticket,0,ticket.toLong());return;}
   var chunk=world.getChunkSource().getChunkNow(ticket.x,ticket.z);if(chunk==null)return;if(known!=null){found.accept(known);close();return;}var start=chunk.getStartForStructure(candidate.type);
   if(start!=null&&start.isValid()){var pos=position.apply(start);if(allowed.test(pos)){found.accept(pos);close();return;}}
   release();index++;
  }catch(RuntimeException e){close();source.sendFailure(Component.literal("Поиск остановлен: "+e.getMessage()));}}
 }
 private BoundedStructureSearch(){}
}
