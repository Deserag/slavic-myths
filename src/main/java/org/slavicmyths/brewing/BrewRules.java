package org.slavicmyths.brewing;
/** Tick based brewing arithmetic, independent of client and registries. */
public final class BrewRules {
 public static String quality(int age){return age<2400?"young":age<8400?"ready":age<12000?"aged":"spoiled";}
 public static int pourCount(String container,boolean pitcherAllowed,boolean mugAllowed,int servings){return container.equals("ceramic_pitcher")&&pitcherAllowed?Math.min(4,servings):servings>0&&(container.equals("dark_bottle")||container.equals("wooden_mug")&&mugAllowed)?1:0;}
 public static int duration(int seconds,String quality){return switch(quality){case "young"->seconds/2;case "aged"->Math.round(seconds*1.2F);case "spoiled"->0;default->seconds;};}
 public static final int INTOXICATION_THRESHOLD=4;
 public static final int DECAY_TICKS=1200;
 /** Preserve the existing effect ladder, starting its first negative step at four doses. */
 public static int stage(int points){return points<INTOXICATION_THRESHOLD?0:Math.min(5,points-INTOXICATION_THRESHOLD+2);}
 public static int addLoad(int points,int dose){return (int)Math.min(Integer.MAX_VALUE,(long)Math.max(0,points)+Math.max(0,dose));}
 public static int decay(int points,long last,long now){return Math.max(0,points-(int)Math.min(Integer.MAX_VALUE,Math.max(0,now-last)/DECAY_TICKS));}
 private BrewRules(){}
}
