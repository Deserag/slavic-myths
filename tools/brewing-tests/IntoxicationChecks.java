import org.slavicmyths.brewing.BrewRules;

/** Exercises production arithmetic; no Minecraft runtime or copied implementation. */
public class IntoxicationChecks {
 private static void equal(int actual,int expected,String scenario){if(actual!=expected)throw new AssertionError(scenario+": "+actual+" != "+expected);}
 public static void main(String[] args){
  int[] stages={0,0,0,0,2,3,4,5};
  for(int mugs=0;mugs<=7;mugs++)equal(BrewRules.stage(mugs),stages[mugs],"ordinary mugs "+mugs);
  for(int dose:new int[]{0,1,3,4,5,6})equal(BrewRules.amplifier(dose),0,"level I below 7");
  for(int dose:new int[]{7,8,10,11})equal(BrewRules.amplifier(dose),1,"level II from 7");
  for(int dose:new int[]{12,13,20,Integer.MAX_VALUE})equal(BrewRules.amplifier(dose),2,"level III from 12");
  equal(BrewRules.amplifier(BrewRules.decay(12,0,1200)),1,"12 to 11 drops to II");
  equal(BrewRules.amplifier(BrewRules.decay(7,0,1200)),0,"7 to 6 drops to I");
  int points=0;
  for(int mugs=1;mugs<=6;mugs++){
   points=BrewRules.addLoad(BrewRules.decay(points,(mugs-1)*32L,mugs*32L),1);
   equal(points,mugs,"32-tick drinking interval");equal(BrewRules.stage(points),stages[mugs],"rapid progression");
  }
  equal(BrewRules.stage(BrewRules.addLoad(0,2)),0,"one strong drink");
  equal(BrewRules.stage(BrewRules.addLoad(2,2)),2,"two strong drinks");
  equal(BrewRules.stage(BrewRules.addLoad(BrewRules.addLoad(0,2),2)),2,"two strong equivalents");
  equal(BrewRules.stage(BrewRules.addLoad(BrewRules.addLoad(2,1),1)),2,"strong + ordinary + ordinary");
  for(int elapsed:new int[]{0,1199,1200,2399,2400,3600,4800,7200}){
   int expected=Math.max(0,6-elapsed/1200);
   equal(BrewRules.decay(6,100,100+elapsed),expected,"decay boundary "+elapsed);
   equal(BrewRules.stage(BrewRules.decay(6,100,100+elapsed)),stages[expected],"reverse stage "+elapsed);
  }
  points=0;long last=0;
  for(int mug=1;mug<=6;mug++){long now=mug*1200L;points=BrewRules.addLoad(BrewRules.decay(points,last,now),1);last=now;equal(points,1,"spaced drinks stay below threshold");}
  equal(BrewRules.decay(4,100,99),4,"clock rollback");
  equal(BrewRules.addLoad(Integer.MAX_VALUE,2),Integer.MAX_VALUE,"no spam overflow");
  equal(BrewRules.stage(Integer.MAX_VALUE),5,"saturated stage");
  equal(BrewRules.addLoad(3,0),3,"nonalcoholic dose");
  equal(BrewRules.duration(45,"young"),22,"young benefit unchanged");
  equal(BrewRules.duration(45,"aged"),54,"aged benefit unchanged");
  equal(BrewRules.duration(45,"spoiled"),0,"spoiled benefit unchanged");
  System.out.println("INTOXICATION_OFFLINE PASS: progression 0-7, strong/mixed, decay boundaries, spaced drinks, overflow, quality. Runtime NOT_RUN.");
 }
}
