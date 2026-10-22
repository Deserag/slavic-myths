package org.slavicmyths.mor;

/** Pure timing and geometry policy for the mor Likho's passive presence. */
public final class MorPresenceRules {
 public static final int SAMPLE_TICKS=10, DWELL_TICKS=20, PERIOD_TICKS=40, AIR_GRACE_TICKS=20;
 public static final double RADIUS=5, HEIGHT=1.5;
 public static boolean near(double dx,double dy,double dz){return dx*dx+dz*dz<=RADIUS*RADIUS&&Math.abs(dy)<=HEIGHT;}
 public static boolean ready(long now,long entered){return now-entered>=DWELL_TICKS;}
 public static boolean retainInAir(long now,long lastGround){return now-lastGround<=AIR_GRACE_TICKS;}
 public static boolean mayHit(long now,long next){return now>=next;}
 private MorPresenceRules(){}
}
