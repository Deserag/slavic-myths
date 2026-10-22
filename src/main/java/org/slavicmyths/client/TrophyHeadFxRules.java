package org.slavicmyths.client;

/** Deterministic, bounded client-only cosmetic rules for placed Likho trophy heads. */
public final class TrophyHeadFxRules {
 public static float lidOffset(double time){
  double phase=((time%130)+130)%130;
  return phase>=62&&phase<70?(float)(.65*Math.sin(Math.PI*(phase-62)/8)):0;
 }
 public static float morEyeScale(double time){return (float)(1.025+.025*Math.sin(time*.10));}
 public static boolean likhoGlow(double time){double phase=((time%116)+116)%116;return phase>=65&&phase<88;}
 public static boolean particleDue(long now,long last,int interval){return interval>0&&(last==Long.MIN_VALUE||now<last||now-last>=interval);}
 private TrophyHeadFxRules(){}
}
