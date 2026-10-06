package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import org.slavicmyths.kurgan.*;
public final class KurganReworkHeadless {
 private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  Set<KurganPlan.Archetype> types=EnumSet.noneOf(KurganPlan.Archetype.class);Set<String> maps=new HashSet<>();
  for(int tier=0;tier<3;tier++){
   int minNodes=999,maxNodes=0,minLoops=999,maxLoops=0,minRewards=999,maxRewards=0;double optional=0;
   for(int sample=0;sample<50;sample++){
    long seed=sample*7919L;var p=KurganPlan.create(tier,seed);var m=KurganLayout.metrics(p);
    check(p.formatVersion==2&&p.validate().isEmpty(),"Invalid v2 plan");var n=KurganPlanNbt.write(p);
    check(n.equals(KurganPlanNbt.write(KurganPlanNbt.read(n))),"V2 geometry rerolled on load");
    check(n.equals(KurganPlanNbt.write(KurganPlan.create(tier,seed))),"Nondeterministic layout");var fingerprint=n.copy();fingerprint.remove("Seed");maps.add(fingerprint.toString());
    for(var r:p.rooms)if(r.archetype!=null)types.add(r.archetype);
    if(tier>0){var instance=new KurganInstance(java.util.UUID.randomUUID(),net.minecraft.core.BlockPos.ZERO,p);instance.disturbance=100;var roster=KurganRoster.roster(instance,p.rooms.get(p.finalRoom));check(!roster.isEmpty(),"Deep room lost its existing population policy");if(tier==2)check(roster.equals(List.of(KurganFighter.Kind.PRINCE)),"Great final population changed");}
    minNodes=Math.min(minNodes,m.nodes());maxNodes=Math.max(maxNodes,m.nodes());minLoops=Math.min(minLoops,m.loops());maxLoops=Math.max(maxLoops,m.loops());
    minRewards=Math.min(minRewards,m.containers());maxRewards=Math.max(maxRewards,m.containers());optional+=m.optionalPercent();
    Path dir=Path.of("build/reports/slavicmyths/kurgan");Files.createDirectories(dir);
    Map<String,Object> report=new LinkedHashMap<>();report.put("version",p.formatVersion);report.put("tier",tier);report.put("attemptsUsed",p.attemptsUsed);report.put("seed",seed);report.put("metrics",m);report.put("bounds",p.bounds().array());report.put("validationErrors",p.validate());
    report.put("rooms",p.rooms.stream().map(r->{Map<String,Object> row=new LinkedHashMap<>();row.put("id",r.id);row.put("floor",r.floor);row.put("archetype",r.archetype);row.put("nodeType",r.nodeType);row.put("bounds",r.box().array());row.put("critical",r.critical);row.put("mandatory",r.mandatory);row.put("rewardPoints",KurganLayout.rewards(r));row.put("loot",r.loot);row.put("rewardCoordinates",java.util.stream.IntStream.range(0,KurganLayout.rewards(r)).mapToObj(i->{var s=KurganLayout.rewardPoint(r,i);return new int[]{s.x,s.y,s.z};}).toList());return row;}).toList());
    report.put("links",p.links.stream().map(l->Map.of("a",l.a,"b",l.b,"width",l.width,"height",l.secret?3:l.height,"secret",l.secret,"style",l.style,"transitionStyle",l.transitionStyle,"steps",l.steps.stream().map(s->new int[]{s.x,s.y,s.z}).toList())).toList());
    Files.writeString(dir.resolve(seed+"-"+tier+".json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));
   }
   System.out.println("KURGAN_V2_TIER_PASS tier="+tier+" samples=50 nodes="+minNodes+".."+maxNodes+" loops="+minLoops+".."+maxLoops+" rewards="+minRewards+".."+maxRewards+" meanOptional="+optional/50);
  }
  check(maps.size()==150,"Layout variety lost");check(types.size()==KurganPlan.Archetype.values().length,"Missing archetypes: "+types);
  for(int tier=0;tier<3;tier++){var p=KurganPlan.createLegacy(tier,123);var n=KurganPlanNbt.write(p);check(KurganPlanNbt.read(n).formatVersion==1,"Legacy migrated by reroll");check(n.equals(KurganPlanNbt.write(KurganPlanNbt.read(n))),"Legacy roundtrip changed");}
  System.out.println("KURGAN_V2_HEADLESS_PASS plans=150 archetypes="+types.size()+" legacyVersions=1,2 clientLaunches=0");
 }
}
