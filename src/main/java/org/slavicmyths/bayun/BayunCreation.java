package org.slavicmyths.bayun;

import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.worldgen.*;
import org.slavicmyths.yaga.YagaPlacement;

/** Explicit requests share one bounded, transactional nook construction per dimension. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BayunCreation {
 private static final Map<ServerLevel,Job> JOBS=new HashMap<>();
 public static int create(CommandSourceStack source){
  var world=source.getLevel();if(world.dimension()!=Level.OVERWORLD){source.sendFailure(Component.literal("Создание доступно только в обычном мире."));return 0;}
  var origin=BlockPos.containing(source.getPosition());var known=nearby(world,origin);
  if(known!=null){
   if(world.getChunkSource().getChunkNow(known.position().getX()>>4,known.position().getZ()>>4)!=null&&!(world.getEntity(known.entity()) instanceof BayunEntity cat&&cat.isAlive())){
    source.sendFailure(Component.literal("Место Баюна записано, но кот пока не подтверждён в загруженной области. Новый дом не создаётся."));refresh(source);return 0;
   }
   source.sendSuccess(()->Component.literal("Известное место Баюна: "+known.position().toShortString()),false);refresh(source);return 1;
  }
  var existing=JOBS.get(world);if(existing!=null){
   if(existing.origin.distSqr(origin)>1024.*1024){source.sendFailure(Component.literal("Сейчас создаётся место Баюна в другой области. Дождитесь окончания."));return 0;}
   existing.listen(source);return 1;
  }
  var candidates=new ArrayList<BlockPos>();var loaded=new ArrayList<BlockPos>();
  for(int i=0;i<24;i++){
   double angle=i*2.399963229728653;int radius=64+i*24;var at=new ChunkPos(origin.offset((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius))).getMiddleBlockPosition(64);
   if(!world.getWorldBorder().isWithinBounds(at.offset(-6,0,-6))||!world.getWorldBorder().isWithinBounds(at.offset(6,0,6)))continue;
   var biome=world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(at.getX()>>2,16,at.getZ()>>2,world.getChunkSource().randomState().sampler());
   if(!biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST))continue;
   (world.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null?candidates:loaded).add(at);
  }
  candidates.addAll(loaded);
  if(candidates.isEmpty()){source.sendFailure(Component.literal("Нет подходящего лесного места в локальном радиусе. Дальние чанки не перебираются."));return 0;}
  JOBS.put(world,new Job(source,origin,candidates));source.sendSuccess(()->Component.literal("Подготавливается место Баюна. Карта обновится после создания дома и кота."),false);return 1;
 }
 private static BayunHomes.Home nearby(ServerLevel world,BlockPos origin){var home=BayunHomes.get(world).nearest(origin);return home!=null&&home.position().distSqr(origin)<=1024.*1024?home:null;}
 private static void refresh(CommandSourceStack source){if(source.getEntity() instanceof ServerPlayer p)org.slavicmyths.guides.GuideDiscovery.refresh(p);}
 private static final class Job {
  final CommandSourceStack source;final ServerLevel world;final BlockPos origin;final List<BlockPos> candidates;final Set<UUID> listeners=new LinkedHashSet<>();final UUID token=UUID.randomUUID();
  final long deadline=System.nanoTime()+180_000_000_000L;
  SiteChunks chunks;int candidate;YagaPlacement.Cursor cursor;boolean reserved,stopping;String reason="surface";
  Job(CommandSourceStack source,BlockPos origin,List<BlockPos> candidates){this.source=source;world=source.getLevel();this.origin=origin;this.candidates=List.copyOf(candidates);chunks=new SiteChunks(world);listen(source);}
  void listen(CommandSourceStack source){if(source.getEntity() instanceof ServerPlayer p)listeners.add(p.getUUID());}
  void step(){try{
   if(cursor!=null){
    cursor.step(128,2_000_000);if(!cursor.done())return;
    if(cursor.result.ok){finish(true,"Место Баюна создано: "+cursor.result.pos.toShortString());return;}
    reason=cursor.result.reason;
    if(reason.equals("rollback")){finish(false,"Откат места Баюна завершён не полностью; область остаётся зарезервированной.");return;}
    if(reserved){ManualStructureRecords.get(world).remove(token);reserved=false;}
    cursor=null;if(stopping){finish(false,"Создание Баюна отменено.");return;}next();
   }
   if(System.nanoTime()>deadline){finish(false,"Время подготовки места Баюна истекло. Дом и кот не подтверждены.");return;}
   if(candidate>=candidates.size()){finish(false,"Место Баюна не создано: "+reason);return;}
   if(nearby(world,origin)!=null){finish(false,"Найдено другое записанное место Баюна; повторное строительство остановлено.");return;}
   var at=candidates.get(candidate);if(!chunks.ready(new ChunkPos(at)))return;
   var site=BayunPlacement.prepare(world,at);
   if(site.failure!=null){reason=site.failure.reason;next();return;}
   var box=new BoundingBox(at.getX()-6,site.anchor.getY()-5,at.getZ()-6,at.getX()+6,site.anchor.getY()+3,at.getZ()+6);
   if(!ManualStructureRecords.get(world).overlapping(box).isEmpty()){reason="occupied";next();return;}
   var cat=BayunContent.ENTITY.get().create(world);if(cat==null){reason="npc";next();return;}
   cat.home=site.anchor;cat.moveTo(site.anchor.getX()+.5,site.anchor.getY(),site.anchor.getZ()+.5,0,0);
   ManualStructureRecords.get(world).add(token,"construction",0,site.anchor,box);reserved=true;
   cursor=new YagaPlacement.Cursor(world,site,()->{
    if(nearby(world,origin)!=null)return false;
    if(!world.addFreshEntity(cat))return false;
    ManualStructureRecords.get(world).replace(token,"site/bayun",0,site.anchor,box);return true;
   },()->{cat.discard();BayunHomes.get(world).forget(site.anchor,cat.getUUID());},result->{});
  }catch(RuntimeException failure){
   com.mojang.logging.LogUtils.getLogger().warn("Bayun site failed safely",failure);reason="internal";
   if(cursor!=null&&!cursor.done()){stopping=true;cursor.abort();return;}
   finish(false,"Место Баюна не создано: "+failure.getMessage());
  }}
  void next(){chunks.close();chunks=new SiteChunks(world);candidate++;}
  void finish(boolean ok,String message){chunks.close();JOBS.remove(world,this);if(ok)source.sendSuccess(()->Component.literal(message),false);else source.sendFailure(Component.literal(message));for(var id:listeners){var p=world.getServer().getPlayerList().getPlayer(id);if(p!=null&&p.serverLevel()==world)org.slavicmyths.guides.GuideDiscovery.refresh(p);}}
  void stop(){stopping=true;if(cursor!=null&&!cursor.done()){cursor.abort();while(!cursor.done())cursor.step(65536,1_000_000_000L);if(!cursor.result.reason.equals("rollback")&&reserved)ManualStructureRecords.get(world).remove(token);}finish(false,"Создание Баюна остановлено.");}
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.step();}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var j:List.copyOf(JOBS.values()))if(j.world.getServer()==e.getServer())j.stop();}
 private BayunCreation(){}
}
