package org.slavicmyths.karachun;
public final class NightRules {
 public static float floor(float max){return max*.05f;}
 public static float clamp(float hp,float max,float hit){return Math.max(0,Math.min(hit,hp-floor(max)));}
 public static int phase(float hp,float max){return hp<=max*.25?3:hp<=max*.7?2:1;}
 public static int tier(double n){return n>=75?3:n>=50?2:n>=25?1:0;}
 public static int phantoms(double fear){return fear>=75?3:fear>=50?2:0;}
 public static boolean eligible(double damage,int active,boolean attacked){return damage>=1||attacked&&active>=300;}
 public record Rewards(boolean wing,boolean scythe,int rune,int shard){}
 public static Rewards roll(boolean first,double roll,double chance){return new Rewards(first,roll<chance,1,1);}
 private NightRules(){}
}
