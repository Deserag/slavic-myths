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
  var world=source.getLevel();if(world.dimension()!=net.minecraft.world.level.Level.OVERWORLD){source.sendFailure(net.minecraft.network.chat.Component.literal("Создание доступно только в обычном мире."));return 0;}var existing=JOBS.get(world);
  if(existing!=null){if(existing.kind.equals(kind)){existing.listeners.add(callback);return 1;}source.sendFailure(Component.literal("Уже создаётся место Чуди. Дождитесь окончания."));return 0;}
  var job=new Job(source,kind,callback);JOBS.put(world,job);
  source.sendSuccess(()->Component.literal("Создаётся место Чуди порциями. Дальние структуры не разыскиваются."),false);return 1;
 }
 public static int control(CommandSourceStack source,boolean cancel){var j=JOBS.get(source.getLevel());if(j==null)return 0;if(cancel)j.finish(null,"Создание остановлено; частично изменённая область зарезервирована.");else source.sendSuccess(()->Component.literal("Чудь: этап "+j.phase+", секция "+j.section+" / "+j.sections.size()),false);return 1;}
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.step();}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.finish(null,"Сервер останавливается.");}
 private static final class Job {
  final CommandSourceStack source;final ServerLevel world;final String kind;final BlockPos requestedFrom;SiteChunks chunks;final List<Consumer<Result>> listeners=new ArrayList<>();final UUID id=UUID.randomUUID();
  ChunkPos owner;int attempts;final List<ChunkPos> footprint=new ArrayList<>();List<Section> sections=List.of();CompletableFuture<List<Section>> raster;
  StructurePiece piece;Structure structure;BoundingBox box;BlockPos entrance;int phase,chunk,section,voxel;boolean reserved,adjusted;long notice;
  Job(CommandSourceStack source,String kind,Consumer<Result> callback){this.source=source;world=source.getLevel();this.kind=kind;chunks=new SiteChunks(world);listeners.add(callback);var origin=BlockPos.containing(source.getPosition());requestedFrom=origin;int occupied=(int)ManualStructureRecords.get(world).entries().stream().filter(e->e.family().equals("chud_city")||e.family().equals("chud_abandoned")||e.family().equals("construction")).count();owner=site(0);}
  ChunkPos site(int n){int[][] offsets={{0,-256},{256,0},{0,256},{-256,0},{384,384},{-384,-384}};var at=offsets[n];return new ChunkPos(requestedFrom.offset(at[0],0,at[1]));}
  void step(){try{
   if(world.getGameTime()>=notice){notice=world.getGameTime()+100;source.sendSuccess(()->Component.literal("Создание Чуди: этап "+phase+", чанки "+chunk+"/"+footprint.size()+", секции "+section+"/"+sections.size()),false);}
   if(phase==0){if(!chunks.ready(owner))return;int ground=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,owner.getMiddleBlockX(),owner.getMiddleBlockZ());int y=Math.max(world.getMinBuildHeight()+60,ground-90);long seed=world.getSeed()^owner.toLong();var at=new BlockPos(owner.getMiddleBlockX(),y,owner.getMiddleBlockZ());
    boolean city=kind.equals("city")||kind.equals("attached");int floor=ground-y;
    if(city){var plan=new ChudPlan(seed,at,0,0,floor);var extension=kind.equals("attached")?new AbandonedPlan(seed,at,0,AbandonedPlan.Kind.ATTACHED_SMALL,floor,plan.cityId(),0):null;piece=new ChudPiece(plan,extension);entrance=plan.world(plan.points.get(ChudPlan.Role.ENTRANCE));}
    else{var plan=new AbandonedPlan(seed,at,0,kind.equals("great")?AbandonedPlan.Kind.INDEPENDENT_GREAT:AbandonedPlan.Kind.INDEPENDENT_MEDIUM,floor,null,0);piece=new AbandonedPiece(plan);entrance=plan.world(plan.surfacePortal());}
    box=piece.getBoundingBox();if(box.minY()<world.getMinBuildHeight()+1||box.maxY()>=world.getMaxBuildHeight()||!world.getWorldBorder().isWithinBounds(new BlockPos(box.minX(),0,box.minZ()))||!world.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(),0,box.maxZ())))throw new IllegalStateException("Граница мира или высоты.");
    if(ManualStructureRecords.get(world).overlapping(box).stream().anyMatch(e->e.family().equals("construction")||e.family().startsWith("chud_")))throw new IllegalStateException("Область занята сохранённой постройкой.");
    structure=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",city?"chud_city":"chud_abandoned"));
    for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)footprint.add(new ChunkPos(x,z));
    phase=1;return;
   }
   if(phase==1){chunks.prefetch(footprint,chunk);if(chunk<footprint.size()){if(chunks.ready(footprint.get(chunk))){chunk++;}return;}if(!adjusted){adjusted=true;int surface=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,entrance.getX(),entrance.getZ())-1;
     if(piece instanceof ChudPiece c){var p=c.plan;var plan=new ChudPlan(p.seed,p.anchor,p.variant,p.rotation,surface-p.anchor.getY(),p.entranceBend);piece=new ChudPiece(plan,c.abandoned==null?null:new AbandonedPlan(p.seed,p.anchor,p.rotation,AbandonedPlan.Kind.ATTACHED_SMALL,plan.entranceFloor,plan.cityId(),0));entrance=plan.world(plan.points.get(ChudPlan.Role.ENTRANCE));}
     else{var p=((AbandonedPiece)piece).plan;var plan=new AbandonedPlan(p.seed,p.anchor,p.rotation,p.kind,surface-p.anchor.getY(),null,0);piece=new AbandonedPiece(plan);entrance=plan.world(plan.surfacePortal()).above();}
     box=piece.getBoundingBox();if(box.maxY()>=world.getMaxBuildHeight())throw new IllegalStateException("Entrance exceeds world height");raster=CompletableFuture.supplyAsync(()->raster(piece),WORKER);return;
    }if(!raster.isDone())return;sections=raster.join();phase=2;return;}
   long deadline=System.nanoTime()+chunks.budgetNanos();int operations=0;
   if(phase==2||phase==3){while(section<sections.size()&&operations<8192&&System.nanoTime()<deadline){var s=sections.get(section);if(phase==2&&voxel==0){var full=world.getChunkSource().getChunkNow(s.origin.getX()>>4,s.origin.getZ()>>4);var nativeSection=full.getSection(world.getSectionIndex(s.origin.getY()));if(!nativeSection.maybeHas(st->st.is(Blocks.BEDROCK)||st.is(net.minecraft.tags.BlockTags.LOGS)||!ManualStructureJobs.natural(st)&&st.getFluidState().isEmpty())){section++;continue;}}if(voxel==4096){voxel=0;section++;continue;}int i=voxel++;var next=s.states[i];if(next==null)continue;operations++;var p=s.origin.offset(i&15,i>>8,(i>>4)&15);var old=world.getBlockState(p);
     if(phase==2){if(world.getBlockEntity(p)!=null||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(p)||!ManualStructureJobs.natural(old)&&old.getFluidState().isEmpty()||old.is(Blocks.BEDROCK))throw new IllegalStateException("Защищённая постройка/блок: "+p.toShortString()+" / "+old);}
     else{if(!old.equals(next)){if(world.getBlockEntity(p)!=null||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(p)||!ManualStructureJobs.natural(old)&&old.getFluidState().isEmpty())throw new IllegalStateException("Площадка изменена игроком: "+p.toShortString());world.setBlock(p,next,2|16);}}
    }
    if(section==sections.size()){section=voxel=0;if(phase==2){if(ManualStructureRecords.get(world).overlapping(box).stream().anyMatch(e->e.family().equals("construction")||e.family().startsWith("chud_")))throw new IllegalStateException("Область занята другим заданием во время подготовки");ManualStructureRecords.get(world).add(id,"construction",0,entrance,box);reserved=true;phase=3;}else{phase=4;chunk=0;}}return;
   }
   if(phase==4){if(chunk<footprint.size()){var pos=footprint.get(chunk++);var clip=new BoundingBox(pos.getMinBlockX(),world.getMinBuildHeight(),pos.getMinBlockZ(),pos.getMaxBlockX(),world.getMaxBuildHeight()-1,pos.getMaxBlockZ());if(piece instanceof ChudPiece p)p.finishManual(world,clip,pos);else ((AbandonedPiece)piece).finishManual(world,clip,pos);return;}
    var start=new StructureStart(structure,owner,0,new PiecesContainer(List.of(piece)));world.getChunkSource().getChunkNow(owner.x,owner.z).setStartForStructure(structure,start);
    for(var pos:footprint){var c=world.getChunkSource().getChunkNow(pos.x,pos.z);c.addReferenceForStructure(structure,owner.toLong());c.setUnsaved(true);}
    if(piece instanceof AbandonedPiece p)org.slavicmyths.underground.EncounterPopulation.data(world).plan(world,p.plan);
    else if(((ChudPiece)piece).abandoned!=null)org.slavicmyths.underground.EncounterPopulation.data(world).plan(world,((ChudPiece)piece).abandoned);
    ManualStructureRecords.get(world).replace(id,kind.equals("city")||kind.equals("attached")?"chud_city":"chud_abandoned",kind.equals("great")?2:0,entrance,box);finish(new Result(entrance,owner,box,1),"Место Чуди создано: "+entrance.toShortString());
   }
  }catch(RuntimeException e){
   com.mojang.logging.LogUtils.getLogger().warn("Chud preflight phase {} attempt {} at {}: {}",phase,attempts,owner,e.toString());
   // A rejected preflight must select another site, never report an unbuilt plan as success.
   if(!reserved&&phase<3&&attempts++<5){chunks.close();if(raster!=null)raster.cancel(false);owner=site(attempts);chunks=new SiteChunks(world);footprint.clear();sections=List.of();raster=null;piece=null;box=null;phase=chunk=section=voxel=0;adjusted=false;return;}
   finish(null,"Создание Чуди остановлено: "+e.getMessage());}}
  void finish(Result result,String message){JOBS.remove(world);chunks.close();if(raster!=null)raster.cancel(false);if(result==null)source.sendFailure(Component.literal(message));else source.sendSuccess(()->Component.literal(message),false);for(var callback:listeners)try{callback.accept(result);}catch(RuntimeException e){com.mojang.logging.LogUtils.getLogger().warn("Chud completion callback",e);}}
 }
 private static List<Section> raster(StructurePiece piece){
  var fills=piece instanceof ChudPiece p?p.blueprint():((AbandonedPiece)piece).blueprint();var decor=piece instanceof ChudPiece p?new ChudDecor(p.plan,p.abandoned):new ChudDecor(null,((AbandonedPiece)piece).plan);return raster(fills,decor,piece.getBoundingBox());
 }
 static List<Section> raster(List<ChudGeometry.Fill> fills,ChudDecor decor,BoundingBox box){var result=new ArrayList<Section>();
  for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)for(int y=box.minY()>>4;y<=box.maxY()>>4;y++){
   var origin=new BlockPos(x*16,y*16,z*16);var clip=new BoundingBox(x*16,Math.max(y*16,box.minY()),z*16,x*16+15,Math.min(y*16+17,box.maxY()),z*16+15);var states=new BlockState[18*256];ChudGeometry.rasterize(fills,clip,(p,s)->states[(p.getY()-origin.getY())*256+(p.getZ()&15)*16+(p.getX()&15)]=s);boolean used=false;
   for(int i=0;i<4096;i++){var s=states[i];if(s==null)continue;used=true;int px=origin.getX()+(i&15),pz=origin.getZ()+((i>>4)&15);boolean open=states[i+256]!=null&&states[i+256].isAir()&&states[i+512]!=null&&states[i+512].isAir();states[i]=decor.surface(s,px,origin.getY()+(i>>8),pz,decor.zone(px,pz),open);}
   if(used)result.add(new Section(origin,Arrays.copyOf(states,4096)));
  }return List.copyOf(result);
 }
 private ChudCreationJobs(){}
}
