import java.nio.file.*;import java.util.*;import jdk.jfr.consumer.*;import java.time.*;
/** Streaming JFR consumer, never expands a profile into a large JSON document. */
class ProfileSummary {
 public static void main(String[] args)throws Exception{
  Path file=Path.of(args[0]);Map<String,Long> alloc=new TreeMap<>(),cpu=new TreeMap<>();long count=0,gc=0,pause=0,max=0;Instant first=null,last=null;
  try(var f=new RecordingFile(file)){while(f.hasMoreEvents()){var e=f.readEvent();count++;var at=e.getStartTime();if(first==null||at.isBefore(first))first=at;if(last==null||at.isAfter(last))last=at;
   String type=e.getEventType().getName();if(type.equals("jdk.GarbageCollection")){gc++;long p=e.getDuration("sumOfPauses").toNanos();pause+=p;max=Math.max(max,e.getDuration("longestPause").toNanos());}
   if(type.equals("jdk.ObjectAllocationSample")){var st=e.getStackTrace();if(st==null)continue;String site="other",owner="none";for(var fr:st.getFrames()){var m=fr.getMethod();String name=m.getType().getName()+"."+m.getName();if(site.equals("other"))site=name;if(owner.equals("none")&&name.equals("org.slavicmyths.client.GuardianClient.cube"))owner="production_cube";else if(owner.equals("none")&&name.equals("org.slavicmyths.cubeaudit.ReferenceCube.cube"))owner="frozen_reference";else if(owner.equals("none")&&name.equals("org.slavicmyths.cubeaudit.HelperCube.cube"))owner="helper_cube";}
    if(!owner.equals("none"))alloc.merge(owner+" | "+e.getClass("objectClass").getName()+" | "+site,e.getLong("weight"),Long::sum);
   }
   if(type.equals("jdk.ExecutionSample")){var st=e.getStackTrace();if(st==null)continue;var t=e.getThread("sampledThread");String thread=t==null?"?":t.getJavaName();Set<String> seen=new HashSet<>();for(var fr:st.getFrames()){var m=fr.getMethod();String name=m.getType().getName()+"."+m.getName();if(name.startsWith("org.slavicmyths.")&&seen.add(name))cpu.merge(thread+" | "+name,1L,Long::sum);}}
  }}
  try(var out=Files.newBufferedWriter(file.resolveSibling(file.getFileName()+".summary.tsv"))){out.write("events\t"+count+"\nduration_seconds\t"+Duration.between(first,last).toMillis()/1000.0+"\ngc_count\t"+gc+"\ngc_sum_pause_ms\t"+pause/1e6+"\ngc_max_pause_ms\t"+max/1e6+"\nALLOCATION_ESTIMATES_BYTES (sampled, not retained)\n");for(var e:alloc.entrySet())out.write(e.getValue()+"\t"+e.getKey()+"\n");out.write("INCLUSIVE_CPU_SAMPLES (overlap, not time)\n");for(var e:cpu.entrySet())out.write(e.getValue()+"\t"+e.getKey()+"\n");}
 }
}
