package org.slavicmyths.client;

/** Deterministic, bounded client-only cosmetic rules for six placed boss trophy heads. */
public final class TrophyHeadFxRules {
 public static float lidOffset(double time){
  double phase=((time%130)+130)%130;
  return phase>=62&&phase<70?(float)(.65*Math.sin(Math.PI*(phase-62)/8)):0;
 }
 public static float morEyeScale(double time){return (float)(1.025+.025*Math.sin(time*.10));}
 public static boolean likhoGlow(double time){double phase=((time%116)+116)%116;return phase>=65&&phase<88;}
 public static float tugarinJaw(double time){return (float)(.018+.016*Math.sin(time*.065));}
 public static float tugarinWhisker(double time){return (float)(.02*Math.sin(time*.07));}
 public static float skimJaw(double time){return (float)(.018+.018*Math.sin(time*.045));}
 public static float skimMane(double time){return (float)(.011*Math.sin(time*.065));}
 public static float volkolakJaw(double time){double phase=((time%145)+145)%145;return phase>=40&&phase<73?(float)(.075*Math.sin(Math.PI*(phase-40)/33)):0;}
 public static float volkolakEar(double time){double phase=((time%137)+137)%137;return phase>=57&&phase<69?(float)(.04*Math.sin(Math.PI*(phase-57)/12)):0;}
 public static float podzemnikJaw(double time){return (float)(.025+.025*Math.sin(time*.026));}
 public static float podzemnikBrow(double time){return (float)(.012*Math.sin(time*.033));}
 public static boolean particleDue(long now,long last,int interval){return interval>0&&(last==Long.MIN_VALUE||now<last||now-last>=interval);}
 private TrophyHeadFxRules(){}
}
