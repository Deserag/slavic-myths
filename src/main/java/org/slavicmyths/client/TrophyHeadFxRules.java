package org.slavicmyths.client;

/** Deterministic, bounded client-only cosmetic rules for six placed boss trophy heads. */
public final class TrophyHeadFxRules {
 public static float lidOffset(double time){
  double phase=((time%64)+64)%64;
  return phase>=24&&phase<34?(float)(1.8*Math.sin(Math.PI*(phase-24)/10)):0;
 }
 public static float morEyeScale(double time){return (float)(1.075+.075*Math.sin(time*.12));}
 public static boolean likhoGlow(double time){double phase=((time%82)+82)%82;return phase>=42&&phase<72;}
 public static float tugarinJaw(double time){return (float)(.24+.20*Math.sin(time*.11));}
 public static float tugarinWhisker(double time){return (float)(.24*Math.sin(time*.13));}
 public static float skimJaw(double time){return (float)(.22+.19*Math.sin(time*.10));}
 public static float skimMane(double time){return (float)(.20*Math.sin(time*.13));}
 public static float volkolakJaw(double time){double phase=((time%88)+88)%88;return phase>=20&&phase<62?(float)(.38*Math.sin(Math.PI*(phase-20)/42)):0;}
 public static float volkolakEar(double time){double phase=((time%64)+64)%64;return phase>=14&&phase<40?(float)(.32*Math.sin(Math.PI*(phase-14)/26)):0;}
 public static float podzemnikJaw(double time){return (float)(.28+.24*Math.sin(time*.10));}
 public static float podzemnikBrow(double time){return (float)(.14*Math.sin(time*.09));}
 public static boolean particleDue(long now,long last,int interval){return interval>0&&(last==Long.MIN_VALUE||now<last||now-last>=interval);}
 private TrophyHeadFxRules(){}
}
