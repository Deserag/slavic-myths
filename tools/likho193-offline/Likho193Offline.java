package org.slavicmyths.verify;

import java.util.*;
import org.slavicmyths.hunt.*;
import org.slavicmyths.mor.*;
import static org.slavicmyths.hunt.BossRules.Move;

/** Pure Java 21 policy tests only. No game, world, or Entity construction. */
public final class Likho193Offline {
 private static int checks;
 private static void ok(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 private static void close(double actual,double expected,double tolerance,String why){
  ok(Math.abs(actual-expected)<=tolerance,why+" actual="+actual+" expected="+expected);
 }
 public static void main(String[] args){
  ok(BossKind.LIKHO.hp==350,"normal HP");
  ok(BossKind.TUGARIN.hp==400,"Tugarin HP unchanged");
  ok(BossRules.RESPAWN==7L*24000,"respawn is seven days");
  ok(BossRules.phase(BossKind.LIKHO,.65F)==2,"Likho phase2 threshold");
  ok(BossRules.phase(BossKind.LIKHO,.30F)==3,"Likho phase3 threshold");
  ok(BossRules.phase(BossKind.TUGARIN,.51F)==1,"Tugarin phase1");
  ok(BossRules.phase(BossKind.TUGARIN,.50F)==2,"Tugarin phase2");
  ok(MorPresenceRules.near(3,0,4),"aura radius 5 boundary");
  ok(!MorPresenceRules.near(3.01,0,4),"outside aura radius");
  ok(MorPresenceRules.near(0,1.5,0),"aura vertical boundary");
  ok(!MorPresenceRules.near(0,1.501,0),"outside aura height");
  ok(!MorPresenceRules.ready(19,0),"dwell not early");
  ok(MorPresenceRules.ready(20,0),"dwell at 20 ticks");
  ok(MorPresenceRules.retainInAir(30,10),"short jump grace");
  ok(!MorPresenceRules.retainInAir(31,10),"long jump resets");
  ok(!MorPresenceRules.mayHit(39,40),"DOT interval respected");
  ok(MorPresenceRules.mayHit(40,40),"DOT resumes on interval");
  ok(LikhoGazeRules.WEAKNESS_TICKS==120,"Weakness 6 seconds");
  ok(LikhoGazeRules.BLINDNESS_TICKS==25,"Blindness 1.25 seconds");
  ok(LikhoGazeRules.NAUSEA_TICKS==70,"Nausea 3.5 seconds");
  ok(LikhoGazeRules.VISUAL_COOLDOWN_TICKS==240,"GAZE cooldown 12 seconds");
  ok(!LikhoGazeRules.visualReady(239,240),"GAZE visual not early");
  ok(LikhoGazeRules.visualReady(240,240),"GAZE visual after cooldown");
  ok(LikhoGazeRules.visualReady(0,241),"clock rollback recovery");
  ok(!LikhoGazeRules.visualReady(1,240),"no premature reapply");
  UUID victim=new UUID(1,2);
  MorExposure ground=new MorExposure();
  ok(!ground.sample(victim,10,false).effects(),"ground starts harmless");
  for(int tick=20;tick<60;tick+=10)ground.sample(victim,tick,false);
  MorExposure.Sample sample=ground.sample(victim,60,false);
  ok(sample.effects()&&sample.damage()==1,"ground 60-tick damage");
  ok(ground.sample(victim,60,false).damage()==0,"one sample per tick");
  ok(ground.sample(victim,70,false).damage()==0,"ground even intervals");
  ok(ground.sample(victim,80,false).damage()==1,"ground 80-tick damage");
  ground.retain(Set.of());
  ok(ground.sample(victim,90,false).time()==10,"zone exit resets exposure");
  MorExposure cloud=new MorExposure();
  ok(cloud.sample(victim,10,true).effects(),"cloud first sample effects");
  ok(cloud.sample(victim,20,true).damage()==2,"cloud first damage after 20 ticks");
  cloud.clear();
  ok(cloud.sample(victim,30,true).damage()==0,"cloud reset");
  ok(HigherRules.MAX_ZONES==12,"zone limit unchanged");
  ok(HigherRules.life(false)==180,"ground lifetime unchanged");
  ok(HigherRules.life(true)==120,"cloud lifetime unchanged");
  ok(HigherRules.phase2(50,100),"mor phase transition unchanged");
  ok(LikhoCombatRules.choose(2,3,true,true,2,m->true)==Move.SWEEP,"surrounded sweep");
  ok(LikhoCombatRules.choose(2,6,true,false,0,m->true)==Move.GROUND,"no gaze on mobs");
  ok(LikhoCombatRules.choose(2,6,true,true,0,m->true)==Move.GAZE,"gaze available on player");
  ok(LikhoCombatRules.choose(3,12,true,true,0,m->true)==Move.LEAP,"phase3 leap");
  ok(LikhoCombatRules.choose(3,12,false,true,0,m->true)==Move.IDLE,"LOS restriction");
  ok(LikhoCombatRules.phaseBoundary(Move.RECOVERY),"safe phase switch");
  ok(!LikhoCombatRules.phaseBoundary(Move.SWEEP),"do not interrupt sweep");
  long[] seeds={0L,1L,42L,987654321L,-12345678L};
  int[] counts=new int[3];int tugarin=0;long regions=0;
  for(long seed:seeds)for(int x=-48;x<48;x++)for(int z=-32;z<32;z++){
   long regionSeed=BossRules.seed(seed,x,z,BossKind.LIKHO);
   double roll=new Random(regionSeed^0x4C494B484F313933L).nextDouble();
   double[] chances={.60,.15,.10};
   for(int i=0;i<3;i++)if(roll<chances[i])counts[i]++;
   if(BossRules.eligibleRegion(seed,x,z,BossKind.TUGARIN))tugarin++;
   int[] a=BossRules.candidate(regionSeed,x,z,0);
   int[] b=BossRules.candidate(regionSeed,x,z,0);
   ok(Arrays.equals(a,b),"candidate deterministic");
   ok(a[0]>=x*512&&a[0]<x*512+512&&a[1]>=z*512&&a[1]<z*512+512,"candidate inside region");
   regions++;
  }
  for(int i=0;i<3;i++)close((double)counts[i]/regions,new double[]{.60,.15,.10}[i],.008,"forest odds "+i);
  close((double)tugarin/regions,.25,.008,"Tugarin odds regression");
  System.out.println("PASS checks="+checks+" regions="+regions+" dark="+counts[0]+" forest="+counts[1]+" birch="+counts[2]+" Tugarin="+tugarin);
 }
 private Likho193Offline(){}
}
