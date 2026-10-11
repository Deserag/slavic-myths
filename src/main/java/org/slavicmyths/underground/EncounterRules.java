package org.slavicmyths.underground;
/** Pure shared admission rules, isolated from world loading. */
public final class EncounterRules {
 public static final float HEAVY_DAMAGE=32,STONE_DAMAGE=5.5F;
 public static boolean protectedByRunes(double maximumHealth,int activeRunes,boolean excluded,boolean provoked){return activeRunes>=2&&maximumHealth<=50&&!excluded&&!provoked;}
 public static boolean inCone(double dot,double distance,boolean controlled){return distance<=3.1&&dot>=Math.cos(Math.toRadians(55))&&!controlled;}
 public static float exhaustion(float realCost,boolean aura){return realCost>0&&aura?realCost*3:realCost;}
 private EncounterRules(){}
}
