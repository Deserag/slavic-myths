package org.slavicmyths.mor;

import java.util.*;

/** Per-zone exposure, sampled by the existing ten-tick local query. No world access. */
public final class MorExposure {
 private record Entry(int time,int tick){}
 public record Sample(int time,boolean effects,int damage){}
 private final Map<UUID,Entry> entries=new HashMap<>();
 public Sample sample(UUID victim,int tick,boolean cloud){
  var old=entries.get(victim);
  if(old!=null&&old.tick==tick)return new Sample(old.time,false,0);
  int time=old==null?10:old.time+10;entries.put(victim,new Entry(time,tick));
  boolean effects=HigherRules.harms(cloud,time);
  return new Sample(time,effects,effects&&time%20==0?(cloud?2:1):0);
 }
 public void retain(Set<UUID> present){entries.keySet().retainAll(present);}
 public void clear(){entries.clear();}
}
