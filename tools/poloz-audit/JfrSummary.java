import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.util.*;
import java.time.*;
public class JfrSummary {
 static Map<String,Long> leaf=new HashMap<>(),mod=new HashMap<>(),allocation=new HashMap<>(),threads=new HashMap<>(),allocationSite=new HashMap<>(),modAllocation=new HashMap<>();
 static void add(Map<String,Long> m,String k,long n){m.merge(k,n,Long::sum);}
 static void table(java.io.PrintWriter out,String title,Map<String,Long> data,int limit){out.println("\n"+title+"\tvalue");data.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(limit).forEach(x->out.println(x.getKey()+"\t"+x.getValue()));}
 public static void main(String[] args)throws Exception {
  Path dir=Path.of(args[0]);TreeMap<Long,String> scenes=new TreeMap<>();for(String line:Files.readAllLines(dir.resolve("events.txt"))){var m=java.util.regex.Pattern.compile("SCENE (\\S+) repeat=(\\d+) time=(\\d+)").matcher(line);if(m.find())scenes.put(Long.parseLong(m.group(3)),m.group(1)+"/"+m.group(2));}
  var gcRows=new ArrayList<String>();var heapRows=new ArrayList<String>();long count=0,gc=0,pause=0,maxPause=0;Instant first=null,last=null;
  try(var f=new RecordingFile(dir.resolve("profile.jfr"))){while(f.hasMoreEvents()){var e=f.readEvent();count++;Instant at=e.getStartTime();if(first==null||at.isBefore(first))first=at;if(last==null||at.isAfter(last))last=at;String type=e.getEventType().getName();String scene=Optional.ofNullable(scenes.floorEntry(at.toEpochMilli())).map(Map.Entry::getValue).orElse("startup");
   if(type.equals("jdk.ExecutionSample")){var t=e.getThread("sampledThread");String thread=t==null?"unknown":t.getJavaName();add(threads,thread,1);var st=e.getStackTrace();if(st==null)continue;Set<String> seen=new HashSet<>();boolean top=true;for(var frame:st.getFrames()){var m=frame.getMethod();String name=m.getType().getName()+"."+m.getName();if(top){add(leaf,scene+" | "+thread+" | "+name,1);top=false;}if(name.startsWith("org.slavicmyths.")&&seen.add(name))add(mod,scene+" | "+thread+" | "+name,1);}}
   if(type.equals("jdk.ObjectAllocationSample")){long weight=e.getLong("weight");add(allocation,scene+" | "+e.getClass("objectClass").getName(),weight);var stack=e.getStackTrace();if(stack!=null){boolean top=true;for(var fr:stack.getFrames()){var method=fr.getMethod();String name=method.getType().getName()+"."+method.getName();if(top){add(allocationSite,scene+" | "+name,weight);top=false;}if(name.startsWith("org.slavicmyths.")){add(modAllocation,scene+" | "+name,weight);break;}}}}
   if(type.equals("jdk.GCHeapSummary"))heapRows.add(at+","+e.getString("when")+","+e.getLong("heapUsed"));
   if(type.equals("jdk.GarbageCollection")){gcRows.add(at+","+e.getString("name")+","+e.getString("cause")+","+e.getDuration("sumOfPauses").toNanos()/1e6+","+e.getDuration("longestPause").toNanos()/1e6);gc++;long n=e.getDuration("sumOfPauses").toNanos();pause+=n;maxPause=Math.max(maxPause,e.getDuration("longestPause").toNanos());}
  }}
  gcRows.add(0,"time,name,cause,sum_pause_ms,longest_pause_ms");heapRows.add(0,"time,when,heap_used_bytes");Files.write(dir.resolve("gc.csv"),gcRows);Files.write(dir.resolve("gc-heap.csv"),heapRows);
  try(var out=new java.io.PrintWriter(Files.newBufferedWriter(dir.resolve("jfr-summary.tsv")))){out.println("events\t"+count);out.println("duration_seconds\t"+Duration.between(first,last).toMillis()/1000.0);out.println("gc_count\t"+gc);out.println("gc_sum_pause_ms\t"+pause/1e6);out.println("gc_max_pause_ms\t"+maxPause/1e6);table(out,"Execution samples by thread",threads,50);table(out,"Top leaf CPU samples (not calls or milliseconds)",leaf,500);table(out,"Inclusive mod samples (overlapping, includes startup/transitions)",mod,500);table(out,"Estimated allocation weights by class (bytes, not retained heap)",allocation,500);table(out,"Estimated allocation weights by leaf site",allocationSite,500);table(out,"Estimated allocation by first mod frame (includes audit instrumentation)",modAllocation,500);}
 }
}
