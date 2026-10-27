package org.slavicmyths.chud;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.slavicmyths.worldgen.*;

/** One selected excavation, not a search through remote natural starts.
 * Pure geometry is rasterized on a worker. World reads/writes have a per-tick budget. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ChudCreationJobs {
 public record Result(BlockPos entrance,ChunkPos owner,BoundingBox bounds,int pieces){}
 private static final Map<ServerLevel,Job> JOBS=new HashMap<>();
 private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"slavicmyths-excavation-plan");t.setDaemon(true);t.setPriority(Thread.MIN_PRIORITY);return t;});
 record Section(BlockPos origin,BlockState[] states){}
 public static int create(CommandSourceStack source,String kind,Consumer<Result> callback){
  return create(source,kind,callback,false);
 }
 public static int createStory(CommandSourceStack source,Consumer<Result> callback){return create(source,"city",callback,true);}
 private static int create(CommandSourceStack source,String kind,Consumer<Result> callback,boolean story){
  var world=source.getLevel();if(world.dimension()!=net.minecraft.world.level.Level.OVERWORLD){source.sendFailure(net.minecraft.network.chat.Component.literal("Создание доступно только в обычном мире."));return 0;}var existing=JOBS.get(world);
  if(existing!=null){if(existing.kind.equals(kind)&&existing.story==story&&(!story||existing.origin.distSqr(BlockPos.containing(source.getPosition()))<64*64)){existing.listeners.add(callback);return 1;}source.sendFailure(Component.literal("Уже создаётся место Чуди. Дождитесь окончания."));return 0;}
  var job=new Job(source,kind,callback,story);JOBS.put(world,job);
  source.sendSuccess(()->Component.literal("Создаётся место Чуди порциями. Дальние структуры не разыскиваются."),false);return 1;
 }
 public static int control(CommandSourceStack source,boolean cancel){var j=JOBS.get(source.getLevel());if(j==null)return 0;if(cancel)j.finish(null,j.reserved?"Создание остановлено; частично изменённая область зарезервирована.":"Создание остановлено до записи; место не создано, резервирование освобождено.");else source.sendSuccess(()->Component.literal("Чудь: этап "+j.phase+", секция "+j.section+" / "+j.sections.size()),false);return 1;}
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.step();}
 @net.neoforged.bus.api.SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload e){if(e.getLevel() instanceof ServerLevel world){var j=JOBS.get(world);if(j!=null)j.finish(null,"Level is unloading.");}}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.finish(null,"Сервер останавливается.");}
 private static final class Job {
  final CommandSourceStack source;final ServerLevel world;final String kind;SiteChunks chunks;final List<Consumer<Result>> listeners=new ArrayList<>();final UUID id=UUID.randomUUID();
  final boolean story;final BlockPos origin;final long planningDeadline=System.nanoTime()+120_000_000_000L;final List<ChunkPos> explicitSites;int attempt;Future<StructurePiece> planned;Future<List<ChunkPos>> surveyed;List<ChunkPos> storySites=List.of();
  ChunkPos owner;final List<ChunkPos> footprint=new ArrayList<>();List<Section> sections=List.of();CompletableFuture<List<Section>> raster;java.util.concurrent.atomic.AtomicBoolean rasterCancelled;
  StructurePiece piece;Structure structure;BoundingBox box;BlockPos entrance;int phase,chunk,section,voxel;boolean reserved,adjusted;long notice;
  Job(CommandSourceStack source,String kind,Consumer<Result> callback,boolean story){this.source=source;world=source.getLevel();this.kind=kind;this.story=story;chunks=new SiteChunks(world);listeners.add(callback);origin=BlockPos.containing(source.getPosition());explicitSites=List.of(new ChunkPos(origin.offset(0,0,-256)),new ChunkPos(origin.offset(384,0,-256)),new ChunkPos(origin.offset(-384,0,-256)),new ChunkPos(origin.offset(0,0,384)),new ChunkPos(origin.offset(512,0,384)),new ChunkPos(origin.offset(-512,0,384)),new ChunkPos(origin.offset(768,0,0)),new ChunkPos(origin.offset(-768,0,0)));owner=candidateOwner();}
  ChunkPos candidateOwner(){return story?storySites.isEmpty()?new ChunkPos(origin):storySites.get(attempt):explicitSites.get(attempt);}
  void step(){try{
   if(!reserved&&System.nanoTime()>planningDeadline){finish(null,"Создание Чуди остановлено: общий лимит подготовки 120 секунд; блоки сооружения не записывались.");return;}
   if(world.getGameTime()>=notice){notice=world.getGameTime()+100;source.sendSuccess(()->Component.literal("Создание Чуди: площадка "+(attempt+1)+"/"+(story?Math.min(6,Math.max(1,storySites.size())):explicitSites.size())+", этап "+phase+", чанки "+chunk+"/"+footprint.size()+", секции "+section+"/"+sections.size()),false);}
   if(phase==0){
    if(story&&storySites.isEmpty()){
     if(surveyed==null){var generator=world.getChunkSource().getGenerator();var context=new Structure.GenerationContext(world.registryAccess(),generator,generator.getBiomeSource(),world.getChunkSource().randomState(),world.getStructureManager(),world.getSeed(),new ChunkPos(origin),world,b->true);surveyed=WORKER.submit(()->rankStorySites(context,origin));return;}
     if(!surveyed.isDone())return;try{storySites=surveyed.get();if(storySites.isEmpty())throw new IllegalStateException("Нет допустимых сюжетных кандидатов");owner=candidateOwner();}catch(Exception failure){throw new IllegalStateException("Предварительный отбор площадок завершился отказом",failure);}
    }
    if(!chunks.ready(owner))return;
    boolean city=kind.equals("city")||kind.equals("attached");var g=world.getChunkSource().getGenerator();
    var context=new Structure.GenerationContext(world.registryAccess(),g,g.getBiomeSource(),world.getChunkSource().randomState(),world.getStructureManager(),world.getSeed(),owner,world,b->true);
    if(planned==null){planned=WORKER.submit(()->{ChudSearchBudget.begin(System.nanoTime()+30_000_000_000L);try(var terrain=new ChudRegionPlanner.TerrainScope()){return city?ChudSimplePlacement.city(context,false,kind.equals("attached")):ChudSimplePlacement.abandoned(context,false,kind.equals("great"));}finally{ChudSearchBudget.end();}});return;}
    if(!planned.isDone())return;
    try{piece=planned.get();}catch(Exception failure){throw new IllegalStateException("Планирование площадки завершилось отказом",failure);}
    if(piece==null)throw new IllegalStateException("Подходящий подземный объём не найден у кандидата "+(attempt+1)+" в "+owner);
    // Underground complexes need no surface annex. Publish a surface X/Z landmark
    // for the quest/book, without pretending that a staircase has been built.
    var annex=access(piece);entrance=annex!=null?annex.mouth:new BlockPos(owner.getMiddleBlockX(),world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,owner.getMiddleBlockX(),owner.getMiddleBlockZ()),owner.getMiddleBlockZ());
    box=access(piece)==null?piece.getBoundingBox():BoundingBox.encapsulatingBoxes(List.of(piece.getBoundingBox(),access(piece).bounds)).orElseThrow();    if(box.minY()<world.getMinBuildHeight()+1||box.maxY()>=world.getMaxBuildHeight()||!world.getWorldBorder().isWithinBounds(new BlockPos(box.minX(),0,box.minZ()))||!world.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(),0,box.maxZ())))throw new IllegalStateException("Граница мира или высоты.");
    if(!ManualStructureRecords.get(world).overlapping(box).isEmpty())throw new IllegalStateException("Область занята сохранённой постройкой.");
    structure=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",city?"chud_city":"chud_abandoned"));
    phase=1;return;
   }
   if(phase==1){if(!adjusted){adjusted=true;var selected=piece;var cancelled=new java.util.concurrent.atomic.AtomicBoolean();rasterCancelled=cancelled;raster=CompletableFuture.supplyAsync(()->raster(selected,cancelled::get),WORKER);return;    }if(!raster.isDone())return;
    if(sections.isEmpty()){sections=raster.join();var needed=new LinkedHashSet<ChunkPos>();needed.add(owner);needed.add(new ChunkPos(entrance));for(var s:sections)needed.add(new ChunkPos(s.origin));footprint.clear();footprint.addAll(needed);}
    chunks.prefetch(footprint,chunk);if(chunk<footprint.size()){if(chunks.ready(footprint.get(chunk))){var at=footprint.get(chunk);var cover=piece instanceof ChudPiece city?city.roofCover():((AbandonedPiece)piece).roofCover();// Caves and ravines can remove modeled overburden after noise sampling.
            // Do not discard a valid underground hall: repair available air/fluid
            // above its roof during the final per-chunk placement instead.
            chunk++;}return;}
    phase=2;return;}
   long deadline=System.nanoTime()+chunks.budgetNanos();int operations=0;
   if(phase==2||phase==3){while(section<sections.size()&&operations<8192&&System.nanoTime()<deadline){var s=sections.get(section);if(phase==2&&voxel==0){if(org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(s.origin))throw new IllegalStateException("Защищённая область: "+s.origin.toShortString());var full=world.getChunkSource().getChunkNow(s.origin.getX()>>4,s.origin.getZ()>>4);var nativeSection=full.getSection(world.getSectionIndex(s.origin.getY()));if(!nativeSection.maybeHas(st->st.hasBlockEntity()||st.is(Blocks.BEDROCK)||st.is(net.minecraft.tags.BlockTags.LOGS)||!excavatable(st))){section++;continue;}}if(voxel==4096){voxel=0;section++;continue;}int i=voxel++;var next=s.states[i];if(next==null)continue;operations++;var p=s.origin.offset(i&15,i>>8,(i>>4)&15);var old=world.getBlockState(p);
     if(phase==2){if(world.getBlockEntity(p)!=null||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(p)||!excavatable(old)||old.is(Blocks.BEDROCK))throw new IllegalStateException("Защищённая постройка/блок: "+p.toShortString()+" / "+old);}
     else{if(!old.equals(next)){if(world.getBlockEntity(p)!=null||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(p)||!excavatable(old))throw new IllegalStateException("Площадка изменена игроком: "+p.toShortString());world.setBlock(p,next,2|16);}}
    }
    if(section==sections.size()){section=voxel=0;if(phase==2){if(!ManualStructureRecords.get(world).overlapping(box).isEmpty())throw new IllegalStateException("Область занята другим заданием во время подготовки");ManualStructureRecords.get(world).add(id,"construction",0,entrance,box);reserved=true;phase=3;}else{phase=4;chunk=0;}}return;
   }
   if(phase==4){if(chunk<footprint.size()){var pos=footprint.get(chunk++);var clip=new BoundingBox(pos.getMinBlockX(),world.getMinBuildHeight(),pos.getMinBlockZ(),pos.getMaxBlockX(),world.getMaxBuildHeight()-1,pos.getMaxBlockZ());if(piece instanceof ChudPiece p){if(p.repairsRoof())p.roofCover().repair(world,clip,pos);p.finishManual(world,clip,pos);}else {var p=(AbandonedPiece)piece;if(p.repairsRoof())p.roofCover().repair(world,clip,pos);p.finishManual(world,clip,pos);}return;}
    var pieces=new ArrayList<StructurePiece>();pieces.add(piece);if(access(piece)!=null)pieces.add(new ChudAccessPiece(access(piece)));
    var start=new StructureStart(structure,owner,0,new PiecesContainer(pieces));world.getChunkSource().getChunkNow(owner.x,owner.z).setStartForStructure(structure,start);
    for(var pos:footprint){var c=world.getChunkSource().getChunkNow(pos.x,pos.z);c.addReferenceForStructure(structure,owner.toLong());c.setUnsaved(true);}
    if(piece instanceof AbandonedPiece p)org.slavicmyths.underground.EncounterPopulation.data(world).plan(world,p.plan);
    else if(((ChudPiece)piece).abandoned!=null)org.slavicmyths.underground.EncounterPopulation.data(world).plan(world,((ChudPiece)piece).abandoned);
    ManualStructureRecords.get(world).replace(id,kind.equals("city")||kind.equals("attached")?"chud_city":"chud_abandoned",kind.equals("great")?2:0,entrance,box);finish(new Result(entrance,owner,box,pieces.size()),"Подземное место Чуди создано. Ориентир на поверхности X/Z: "+entrance.getX()+" / "+entrance.getZ()+"; найдите вход раскопкой.");
   }
  }catch(RuntimeException e){
   com.mojang.logging.LogUtils.getLogger().warn("Chud preflight phase {} at {}: {}",phase,owner,e.toString());
   if(phase<3&&!reserved&&attempt<(story?Math.min(6,storySites.size()):explicitSites.size())-1){cancelRaster();chunks.close();chunks=new SiteChunks(world);attempt++;owner=candidateOwner();planned=null;raster=null;rasterCancelled=null;piece=null;box=null;entrance=null;phase=chunk=section=voxel=0;adjusted=false;sections=List.of();footprint.clear();return;}
   // Both OP and story creation retry safe alternative sites only before reserving or writing blocks.
   finish(null,"Создание Чуди остановлено после "+(attempt+1)+" площадок: "+e.getMessage());}}
  void cancelRaster(){if(surveyed!=null&&!surveyed.isDone())surveyed.cancel(true);if(planned!=null&&!planned.isDone())planned.cancel(true);if(rasterCancelled!=null)rasterCancelled.set(true);if(raster!=null)raster.cancel(false);}
  void finish(Result result,String message){JOBS.remove(world);chunks.close();cancelRaster();if(result==null)source.sendFailure(Component.literal(message));else source.sendSuccess(()->Component.literal(message),false);for(var callback:listeners)try{callback.accept(result);}catch(RuntimeException e){com.mojang.logging.LogUtils.getLogger().warn("Chud completion callback",e);}}
 }
 /** Bounded seed-only ridge survey; no world chunks or placement attempts are requested here. */
 private static List<ChunkPos> rankStorySites(Structure.GenerationContext c,BlockPos origin){
  record Candidate(ChunkPos at,int minimum){}
  var sites=new ArrayList<Candidate>();int rotation=Math.floorMod(c.seed()^origin.asLong(),4);int[][] directions={{1,0},{0,1},{-1,0},{0,-1}};
  ChudSearchBudget.begin(System.nanoTime()+10_000_000_000L);
  try(var terrain=new ChudRegionPlanner.TerrainScope()){
   for(int radius:new int[]{1600,2200,2800})for(int direction=0;direction<4;direction++){
    var d=directions[(rotation+direction)%4];var at=new ChunkPos(origin.offset(d[0]*radius,0,d[1]*radius));int minimum=Integer.MAX_VALUE;
    for(int dx:new int[]{-96,0,96})for(int dz:new int[]{-96,0,96})minimum=Math.min(minimum,ChudRegionPlanner.surface(c,at.getMiddleBlockX()+dx,at.getMiddleBlockZ()+dz));
    sites.add(new Candidate(at,minimum));
   }
   sites.sort(Comparator.comparingInt(Candidate::minimum).reversed());return sites.stream().map(Candidate::at).toList();
  }finally{ChudSearchBudget.end();}
 }
 private static ChudSurfaceAccess access(StructurePiece piece){return piece instanceof ChudPiece c?c.access:((AbandonedPiece)piece).access;}
 private static List<Section> raster(StructurePiece piece,java.util.function.BooleanSupplier cancelled){
  ChudGeometry.checkCancelled(cancelled);
  var fills=new ArrayList<>(piece instanceof ChudPiece p?p.blueprint():((AbandonedPiece)piece).blueprint());var access=access(piece);if(access!=null)fills.addAll(access.blueprint());var decor=piece instanceof ChudPiece p?new ChudDecor(p.plan,p.abandoned):new ChudDecor(null,((AbandonedPiece)piece).plan);return raster(fills,decor,access==null?piece.getBoundingBox():BoundingBox.encapsulatingBoxes(List.of(piece.getBoundingBox(),access.bounds)).orElseThrow(),cancelled);
 }
 static List<Section> raster(List<ChudGeometry.Fill> fills,ChudDecor decor,BoundingBox box){return raster(fills,decor,box,()->false);}
 static List<Section> raster(List<ChudGeometry.Fill> fills,ChudDecor decor,BoundingBox box,java.util.function.BooleanSupplier cancelled){var result=new ArrayList<Section>();var columns=new ChudGeometry.ColumnIndex(fills,cancelled);
  for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)for(int y=box.minY()>>4;y<=box.maxY()>>4;y++){
   ChudGeometry.checkCancelled(cancelled);var column=columns.at(x,z);if(column.isEmpty())continue;
   var origin=new BlockPos(x*16,y*16,z*16);var clip=new BoundingBox(x*16,Math.max(y*16,box.minY()),z*16,x*16+15,Math.min(y*16+17,box.maxY()),z*16+15);var states=new BlockState[18*256];ChudGeometry.rasterize(column,clip,(p,s)->states[(p.getY()-origin.getY())*256+(p.getZ()&15)*16+(p.getX()&15)]=s);boolean used=false;
   for(int i=0;i<4096;i++){var s=states[i];if(s==null)continue;used=true;int px=origin.getX()+(i&15),pz=origin.getZ()+((i>>4)&15);boolean open=states[i+256]!=null&&states[i+256].isAir()&&states[i+512]!=null&&states[i+512].isAir();states[i]=decor.surface(s,px,origin.getY()+(i>>8),pz,decor.zone(px,pz),open);}
   if(used)result.add(new Section(origin,Arrays.copyOf(states,4096)));
  }return List.copyOf(result);
 }
 // Raw ore blocks occur naturally in large ore veins. Fences/cobweb/mossy cobble remain
 // ambiguous dungeon/player blocks and are intentionally protected.
 public static boolean excavatable(BlockState state){
  if(state.hasBlockEntity()||state.is(Blocks.BEDROCK)||state.is(Blocks.NETHER_PORTAL)||state.is(Blocks.END_PORTAL)||state.is(Blocks.END_GATEWAY)||state.is(Blocks.END_PORTAL_FRAME))return false;
  return state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock||ManualStructureJobs.natural(state)||state.is(Blocks.RAW_COPPER_BLOCK)||state.is(Blocks.RAW_IRON_BLOCK)||state.is(Blocks.BIG_DRIPLEAF)||state.is(Blocks.BIG_DRIPLEAF_STEM)||state.is(Blocks.CAVE_VINES)||state.is(Blocks.CAVE_VINES_PLANT)||state.is(Blocks.SPORE_BLOSSOM)||state.is(Blocks.HANGING_ROOTS)||state.is(Blocks.KELP)||state.is(Blocks.KELP_PLANT)||state.is(Blocks.SEAGRASS)||state.is(Blocks.TALL_SEAGRASS)||state.is(Blocks.SEA_PICKLE)||state.is(Blocks.BUBBLE_COLUMN)||state.is(net.minecraft.tags.BlockTags.CORAL_BLOCKS)||state.is(net.minecraft.tags.BlockTags.CORALS);
 }
 private ChudCreationJobs(){}
}
