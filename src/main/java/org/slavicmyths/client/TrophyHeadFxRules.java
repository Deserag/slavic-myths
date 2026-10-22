package org.slavicmyths.client;

/** Deterministic, bounded client-only cosmetic rules for six placed boss trophy heads. */
public final class TrophyHeadFxRules {
 public static float lidOffset(double time){
  double phase=((time%94)+94)%94;
  return phase>=45&&phase<55?(float)(1.2*Math.sin(Math.PI*(phase-45)/10)):0;
 }
 public static float morEyeScale(double time){return (float)(1.025+.025*Math.sin(time*.10));}
 public static boolean likhoGlow(double time){double phase=((time%116)+116)%116;return phase>=65&&phase<88;}
 public static float tugarinJaw(double time){return (float)(.16+.16*Math.sin(time*.09));}
 public static float tugarinWhisker(double time){return (float)(.18*Math.sin(time*.10));}
 public static float skimJaw(double time){return (float)(.14+.14*Math.sin(time*.09));}
 public static float skimMane(double time){return (float)(.12*Math.sin(time*.10));}
 public static float volkolakJaw(double time){double phase=((time%145)+145)%145;return phase>=40&&phase<83?(float)(.34*Math.sin(Math.PI*(phase-40)/43)):0;}
 public static float volkolakEar(double time){double phase=((time%137)+137)%137;return phase>=45&&phase<79?(float)(.24*Math.sin(Math.PI*(phase-45)/34)):0;}
 public static float podzemnikJaw(double time){return (float)(.17+.16*Math.sin(time*.075));}
 public static float podzemnikBrow(double time){return (float)(.10*Math.sin(time*.06));}
 public static boolean particleDue(long now,long last,int interval){return interval>0&&(last==Long.MIN_VALUE||now<last||now-last>=interval);}
 private TrophyHeadFxRules(){}
}
