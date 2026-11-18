package org.slavicmyths.verify;
/** Test-only read of the real ticket manager, not a count of intended close() calls. */
final class TicketEvidence300 {
 static int siteBuild(net.minecraft.server.level.ServerLevel world){try{
  var field=net.minecraft.server.level.DistanceManager.class.getDeclaredField("tickets");field.setAccessible(true);
  var tickets=(java.util.Map<?,?>)field.get(world.getChunkSource().chunkMap.getDistanceManager());int count=0;
  for(var set:tickets.values())for(var value:(Iterable<?>)set)if(((net.minecraft.server.level.Ticket<?>)value).getType().toString().equals("slavicmyths_site_build"))count++;
  return count;
 }catch(ReflectiveOperationException x){throw new IllegalStateException("Cannot inspect actual pinned ticket manager",x);}}
 private TicketEvidence300(){}
}
