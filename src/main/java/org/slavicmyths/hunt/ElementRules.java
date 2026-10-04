package org.slavicmyths.hunt;
/** Fixed Hunt II timings and tunings, shared by server behavior and headless contracts. */
public final class ElementRules {
 public enum State {APPROACH,ORBIT,ALIGN_DASH,FIRE_DASH,ALIGN_DIVE,STAR_DIVE,DECOY_CAST,MELEE_SNAP,WATER_STUN,RECOVERY,RETURN_TO_ANCHOR,HUNT,STRAFE,VORTEX_CHARGE,VORTEX_ACTIVE,GUST_CHARGE,GUST_RELEASE,BROKEN_PATH_CHARGE,BROKEN_PATH_DASH}
 public static int cooldown(State s,boolean rain){switch(s){case ALIGN_DASH:return rain?120:100;case ALIGN_DIVE:return 150;case DECOY_CAST:return 220;case MELEE_SNAP:return 22;case VORTEX_CHARGE:return 160;case GUST_CHARGE:return 110;case BROKEN_PATH_CHARGE:return 120;default:return 20;}}
 public static int windup(State s){switch(s){case ALIGN_DASH:return 16;case ALIGN_DIVE:return 24;case DECOY_CAST:return 18;case MELEE_SNAP:return 7;case VORTEX_CHARGE:return 20;case GUST_CHARGE:return 14;case BROKEN_PATH_CHARGE:return 10;default:return 0;}}
 public static float fireDamage(float base,boolean rain){return base*(rain?.8F:1);}
 public static int ignite(int base,boolean rain){return rain?base*3/4:base;}
 public static double pull(double d){return d>5?.025:d>3?.055:.09;}
 public static boolean dangerousFall(float distance,double y,boolean flight){return !flight&&distance>=5.5F&&y<-.15;}
 public static double pushScale(double hp,boolean hunt){return hp>=250?.15:hunt||hp>=100?.35:1;}
 private ElementRules(){}
}
