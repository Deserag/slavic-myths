package org.slavicmyths.worldgen;

import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;

/** Native ticket preparation. Only the server thread touches the world; no blocking futures. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class SiteChunks implements AutoCloseable {
 private static final TicketType<Long> TICKET=TicketType.create("slavicmyths_site_build",Long::compare);
 private static final Map<ServerLevel,SiteChunks> IN_FLIGHT=new HashMap<>();
 private static final java.util.concurrent.atomic.AtomicLong NEXT_TOKEN=new java.util.concurrent.atomic.AtomicLong();
 private final long token=NEXT_TOKEN.incrementAndGet();
 private final ServerLevel world;private final Set<ChunkPos> held=new HashSet<>();
 private final Map<ChunkPos,Long> requested=new HashMap<>();private boolean closed;
 public SiteChunks(ServerLevel world){this.world=world;}
 public boolean ready(ChunkPos pos){
  if(closed)throw new IllegalStateException("Closed site preparation");
  if(world.getChunkSource().getChunkNow(pos.x,pos.z)!=null){
   hold(pos);requested.remove(pos);if(requested.isEmpty())IN_FLIGHT.remove(world,this);return true;
  }
  var since=requested.get(pos);
  if(since!=null){if(System.nanoTime()-since>60_000_000_000L)throw new IllegalStateException("Chunk preparation timed out: "+pos);return false;}
  var other=IN_FLIGHT.get(world);if(other!=null&&other!=this)return false;
  if(requested.size()>=4)return false;
  IN_FLIGHT.put(world,this);requested.put(pos,System.nanoTime());hold(pos);
  // A region ticket schedules FULL conversion and keeps the chunk accessible.
  // The normal chunk-cache tick does the work; getChunkNow is the only readiness test.
  return false;
 }
 public long budgetNanos(){return Math.clamp(20_000_000L-world.getServer().getAverageTickTimeNanos(),2_000_000L,8_000_000L);}
 public void prefetch(java.util.List<ChunkPos> footprint,int from){for(int i=from;i<Math.min(from+4,footprint.size());i++)ready(footprint.get(i));}
 private void hold(ChunkPos pos){if(held.add(pos))world.getChunkSource().addRegionTicket(TICKET,pos,0,token);}
 @Override public void close(){if(closed)return;closed=true;IN_FLIGHT.remove(world,this);requested.clear();for(var p:held)world.getChunkSource().removeRegionTicket(TICKET,p,0,token);held.clear();}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){IN_FLIGHT.keySet().removeIf(w->w.getServer()==e.getServer());}
}
