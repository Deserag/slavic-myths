package org.slavicmyths.worldgen;

import java.util.*;
import java.util.concurrent.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Prepare an already selected construction footprint, never search for structures.
 * One native FULL future in flight per dimension; native waiting is off the tick thread. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class SiteChunks implements AutoCloseable {
 private static final ExecutorService DISPATCH=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"slavicmyths-site-chunks");t.setDaemon(true);t.setPriority(Thread.MIN_PRIORITY);return t;});
 private static final TicketType<Long> TICKET=TicketType.create("slavicmyths_site_build",Long::compare);
 private static final Map<ServerLevel,CompletableFuture<?>> IN_FLIGHT=new HashMap<>();
 private final ServerLevel world;
 private final Set<ChunkPos> held=new HashSet<>();
 private CompletableFuture<LevelChunk> pending;private ChunkPos requested;private boolean closed;
 public SiteChunks(ServerLevel world){this.world=world;}
 /** Poll from a bounded construction task. Never joins or requests synchronous terrain. */
 public boolean ready(ChunkPos pos){
  if(closed)throw new IllegalStateException("Closed site preparation");
  var loaded=world.getChunkSource().getChunkNow(pos.x,pos.z);
  if(loaded!=null){hold(pos);return true;}
  if(pending!=null){
   if(!pending.isDone())return false;
   if(pending.isCompletedExceptionally())pending.join(); // completed failure only, never waits
   pending=null;requested=null;
   return false;
  }
  var busy=IN_FLIGHT.get(world);if(busy!=null&&!busy.isDone())return false;
  requested=pos;var result=new CompletableFuture<LevelChunk>();pending=result;var gate=new CompletableFuture<Void>();IN_FLIGHT.put(world,gate);
  // getChunkFuture marshals to the server. Calling it there directly can managedBlock;
  // dispatch it here, and consume completion without a tick-thread get/join.
  DISPATCH.execute(()->{
   try{world.getChunkSource().getChunkFuture(pos.x,pos.z,ChunkStatus.FULL,true).whenComplete((value,error)->world.getServer().execute(()->{
    gate.complete(null);IN_FLIGHT.remove(world,gate);
    if(closed){result.cancel(false);return;}
    if(error!=null){result.completeExceptionally(error);return;}
    var chunk=value==null?null:value.orElse(null);
    if(!(chunk instanceof LevelChunk full)){result.completeExceptionally(new IllegalStateException("Construction chunk unavailable: "+pos));return;}
    hold(pos);result.complete(full);
   }));}catch(RuntimeException failure){world.getServer().execute(()->{gate.complete(null);IN_FLIGHT.remove(world,gate);result.completeExceptionally(failure);});}
  });return false;
 }
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){IN_FLIGHT.keySet().removeIf(w->w.getServer()==e.getServer());}
 private void hold(ChunkPos pos){if(held.add(pos))world.getChunkSource().addRegionTicket(TICKET,pos,0,pos.toLong());}
 @Override public void close(){if(closed)return;closed=true;for(var pos:held)world.getChunkSource().removeRegionTicket(TICKET,pos,0,pos.toLong());held.clear();if(pending!=null)pending.cancel(false);}
}
