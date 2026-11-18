package org.slavicmyths.burningislands;

/** Shared authoritative timeline and geometry. No world access; renderer uses the same frames. */
public final class MagmaCombat {
 public enum Action {
  IDLE(0,0,0), WARN(20,30,0), WAKE(35,45,0), CLAWS(23,40,14),
  FRACTURE(27,45,18), PULL(27,52,0), HANDS(25,46,16),
  ERUPTION(31,67,18), RING(37,68,20), PHASE(55,70,0);
  public final int release,duration; public final float damage;
  Action(int release,int duration,float damage){this.release=release;this.duration=duration;this.damage=damage;}
  public boolean heavy(){return this==FRACTURE||this==ERUPTION||this==RING;}
 }
 public static Action choose(int sequence,boolean second,double distance){
  if(distance>22)return Action.HANDS; // Answer a real distant attacker without chasing outside home.
  Action[] cycle=second?new Action[]{Action.CLAWS,Action.FRACTURE,Action.HANDS,Action.PULL,Action.ERUPTION,Action.RING}:new Action[]{Action.CLAWS,Action.FRACTURE,Action.PULL};
  Action a=cycle[Math.floorMod(sequence,cycle.length)];
  return a==Action.CLAWS&&distance>5?Action.FRACTURE:a;
 }
 /** Three non-overlapping rays, in coordinates fixed at cast start. */
 public static boolean fracture(double forward,double side){
  if(forward<3||forward>22)return false;
  for(double angle:new double[]{-.35,0,.35})if(Math.abs(side-Math.tan(angle)*forward)<.45)return true;
  return false;
 }
 public static boolean eruption(double forward,double side,int pulse){return Math.hypot(forward-(5+6*pulse),side)<2.2;}
 public static double ring(int tick){return 2+Math.max(0,tick-Action.RING.release)*.45;}
 public static boolean hostilePlayer(boolean creative,boolean spectator){return !creative&&!spectator;}
 private MagmaCombat(){}
}
