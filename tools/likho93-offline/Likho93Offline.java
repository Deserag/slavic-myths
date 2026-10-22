package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import org.slavicmyths.hunt.*;
import org.slavicmyths.mor.*;
import net.minecraft.world.entity.monster.*;
import static org.slavicmyths.hunt.BossRules.Move;

/** Production pure policies/exposure, not a simulated Minecraft battle. No world or entity created. */
public final class Likho93Offline {
 private static int checks;
 private static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 private static Move choose(int phase,double distance,boolean player,int nearby,Move... unavailable){var blocked=Set.of(unavailable);return LikhoCombatRules.choose(phase,distance,true,player,nearby,m->!blocked.contains(m));}
 public static void main(String[] args)throws Exception {
  var scenarios=new ArrayList<Map<String,Object>>();
  for(Class<?> type:List.of(Zombie.class,Skeleton.class,Spider.class)){
   check(Enemy.class.isAssignableFrom(type),"vanilla type implements Enemy: "+type);
   check(LikhoTargets.candidate(false,Enemy.class.isAssignableFrom(type),false,false),"normal candidate "+type);
   for(int phase=1;phase<=3;phase++){
    check(choose(phase,3,false,3)==Move.SWEEP,"surrounded defensive sweep "+type+phase);
    check(choose(phase,3,false,1)!=Move.GAZE,"no inapplicable gaze "+type+phase);
   }
   scenarios.add(Map.of("type",type.getSimpleName(),"normal_candidate",true,"normal_surrounded","SWEEP","runtime","NOT_RUN / OWNER_PENDING"));
  }
  check(!LikhoTargets.candidate(false,false,false,false),"peaceful bystander excluded");
  check(LikhoTargets.candidate(false,false,true,false),"legal direct attacker allowed after shared protection gate");
  for(boolean enemy:new boolean[]{false,true})check(!LikhoTargets.candidate(false,enemy,true,true),"tamed target excluded");
  check(LikhoTargets.candidate(true,false,false,false),"survival player eligible after creative/spectator/allies gate");
  check(choose(2,3,true,1)==Move.GAZE,"player gaze retained");
  check(choose(2,3,false,3,Move.SWEEP)==Move.GROUND,"ground fallback under pressure");
  check(choose(1,3,false,3,Move.SWEEP)==Move.GRAB,"phase1 cooldown fallback");
  check(choose(3,10,false,1)==Move.LEAP,"leap no longer shadowed by chase");
  check(choose(3,6,false,1)==Move.CHASE,"chase retains midrange");
  check(LikhoCombatRules.choose(3,3,false,false,3,m->true)==Move.IDLE,"LOS gate remains");
  check(choose(2,20,false,1)==Move.IDLE,"outside skill range");
  for(Move m:List.of(Move.SWEEP,Move.GRAB,Move.HOLD,Move.GAZE,Move.GROUND,Move.LEAP,Move.CHASE))check(!LikhoCombatRules.phaseBoundary(m),"phase cannot interrupt "+m);
  check(LikhoCombatRules.phaseBoundary(Move.RECOVERY)&&LikhoCombatRules.phaseBoundary(Move.IDLE),"phase transition remains reachable");
  check(LikhoCombatRules.committed(Move.GRAB)&&LikhoCombatRules.committed(Move.LEAP)&&!LikhoCombatRules.committed(Move.CHASE),"windup focus stable, chase tracks current target");
  check(BossRules.phase(BossKind.LIKHO,.65F)==2&&BossRules.phase(BossKind.LIKHO,.30F)==3,"unchanged thresholds");
  check(BossRules.cooldown(BossKind.LIKHO,Move.SWEEP,1)==50&&BossRules.cooldown(BossKind.LIKHO,Move.SWEEP,3)==43,"unchanged cooldowns");
  check(BossKind.LIKHO.hp==350&&BossKind.TUGARIN.hp==400,"unchanged base HP");
  var zoneResults=new ArrayList<Map<String,Object>>();
  for(boolean cloud:new boolean[]{false,true}){
   var exposure=new MorExposure();var a=UUID.randomUUID();var b=UUID.randomUUID();int total=0,hits=0,first=0;
   for(int tick=10;tick<=HigherRules.life(cloud);tick+=10){
    var sample=exposure.sample(a,tick,cloud);var other=exposure.sample(b,tick,cloud);
    check(sample.equals(other),"same exposure for independent mob/player entries");
    check(sample.effects()==(cloud||tick>=60),"original exposure effect threshold");
    if(sample.damage()>0){total+=sample.damage();hits++;if(first==0)first=tick;}
    var duplicate=exposure.sample(a,tick,cloud);check(!duplicate.effects()&&duplicate.damage()==0,"same zone cannot double apply same tick");
    exposure.retain(Set.of(a,b));
   }
   check(first==(cloud?20:60)&&total==(cloud?12:7)&&hits==(cloud?6:7),"exact original full-lifetime damage cadence");
   exposure.retain(Set.of(b));check(exposure.sample(a,200,cloud).time()==10,"leaving resets exposure");
   exposure.clear();check(exposure.sample(b,210,cloud).time()==10,"owner disappearance resets exposure");
   zoneResults.add(Map.of("cloud",cloud,"first_damage_tick",first,"full_life_hits",hits,"full_life_damage",total,"life",HigherRules.life(cloud)));
  }
  check(HigherRules.MAX_ZONES==12&&HigherRules.life(false)==180&&HigherRules.life(true)==120,"unchanged zone limits");
  var output=Map.of("checks",checks,"production_policy_scenarios",scenarios,"exposure",zoneResults,"runtime_launches",0,"combat_runtime","NOT_RUN / OWNER_PENDING","entity_policy_adapter","source-audited separately; no entity/world instantiated");
  Files.writeString(Path.of("docs/implementation/likho93/offline.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(output)+"\n");
  System.out.println("LIKHO93_OFFLINE PASS checks="+checks+"; production choice/exposure policies only, runtime NOT_RUN");
 }
}
