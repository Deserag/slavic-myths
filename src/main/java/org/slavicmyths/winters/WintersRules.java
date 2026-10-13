package org.slavicmyths.winters;
/** Shared arithmetic, tested without starting Minecraft. */
public final class WintersRules {
 public static final int COOLDOWN=2400,SHIELD_TICKS=160;
 public static int stage(long ticks){return ticks<600?0:ticks<1200?1:ticks<1800?2:3;}
 public static int interval(int stage){return stage==2?80:stage==3?60:0;}
 public static float shieldDamage(float damage,float reserve){return Math.max(0,damage-Math.max(0,reserve));}
 public static float shieldLeft(float damage,float reserve){return Math.max(0,reserve-Math.max(0,damage));}
 private WintersRules(){}
}
