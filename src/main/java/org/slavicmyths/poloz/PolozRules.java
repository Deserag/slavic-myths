package org.slavicmyths.poloz;
/** Shared production policy, directly exercised by the offline gates. */
public final class PolozRules {
 public static int phase(float hp){return hp<=900?3:hp<=2100?2:1;}
 public static float multiplier(int phase,int segment,boolean scar){if(phase==1)return scar?1.5f:segment<0?1:segment>=22?.8f:0;if(phase==2)return scar?1.25f:segment<0?1.2f:.75f;return scar?1.5f:segment<0?1.4f:segment>=20?.9f:1;}
 public static float width(int i){return i<20?(float)(3.1+.8*Math.sin(i*Math.PI/20)):(float)(2.2-(i-20)*.4);}
 public static double offset(int i){return i<20?3.0+i*1.5:33+(i-20)*1.0;}
 public static int cooldown(int skill,int phase){return switch(skill){case 1->new int[]{440,360,300}[phase-1];case 2->new int[]{600,520,440}[phase-1];case 3->new int[]{560,460,380}[phase-1];case 4->new int[]{360,320,280}[phase-1];case 5->1100;default->phase==3?700:900;};}
 private PolozRules(){}
}
