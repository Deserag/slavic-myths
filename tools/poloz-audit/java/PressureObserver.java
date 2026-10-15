package org.slavicmyths.polozaudit;
import java.io.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import java.lang.reflect.*;
import net.minecraft.client.Minecraft;import net.minecraft.client.particle.*;import net.minecraft.client.resources.sounds.SoundInstance;import net.minecraft.client.sounds.SoundEngine;
/** Test-only synchronous counters; no effect interception/cancellation. Runtime-class type for direct terrain adds. */
public final class PressureObserver {
 static volatile String scene="bootstrap";static volatile int age;public static String type;public static final ThreadLocal<String> currentSound=new ThreadLocal<>();
 record Birth(String key,int age){}
 static final IdentityHashMap<Particle,Birth> live=new IdentityHashMap<>();
 static final Map<String,long[]> counts=new TreeMap<>();static final Map<String,String> callers=new ConcurrentSkipListMap<>();
 static final ConcurrentMap<String,AtomicLong> sounds=new ConcurrentHashMap<>(),stops=new ConcurrentHashMap<>(),failures=new ConcurrentHashMap<>();static final AtomicLong destroys=new AtomicLong(),poolWarnings=new AtomicLong();static int duplicates;static boolean enabled;
 static long[] row(String key){return counts.computeIfAbsent(key,k->new long[5]);}
 public static void born(Particle p){if(!enabled||p==null)return;String key=type==null?"class:"+p.getClass().getSimpleName():type;var old=live.put(p,new Birth(key,age));if(old!=null)duplicates++;row(key)[0]++;if(!callers.containsKey("particle:"+key))callers.put("particle:"+key,trace());}
 static String trace(){var b=new StringBuilder();for(var f:Thread.currentThread().getStackTrace())if(!f.getClassName().contains("polozaudit")&&!f.getClassName().equals("java.lang.Thread")&&!f.getMethodName().startsWith("handler$")){b.append(f).append(" <- ");if(b.length()>1100)break;}return b.toString();}
 public static void sound(SoundInstance s){if(!enabled)return;String key=s.getLocation().toString();currentSound.set(key);sounds.computeIfAbsent(key,k->new AtomicLong()).incrementAndGet();if(!callers.containsKey("sound:"+key))callers.put("sound:"+key,trace());}
 public static void soundStop(SoundInstance s){if(enabled&&s!=null)stops.computeIfAbsent(s.getLocation().toString(),k->new AtomicLong()).incrementAndGet();}
 public static void soundEnd(SoundInstance s){if(enabled&&s!=null)stops.computeIfAbsent("native_end:"+s.getLocation(),k->new AtomicLong()).incrementAndGet();}
 public static void destroy(net.minecraft.world.entity.Entity source){if(enabled&&source instanceof org.slavicmyths.poloz.GreatPoloz)destroys.incrementAndGet();}
 static Object field(Object o,String name){try{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}catch(Exception e){throw new RuntimeException(e);}}
 static void installLogger(){var logger=(org.apache.logging.log4j.core.Logger)org.apache.logging.log4j.LogManager.getRootLogger();var a=new org.apache.logging.log4j.core.appender.AbstractAppender("PolozPressure",null,null,false,org.apache.logging.log4j.core.config.Property.EMPTY_ARRAY){public void append(org.apache.logging.log4j.core.LogEvent e){if(!enabled)return;String m=e.getMessage().getFormattedMessage();if(m.contains("Maximum sound pool size"))poolWarnings.incrementAndGet();if(m.contains("Failed to create new sound handle"))failures.computeIfAbsent(String.valueOf(currentSound.get()),k->new AtomicLong()).incrementAndGet();}};a.start();logger.addAppender(a);}
 static void reset(String name){scene=name;age=0;enabled=true;live.clear();counts.clear();callers.clear();sounds.clear();stops.clear();failures.clear();duplicates=0;destroys.set(0);poolWarnings.set(0);}
 static void snapshot(PrintWriter out){var engine=Minecraft.getInstance().particleEngine;Map<?,?> groups=(Map<?,?>)field(engine,"particles");Set<Particle> present=Collections.newSetFromMap(new IdentityHashMap<>());for(Object q:groups.values())for(Object p:(Collection<?>)q)present.add((Particle)p);for(Object p:(Collection<?>)field(engine,"particlesToAdd"))present.add((Particle)p);
  for(var it=live.entrySet().iterator();it.hasNext();){var e=it.next();if(!present.contains(e.getKey())&&age-e.getValue().age()>1){var r=row(e.getValue().key());r[1]++;r[2]+=age-e.getValue().age();it.remove();}}
  Map<String,Integer> alive=new TreeMap<>();for(var p:present){var b=live.get(p);String key=b==null?"preexisting:"+p.getClass().getSimpleName():b.key();alive.merge(key,1,Integer::sum);}
  for(String key:alive.keySet())row(key);for(var e:counts.entrySet()){var c=e.getValue();out.println(scene+","+age+",particle,"+e.getKey()+","+c[0]+","+c[1]+","+alive.getOrDefault(e.getKey(),0)+","+c[2]);}
  var manager=Minecraft.getInstance().getSoundManager();var soundEngine=field(manager,"soundEngine");Map<?,?> channels=(Map<?,?>)field(soundEngine,"instanceToChannel");Map<String,Integer> playing=new TreeMap<>();for(var s:channels.keySet())playing.merge(((SoundInstance)s).getLocation().toString(),1,Integer::sum);
  for(var e:sounds.entrySet())out.println(scene+","+age+",sound,"+e.getKey()+","+e.getValue().get()+","+stops.getOrDefault(e.getKey(),new AtomicLong()).get()+","+playing.getOrDefault(e.getKey(),0)+","+stops.getOrDefault("native_end:"+e.getKey(),new AtomicLong()).get());
  out.println(scene+","+age+",engine,total,"+present.size()+","+channels.size()+","+poolWarnings.get()+","+failures.values().stream().mapToLong(AtomicLong::get).sum());out.flush();
 }
 static void end(PrintWriter events){events.println("OBSERVER_END "+scene+" destroys="+destroys.get()+" duplicate_add="+duplicates+" pool_warnings="+poolWarnings.get()+" failures="+failures);callers.forEach((k,v)->events.println("CALLER "+scene+" "+k+" "+v));events.flush();}
}
