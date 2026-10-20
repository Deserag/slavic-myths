import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.nbt.*;
import org.slavicmyths.skim.*;
import org.slavicmyths.client.*;
/** Native budgets/NBT and actual ModelPart before/after. No world or server launch. */
public final class Bogatyr78Checks {
 static int checks;static void check(boolean v,String msg){checks++;if(!v)throw new AssertionError(msg);}
 public static void main(String[] args)throws Exception {
  var report=new JsonObject();var traces=new JsonArray();
  for(int player=0;player<2;player++)for(String hero:List.of("ilya_muromets","dobrynya_nikitich","alyosha_popovich","finist"))for(int defeats=0;defeats<=3;defeats++){
   var n=new CompoundTag();HeroLease.start(n,173);
   for(int defeat=1;defeat<=defeats;defeat++){boolean ok=HeroLease.recovery(n,173+defeat);check(ok==(defeat<=2),"Recovery limit");n=n.copy();}
   check(n.getInt("Recoveries")==Math.min(defeats,2),"Budget serialization");
   check(!HeroLease.expired(n,12172),"Before exact expiry");check(HeroLease.expired(n,12173),"Exact 12000-tick expiry");
   HeroLease.end(n,12173);long ready=n.getLong("ReadyAt");check(ready==48173,"Independent 36000 cooldown");HeroLease.end(n,15000);check(n.getLong("ReadyAt")==ready,"No cooldown restart");
   var other=new CompoundTag();HeroLease.start(other,12174);check(other.getInt("Recoveries")==0&&other.getLong("ReadyAt")==0,"Other hero/player independent");
   check(!HeroLease.recovery(n,12175),"No dormant recovery after expiry");
   var row=new JsonObject();row.addProperty("player",player);row.addProperty("hero",hero);row.addProperty("defeats",defeats);row.addProperty("recoveries",n.getInt("Recoveries"));row.addProperty("expiresAt",n.getLong("ExpiresAt"));row.addProperty("readyAt",ready);traces.add(row);
  }
  var pause=new CompoundTag();HeroLease.start(pause,100);HeroLease.advance(pause,1100,false);HeroLease.recovery(pause,1100);HeroLease.advance(pause,9100,true);check(pause.getLong("ExpiresAt")==20100,"Mandatory phase adds only elapsed phase time");check(pause.getInt("Recoveries")==1,"Pause adds no recovery budget");check(!HeroLease.expired(pause,20099)&&HeroLease.expired(pause,20100),"Resume exact remaining duration");pause.putBoolean("Exhausted",true);check(!HeroLease.recovery(pause,9100),"No third recovery during mandatory phase");
  var saved=new CompoundTag();var lives=new CompoundTag();var owner=UUID.randomUUID();var entity=UUID.randomUUID();pause.putUUID("Owner",owner);pause.putUUID("Entity",entity);pause.putString("Hero","finist");lives.put(owner+"/finist",pause);saved.put("HeroLifecycles",lives);var original=HeroCalls.load(saved,null);var serial=original.save(new CompoundTag(),null);var reload=HeroCalls.load(serial,null).save(new CompoundTag(),null);check(serial.equals(reload),"Real HeroCalls SavedData codec preserves timers/budgets/exhaustion");
  var poses=new JsonObject();var before=new SkimModelBefore78<SkimActor>(AlyoshaMesh.create(),false);var after=new SkimModel<SkimActor>(AlyoshaMesh.create(),false);
  for(var action:List.of(SkimAction.ARRIVE,SkimAction.RECALL,SkimAction.SABER,SkimAction.BOW,SkimAction.DRINK))for(int t:new int[]{0,6,12,18,24}){
   before.pose(action,t,0,0,20,0,0);after.pose(action,t,0,0,20,0,0);
   check(after.root.xScale==1&&after.root.yScale==1&&after.root.zScale==1,"Fixed scale in "+action);
   if(action!=SkimAction.ARRIVE&&action!=SkimAction.RECALL)check(Skim77Native.fingerprint(before.nodes).equals(Skim77Native.fingerprint(after.nodes)),"Adjacent action unchanged");
   poses.add("before_"+action+"_"+t,Skim77Native.capture(before.root));poses.add("after_"+action+"_"+t,Skim77Native.capture(after.root));
  }
  var oldBeast=new SkimModelBefore78<SkimActor>(SkimMesh.create(),true);var newBeast=new SkimModel<SkimActor>(SkimMesh.create(),true);
  for(var action:List.of(SkimAction.CLAWS,SkimAction.GRIP,SkimAction.ROAR,SkimAction.DEATH))for(int t:new int[]{0,11,19,28,41,45,75}){oldBeast.pose(action,t,0,0,20,0,0);newBeast.pose(action,t,0,0,20,0,0);check(Skim77Native.fingerprint(oldBeast.nodes).equals(Skim77Native.fingerprint(newBeast.nodes)),"#77 beast unchanged");}
  long start=System.nanoTime();var bench=new CompoundTag();HeroLease.start(bench,0);for(int i=0;i<200000;i++)HeroLease.advance(bench,i,false);report.addProperty("budget_cpu_200000_calls_ms",(System.nanoTime()-start)/1000000.0);report.addProperty("benchmark_scope","Pure NBT budget only; NOT server MSPT/FPS or AI performance");
  report.addProperty("checks",checks);report.addProperty("runtime_launches",0);report.addProperty("status","PASS_OFFLINE_ONLY");report.add("lifecycle_traces",traces);var gson=new GsonBuilder().setPrettyPrinting().create();var path=Path.of("docs/implementation/bogatyr-78");Files.writeString(path.resolve("native-checks.json"),gson.toJson(report));Files.writeString(path.resolve("native-poses.json"),new Gson().toJson(poses));System.out.println("BOGATYR78 native checks="+checks+" PASS; runtime=0");
 }
}
