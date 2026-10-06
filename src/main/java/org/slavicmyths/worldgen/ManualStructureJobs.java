package org.slavicmyths.worldgen;

import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import java.lang.reflect.Proxy;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slavicmyths.kurgan.*;
import org.slavicmyths.bandit.*;

/** Manual placement is a bounded server-thread job, independent of native rarity and biome settings. */
@EventBusSubscriber(modid="slavicmyths")
public final class ManualStructureJobs {
 public static final int TERRAIN_OPERATIONS=8192, SCAN_OPERATIONS=8192;
 private static final TicketType<Long> TICKET=TicketType.create("slavicmyths_manual",Long::compare);
 private static final Map<ServerLevel,Job> ACTIVE=new HashMap<>();
 public record Result(boolean success,String message,BlockPos entrance,int surfaceY,long changedBlocks,long peakOperations,long peakNanos,String slowestPhase,long ticksElapsed){}
 private static final Map<UUID,Result> RESULTS=new LinkedHashMap<>();
 public static Result result(UUID id){return RESULTS.get(id);}
 public static boolean active(ServerLevel world){return ACTIVE.containsKey(world);}
 public static UUID lastJob;
 public static int start(CommandSourceStack source,String family,int tier,long seed){
  ServerLevel world=source.getLevel();if(!world.dimension().equals(Level.OVERWORLD)){source.sendFailure(Component.literal("Наземные структуры создаются в обычном мире."));return 0;}
  if(active(world)||BoundedStructureSearch.active(world)){source.sendFailure(Component.literal("Генерация уже выполняется. /slavicmyths generation status или cancel"));return 0;}
  try{Job job=new Job(source,family,tier,seed);ACTIVE.put(world,job);lastJob=job.id;source.sendSuccess(()->Component.literal("Подготавливается территория: "+family+" / "+tier+". Задание "+job.id),false);return 1;}
  catch(RuntimeException e){source.sendFailure(Component.literal("Ошибка плана структуры: "+e.getMessage()));return 0;}
 }
 public static int status(CommandSourceStack source){Job job=ACTIVE.get(source.getLevel());source.sendSuccess(()->Component.literal(job==null?"Активной генерации нет.":job.progress()),false);return job==null?0:1;}
 public static int cancel(CommandSourceStack source){Job job=ACTIVE.get(source.getLevel());if(job==null)return status(source);job.finish(false,"Генерация отменена; уже подготовленный грунт сохранён.");return 1;}
 @SubscribeEvent public static void tick(ServerTickEvent.Post event){for(Job job:List.copyOf(ACTIVE.values()))if(job.world.getServer()==event.getServer())job.step();}
 @SubscribeEvent public static void unload(LevelEvent.Unload event){if(event.getLevel() instanceof ServerLevel world){Job job=ACTIVE.get(world);if(job!=null)job.finish(false,"Генерация остановлена при выгрузке мира; подготовленный грунт сохранён.");}}
 private enum Phase{PREPARE_CHUNKS,SURVEY,SCAN,BUILD_PIECES,PREFLIGHT,TERRAFORM,PLACE_STRUCTURE,FINALIZE}
 private static int surface(ServerLevel world,int x,int z){
  if(world.getChunkSource().getChunkNow(x>>4,z>>4)==null)throw new IllegalStateException("Heightmap requested before FULL chunk is available");
  int y=world.getHeight(Heightmap.Types.WORLD_SURFACE,x,z)-1;
  while(y>world.getMinBuildHeight()){var state=world.getBlockState(new BlockPos(x,y,z));if(!state.getFluidState().isEmpty())break;if(LandTerrain.vegetation(state))y--;else break;}
  return y;
 }
 private static boolean wetSurface(ServerLevel world,int x,int z,int y){var pos=new BlockPos(x,y,z);var state=world.getBlockState(pos);return !state.getFluidState().isEmpty()||state.is(Blocks.ICE)&&!world.getFluidState(pos.below()).isEmpty();}
 private static boolean natural(BlockState s){
  return s.isAir()||LandTerrain.vegetation(s)||s.getBlock() instanceof BushBlock||s.is(Blocks.PUMPKIN)||s.is(Blocks.MELON)||s.is(Blocks.ICE)||s.canBeReplaced()||s.is(BlockTags.BASE_STONE_OVERWORLD)||s.is(BlockTags.DIRT)||s.is(BlockTags.LOGS)||s.is(BlockTags.LEAVES)||s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.GRAVEL)||s.is(Blocks.SAND)||s.is(Blocks.SANDSTONE)||s.is(Blocks.CLAY)||s.is(Blocks.SNOW)||s.is(Blocks.SNOW_BLOCK)||s.is(Blocks.BEDROCK)||s.is(Blocks.CALCITE)||s.is(Blocks.COAL_ORE)||s.is(Blocks.IRON_ORE)||s.is(Blocks.COPPER_ORE)||s.is(Blocks.GOLD_ORE)||s.is(Blocks.REDSTONE_ORE)||s.is(Blocks.LAPIS_ORE)||s.is(Blocks.DIAMOND_ORE)||s.is(Blocks.EMERALD_ORE)||s.is(Blocks.DEEPSLATE_COAL_ORE)||s.is(Blocks.DEEPSLATE_IRON_ORE)||s.is(Blocks.DEEPSLATE_COPPER_ORE)||s.is(Blocks.DEEPSLATE_GOLD_ORE)||s.is(Blocks.DEEPSLATE_REDSTONE_ORE)||s.is(Blocks.DEEPSLATE_LAPIS_ORE)||s.is(Blocks.DEEPSLATE_DIAMOND_ORE)||s.is(Blocks.DEEPSLATE_EMERALD_ORE);
 }
 private static final class Job {
  final CommandSourceStack source;final ServerLevel world;final String family;final int tier;final long seed;final UUID id=UUID.randomUUID();final KurganPlan kurgan;final KurganPlan.Box planBounds;
  final java.util.concurrent.CompletableFuture<Void> templateLoad;
  final List<StructurePiece> pieces=new ArrayList<>();final Set<ChunkPos> tickets=new LinkedHashSet<>();final List<ChunkPos> requested=new ArrayList<>();
  Phase phase=Phase.PREPARE_CHUNKS;BlockPos anchor,entrance;BoundingBox core,bounds,placedBounds;final List<BoundingBox> protectedBoxes=new ArrayList<>();int loadIndex,waitTicks,column,voxelY=Integer.MIN_VALUE,target,width,depth,surveyRound,relocation;int[] ground,top,clearTop;long changed,peakOperations,peakNanos,chunkWaitStart,nextProgress,startedTick;String slowestPhase="";boolean done;
  final List<BoundingBox> clips=new ArrayList<>();int clipIndex;
  Job(CommandSourceStack source,String family,int tier,long seed){this.source=source;world=source.getLevel();this.family=family;this.tier=tier;this.seed=seed;startedTick=world.getGameTime();nextProgress=startedTick+100;kurgan=family.equals("kurgan")?KurganPlan.create(tier,seed):null;planBounds=kurgan==null?null:kurgan.bounds();var templates=world.getStructureManager();templateLoad=family.equals("kurgan")?java.util.concurrent.CompletableFuture.completedFuture(null):java.util.concurrent.CompletableFuture.runAsync(()->{
   // StructureTemplateManager is also used by native worldgen workers. No world/chunk APIs here.
   for(String name:ManualCampLayout.templateNames(tier))templates.getOrCreate(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",(tier==2?"stronghold/":"bandit/")+name));});var pos=BlockPos.containing(source.getPosition());anchor=new BlockPos(pos.getX(),0,pos.getZ()-(kurgan==null?96+(tier==2?84:44):128+kurgan.radius));setCore();prepare(core);}
  void setCore(){if(kurgan!=null){var b=planBounds;core=new BoundingBox(anchor.getX()+b.x0,world.getMinBuildHeight(),anchor.getZ()+b.z0,anchor.getX()+b.x1,world.getMaxBuildHeight()-1,anchor.getZ()+b.z1);}else{int size=tier==0?28:tier==1?46:84;core=new BoundingBox(anchor.getX(),world.getMinBuildHeight(),anchor.getZ(),anchor.getX()+size,world.getMaxBuildHeight()-1,anchor.getZ()+size);}}
  void prepare(BoundingBox box){
   if(!world.getWorldBorder().isWithinBounds(new BlockPos(box.minX(),0,box.minZ()))||!world.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(),0,box.maxZ())))throw new IllegalStateException("Область выходит за границу мира.");
   requested.clear();for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)requested.add(new ChunkPos(x,z));requested.sort(Comparator.comparingLong(p->{long dx=p.x-(anchor.getX()>>4),dz=p.z-(anchor.getZ()>>4);return dx*dx+dz*dz;}));loadIndex=0;waitTicks=0;phase=Phase.PREPARE_CHUNKS;
  }
  String progress(){String stage=switch(phase){case PREPARE_CHUNKS->"Загрузка территории";case SURVEY->"Выбор площадки";case SCAN->"Планирование грунта";case BUILD_PIECES->"Подготовка шаблонов";case PREFLIGHT->"Проверка конечного места";case TERRAFORM->"Подготовка грунта";case PLACE_STRUCTURE->"Размещение структуры";case FINALIZE->"Завершение";};int complete=phase==Phase.PREPARE_CHUNKS?loadIndex:phase==Phase.PLACE_STRUCTURE?clipIndex:column;int total=phase==Phase.PREPARE_CHUNKS?requested.size():phase==Phase.PLACE_STRUCTURE?clips.size():ground==null?0:ground.length;return stage+(total>0?": "+Math.min(100,complete*100L/total)+"%":"")+", изменено блоков: "+changed;}
  void step(){if(done)return;if(world.getGameTime()>=nextProgress){nextProgress=world.getGameTime()+100;source.sendSuccess(()->Component.literal(progress()),false);}long begin=System.nanoTime();Phase measuredPhase=phase;try{
   switch(phase){case PREPARE_CHUNKS->load();case SURVEY->survey();case SCAN->scan();case BUILD_PIECES->{if(templateLoad.isDone()){templateLoad.join();buildPieces();protectedBoxes.clear();for(var entry:ManualStructureRecords.get(world).overlapping(bounds))if(entry.bounds().intersects(bounds))protectedBoxes.add(entry.bounds());for(var instance:BurialRecords.get(world).overlapping(bounds)){var b=instance.plan.bounds();var box=new BoundingBox(instance.origin.getX()+b.x0,instance.origin.getY()+b.y0,instance.origin.getZ()+b.z0,instance.origin.getX()+b.x1,instance.origin.getY()+b.y1,instance.origin.getZ()+b.z1);if(box.intersects(bounds))protectedBoxes.add(box);}column=0;voxelY=Integer.MIN_VALUE;phase=Phase.PREFLIGHT;}}case PREFLIGHT->preflight();case TERRAFORM->terraform();case PLACE_STRUCTURE->place();case FINALIZE->{ManualStructureRecords.get(world).add(id,family,tier,entrance,placedBounds);finish(true,"Структура создана: "+entrance.toShortString());}}
  }catch(RuntimeException e){finish(false,"Генерация остановлена: "+e.getMessage());}finally{long elapsed=System.nanoTime()-begin;if(elapsed>peakNanos){peakNanos=elapsed;slowestPhase=measuredPhase.name();}}}
  void load(){
   if(loadIndex==requested.size()){if(surveyRound==0){phase=Phase.SURVEY;column=0;}else initialize();return;}
   ChunkPos pos=requested.get(loadIndex);if(tickets.add(pos)){world.getChunkSource().addRegionTicket(TICKET,pos,0,pos.toLong());chunkWaitStart=System.nanoTime();}
   if(world.getChunkSource().getChunkNow(pos.x,pos.z)!=null){
    if(surveyRound==0&&pos.equals(new ChunkPos(anchor))){int y=surface(world,anchor.getX(),anchor.getZ());if(wetSurface(world,anchor.getX(),anchor.getZ(),y)){relocate();return;}}
    loadIndex++;waitTicks=0;chunkWaitStart=System.nanoTime();}else if(++waitTicks>1200&&System.nanoTime()-chunkWaitStart>60_000_000_000L)throw new IllegalStateException("Не удалось загрузить чанк "+pos);
  }
  void relocate(){
    if(++relocation>16)throw new IllegalStateException("Нет свободной сухой площадки в локальном радиусе 256 блоков.");
    for(ChunkPos pos:tickets)world.getChunkSource().removeRegionTicket(TICKET,pos,0,pos.toLong());tickets.clear();
    int r=(relocation+3)/4*64,n=(relocation-1)%4;var base=BlockPos.containing(source.getPosition());int baseZ=base.getZ()-(kurgan==null?96+(tier==2?84:44):128+kurgan.radius);anchor=new BlockPos(base.getX()+(n==0?r:n==1?-r:0),0,baseZ+(n==2?r:n==3?-r:0));setCore();surveyRound=0;column=0;ground=null;top=null;clearTop=null;pieces.clear();clips.clear();clipIndex=0;protectedBoxes.clear();prepare(core);source.sendSuccess(()->Component.literal("Проверяется другая площадка: "+relocation+" / 16"),false);
  }
  void survey(){
   var ys=new ArrayList<Integer>();int wet=0,total=0;
   for(int x=core.minX();x<=core.maxX();x+=8)for(int z=core.minZ();z<=core.maxZ();z+=8){int y=surface(world,x,z);var state=world.getBlockState(new BlockPos(x,y,z));if(y<world.getMinBuildHeight())throw new IllegalStateException("Нет основания в пределах высоты мира.");if(wetSurface(world,x,z,y))wet++;ys.add(y);total++;}
   int center=surface(world,anchor.getX(),anchor.getZ());if(wet>total/8||wetSurface(world,anchor.getX(),anchor.getZ(),center)){
    relocate();return;
   }
   Collections.sort(ys);target=ys.get(ys.size()/2);if(kurgan!=null)target=Math.max(target,world.getMinBuildHeight()+2-planBounds.y0);else target=Math.max(target,world.getMinBuildHeight()+(tier==2?7:1));
   if(target+(kurgan==null?32:planBounds.y1)>=world.getMaxBuildHeight()-2)throw new IllegalStateException("Конструкция выходит за высоту мира.");anchor=anchor.atY(target);
   int raise=Math.max(0,target-center),margin=Math.max(12,Math.max(Math.abs(target+(kurgan==null?0:3)-ys.getFirst()),Math.abs(target+(kurgan==null?0:3)-ys.getLast()))+8);int endZ=core.maxZ()+margin;
   if(kurgan!=null){endZ=Math.max(endZ,anchor.getZ()+kurgan.radius+12+raise);for(var room:kurgan.rooms){var b=room.box();if(b.y1<0&&b.x0<=1&&b.x1>=-1)endZ=Math.max(endZ,anchor.getZ()+b.z1+Math.max(0,target+b.y1+2-center)+4);}}
   bounds=new BoundingBox(core.minX()-margin,world.getMinBuildHeight(),core.minZ()-margin,core.maxX()+margin,world.getMaxBuildHeight()-1,endZ);
   var planned=new BoundingBox(bounds.minX(),Math.min(ys.getFirst(),kurgan==null?target-(tier==2?5:0):target+planBounds.y0),bounds.minZ(),bounds.maxX(),Math.max(ys.getLast()+4,kurgan==null?target+32:target+planBounds.y1),bounds.maxZ());
   if(!ManualStructureRecords.get(world).overlapping(planned).isEmpty()){relocate();return;}
   for(var instance:BurialRecords.get(world).overlapping(planned)){var b=instance.plan.bounds();var existing=new BoundingBox(instance.origin.getX()+b.x0,instance.origin.getY()+b.y0,instance.origin.getZ()+b.z0,instance.origin.getX()+b.x1,instance.origin.getY()+b.y1,instance.origin.getZ()+b.z1);if(existing.intersects(planned)){relocate();return;}}
   surveyRound=1;prepare(bounds);
  }
  void initialize(){
   width=bounds.getXSpan();depth=bounds.getZSpan();ground=new int[width*depth];top=new int[ground.length];clearTop=new int[ground.length];column=0;phase=Phase.SCAN;
   source.sendSuccess(()->Component.literal("Чанки загружены. Подготовка окончательного положения на Y="+target),false);
  }
  int x(int index){return bounds.minX()+index/depth;}int z(int index){return bounds.minZ()+index%depth;}
  void scan(){
   long deadline=System.nanoTime()+2000000;int count=0;while(column<ground.length&&count++<128&&System.nanoTime()<deadline){int x=x(column),z=z(column),g=surface(world,x,z),surface=world.getHeight(Heightmap.Types.WORLD_SURFACE,x,z)-1;
    int distance=Math.max(Math.max(core.minX()-x,x-core.maxX()),Math.max(core.minZ()-z,z-core.maxZ()));int plane=target+(kurgan==null?0:3);
    int t=distance<=0?plane:g+Integer.signum(plane-g)*Math.max(0,Math.abs(plane-g)-distance);
    ground[column]=g;top[column]=t;clearTop[column]=Math.max(surface,t+4);column++;
   }
   if(column==ground.length){
    if(kurgan!=null){int start=kurgan.radius+1;int end=bounds.maxZ()-anchor.getZ();int[] ramp=new int[end-start+1];
     for(int dz=start;dz<=end;dz++){int i=(anchor.getX()-bounds.minX())*depth+anchor.getZ()+dz-bounds.minZ();int y=Math.max(ground[i],target+3-Math.max(0,dz-kurgan.radius-7));for(var r:kurgan.rooms){var b=r.box();if(b.y1<0&&b.x0<=1&&b.x1>=-1&&dz>=b.z0&&dz<=b.z1)y=Math.max(y,target+b.y1+2);}for(var l:kurgan.links)for(int j=0;j<l.steps.size();j++){var b=l.slice(j);if(b.y1<0&&b.x0<=1&&b.x1>=-1&&dz>=b.z0&&dz<=b.z1)y=Math.max(y,target+b.y1+2);}ramp[dz-start]=y;}
     for(int i=ramp.length-2;i>=0;i--)ramp[i]=Math.max(ramp[i],ramp[i+1]-1);for(int i=1;i<ramp.length;i++)ramp[i]=Math.max(ramp[i],ramp[i-1]-1);
     for(int dx=-1;dx<=1;dx++)for(int dz=start;dz<=end;dz++){int i=(anchor.getX()+dx-bounds.minX())*depth+anchor.getZ()+dz-bounds.minZ();top[i]=Math.min(ramp[dz-start],target+Math.max(0,dz-start-3));clearTop[i]=Math.max(clearTop[i],top[i]+4);}
    }
    // Check block entities over the FINAL affected footprint before any mutation.
    for(ChunkPos c:requested){LevelChunk chunk=world.getChunkSource().getChunkNow(c.x,c.z);if(chunk==null)throw new IllegalStateException("Загруженный чанк недоступен");for(var p:chunk.getBlockEntities().keySet())if(p.getX()>=bounds.minX()&&p.getX()<=bounds.maxX()&&p.getZ()>=bounds.minZ()&&p.getZ()<=bounds.maxZ()){
      int index=(p.getX()-bounds.minX())*depth+p.getZ()-bounds.minZ();boolean soil=p.getY()>=Math.min(ground[index],top[index])&&p.getY()<=clearTop[index];
      boolean building=kurgan!=null?planBounds.contains(p.getX()-anchor.getX(),p.getY()-target,p.getZ()-anchor.getZ()):core.isInside(p.atY(core.minY()))&&p.getY()>=target-(tier==2?5:0)&&p.getY()<=target+32;
      if(soil||building){relocate();return;}}}
    phase=Phase.BUILD_PIECES;
   }
  }
  void buildPieces(){
   if(kurgan!=null){var soil=KurganEarthwork.prepared(bounds.minX()-anchor.getX(),bounds.minZ()-anchor.getZ(),width,depth,ground,top);var piece=new KurganDungeonPiece(kurgan,anchor,id,soil);pieces.add(piece);entrance=piece.arrival();}
   else {pieces.addAll(ManualCampLayout.create(world,tier,anchor,id,seed));entrance=anchor.offset(tier==2?39:10,1,2);}
   BoundingBox box=pieces.get(0).getBoundingBox();box=new BoundingBox(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ());for(var p:pieces)box.encapsulate(p.getBoundingBox());
   placedBounds=box;for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)for(int y=Math.max(world.getMinBuildHeight(),box.minY());y<=box.maxY();y+=8)clips.add(new BoundingBox(x*16,y,z*16,x*16+15,Math.min(y+7,box.maxY()),z*16+15));
  }
  int preflightFloor(int index){int floor=Math.min(ground[index],top[index]);if(core.isInside(new BlockPos(x(index),core.minY(),z(index))))floor=Math.min(floor,kurgan==null?target-(tier==2?5:0):target+planBounds.y0);return floor;}
  int preflightCeiling(int index){int ceiling=clearTop[index];if(core.isInside(new BlockPos(x(index),core.minY(),z(index))))ceiling=Math.max(ceiling,kurgan==null?target+32:target+planBounds.y1);return ceiling;}
  void preflight(){
   long deadline=System.nanoTime()+2000000;int ops=0;while(column<ground.length&&ops<SCAN_OPERATIONS&&(ops%64!=0||System.nanoTime()<deadline)){if(voxelY==Integer.MIN_VALUE){voxelY=preflightFloor(column);int ceiling=preflightCeiling(column),px=x(column),pz=z(column);for(var box:protectedBoxes)if(px>=box.minX()&&px<=box.maxX()&&pz>=box.minZ()&&pz<=box.maxZ()&&voxelY<=box.maxY()&&ceiling>=box.minY()){relocate();return;}}if(voxelY>preflightCeiling(column)){column++;voxelY=Integer.MIN_VALUE;continue;}
    var pos=new BlockPos(x(column),voxelY,z(column));var old=world.getBlockState(pos);if(!natural(old)&&old.getFluidState().isEmpty()){relocate();return;}voxelY++;ops++;
   }peakOperations=Math.max(peakOperations,ops);if(column==ground.length){column=0;voxelY=Integer.MIN_VALUE;phase=Phase.TERRAFORM;}
  }
  void terraform(){
   long deadline=System.nanoTime()+2000000;int ops=0;while(column<ground.length&&ops<TERRAIN_OPERATIONS&&(ops%64!=0||System.nanoTime()<deadline)){int x=x(column),z=z(column);if(voxelY==Integer.MIN_VALUE)voxelY=Math.min(ground[column],top[column]);
    if(voxelY>clearTop[column]){if(kurgan!=null)KurganSurface.decorate(world,anchor,new BlockPos(x,top[column],z),kurgan.seed,kurgan.radius,bounds);column++;voxelY=Integer.MIN_VALUE;continue;}var pos=new BlockPos(x,voxelY,z);BlockState old=world.getBlockState(pos);
    if(!natural(old)&&old.getFluidState().isEmpty())throw new IllegalStateException("Защищённый строительный блок: "+pos.toShortString()+" / "+old);
    if(!old.is(Blocks.BEDROCK)){BlockState next=voxelY>top[column]?Blocks.AIR.defaultBlockState():voxelY==top[column]?Blocks.GRASS_BLOCK.defaultBlockState():voxelY<top[column]-4?Blocks.STONE.defaultBlockState():Blocks.DIRT.defaultBlockState();if(kurgan!=null&&voxelY<=top[column]){int skin=top[column];for(int neighbor:new int[]{column-depth,column+depth,column-1,column+1})if(neighbor>=0&&neighbor<top.length&&Math.abs(x(neighbor)-x)+Math.abs(z(neighbor)-z)==1)skin=Math.min(skin,top[neighbor]);if(voxelY>=skin&&!(Math.abs(x-anchor.getX())<=1&&z-anchor.getZ()>=kurgan.radius+1))next=KurganSurface.block(world,anchor,pos,kurgan.seed,kurgan.radius,tier,top[column]-skin,voxelY==top[column]).defaultBlockState();}if(!old.equals(next)){world.setBlock(pos,next,2);changed++;}}
    voxelY++;ops++;
   }peakOperations=Math.max(peakOperations,ops);if(column==ground.length){phase=Phase.PLACE_STRUCTURE;source.sendSuccess(()->Component.literal("Территория подготовлена. Размещается структура..."),false);}
  }
  void place(){
   if(clipIndex==clips.size()){phase=Phase.FINALIZE;return;}BoundingBox clip=clips.get(clipIndex++);ChunkPos chunk=new ChunkPos(clip.minX()>>4,clip.minZ()>>4);
   // One 16x16x8 section per tick, not an entire dungeon in one command call.
   long[] writes={0};WorldGenLevel counted=(WorldGenLevel)Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(),new Class<?>[]{WorldGenLevel.class},(proxy,method,args)->{if(method.getName().equals("setBlock"))writes[0]++;try{return method.invoke(world,args);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}});
   for(var piece:pieces)if(piece.getBoundingBox().intersects(clip)){if(piece instanceof KurganDungeonPiece k)k.placePrepared(counted,world.structureManager(),world.getChunkSource().getGenerator(),RandomSource.create(seed),clip,chunk);else piece.postProcess(counted,world.structureManager(),world.getChunkSource().getGenerator(),RandomSource.create(seed),clip,chunk,anchor);}
   peakOperations=Math.max(peakOperations,writes[0]);if(writes[0]>65536)throw new IllegalStateException("Placement section exceeded operation limit");
  }
  void finish(boolean success,String text){if(done)return;done=true;ACTIVE.remove(world);for(ChunkPos pos:tickets)world.getChunkSource().removeRegionTicket(TICKET,pos,0,pos.toLong());RESULTS.put(id,new Result(success,text,entrance,target,changed,peakOperations,peakNanos,slowestPhase,world.getGameTime()-startedTick));while(RESULTS.size()>32)RESULTS.remove(RESULTS.keySet().iterator().next());if(success)source.sendSuccess(()->Component.literal(text),false);else source.sendFailure(Component.literal(text));}
 }
 private ManualStructureJobs(){}
}
