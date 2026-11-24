package org.slavicmyths.icequeen;
public final class QueenRules {
 public static final float HP=3000, PHASE_TWO=1500, VICTORY=100;
 public static final long COOLDOWN_MS=3_600_000L;
 public static final int SUMMON_INTERVAL=200, MAX_MINIONS=10, MAX_COPIES=3, SHIELDS=5;
 public static final double PROJECTILE_BLOCK=.30, ARENA_RADIUS=50;
 public static float health(float requested){return Math.clamp(requested,VICTORY,HP);}
 public static boolean ready(long now,long until){return now>=until;}
 public static long nextTrial(long now){return now+COOLDOWN_MS;}
 public static int summonKind(int roll){return roll<25?0:roll<40?1:roll<55?2:roll<75?3:roll<95?4:5;}
 private QueenRules(){}
}
